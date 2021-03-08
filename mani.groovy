
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import java.util.regex.Pattern
import java.util.regex.Matcher

class mani {

  final static String CACHE_FILE = "mani-all-jars-map.json"

  public static void main(String[] args) {
    new mani().run(args as List);
  }

  File jarsMapFile 
  Map<String, List<String>> jarsMap = [:]
  List<String> jarFilenames = []
  boolean listMode = false
  boolean includeDist = false

  void parseArgs(List<String> args) {
    args.each { String arg ->
      if (arg in ['-l', '--list']) {
        listMode = true
      }
      else if (arg in ['-d', '--dist']) {
        includeDist = true
      }
      else {
        jarFilenames << arg
      }
    }

    if (!jarFilenames.size()) {
      println "Please provide only one argument, a jar file."
      println "Can pass modes:"
      println " --list/-l       just list jar files, don't dump manifest for first"
      println "Listmode? ${listMode}"
      println "jarFilenames? ${jarFilenames}"
      System.exit(1)
    }
  }

  int numTopLevelJars = 0
  int numTopLevelJarsNotFound = 0
  int totalJarsFound = 0

  void run(List<String> args) {

    parseArgs(args)
    long start = 0
    long end  = 0

    jarsMapFile = new File(CACHE_FILE)
    if (!jarsMapFile.exists()) {
      // The list of jars is pretty static...
      // Create a map of jars foud within the current folder
      // and below. Write the map to CACHE_FILE

      // Closure to help with identifying and collecting the jars we find
      def findJarsClosure = { File file ->
        if (file.name.endsWith(".jar")) {
            String filePath = file.path
            if (!jarsMap[file.name]) {
              jarsMap[file.name] = []
            }
            jarsMap[file.name] << file.path
        }
      }

      // Find all of the .jar files
      println "First run. Caching a map of jar files: ${jarsMapFile}"
      start = System.currentTimeMillis()
      new File(".").eachFileRecurse(findJarsClosure)
      // Write the map of jar files as JSON to CACHE_FILE
      def jarsMapJson = JsonOutput.prettyPrint(JsonOutput.toJson(jarsMap))
      jarsMapFile.write jarsMapJson
      end = System.currentTimeMillis()
      println "... done - took ${end-start}ms"
    }
    else {
      // Read the map of jars from CACHE_FILE
      println "Using the cache file: ${jarsMapFile}"
      def jsonSlurper = new JsonSlurper()
      jarsMap = jsonSlurper.parseText(jarsMapFile.text)
    }

    // Execute the searches
    handleGlobs(jarFilenames).each { String jarFilename ->
      iteration(jarFilename)
    }

    println "Found ${totalJarsFound} for ${numTopLevelJars} jars"
    println "Didn't find ${numTopLevelJarsNotFound} jars"
  }

  void iteration(String jarFilename) {
    List<String> foundJars = jarsMap[jarFilename].findAll { jar ->
      // Ignore copies in \dist\ folders unless we are running -d, --dist
      return includeDist || !jar.toLowerCase().contains(File.separator + 'dist' + File.separator)
    }

    if (!foundJars) {
      // The requested jar file didn't exist.
      println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
      println "!! Jar ${jarFilename} not found"
      println ""
      numTopLevelJarsNotFound++
      return
    }

    // Output the manifest of the jar file
    println "--------------------------------------------------------"
    println "---- Matches for [${numTopLevelJars}] ${jarFilename} ${includeDist ? "(includes ${File.separator}dist${File.separator} jars)" : ""}"
    foundJars.eachWithIndex { path, index ->
      println "---- [${index}] ${path}"
      totalJarsFound++
      if (!listMode) {
        try {
          println "---- Manifest for [${index}] ${path}"
          new java.util.jar.JarFile(foundJars[0]).manifest.mainAttributes.entrySet().each {
            println "${it.key}: ${it.value}"
          }
        }
        catch (Exception e) {
          println "!! ERROR reading manifest"
        }
        println ""
      }
    }
    println ""
    numTopLevelJars++
  }

  List<String> handleGlobs(List<String> jarFilenames) {
    List<String> result = []
    jarFilenames.each { String jarFilename ->
      // Very simple glob to regex (only supporting * and fixing .)
      String jarRegex = jarFilename.replaceAll('[.]', '\\\\.').replaceAll('\\*', '.*')
      Pattern jarPattern = Pattern.compile(jarRegex)
      jarsMap.keySet().each { String jarInMap ->
        Matcher matcher = jarInMap =~ jarPattern
        matcher.find()
        if (matcher.size() > 0) {
          result << jarInMap
        }
      }
    }
    return result
  }
}
