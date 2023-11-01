package icu.windea.repl.util

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.google.common.cache.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.invoke.MethodHandles
import java.lang.management.ManagementFactory
import java.lang.reflect.Type
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap

//region Stdlib Extensions

inline fun <reified T> Any?.cast(): T = this as T

inline fun <reified T> Any?.castOrNull(): T? = this as? T

fun String.toFile() = File(this)

fun String.toFileOrNull() = runCatching { File(this) }.getOrNull()

fun String.toPath() = Paths.get(this)

fun String.toPathOrNull() = runCatching { Paths.get(this) }.getOrNull()

inline fun <reified T> javaTypeOf(): Type {
    return object : TypeReference<T>() {}.type
}

@Suppress("UNUSED_PARAMETER", "NOTHING_TO_INLINE")
inline fun <T> javaTypeOf(target: T): Type {
    return object : TypeReference<T>() {}.type
}

fun String.matchesGlobFileName(pattern: String, ignoreCase: Boolean = false): Boolean {
    if (pattern.isEmpty()) return false
    if (pattern == "*") return true
    return pattern.split(';').any { doMatchGlobFileName(it.trim(), ignoreCase) }
}

private val globPatternToRegexCache = CacheBuilder.newBuilder().buildCache<String, Regex> {
    buildString {
        append("\\Q")
        var i = 0
        while (i < it.length) {
            val c = it[i]
            when {
                c == '*' -> append("\\E.*\\Q")
                c == '?' -> append("\\E.\\Q")
                else -> append(c)
            }
            i++
        }
        append("\\E")
    }.toRegex()
}

private fun String.doMatchGlobFileName(pattern: String, ignoreCase: Boolean): Boolean {
    val usedPath = this.let { if (ignoreCase) it.lowercase() else it }
    val usedPattern = pattern.let { if (ignoreCase) it.lowercase() else it }
    val regex = globPatternToRegexCache.get(usedPattern)
    return usedPath.matches(regex)
}

fun String.matchesAntPath(pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    if (pattern.isEmpty()) return false
    if (pattern == "**" || pattern == "/**") return true
    return pattern.split(';').any { doMatchAntPath(it.trim(), ignoreCase, trimSeparator) }
}

private val antPatternToRegexCache = CacheBuilder.newBuilder().buildCache<String, Regex> {
    buildString {
        append("\\Q")
        var i = 0
        while (i < it.length) {
            val c = it[i]
            when {
                c == '*' -> {
                    val nc = it.getOrNull(i + 1)
                    if (nc == '*') {
                        i++
                        append("\\E.*\\Q")
                    } else {
                        append("\\E[^/]*\\Q")
                    }
                }

                c == '?' -> append("\\E[^/]\\Q")
                else -> append(c)
            }
            i++
        }
        append("\\E")
    }.toRegex()
}

private fun String.doMatchAntPath(pattern: String, ignoreCase: Boolean, trimSeparator: Boolean): Boolean {
    val usedPath = this.let { if (trimSeparator) it.trim('/') else it }.let { if (ignoreCase) it.lowercase() else it }
    val usedPattern = pattern.let { if (trimSeparator) it.trim('/') else it }.let { if (ignoreCase) it.lowercase() else it }
    val regex = antPatternToRegexCache.get(usedPattern)
    return usedPath.matches(regex)
}

fun isDebug(): Boolean {
    return try {
        val inputArguments = ManagementFactory.getRuntimeMXBean().inputArguments
        inputArguments.any { it.startsWith("-agentlib:jdwp") || it.startsWith("-Xrunjdwp:") }
    } catch (e: Exception) {
        false
    }
}

//endregion

//region Logger Extensions

private val loggerCache = ConcurrentHashMap<String, Logger>()

fun logger(name: String) = loggerCache.computeIfAbsent(name) { LoggerFactory.getLogger(name) }

fun <T> logger(clazz: Class<T>) = loggerCache.computeIfAbsent(clazz.name) { LoggerFactory.getLogger(clazz.name) }

@Suppress("NOTHING_TO_INLINE")
inline fun thisLogger() = logger(MethodHandles.lookup().lookupClass())

inline fun <reified T : Any> logger() = logger(T::class.java)

//endregion

//region Cache Extensions

@Suppress("NOTHING_TO_INLINE")
inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(): Cache<K, V> {
    return build()
}

inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(crossinline loader: (K) -> V): LoadingCache<K, V> {
    return build(object : CacheLoader<K, V>() {
        override fun load(key: K): V {
            return loader(key)
        }
    })
}

//endregion

//region Mapper Extensions

val jsonMapper = JsonMapper().findAndRegisterModules()

val yamlMapper = YAMLMapper().findAndRegisterModules()

//endregion

//region Convert Extensions

//based on com.fasterxml.jackson.databind.ObjectMapper.convertValue(java.lang.Object, com.fasterxml.jackson.databind.JavaType)

inline fun <reified T> Any?.convert(): T {
    return convert(javaTypeOf<T>())
}

inline fun <reified T> Any?.convertOrNull(): T? {
    return convertOrNull(javaTypeOf<T>())
}

fun <T> Any?.convert(targetType: Type): T {
    return jsonMapper.convertValue(this, TypeFactory.defaultInstance().constructType(targetType))
}

fun <T> Any?.convertOrNull(targetType: Type): T? {
    return runCatching { convert<T>(targetType) }.getOrElse { null }
}

//endregion

//region Project Extensions

fun File.isBinaryFile(): Boolean {
    return this.reader().use {
        for (i in 0..<100) {
            val r = it.read()
            if (r == -1) break
            val b = r.toByte()
            if (b < 0x09 || (b > 0x0d && b < 0x20) || b == 0x7F.toByte()) return@use true
        }
        false
    }
}

inline infix fun <reified T> Args.withDefault(defaultValue: T): Args.Delegate<T> {
    return Args.Delegate(this, defaultValue, javaTypeOf<T>())
}

fun randomUUidString(): String {
    return UUID.randomUUID().toString().take(8)
}

//endregion

