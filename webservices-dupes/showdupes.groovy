lineNo = 0
Set<String> dupeClasses = [] as Set
(new File("/var/tmp/webservices.dupe.txt")).eachLine { line ->
    String classesList = line.split('classes were found: ')[1].trim() - "[" - "]."
    println "'${classesList}'"
    classesList = classesList.split(",").findAll {
        it.indexOf('$') == -1
    }.each { 
        dupeClasses << it.trim() 
    }
}
dupeClasses.sort().each {
    println it
}