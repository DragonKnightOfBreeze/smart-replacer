package icu.windea.repl.processor

import icu.windea.repl.util.matchesGlobFileName

interface FileProcessor : Processor {
    val includeFiles: Set<String>
    val excludeFiles: Set<String>

    fun supportsByFileName(fileName: String): Boolean {
        if (excludeFiles.isNotEmpty() && excludeFiles.any { fileName.matchesGlobFileName(it) }) return false
        if (includeFiles.isNotEmpty() && includeFiles.none { fileName.matchesGlobFileName(it) }) return false
        return true
    }
}