package icu.windea.repl.task

/**
 * 用于声明工具的执行流程。
 */
interface Task {
    val paths: List<String>
    val outputPaths: List<String>
    val steps: List<Step>

    /**
     * 用于声明执行流程中的步骤。
     */
    interface Step {
        val replacers: List<String>
        val processors: List<String>
    }
}

