#!/usr/bin/env groovy

// Copy grab's to build.gradle dependencies
// Removing them from the script is optional.
@Grab(group='org.apache.commons', module='commons-csv', version='1.9.0')

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.util.Calendar

if (!args) {
  System.err.println "Provide a single argument, a jmeter output .csv file"
  System.exit(1)
}

def file = null
def count = false

args.each { arg ->
  if (arg == '--count') {
    count = true
  }
  else {
    file = new File(arg)
  }
}
if (!file) {
  System.err.println "Please specify a filename to process."
  System.exit(1)
}
if (!file.exists()) {
  System.err.println "Specified file does not exist."
  System.exit(2)
}

System.err.println "Reading JMeter csv file ${file}"

def csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT.withFirstRecordAsHeader())
        .setIgnoreSurroundingSpaces(true)
        .setCommentMarker((char) '#')
        .build()
def records = csvFormat.parse(new FileReader(file))
def header = [
  //'date',
  'responseCode',
  'responseMessage',
  // 'allThreads',
  'URL',
  'label',
].join(" | ")
if (!count) {
  println header
}
def lineToCount = [:]
for (CSVRecord record : records) {
  if (record.get('success').toString() == 'false') {
    Date date = new Date(record.get('timeStamp') as long)
    Calendar cal = Calendar.getInstance()
    cal.setTime(date)
    cal.add(Calendar.HOUR_OF_DAY, 1)
    date = cal.getTime()
    if (record.get('URL') && record.get('URL') != '' && record.get('URL') != 'null') {
      def data = [
        // date,
        record.get('responseCode'),
        record.get('responseMessage'),
        // record.get('allThreads'),
        record.get('URL'),
        record.get('label'),
      ].join(" | ").toString()
      if (count) {
        Integer countOfLine = lineToCount[data]
        if (countOfLine == null) {
          lineToCount[data] = 1
        }
        else {
          lineToCount[data] = countOfLine + 1
        }
      }
      else {
        println data
      }
    }
  }
}

if (!count) {
    return
}

def keys = lineToCount.keySet().sort()
keys.each { key ->
  println "  ${lineToCount[key]}x   ${key}"
}
