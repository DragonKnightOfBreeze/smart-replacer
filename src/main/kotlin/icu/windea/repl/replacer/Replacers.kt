package icu.windea.repl.replacer

import icu.windea.repl.util.withDefault

class LiteralReplacer : BaseReplacer() {
    val oldValue: String by args withDefault ""
    val newValue: String by args withDefault ""
    val ignoreCase: Boolean by args withDefault false

    override fun validateArgs(id: String) {
        requireArg(id, oldValue.isNotEmpty()) { "oldValue cannot be empty." }
    }

    override fun replace(string: String): String {
        return string.replace(oldValue, newValue, ignoreCase)
    }
}

class RegexReplacer : BaseReplacer() {
    val regexString: String by args withDefault ""
    val replacement: String by args withDefault ""
    val options: String by args withDefault ""

    val regex by lazy {
        val options = if(options.isEmpty()) emptySet() else options.mapNotNullTo(mutableSetOf()) { c -> 
            when(c) {
                'i' -> RegexOption.IGNORE_CASE
                'm' -> RegexOption.MULTILINE
                'l' -> RegexOption.LITERAL
                'u' -> RegexOption.UNIX_LINES
                'c' -> RegexOption.COMMENTS
                'd' -> RegexOption.DOT_MATCHES_ALL
                'e' -> RegexOption.CANON_EQ
                else -> null
            }
        }
        regexString.toRegex(options)
    }

    override fun validateArgs(id: String) {
        requireArg(id, regexString.isNotEmpty()) { "regexString cannot be empty." }
    }

    override fun replace(string: String): String {
        return string.replace(regex, replacement)
    }
}
