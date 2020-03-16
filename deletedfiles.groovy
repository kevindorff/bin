#!/usr/bin/env groovy

String prevLine = ""
(new File(args[0])).eachLine { line ->
    if (line.contains("deleted file mode")) {
        println prevLine
    }
    prevLine = line
}
