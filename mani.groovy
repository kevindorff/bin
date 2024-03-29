
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import java.util.regex.Pattern
import java.util.regex.Matcher

class mani {

  final static String CACHE_FILE = "mani-all-jars-map.json"

  /**
   * Program entry point.
   */
  public static void main(String[] args) {
    new mani().run(args as List);
  }

  File jarsMapFile 
  File baseFolder
  Map<String, List<String>> jarsMap = [:]
  List<String> jarFilenames = []
  boolean quiet = false   // don't output manifest
  boolean zipList = false
  boolean includeDist = false
  boolean listKnownJars = false
  // Let's always search for all versions... for now.
  // Not a great default, though.
  boolean allVersions = true
  boolean refresh = false

  /**
   * Parse command line arguments.
   */
  void parseArgs(List<String> args) {
    args.each { String arg ->
      if (arg in ['-q', '--quiet']) {
        quiet = true
      }
      else if (arg in ['-d', '--dist']) {
        includeDist = true
      }
      else if (arg in ['-z', '--ziplist']) {
        zipList = true
      }
      else if (arg in ['--list-known-jars']) {
        listKnownJars  = true
      }
      else if (arg in ['-a', '--all-versions']) {
        allVersions = true
      }
      else if (arg in ['--refresh']) {
        refresh = true
      }
      else {
        jarFilenames << arg
      }
    }

    if (!listKnownJars && !jarFilenames.size()) {
      println "Please propvide a list of jar files."
      println "Can pass modes:"
      println " --quiet/-q          don't output the manifest"
      println " --ziplist/-z        list contents of jar file"
      println " --dist/-d           Report for jars in dist folders, too"
      println " --all-versions/-a   Try to expand to all versions"
      println " --list-known-jars   Display list of known jars"
      println " --refresh           Refresh the cache. If jars have changed, add this flag."
      println "Quiet? ${quiet}"
      println "Zip list? ${zipList}"
      println "Include Dist? ${includeDist}"
      println ""
      System.exit(1)
    }
  }

  int numTopLevelJars = 0
  int numTopLevelJarsNotFound = 0
  int totalJarsFound = 0

  /**
   * Kick off the process.
   * 1. Parse arguments
   * 2. If CACHE_FILE couldn't be found, build it.
   * 3. Do the work.
   */
  void run(List<String> args) {

    parseArgs(args)
    long start = 0
    long end  = 0

    jarsMapFile = locateCacheFile()
    baseFolder = jarsMapFile.getParentFile()

    if (refresh) {
        if (jarsMapFile.exists()) {
          jarsMapFile.delete()
        }
    }

    println "Cache file at location: ${jarsMapFile.toString()}"
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

      // Find all of the .jar files and write to cache file
      println "Caching a map of jar files: ${jarsMapFile}"
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

    if (listKnownJars) {
      // List all the jars we know about and quit.
      jarsMap.each { jar, list ->
        println "${jar}"
      }
      // All done
      return
    }

    if (allVersions) {
      jarFilenames = jarFilenames.collect { jarFilename ->
        // We already have a wildcard. Bail.
        def matcher = (jarFilename =~ /^(.*?)-[\d.-]+\.jar$/)
        if (matcher.find()) {
            // We think we found a version. Strip it out and use a wildcard.
            String wildcarded = matcher.group(1) + "-*.jar"
            println "allVersions mode: Changing input from ${jarFilename} to ${wildcarded}"
            return wildcarded
            
        }
        return jarFilename
      }
    }

    // Execute the searches
    handleGlobs(jarFilenames).each { String jarFilename ->
      iteration(jarFilename)
    }

    println "Found ${totalJarsFound} for ${numTopLevelJars} jars"
    println "Didn't find ${numTopLevelJarsNotFound} jars"
  }

  /**
   * Output details from a single jar file that has been found.
   */
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
      if (!quiet) {
        try {
          println "---- Manifest for [${index}] ${path}"
          new java.util.jar.JarFile(new File(baseFolder, foundJars[0])).manifest.mainAttributes.entrySet().each {
            println "${it.key}: ${it.value}"
          }
        }
        catch (Exception e) {
          println "!! ERROR reading manifest"
        }
        println ""
      }
      if (zipList) {
        println "---- Jar List for [${index}] ${path}"
        println "unzip -v ${new File(path, baseFolder)}".execute().text
        println ""
      }
    }
    println ""
    numTopLevelJars++
  }

  /**
   * Convert globs (* for wildcards, ? isn't supported) into
   * regular expressions.
   */
  List<String> handleGlobs(List<String> jarFilenames) {
    List<String> result = []
    jarFilenames.each { String jarFilename ->
      // Very simple glob to regex (only supporting * and fixing .)
      String jarRegex = jarFilename.replaceAll('[.]', '\\\\.').replaceAll('\\*', '.*')
      Pattern jarPattern = Pattern.compile("^${jarRegex}\$")
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

  /**
   * Look in the current and all parent directories for CACHE_FILE.
   * If found, that config file is returned (and all searching will
   * be relative to that folder). If not found, this will
   * return a File to CACHE_FILE in the current directory.
   */
  File locateCacheFile() {
    // getParentFile() seems to depend on the File having
    // a fill path, not just starting with ".".
    File currentFolder = new File(new File(".").canonicalPath)
    File current = currentFolder
    while (true) {
      File possibleCacheFile = new File(current, CACHE_FILE)
      if (possibleCacheFile.exists()) {
        return possibleCacheFile
      }
      current = current.getParentFile()
      if (current == null) {
        break
      }
    }
    // Not found. Put it in the current folder.
    return new File(currentFolder, CACHE_FILE)
  }

}
