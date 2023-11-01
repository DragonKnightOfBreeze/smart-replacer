package icu.windea.repl.replacer

import icu.windea.repl.util.Args

/**
 * 用于声明替换方式。例如直接替换、正则替换。
 */
interface Replacer {
    val args: Args

    fun validateArgs(id: String) {}

    /**
     * @return 替换后的字符串。
     */
    fun replace(string: String): String
}

