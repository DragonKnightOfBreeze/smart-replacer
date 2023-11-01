package icu.windea.repl.task

class DefaultTask(
    override val paths: List<String>,
    override val outputPaths: List<String>,
    override val steps: List<Task.Step>,
): Task {
    class Step (
        override val replacers: List<String>,
        override val processors: List<String>,
    ): Task.Step
}
