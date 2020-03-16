/**
 * Timesheet data for the week. Here we have friendly values (keys to maps as defined
 * in translateTimesheetEntry() blow). The column order is defined in index() below.
 */
var timesheetData = [
    ['monday', 'ltg', 'ltg_meetings',  .5],
    ['monday', 'pf',  'pf_tmNewEng', 7.5],

    ['tuesday', 'ltg', 'ltg_meetings',  .5],
    ['tuesday', 'pf',  'pf_tmNewEng', 7.5],

    ['wednesday', 'ltg', 'ltg_meetings',  1.5],
    ['wednesday', 'pf',  'pf_tmNewEng', 6.5],

    ['thursday', 'ltg', 'ltg_meetings',  1],
    ['thursday', 'pf',  'pf_tmNewEng', 7],

    ['friday', 'ltg', 'ltg_meetings',  .5],
    ['friday', 'pf',  'pf_tmNewEng', 7.5],
];

/**
 * Clear any data that already exists in the timesheet. 
 * Populate the timesheet as defined by the variable 'timesheet'.
 */
clearEntireTimesheet();

// Populate the timesheet one entry at a time.
timesheetData.forEach(processTimesheetEntry);

/**
 * Process a single entry in the timesheet.
 * @param {*} untranslatedLine entry for the timesheet (not yet translated to codes)
 * @param {*} i the index of the timesheet entry (fist timesheet entry is index 0)
 */
function processTimesheetEntry(untranslatedLine, i) {
    var line = translateTimesheetEntry(untranslatedLine);
    $("select.client:last option[value='" + line[index('client')] + "']").attr("selected", true).change();
    $("select.project:last option[value='" + line[index('project')] + "']").attr("selected", true);
    $("input.hours:last").val(line[index('hours')]);
    $("select.days:last option[value='" + line[index('day')] + "']").attr("selected", true);  
    timesheet.save();
    console.log("Saved ", line);
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
    var clientToCode = {
        'ltg': "671",
        'pf': "2212"
    };

    /**
     * Project string mapped project code for dropdown.
     */
    var projectToCode = {
        'ltg_meetings': "761",
        'pf_tmNewEng': "10634"
    }

    /**
     * Day string mapped day code for dropdown.
     */
    var dayToCode = {
        'saturday': "1",
        'sunday': "2",
        'monday': "3",
        'tuesday': "4",
        'wednesday': "5",
        'thursday': "6",
        'friday': "7"
    }

    return [
        dayToCode[line[index('day')]],
        clientToCode[line[index('client')]],
        projectToCode[line[index('project')]],
        line[index('hours')]
    ];
}

/**
 * Return the column index for the desired named column
 * @param {*} which the named column to get the column index for.
 */
function index(which) {
    /**
     * Column name for the various columns in 'timesheetData'.
     */
    var columnToIndex = {
        'day': 0,
        'client': 1,
        'project': 2,
        'hours': 3
    }
    return columnToIndex[which];
}
