package icu.windea.repl.processor

enum class ProcessorType {
    /**文件名。*/
    FileName,

    /** 目录名。*/
    DirectoryName,

    /**文件文本。*/
    FileText,
    
    /** 压缩包中的文件名。 */
    InArchiveFileName,

    /** 压缩包中的目录名。 */
    InArchiveDirectoryName,

    /** 压缩包中的文件文本。 */
    InArchiveFileText,
}