package icu.windea.repl

import icu.windea.repl.util.isDebug
import icu.windea.repl.util.thisLogger
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    if(isDebug()) {
        val timeout = 5
        thisLogger().warn("Debugging, sleep $timeout seconds...")
        TimeUnit.SECONDS.sleep(5)
    }
    
    val configPaths = args
    if (configPaths.isEmpty()) {
        thisLogger().warn("No config paths specified, skipped.")
        return
    }
    
    val allJobs = mutableListOf<Job>()
    configPaths.forEach { configPath ->
        val jobs = Facade.createJobs(configPath)
        allJobs.addAll(jobs)
    }
    allJobs.forEach { job ->
        job.run()
    }
}