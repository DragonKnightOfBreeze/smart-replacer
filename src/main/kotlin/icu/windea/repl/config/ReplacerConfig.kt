package icu.windea.repl.config

import icu.windea.repl.replacer.Replacer

/**
 * @see Replacer
 */
data class ReplacerConfig(
    val id: String,
    val name: String,
    val args: Map<String, Any?> = emptyMap(),
)