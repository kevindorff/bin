

/**
 * Use the associated maskBuildLog.bat to launch.
 *
 * This will mask out or remove things like times from TM ant build logs to make them
 * easier to compare with a diff tool like Meld https://meldmerge.org/
 * 
 * Command line arguments: one or more log files from TM ant build. The easiest way to capture this is to pipe
 * the build through "tee" such as 
 *     ant allFullBuild | tee logfile.txt
 *     groovy logfile.txt
 * This will create a file "logfile.txt.masked" which should be easier to diff with another masked log file.
 */

if (args.size() == 0) {
    println "No files to process"
    System.exit(1)
}

args.each { String filename ->
    File processFile = new File(filename)
    process(processFile)
}


String maskLine(String line) {
    def JUNIT_MASK_PATTERN = ~/^(\s+\[junit\] )\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d+ (.*)$/
    def matcher = line =~ JUNIT_MASK_PATTERN
    matcher.find { String whole, prefix, logLine ->
        line = "${prefix}${logLine}"
    }

    def JAVA_MASK_PATTERN = ~/^(\s+\[java\] )\d{2}:\d{2}:\d{2},\d+ (.*)$/
    matcher = line =~ JAVA_MASK_PATTERN
    matcher.find { String whole, prefix, logLine ->
        line = "${prefix}${logLine}"
    }

    def ECHO_MASK_PATTERN = ~/^(\s+\[echo\] )\[\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} \w+\]: (.*)$/
    matcher = line =~ ECHO_MASK_PATTERN
    matcher.find { String whole, prefix, logLine ->
        line = "${prefix}${logLine}"
    }


    def TOOK_PATTERN = ~/^(.* Took \[)\d+( ms\]>)$/
    matcher = line =~ TOOK_PATTERN
    matcher.find { String whole, prefix, postfix ->
        line = "${prefix}XX${postfix}"
    }

    def TOOK2_PATTERN = ~/^(.* took )\d+( ms>)$/
    matcher = line =~ TOOK2_PATTERN
    matcher.find { String whole, prefix, postfix ->
        line = "${prefix}XX${postfix}"
    }

    // Long took pattern, not yet implemented
    // [junit] INFO [com.authoria.hibernate.filters.TestCandidateNoteRLSBuilder] - <Test [TestCandidateNoteRLSBuilder] Function [testAllTests] Took [3869 ms] [sql ct]:[1502] [sql row ct]:[291] [sql time]:[3081] [qc hit ct]:[18] [qc miss ct]:[84] [qc put ct]:[84] [flush ct]:[4] [hql ct]:[105] [hql row ct]:[309] [hql time]:[3038 ms] [empty ut]:[0] [cache hit ct]:[25] [cache miss ct]:[0] [cache put ct]:[0] [cache evict ct]:[259] [cache removeAll ct]:[20] [pg sz]:[0] [context]:[]>

    return line
}

boolean process(File input) {
    if (!input.exists()) {
        return false
    }
    File output = new File(input.getCanonicalPath() + ".masked")
    output.withWriter { Writer writer ->
        input.withReader { Reader reader ->
            reader.eachLine { String line ->
                writer.println maskLine(line)
            }
        }
    }
}