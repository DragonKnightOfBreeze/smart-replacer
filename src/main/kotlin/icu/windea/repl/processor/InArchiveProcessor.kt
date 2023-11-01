package icu.windea.repl.processor

import icu.windea.repl.util.matchesGlobFileName

interface InArchiveProcessor : Processor {
    val includeArchiveFiles: Set<String>
    val excludeArchiveFiles: Set<String>

    fun supportsByArchiveFileName(entryName: String): Boolean {
        if (excludeArchiveFiles.isNotEmpty() && excludeArchiveFiles.any { entryName.matchesGlobFileName(it) }) return false
        if (includeArchiveFiles.isNotEmpty() && includeArchiveFiles.none { entryName.matchesGlobFileName(it) }) return false
        return true
    }
}