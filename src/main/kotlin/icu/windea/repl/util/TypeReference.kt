package icu.windea.repl.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 类型引用。
 */
abstract class TypeReference<T> {
	val type: Type

	init {
		val superClass = javaClass.genericSuperclass
		if(superClass !is ParameterizedType) {
			throw IllegalArgumentException("TypeReference must be constructed with actual type information.")
		}
		type = superClass.actualTypeArguments[0]
	}
}
