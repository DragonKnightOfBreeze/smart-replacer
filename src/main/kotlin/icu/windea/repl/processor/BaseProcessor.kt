package icu.windea.repl.processor

import icu.windea.repl.util.Args

abstract class BaseProcessor: Processor {
    override val args: Args = Args()

    protected fun requireArg(id: String, value: Boolean, lazyMessage: () -> Any) {
        require(value) { "Processor $id: ${lazyMessage()}" }
    }
}
