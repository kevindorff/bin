## Overview

This is a script that will assist you with entering your Hive timesheet.

## Preparing the script for entering time for the week

* Edit `timesheetData` (your timesheet).
* You will need to create translations within `clientToCode` and `projectToCode`.
   These values can be found when viewing the Hive Timesheet. Open **Developer Tools** (**Control-Shift-I**) and select the **Elements** tab. Click **Select an element in the page to inspect it** (**Control-Shift-C**) to find the code as the `option`'s `value`.
* The last column represents the number of hours for the specific project for the day. 
   The value of 'REMAIN' can be used, but should always be the LAST entry for a day 
   (this will assume an 8 hour day).

## Entering you time

**NOTE: When you run this script, your existing timesheet will be cleared and REPLACED with the data
from this script.**

1. Verify the list `timesheetData` represents the timesheet you want to submit.

2. Navigate Chrome so you can see the timesheet for the week in question, such as

    https://hive.ltgplc.com/TimesheetEntry/Timesheet.aspx

3. Copy the contents of the **entire** script to your clipboard.

4. Open **Developer Tools** (**Control-Shift-I**) and select the **Console** tab.

5. Paste the contents of the script from your clipboard. 

6. Press return one more time to execute.

