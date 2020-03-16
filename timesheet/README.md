## Overview

This is a script that will assist you with entering your Hive timesheet.

## Preparing the script for entering time for the week

Review the data in fill-timesheet.js within the variable 'timesheetData'. This is how your timesheet
will be filled out. 

* Column order is critical (and defined below in `index(...)`)
* The data for day, client, and project are keys within maps that must later be translated to the map values.
  Adding additional clients, projects requires viewing the source of the Timesheet webapp and editing
  the maps found in `translateTimesheetEntry(...)`.

## Filling your timesheet

**NOTE: When you run this script, your existing timesheet will be cleared and REPLACED with the data
from this script.**

1. Verify the list `timesheetData` represents the timesheet you want to submit.

2. Navigate Chrome so you can see the timesheet for the week in question, such as

    https://hive.ltgplc.com/TimesheetEntry/Timesheet.aspx

3. Copy the contents of the **entire** script to your clipboard.

4. Open **Developer Tools** (**Control-Shift-I**) and select the **Console** tab.

5. Paste the contents of the script from your clipboard. 

6. Press return one more time to execute.

