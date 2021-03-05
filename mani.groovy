import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class mani {

  final static String CACHE_FILE = "mani-all-jars-map.json"

  public static void main(String[] args) {
    if (args.size() != 1) {
      println "Please provide only one argument, a jar file."
      System.exit(1)
    }
    new mani().run(args[0]);
  }
  

  void run(String jarFilename) {

    long start = 0
    long end  = 0

    File jarsMapFile = new File(CACHE_FILE)
    Map<String, List<String>> jarsMap = [:]
    if (!jarsMapFile.exists()) {

      // The list of jars is pretty static...
      // Create a map of jars foud within the current folder
      // and below. Write the map to CACHE_FILE

      // Closure to help with identifying and collecting the jars we find
      def findJarsClosure = { File file ->
        if (file.name.endsWith(".jar")) {
            if (!jarsMap[file.name.toLowerCase()]) {
              jarsMap[file.name.toLowerCase()] = []
            }
            jarsMap[file.name.toLowerCase()] << file.path
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

    List<String> foundJars = jarsMap[jarFilename.toLowerCase()]
    if (!foundJars) {
      // The requested jar file didn't exist.
      println "Jar ${jarFilename} not found"
      System.exit(2)
    }

    // Output the manifest of the jar file
    println ""
    println "----Manifest for"
    foundJars.each {
      println "---- ${it}"
    }
    new java.util.jar.JarFile(foundJars[0]).manifest.mainAttributes.entrySet().each {
      println "${it.key}: ${it.value}"
    }
    println ""
  }
}
