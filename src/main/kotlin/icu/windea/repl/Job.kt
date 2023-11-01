package icu.windea.repl

import icu.windea.repl.config.Config
import icu.windea.repl.processor.*
import icu.windea.repl.replacer.Replacer
import icu.windea.repl.task.Task
import icu.windea.repl.util.*
import org.apache.commons.compress.archivers.*
import org.apache.commons.compress.compressors.CompressorException
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.apache.commons.compress.utils.IOUtils
import java.io.File
import java.nio.file.Files
import kotlin.io.path.*

private val logger = logger<Job>()

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalPathApi::class)
class Job(
    val id: String,
    val configPath: String,
    val config: Config
) : Runnable {
    val replacers: Map<String, Replacer> = Facade.createReplacers(config)
    val processors: Map<String, Processor> = Facade.createProcessors(config)
    val tasks: List<Task> = Facade.createTasks(config)

    val archiveStreamFactory = ArchiveStreamFactory()
    val compressorStreamFactory = CompressorStreamFactory()

    override fun run() {
        logger.info("Execute job '$id'...")
        logger.info("Job detail: configPath=${configPath}, config=${config}")

        if (tasks.isEmpty()) {
            logger.warn("No tasks specified, skipped.")
            return
        }

        tasks.forEachIndexed { i1, task ->
            logger.info("Execute task #${i1}...")
            logger.info("Task detail: paths=${task.paths}, outputPaths=${task.outputPaths}")

            task.paths.forEachIndexed { i2, path ->
                val outputPath = task.outputPaths[i2]
                processPath(task, path, outputPath)
            }

            logger.info("Done.")
        }
    }

    private fun processPath(task: Task, path: String, outputPath: String) {
        val filePath = path.toPath().normalize().absolute()
        val file = filePath.toFile()
        val outputFilePath = outputPath.toPath().normalize().absolute()
        val outputFile = outputFilePath.toFile()
        if(file != outputFile && outputFile.exists()) {
            logger.warn("Output file '${outputFile}' exists, skipped.")
            return
        }
        
        val tempRootDirPath = filePath.resolveSibling(filePath.name + "@" + randomUUidString()).normalize()
        val tempRootDir = tempRootDirPath.toFile()
        try {
            val tempFilePath = tempRootDirPath.resolve(file.name)
            val tempFile = tempFilePath.toFile()
            file.copyRecursively(tempFile, true)

            replaceInRootFile(task, tempRootDir, false)
            
            if (filePath == outputFilePath) {
                //override input file
                logger.debug("Delete '${file}'...")
                filePath.deleteRecursively()
            }
            val newTempFile = tempRootDir.listFiles()?.singleOrNull()
                ?: throw IllegalStateException("Cannot find replaced file of '${tempFile}'")
            logger.debug("Copy '{}' to '{}'...", newTempFile, outputFile)
            newTempFile.copyRecursively(outputFile, true)
        } finally {
            if(tempRootDir.exists()) {
                //delete temp files
                logger.debug("Delete '${tempRootDir}'...")
                tempRootDir.deleteRecursively()
            }
        }
    }

    private fun replaceInRootFile(task: Task, rootFile: File, inArchive: Boolean): Boolean {
        var replaced = false
        rootFile.walk()
            .onLeave { file ->
                if (file != rootFile && file.isDirectory) {
                    logger.debug("Process directory '${file.canonicalPath}'...")

                    //替换目录名
                    replaced = replaced or replaceDirectoryName(task, rootFile, file, inArchive)
                }
            }
            .forEach { file ->
                if (file != rootFile && file.isFile) {
                    logger.debug("Process file '${file.canonicalPath}'...")

                    //替换文件内容
                    replaced = replaced or replaceFileContent(task, rootFile, file, inArchive)

                    //替换文件名
                    replaced = replaced or replaceFileName(task, rootFile, file, inArchive)
                }
            }
        return replaced
    }

    private fun replaceFileName(task: Task, rootFile: File, file: File, inArchive: Boolean): Boolean {
        val processorType = if (inArchive) ProcessorType.InArchiveFileName else ProcessorType.FileName
        val supportedProcessorIds = getSupportedProcessorIds(task, rootFile, file) { it.supports(processorType) }
        if (supportedProcessorIds.isEmpty()) return false
        val replacedValue = applySupportedProcessors(task, supportedProcessorIds) { file.name }
        if (replacedValue == null) return false
        file.renameTo(file.resolveSibling(replacedValue))
        return true
    }

    private fun replaceDirectoryName(task: Task, rootFile: File, file: File, inArchive: Boolean): Boolean {
        val processorType = if (inArchive) ProcessorType.InArchiveDirectoryName else ProcessorType.DirectoryName
        val supportedProcessorIds = getSupportedProcessorIds(task, rootFile, file) { it.supports(processorType) }
        if (supportedProcessorIds.isEmpty()) return false
        val replacedValue = applySupportedProcessors(task, supportedProcessorIds) { file.name }
        if (replacedValue == null) return false
        file.renameTo(file.resolveSibling(replacedValue))
        return true
    }

    private fun replaceFileContent(task: Task, rootFile: File, file: File, inArchive: Boolean): Boolean {
        var replaced = replaceArchiveFileContent(task, rootFile, file, inArchive)
        replaced = replaced || replaceFileText(task, rootFile, file, inArchive)
        return replaced
    }

    private fun replaceFileText(task: Task, rootFile: File, file: File, inArchive: Boolean): Boolean {
        if (file.isBinaryFile()) return false
        val processorType = if (inArchive) ProcessorType.InArchiveFileText else ProcessorType.FileText
        val supportedProcessorIds = getSupportedProcessorIds(task, rootFile, file) { it.supports(processorType) }
        val replacedValue = applySupportedProcessors(task, supportedProcessorIds) { file.bufferedReader().use { it.readText() } }
        if (replacedValue == null) return false
        file.bufferedWriter().use { it.write(replacedValue) }
        return true
    }

    private fun replaceArchiveFileContent(task: Task, rootFile: File, file: File, inArchive: Boolean): Boolean {
        val supportedProcessorIds = getSupportedProcessorIds(task) { it is InArchiveProcessor }
        if (supportedProcessorIds.isEmpty()) return false

        var replaced = replaceInCompressorFile(task, rootFile, file, inArchive)
        replaced = replaced || replaceInArchiveFile(task, rootFile, file, inArchive)
        return replaced
    }

    private fun replaceInCompressorFile(task: Task, rootFile: File, file: File, inArchive: Boolean): Boolean {
        val tempDir = file.resolveSibling(file.name + "@" + randomUUidString())
        val tempFile = tempDir.resolve(file.nameWithoutExtension)
        val bufferedInputStream = file.inputStream().buffered()
        return bufferedInputStream.use {
            try {
                val compressorName = CompressorStreamFactory.detect(bufferedInputStream)

                logger.debug("Decompress '${file.canonicalPath}' into directory '${tempDir.canonicalPath}'...")

                //解压
                compressorStreamFactory.createCompressorInputStream(compressorName, bufferedInputStream).use { input ->
                    val tempFilePath = tempFile.toPath()
                    tempFilePath.createParentDirectories()
                    Files.newOutputStream(tempFilePath).use { output ->
                        IOUtils.copy(input, output)
                    }
                }
                val replaced = replaceInRootFile(task, tempDir, true)

                if (replaced) {
                    logger.debug("Recompress '${file.canonicalPath}' from directory '${tempDir.canonicalPath}'...")

                    //重新压缩
                    val tempCompressorFile = file.resolveSibling(file.name + "@" + randomUUidString())
                    Files.newOutputStream(tempCompressorFile.toPath()).use { output0 ->
                        compressorStreamFactory.createCompressorOutputStream(compressorName, output0).use { output ->
                            val newTempFile = tempDir.listFiles()?.singleOrNull()
                                ?: throw IllegalStateException("Cannot find replaced file of '${tempFile}'")
                            Files.newInputStream(newTempFile.toPath()).use { input ->
                                IOUtils.copy(input, output)
                            }
                        }
                    }
                    file.delete()
                    tempCompressorFile.renameTo(file)
                }

                replaced
            } catch (e: CompressorException) {
                //not a compressor file (or some other problems), skip
                false
            } finally {
                if(tempDir.exists()) {
                    //delete temp files
                    logger.debug("Delete '${tempDir}'...")
                    tempDir.deleteRecursively()
                }
            }
        }
    }

    private fun replaceInArchiveFile(task: Task, rootFile: File, file: File, inArchive: Boolean): Boolean {
        val tempDir = file.resolveSibling(file.name + "@" + randomUUidString())
        val bufferedInputStream = file.inputStream().buffered()
        return bufferedInputStream.use {
            try {
                val archiverName = ArchiveStreamFactory.detect(bufferedInputStream)

                logger.debug("Decompress '${file.canonicalPath}' into directory '${tempDir.canonicalPath}'...")

                //解压
                archiveStreamFactory.createArchiveInputStream(archiverName, bufferedInputStream).use { input ->
                    while (true) {
                        val entry = input.nextEntry ?: break
                        if (entry.isDirectory) continue
                        val tempEntryFile = tempDir.resolve(entry.name)
                        val tempEntryFilePath = tempEntryFile.toPath()
                        tempEntryFilePath.createParentDirectories()
                        Files.newOutputStream(tempEntryFilePath).use { output ->
                            IOUtils.copy(input, output)
                        }
                    }
                }

                val replaced = replaceInRootFile(task, tempDir, true)

                if (replaced) {
                    logger.debug("Recompress '${file.canonicalPath}' from directory '${tempDir.canonicalPath}'...")

                    //重新压缩
                    val tempArchiveFile = file.resolveSibling(file.name + "@" + randomUUidString())
                    Files.newOutputStream(tempArchiveFile.toPath()).use { output0 ->
                        archiveStreamFactory.createArchiveOutputStream(archiverName, output0).use { output ->
                            tempDir.walkBottomUp().forEach f@{ f ->
                                if (f.isDirectory) return@f
                                val archiveEntry = output.createArchiveEntry(f, f.toRelativeString(tempDir))
                                output.putArchiveEntry(archiveEntry)
                                Files.newInputStream(f.toPath()).use { input ->
                                    IOUtils.copy(input, output)
                                }
                                output.closeArchiveEntry()
                            }
                        }
                    }
                    file.delete()
                    tempArchiveFile.renameTo(file)
                }

                replaced
            } catch (e: StreamingNotSupportedException) {
                //not supported, skip
                false
            } catch (e: ArchiveException) {
                //not an archive file, skip
                false
            } finally {
                if(tempDir.exists()) {
                    //delete temp files
                    logger.debug("Delete '${tempDir}'...")
                    tempDir.deleteRecursively()
                }
            }
        }
    }

    private fun getSupportedProcessorIds(task: Task, predicate: (Processor) -> Boolean): Set<String> {
        val result = mutableSetOf<String>()
        task.steps.forEach f1@{ step ->
            step.processors.forEach f2@{ processorId ->
                val processor = processors[processorId] ?: return@f2
                if (predicate(processor)) result += processorId
            }
        }
        return result
    }

    private fun getSupportedProcessorIds(task: Task, rootFile: File, file: File, predicate: (Processor) -> Boolean): Set<String> {
        return getSupportedProcessorIds(task) { processor ->
            var r = predicate(processor)
            if (processor is FileProcessor) r = r && processor.supportsByFileName(file.name)
            if (processor is InArchiveProcessor) r = r && processor.supportsByArchiveFileName(rootFile.name.substringBeforeLast('@'))
            r
        }
    }

    private fun applySupportedProcessors(task: Task, supportedProcessorIds: Set<String>, valueProvider: () -> String): String? {
        var value: String? = null
        var resultValue: String? = null
        task.steps.forEach f1@{ step ->
            step.processors.forEach f2@{ processorId ->
                if (processorId !in supportedProcessorIds) return@f2
                step.replacers.forEach f3@{ replacerId ->
                    val replacer = replacers[replacerId] ?: return@f3
                    if (value == null) {
                        value = valueProvider()
                        if (value.isNullOrEmpty()) return null
                        resultValue = value
                    }
                    resultValue = replacer.replace(resultValue!!)
                }
            }
        }
        if (value == resultValue) return null
        return resultValue
    }
}