package icu.windea.repl.processor

import icu.windea.repl.util.withDefault

class EveryWhereProcessor: BaseProcessor(), FileProcessor, InArchiveProcessor {
    override val includeFiles: Set<String> by args withDefault emptySet()
    override val excludeFiles: Set<String> by args withDefault emptySet()
    override val includeArchiveFiles: Set<String> by args withDefault emptySet()
    override val excludeArchiveFiles: Set<String> by args withDefault emptySet()

    override fun supports(type: ProcessorType): Boolean {
        return true
    }
}

class FileNameProcessor : BaseProcessor(), FileProcessor {
    override val includeFiles: Set<String> by args withDefault emptySet()
    override val excludeFiles: Set<String> by args withDefault emptySet()

    override fun supports(type: ProcessorType): Boolean {
        return type == ProcessorType.FileName
    }
}

class DirectoryNameProcessor: BaseProcessor(), FileProcessor {
    override val includeFiles: Set<String> by args withDefault emptySet()
    override val excludeFiles: Set<String> by args withDefault emptySet()

    override fun supports(type: ProcessorType): Boolean {
        return type == ProcessorType.DirectoryName
    }
}

class FileTextProcessor: BaseProcessor(), FileProcessor {
    override val includeFiles: Set<String> by args withDefault emptySet()
    override val excludeFiles: Set<String> by args withDefault emptySet()

    override fun supports(type: ProcessorType): Boolean {
        return type == ProcessorType.FileText
    }
}

class InArchiveFileNameProcessor: BaseProcessor(), FileProcessor, InArchiveProcessor {
    override val includeFiles: Set<String> by args withDefault emptySet()
    override val excludeFiles: Set<String> by args withDefault emptySet()
    override val includeArchiveFiles: Set<String> by args withDefault emptySet()
    override val excludeArchiveFiles: Set<String> by args withDefault emptySet()

    override fun supports(type: ProcessorType): Boolean {
        return type == ProcessorType.InArchiveFileName
    }
}

class InArchiveDirectoryNameProcessor: BaseProcessor(), FileProcessor, InArchiveProcessor {
    override val includeFiles: Set<String> by args withDefault emptySet()
    override val excludeFiles: Set<String> by args withDefault emptySet()
    override val includeArchiveFiles: Set<String> by args withDefault emptySet()
    override val excludeArchiveFiles: Set<String> by args withDefault emptySet()

    override fun supports(type: ProcessorType): Boolean {
        return type == ProcessorType.InArchiveDirectoryName
    }
}

class InArchiveFileTextProcessor: BaseProcessor(), FileProcessor, InArchiveProcessor {
    override val includeFiles: Set<String> by args withDefault emptySet()
    override val excludeFiles: Set<String> by args withDefault emptySet()
    override val includeArchiveFiles: Set<String> by args withDefault emptySet()
    override val excludeArchiveFiles: Set<String> by args withDefault emptySet()

    override fun supports(type: ProcessorType): Boolean {
        return type == ProcessorType.InArchiveFileText
    }
}