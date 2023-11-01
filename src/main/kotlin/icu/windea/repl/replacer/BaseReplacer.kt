package icu.windea.repl.replacer

import icu.windea.repl.util.Args

abstract class BaseReplacer: Replacer {
    override val args: Args = Args()
    
    protected fun requireArg(id: String, value: Boolean, lazyMessage: () -> Any) {
        require(value) { "Replacer $id: ${lazyMessage()}" }
    }
}