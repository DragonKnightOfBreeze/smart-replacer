package icu.windea.repl.util

import java.lang.reflect.Type
import kotlin.reflect.KProperty

class Args : MutableMap<String, Any?> by mutableMapOf() {
    fun <T> getArgument(key: String, type: Type): T {
        val v = getValue(key)
        return v.convert(type)
    }

    fun <T> getArgument(key: String, defaultValue: T, type: Type): T {
        val v = get(key) ?: return defaultValue
        return v.convertOrNull(type) ?: return defaultValue
    }

    inline fun <reified T> getArgument(key: String): T {
        return getArgument(key, javaTypeOf<T>())
    }

    inline fun <reified T> getArgument(key: String, defaultValue: T): T {
        return getArgument(key, defaultValue, javaTypeOf<T>())
    }

    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T {
        return getArgument(property.name)
    }

    class Delegate<T>(val args: Args, val defaultValue: T, val type: Type) {
        @Suppress("NOTHING_TO_INLINE")
        inline operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return args.getArgument(property.name, defaultValue, type)
        }
    }
}
