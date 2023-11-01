import org.apache.tools.ant.filters.FixCrLfFilter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "icu.windea.repl"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")
    implementation("ch.qos.logback:logback-classic:1.4.8")
    implementation("org.apache.commons:commons-compress:1.23.0")
    implementation("com.google.guava:guava:31.1-jre")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("icu.windea.repl.MainKt")
}

tasks {
    test {
        useJUnitPlatform()
    }
    jar {
        from("LICENSE")
        exclude("conf")
    }
    startScripts {
        enabled = false
    }
    distZip {
        enabled = false
    }
    distTar {
        compression = Compression.GZIP
        archiveBaseName.set("smart-replacer")
        archiveClassifier.set(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        archiveExtension.set("tar.gz")
        
        //确保解压后的根目录不带版本号和时间戳
        rootSpec.children.single().children.single().into("smart-replacer")
    }
    register<JavaExec>("runWithConfigFile") {
        mainClass.set("icu.windea.repl.MainKt")
        classpath = sourceSets.main.get().runtimeClasspath
        jvmArgs("-DLOG_HOME=src/main/resources/conf/logback.xml")
        args(args?.getOrNull(0) ?: "test.yml")
    }
}

//生成部署包

distributions {
    main {
        contents {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            //复制文件
            from("README.md")
            from("LICENSE")
            into("bin") {
                from("src/assembly/bin")
                fileMode = 777
            }
            into("logs") {
                from("src/assembly/logs")
            }
            into("conf") {
                from("$buildDir/resources/main/conf")
            }
            into("lib") {
                from("$buildDir/libs")
            }

            //排除默认生成的启动脚本
            exclude("$buildDir/scripts/*")

            //设置复制过程中的行分隔符
            filesMatching("**/*") {
                filter(FixCrLfFilter::class, buildMap { put("eol", FixCrLfFilter.CrLf.newInstance("unix")) })
            }
        }
    }
}