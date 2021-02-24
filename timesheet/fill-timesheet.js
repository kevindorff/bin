/**
 * Getting the script ready
 * --------------------------------
 * Edit 'timesheetData' below. You will need to create translations
 * within 'clientToCode' and 'projectToCode'. The last column represents the number
 * of hours for the specific project for the day. The value of 'REMAIN' can be used
 * but should always be the LAST entry for a day (this will assume an 8 hour day).
 * 
 * Using the script to enter time
 * --------------------------------
 * To completely replace your timesheet entries for aspecific week:
 * 
 * * Open up the timesheet to the week in question from
 *      https://hive.ltgplc.com/TimesheetEntry/Timesheet.aspx
 * * Open Chrome's Developer Tools (Control-Shift-I)
 * * Select the Console tab of Chromes Developer Tools (we'll call this Chrome Console)
 * * Paste this entire script into the Chrome Console.
 * * Press return to start the script executing
 */


/**
 * Timesheet data for the week. Here we have friendly values (keys to maps as defined
 * in translateTimesheetEntry() blow). The column order is defined in index() below.
 */
 let timesheetData = [
    // Week of 6-Feb-2020
    ['monday', 'ltg_meetings', 0.5],
    ['monday', 'pf_tmNewEng', 'REMAIN'],

    ['tuesday', 'ltg_meetings',  0.5],
    ['tuesday', 'pf_tmNewEng', 'REMAIN'],

    ['wednesday', 'ltg_meetings',  1.5],
    ['wednesday', 'pf_tmNewEng', 'REMAIN'],

    ['thursday', 'ltg_meetings',  1.0],
    ['thursday', 'pf_tmNewEng', 'REMAIN'],

    ['friday', 'ltg_meetings',  1.5],
    ['friday', 'pf_tmNewEng', 'REMAIN'],

    // // Week of 19-Dec-2020
    // ['monday', 'ltg_holiday', 'REMAIN'],
    // ['tuesday', 'ltg_holiday', 'REMAIN'],
    // ['wednesday', 'ltg_holiday', 'REMAIN'],
    // ['thursday', 'ltg_holiday', 'REMAIN'],
    // ['friday', 'ltg_holiday', 'REMAIN'],

    // Week of 26-Dec-2020
    // ['monday', 'ltg_holiday', 'REMAIN'],
    // ['tuesday', 'ltg_holiday', 'REMAIN'],
    // ['wednesday', 'ltg_holiday', 'REMAIN'],
    // ['thursday', 'ltg_holiday', 'REMAIN'],
    // ['friday', 'ltg_bankholiday', 'REMAIN'],
];

/**
 * Clear any data that already exists in the timesheet. 
 * Populate the timesheet as defined by the variable 'timesheet'.
 */
clearEntireTimesheet();

// Populate the timesheet one entry at a time.
timesheetData.forEach(processTimesheetEntry);
console.log("Timesheet entries complete.")
/**
 * Process a single entry in the timesheet.
 * 
 * I'm augmenting the object processTimesheetEntry (this function) to add
 * additional fields (DAY_EXPECTED_HOURS, currentDay, currentDayTotal).
 * These provide static values that exist between function calls
 * since we cannot use a higher level scope to store data (when
 * we run this code within the browser's console window).
 *
 * @param {*} untranslatedLine entry for the timesheet (not yet translated to codes)
 * @param {*} i the index of the timesheet entry (fist timesheet entry is index 0)
 */
 function processTimesheetEntry(untranslatedLine, i) {
    if (typeof processTimesheetEntry.currentDay == 'undefined') {
        // Initialize some static variables for this function
        processTimesheetEntry.DAY_EXPECTED_HOURS = 8
        processTimesheetEntry.currentDay = ""
        processTimesheetEntry.currentDayTotal = 0
    }

    let line = translateTimesheetEntry(untranslatedLine)

    if (line[index('day')] != processTimesheetEntry.currentDay) {
        processTimesheetEntry.currentDay = line[index('day')];
        processTimesheetEntry.currentDayTotal = 0;
    }

    console.log("equals remains? ", (line[index('hours')] == "REMAIN") , line[index('hours')])

    if (line[index('hours')] == "REMAIN") {
        line[index('hours')] = processTimesheetEntry.DAY_EXPECTED_HOURS - processTimesheetEntry.currentDayTotal
    }
    else {
        processTimesheetEntry.currentDayTotal += line[index('hours')]
    }

    console.log("Entering time for line", line)

    $("select.client:last option[value='" + line[index('client')] + "']").attr("selected", true).change();
    $("select.project:last option[value='" + line[index('project')] + "']").attr("selected", true);
    $("input.hours:last").val(line[index('hours')]);
    $("select.days:last option[value='" + line[index('day')] + "']").attr("selected", true);  
    timesheet.save();
}

/**
 * Clear the entire timsesheet.
 */
function clearEntireTimesheet() {
    $('input.delete:checkbox').each(function(){
        $(this).attr('checked',true);
    })               
    timesheet.save();
}

/*
 * ------------------------------------------------------------------------------
 * Costants used to translate timesheet from friendly strings into the
 * codes necessary for the Timesheet website dropdowns, etc.
 * ------------------------------------------------------------------------------
 */

/**
 * Translate a timesheet entry from strings into codes.
 * We are storing the maps in the functions because it seems that
 * even if we define "global" variables we cannot access them from 
 * functions when used from the Javascript Console.
 * @param {*} line line of untranslated timesheet entry
 * @return the translated timesheet entry
 */
function translateTimesheetEntry(line) {
    /**
     * Client string mapped client code for dropdown.
     */
    let clientToCode = {
        'ltg': "671",
        'pf': "2212"
    };

    /**
     * Project string mapped project code for dropdown.
     */
    let projectToCode = {
        'ltg_training': '760',       // Taking LTG training
        'ltg_meetings': '761',
        'ltg_holiday': '751',        // Vacation, paid-time-off
        'ltg_sick': '752',
        'ltg_bankholiday': '3368',   // Day off such as Christmas, memorial day
        'pf_tmNewEng': '10634',
        'pf_tmBugEng': '10636'
    }

    /**
     * Day string mapped day code for dropdown.
     */
    let dayToCode = {
        'saturday': "1",
        'sunday': "2",
        'monday': "3",
        'tuesday': "4",
        'wednesday': "5",
        'thursday': "6",
        'friday': "7"
    }

    return [
        dayToCode[line[inputIndex('day')]],
        clientToCode[line[inputIndex('project')].split("_")[0]],
        projectToCode[line[inputIndex('project')]],
        line[inputIndex('hours')]
    ];
}

/**
 * Return the column index for the desired named column.
 * This is for the fields coming from translateTimesheetEntry.
 * @param {String} which the named column to get the column index for.
 */
function index(which) {
    /**
     * Column name for the various columns from 'translateTimesheetEntry'.
     */
    let columnToIndex = {
        'day': 0,
        'client': 1,
        'project': 2,
        'hours': 3
    }
    return columnToIndex[which];
}

/**
 * Return the column index for the desired column name.
 * This is for the fields of the input, timesheetData.
 * @param {String} which 
 */
function inputIndex(which) {
    /**
     * Column name for the various columns within 'timesheetData'
     */
    let columnToIndex = {
        'day': 0,
        'project': 1,
        'hours': 2
    }
    return columnToIndex[which];
}
