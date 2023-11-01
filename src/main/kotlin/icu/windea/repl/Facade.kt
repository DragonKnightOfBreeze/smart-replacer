package icu.windea.repl

import icu.windea.repl.config.Config
import icu.windea.repl.processor.Processor
import icu.windea.repl.replacer.Replacer
import icu.windea.repl.task.DefaultTask
import icu.windea.repl.task.Task
import icu.windea.repl.util.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.system.exitProcess

private val logger = logger<Facade>()

object Facade {
    fun getConfigFile(configPath: String) : File {
        val path = configPath.toPath()
        if(path.isAbsolute) return path.toFile()
        val actualWorkDirectoryPath = path.toAbsolutePath().toString().removeSuffix(path.toString()).toPath().normalize()
        if(actualWorkDirectoryPath.endsWith("lib")) {
            //远程
            actualWorkDirectoryPath.resolve("../conf/$configPath").toConfigFile()?.let { return it }
            actualWorkDirectoryPath.resolve("../$configPath").toConfigFile()?.let { return it }
        } else {
            //本地
            actualWorkDirectoryPath.resolve("src/test/resources/conf/$configPath").toConfigFile()?.let { return it }
            actualWorkDirectoryPath.resolve("src/main/resources/conf/$configPath").toConfigFile()?.let { return it }
            actualWorkDirectoryPath.resolve(configPath).toConfigFile()?.let { return it }
        }
        //相对于实际工作目录的路径
        return path.toFile()
    }
    
    private fun Path.toConfigFile(): File? {
        if(!this.exists()) return null
        return this.normalize().toFile()
    }
    
    fun readConfigs(configPath: String): List<Config> {
        try {
            logger.info("Read configs from config path '$configPath'...")

            val configFile = getConfigFile(configPath)
            val configIterator = yamlMapper.readerFor(Config::class.java).readValues<Config>(configFile)
            return configIterator.readAll()
        } catch (e: Exception) {
            logger.error("Cannot read configs from config path '$configPath'", e)
            exitProcess(1)
        } finally {
            
        }
    }

    fun createJobs(configPath: String): List<Job> {
        logger.info("Create jobs from config path '$configPath'...")
        val configs = readConfigs(configPath)
        return configs.mapIndexed { i, config ->
            val id = "$configPath #$i"
            Job(id, configPath, config)
        }
    }
    
    fun createReplacers(config: Config): Map<String, Replacer> {
        return buildMap {
            config.replacers.forEach { c ->
                val type = Registry.replacerRegistry[c.name]
                if (type == null) throw IllegalArgumentException("Cannot find replacer with name '${c.name}'")
                val replacer = type.getConstructor().newInstance()
                replacer.args.putAll(c.args)
                replacer.validateArgs(c.id)
                this[c.id] = replacer
            }
        }
    }

    fun createProcessors(config: Config): Map<String, Processor> {
        return buildMap {
            config.processors.forEach { c ->
                val type = Registry.processorRegistry[c.name]
                if (type == null) throw IllegalArgumentException("Cannot find processor with name '${c.name}'")
                val processor = type.getConstructor().newInstance()
                processor.args.putAll(c.args)
                processor.validateArgs(c.id)
                this[c.id] = processor
            }
        }
    }

    fun createTasks(config: Config): List<Task> {
        return buildList {
            config.tasks.forEach f1@{ t ->
                val paths = mutableSetOf<String>()
                val outputPaths = mutableSetOf<String>()
                val steps = mutableSetOf<Task.Step>()
                
                if (t.paths.isEmpty()) {
                    logger.warn("No paths specified, skipped.")
                    return@f1
                } else {
                    paths.addAll(t.paths)
                }
                if (t.outputPaths.isEmpty()) {
                    logger.warn("No output paths specified, Use paths (files will be replaced).")
                    outputPaths.addAll(t.paths)
                } else {
                    outputPaths.addAll(t.outputPaths)
                    if(paths.size > outputPaths.size) throw IllegalArgumentException("Mismatched paths and output paths.")
                }
                if(t.steps.isEmpty()) {
                    logger.warn("No steps specified, skipped.")
                    return@f1
                } else {
                    t.steps.forEach { s ->
                        val replacers = mutableSetOf<String>()
                        val processors = mutableSetOf<String>()
                        
                        if (s.replacers.isEmpty()) {
                            replacers.addAll(config.replaceIds)
                        } else {
                            val unresolved = s.replacers.toMutableSet().also { it.removeAll(config.replaceIds) }
                            if (unresolved.isNotEmpty()) throw IllegalArgumentException("Cannot find replacers with names '${unresolved.joinToString()}'")
                            replacers.addAll(s.replacers.distinct())
                        }
                        if (s.processors.isEmpty()) {
                            processors.addAll(config.processorIds)
                        } else {
                            val unresolved = s.processors.toMutableSet().also { it.removeAll(config.processorIds) }
                            if (unresolved.isNotEmpty()) throw IllegalArgumentException("Cannot find processors with names '${unresolved.joinToString()}'")
                            processors.addAll(s.processors.distinct())
                        }
                        
                        steps += DefaultTask.Step(replacers.toList(), processors.toList())
                    }
                }
                
                this += DefaultTask(paths.toList(), outputPaths.toList(), steps.toList())
            }
        }
    }
}

