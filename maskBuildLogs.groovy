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

    // Long took pattern
    //  Took [4593 ms] [sql ct]:[3705] [sql row ct]:[1215] [sql time]:[499] [qc hit ct]:[0] [qc miss ct]:[0] [qc put ct]:[0] [flush ct]:[3] [hql ct]:[2] [hql row ct]:[1215] [hql time]:[498 ms] [empty ut]:[0] [cache hit ct]:[1] [cache miss ct]:[1212] [cache put ct]:[0] [cache evict ct]:[17981] [cache removeAll ct]:[0] [pg sz]:[0] [context]:[]

    def TOOK_PATTERN = ~/^(.* Took \[)\d+( ms\]>)$/
    matcher = line =~ TOOK_PATTERN
    matcher.find { String whole, prefix, postfix ->
        line = "${prefix}XX${postfix}"
    }

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