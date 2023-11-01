package icu.windea.repl

import icu.windea.repl.processor.*
import icu.windea.repl.replacer.*

object Registry{
    val replacerRegistry = mutableMapOf<String, Class<out Replacer>>()
    val processorRegistry = mutableMapOf<String, Class<out Processor>>()
    
    init {
        registerReplacers()
        registerProcessors()
    }
    
    private fun registerReplacers() {
        replacerRegistry["literalReplacer"] = LiteralReplacer::class.java
        replacerRegistry["regexReplacer"] = RegexReplacer::class.java
    }
    
    private fun registerProcessors() {
        processorRegistry["everyWhereProcessor"] = EveryWhereProcessor::class.java
        processorRegistry["fileNameProcessor"] = FileNameProcessor::class.java
        processorRegistry["directoryNameProcessor"] = DirectoryNameProcessor::class.java
        processorRegistry["fileTextProcessor"] = FileTextProcessor::class.java
        processorRegistry["inArchiveFileNameProcessor"] = InArchiveFileNameProcessor::class.java
        processorRegistry["inArchiveDirectoryNameProcessor"] = InArchiveDirectoryNameProcessor::class.java
        processorRegistry["inArchiveFileTextProcessor"] = InArchiveFileTextProcessor::class.java
    }
}