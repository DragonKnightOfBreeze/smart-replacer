package icu.windea.repl.config


import icu.windea.repl.processor.Processor

/**
 * @see Processor
 */
data class ProcessorConfig(
    val id: String,
    val name: String,
    val args: Map<String, Any?> = emptyMap(),
)