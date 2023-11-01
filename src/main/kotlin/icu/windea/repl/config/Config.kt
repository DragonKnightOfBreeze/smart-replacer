package icu.windea.repl.config

data class Config(
    val replacers: List<ReplacerConfig> = emptyList(),
    val processors: List<ProcessorConfig> = emptyList(),
    val tasks: List<TaskConfig> = emptyList(),
) {
    val replaceIds = replacers.mapTo(mutableSetOf()) { it.id }
    val processorIds = processors.mapTo(mutableSetOf()) { it.id }
}
