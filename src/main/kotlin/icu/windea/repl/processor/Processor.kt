package icu.windea.repl.processor

import icu.windea.repl.util.Args

/**
 * 用于声明全局替换策略。例如替换文件名、替换目录名、替换文件文本、替换压缩包中的内容。
 */
interface Processor {
    val args: Args

    fun validateArgs(id: String) {}

    fun supports(type: ProcessorType): Boolean
}
