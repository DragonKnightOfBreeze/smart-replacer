package icu.windea.repl.config

import icu.windea.repl.processor.Processor
import icu.windea.repl.replacer.Replacer
import icu.windea.repl.task.Task

/**
 * @see Task
 * @property paths 输入路径。
 * @property outputPaths 输出路径。与输入路径相匹配，如果同名则会替换文件。
 */
data class TaskConfig(
    val paths: List<String> = emptyList(),
    val outputPaths: List<String> = emptyList(),
    val steps: List<Step> = emptyList(),
) {
    /**
     * @property processors [Processor]的ID列表。如果为空，则表示加入当前配置文件中所有已配置的[Processor]。
     * @property replacers [Replacer]的ID列表。如果为空，则表示加入当前配置文件中所有已配置的[Replacer]。
     */
    data class Step(
        val replacers: List<String> = emptyList(),
        val processors: List<String> = emptyList(),
    )
}