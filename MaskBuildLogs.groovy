import java.util.regex.Pattern

/**
 * Use the associated maskBuildLog.bat to launch.
 *
 * This will mask out or remove things like date/timestamps and execution times
 * from TM ant build logs to make them easier to compare with a diff tool 
 * like Meld https://meldmerge.org/
 *
 * If one needs to extend this, https://regex101.com/ is helpful for vetting
 * regular expressions.
 * 
 * Installation
 * ----------------
 * * Place this file, MaskBuildLogs.groovy, and maskBuildsLogs.bat in c:\bin
 * * Put c:\bin on your PATH
 *
 * Usage
 * ----------------
 * Write the output of the build to a file
 *     ant allFullBuild | tee build-output.txt
 *
 * The script will read the specified input file(s), specified on the command line,
 * and mask / remove some portions of the text. Output will be written to the 
 * original filename WITH a suffix of ".masked". 
 * In the below example, the output would be written to 'build-output.txt.masked'.
 *     maskBuildsLogs build-output.txt
 */

class MaskBuildLogs {
    Map<String, Integer> patternUsageCounts = [:]

    List<Pattern> twoArgNoSubPatterns = [
        // Remove time/datestamps from [junit], [java], and [echo].
        ~/^(\s+\[junit\] )\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d+ (.*)$/,
        ~/^(\s+\[java\] )\d{2}:\d{2}:\d{2},\d+ (.*)$/,
        ~/^(\s+\[echo\] )\[\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} \w+\]: (.*)$/,
    ]

    List<Pattern> twoArgPatterns = [
        // [junit] INFO [com.authoria.systemutil.TestErrorLogRunner] - <Test [TestErrorLogRunner] Function [testForSynchronized] Took [2511 ms] [...
        ~/^(.* [Tt]ook \[)\d+( ms\] \[.*)$/,
        // [junit] INFO [com.authoria.hibernate.TestOracleErrMsgUtils] - <Test [TestOracleErrMsgUtils] Function [testInitialization] Took [XX ms] [sql ct]:[2] [sql row ct]:[5989] [sql time]:[256] [qc hit ct]:[0] [qc miss ct]:[0] [qc put ct]:[0] [flush ct]:[1] [hql ct]:[1] [hql row ct]:[5989] [hql time]:[2165 ms] [empty ut]:[0] [cache hit ct]:[1] [cache miss ct]:[0] [cache put ct]:[0] [cache evict ct]:[0] [cache removeAll ct]:[0] [pg sz]:[0] [context]:[]>
        ~/^(.+\] \[sql time\]\:\[)\d+(\] \[.*)$/,
        // [junit] INFO [com.authoria.hibernate.TestOracleErrMsgUtils] - <Test [TestOracleErrMsgUtils] Function [testInitialization] Took [XX ms] [sql ct]:[2] [sql row ct]:[5989] [sql time]:[256] [qc hit ct]:[0] [qc miss ct]:[0] [qc put ct]:[0] [flush ct]:[1] [hql ct]:[1] [hql row ct]:[5989] [hql time]:[2165 ms] [empty ut]:[0] [cache hit ct]:[1] [cache miss ct]:[0] [cache put ct]:[0] [cache evict ct]:[0] [cache removeAll ct]:[0] [pg sz]:[0] [context]:[]>
        ~/^(.+\] \[hql time\]\:\[)\d+( ms\] \[.*)$/,
        // [junit] INFO [com.authoria.systemutil.tx.TransactionHelper] - <[Run Transaction]:[DeleteCustomFieldProcess] [Time]:[11 ms] [sql ct]:[0] [sql row ct]:[0] [sql time]:[XX] [qc hit ct]:[0] [qc miss ct]:[0] [qc put ct]:[0] [flush ct]:[0] [hql ct]:[0] [hql row ct]:[0] [hql time]:[XX ms] [empty ut]:[0] [cache hit ct]:[0] [cache miss ct]:[0] [cache put ct]:[0] [cache evict ct]:[0] [cache removeAll ct]:[0] [pg sz]:[0] [context]:[]>
        ~/^(.+\] \[[Tt]ime\]\:\[)\d+( ms\] \[.*)$/,
        // [junit] ERROR [com.authoria.hibernate.stat.AuthoriaStatistics] - <[UserId]:[55284C012E8F4DDBB3CC1A8B09D6070B] [UserLogin]:[mylogin] [TrackingString]:[/*atm10.x UnitTests */ ] [Took]:[50000 ms] [Rows]:[1000] [Query]:[testHql]>
        ~/^(.+\] \[[Tt]ook\]\:\[)\d+( ms\] \[.*)$/,
        // [junit] WARN [com.authoria.service.ChannelMgr] - <RC connect to URL [http://www.peoplefluent.com] took longer than 5 secs. Total time [8013 ms]>
        ~/^(.* [Tt]otal [Tt]ime \[)\d+( ms\]>)$/,
    ]

    List<Pattern> threeArgPatterns = [
        // [junit] INFO [com.authoria.service.DocumentMgr] - <concatenatePDFs : Processed 1 generators in 100 msecs>
        ~/^(.* [Tt]ook \[?)\d+( (ms|msecs|milliseconds)\]?>)$/,
        // [junit] Parsed 1 blocks [30 patches] from upgrade file [persistence\conf/schema/schema-upgrade-version57.sql] in 10 milliseconds
        ~/^(.+ in )\d+( (ms|milliseconds|msecs)>?)$/,
        // [junit] INFO [com.authoria.service.backgroundtasks.AuthoriaQuartzSchedulerRepository] - <Created Quartz Scheduler for tenant [MT] took [1] ms>
        ~/^(.+ took \[)\d+(\] (ms|milliseconds|msecs)>)$/,
    ]

    static void main(String[] args) {
        MaskBuildLogs mbl = new MaskBuildLogs()
        int exitCode = mbl.run(args)
        System.exit(exitCode)
    }

    int run(String[] args) {
        if (args.size() == 0) {
            println "No files to process"
            return 1
        }
        args.each { String filename ->
            println "Processing ${filename}"
            File processFile = new File(filename)
            initPatternUsage()
            process(processFile)
            patternUsageCounts.each { pattern, count ->
                println "Pattern=${pattern}, count=${count}"
            }
            println ""
        }
        return 0
    }

    void initPatternUsage() {
        twoArgNoSubPatterns.each { pattern ->
            patternUsageCounts[pattern.toString()] = 0
        }

        twoArgPatterns.each { pattern ->
            patternUsageCounts[pattern.toString()] = 0
        }

        threeArgPatterns.each { pattern ->
            patternUsageCounts[pattern.toString()] = 0
        }
    }

    void countPatternUsage(String pattern) {
        patternUsageCounts[pattern] = ((patternUsageCounts[pattern]) + 1)
    }


    String maskLine(String line) {
        twoArgNoSubPatterns.each { pattern ->
            def matcher = line =~ pattern
            matcher.find { String whole, prefix, logLine ->
                line = "${prefix}${logLine}"
                countPatternUsage(pattern.toString())
            }
        }

        twoArgPatterns.each { pattern ->
            def matcher = line =~ pattern
            matcher.find { String whole, prefix, postfix ->
                line = "${prefix}XX${postfix}"
                countPatternUsage(pattern.toString())
            }
        }

        threeArgPatterns.each { pattern ->
            def matcher = line =~ pattern
            matcher.find { String whole, prefix, postfix, msOrMillisecondsIgnored ->
                line = "${prefix}XX${postfix}"
                countPatternUsage(pattern.toString())
            }
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
}