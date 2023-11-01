# Smart Replacer - 全局替换器

## 概述

可配置的全局替换器。

特性：

* 支持按字符串字面量和正则表达式进行替换。
* 可替换文件名和目录名。
* 可替换文本文件的内容。
* 可替换压缩包中的内容，包括压缩后的文件名、目录名、文本文件的内容。

注意事项：

* 某些表现为富文本的文件（如Word文档）实际上也是压缩文件，且通常会在压缩文件中混合xml文件和其他资源文件。默认情况下，处理压缩文件时也会同样尝试处理这些文件。
* 对于上述富文本文件，关键字通常在XML标签中或者被XML标签分割。通过采用合适的正则表达式，能够比较准确地替换其中的内容。
* 目前无法处理压缩文件以外的二进制文件（例如java字节码文件，即.class文件）。

## 快速开始

### 本地

* 配置好开发环境
* 编辑配置文件（默认使用`src/test/resources/conf/test.yml`）
* 在项目根目录打开控制台，执行命令`gradle run runWithConfigFile`

### 远程

* 将部署包解压到远程服务器的/usr/local目录下
* 执行以下命令

```
cd /usr/local
tar -zxvf smart-replacer-*.tar.gz
cd /usr/local/smart-replacer*/conf
vi test.yml # 按照具体的需求编辑配置文件
cd /usr/local/smart-replacer*/bin
sh smart_replacer test.yml # 开始执行
```

## 参考

#### Replacer

`Replacer`用于声明替换方式。例如直接替换、正则替换。

* `icu.windea.repl.replacer.LiteralReplacer`
  * name: literalReplacer
  * args:
    * oldValue: String
    * newValue: String = ""
    * ignoreCase: Boolean = false
* `icu.windea.repl.replacer.RegexReplacer`
  * name: regexReplacer
  * args:
    * regexString: String - 正则表达式，例如`[a-z]+`
    * replacement: String = ""
    * options: String = "" - 正则表达式选项

关于正则表达式选项[RegexOption](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex-option/)：

* 详细说明：
  * `'i'` - `RegexOption.IGNORE_CASE`
  * `'m'` - `RegexOption.MULTILINE`
  * `'l'` - `RegexOption.LITERAL`
  * `'u'` - `RegexOption.UNIX_LINES`
  * `'c'` - `RegexOption.COMMENTS`
  * `'d'` - `RegexOption.DOT_MATCHES_ALL`
  * `'e'` - `RegexOption.CANON_EQ`
* 示例：
  * `""` - 不指定任何选项
  * `"im"` - 多行且忽略大小写

#### Processor

`Processor`用于声明全局替换策略。例如替换文件名、替换目录名、替换文件文本、替换压缩包中的内容。

* `icu.windea.repl.processor.EverywhereProcessor`
  * name: everywhereProcessor
  * args:
    * includeFiles: Set<String> = [] - 用于过滤（压缩包中的）文件名，默认不指定
    * excludeFiles: Set<String> = [] - 用于过滤（压缩包中的）文件名，默认不指定（excludeFiles的优先级要高于includeFiles）
    * includeArchiveFiles: Set<String> = [] - 用于过滤压缩包的文件名，默认不指定
    * excludeArchiveFiles: Set<String> = [] - 用于过滤压缩包文件名，默认不指定（excludeArchiveFiles的优先级要高于includeArchiveFiles）
* `icu.windea.repl.processor.FileNameProcessor`
  * name: fileNameProcessor
  * args:
    * includeFiles: Set<String> = [] - 用于过滤文件名，默认不指定
    * excludeFiles: Set<String> = [] - 用于过滤文件名，默认不指定（excludeFiles的优先级要高于includeFiles）
* `icu.windea.repl.processor.DirectoryNameProcessor`
  * name: directoryNameProcessor
  * args:
    * includeFiles: Set<String> = [] - 用于过滤文件名，默认不指定
    * excludeFiles: Set<String> = [] - 用于过滤文件名，默认不指定（excludeFiles的优先级要高于includeFiles）
* `icu.windea.repl.processor.FileTextProcessor`
  * name: fileTextProcessor
  * args:
    * includeFiles: Set<String> = [] - 用于过滤文件名，默认不指定
    * excludeFiles: Set<String> = [] - 用于过滤文件名，默认不指定（excludeFiles的优先级要高于includeFiles）
* `icu.windea.repl.processor.InArchiveFileNameProcessor`
  * name: inArchiveFileNameProcessor
  * args:
    * includeFiles: Set<String> = [] - 用于过滤（压缩包中的）文件名，默认不指定
    * excludeFiles: Set<String> = [] - 用于过滤（压缩包中的）文件名，默认不指定（excludeFiles的优先级要高于includeFiles）
    * includeArchiveFiles: Set<String> = [] - 用于过滤压缩包的文件名，默认不指定
    * excludeArchiveFiles: Set<String> = [] - 用于过滤压缩包文件名，默认不指定（excludeArchiveFiles的优先级要高于includeArchiveFiles）
* `icu.windea.repl.processor.InArchiveDirectoryNameProcessor`
  * name: inArchiveDirectoryNameProcessor
  * args:
    * includeFiles: Set<String> = [] - 用于过滤（压缩包中的）文件名，默认不指定
    * excludeFiles: Set<String> = [] - 用于过滤（压缩包中的）文件名，默认不指定（excludeFiles的优先级要高于includeFiles）
    * includeArchiveFiles: Set<String> = [] - 用于过滤压缩包的文件名，默认不指定
    * excludeArchiveFiles: Set<String> = [] - 用于过滤压缩包文件名，默认不指定（excludeArchiveFiles的优先级要高于includeArchiveFiles）
* `icu.windea.repl.processor.InArchiveFileTextProcessor`
  * name: inArchiveFileTextProcessor
  * args:
    * includeFiles: Set<String> = [] - 用于过滤（压缩包中的）文件名，默认不指定
    * excludeFiles: Set<String> = [] - 用于过滤（压缩包中的）文件名，默认不指定（excludeFiles的优先级要高于includeFiles）
    * includeArchiveFiles: Set<String> = [] - 用于过滤压缩包的文件名，默认不指定
    * excludeArchiveFiles: Set<String> = [] - 用于过滤压缩包文件名，默认不指定（excludeArchiveFiles的优先级要高于includeArchiveFiles）

`includeFiles`等参数采用以下的文件名匹配模式：

* `*` - 匹配任意个字符。
* `?` - 匹配单个字符。

#### Task

`Task`用于声明工具的执行流程。

* paths: List<String> - 声明输入路径。可以指定文件和目录的路径。
* outputPaths: List<String> - 声明输出路径。输出路径需要留空或者与输入路径一一匹配。输出路径留空或者与输入路径相同时，将会覆盖原文件/原目录。
* steps: List<Step> - 声明每个步骤所适用的`Processor`和`Replacer`

`paths`和`outputPaths`可以指定绝对路径或者相对于conf目录的相对路径。