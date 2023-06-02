/**
 * Getting the script ready
 * --------------------------------
 * * Edit `timesheetData` (your timesheet).
 * * You will need to create translations within `clientToCode` and 
 *   `projectToCode`. These values can be found when viewing the 
 *   Hive Timesheet. Open **Developer Tools** (**Control-Shift-I**) 
 *   and select the **Elements** tab. Click **Select an element in the 
 *   page to inspect it** (**Control-Shift-C**) to find the code as the 
 *   `option`'s `value`.
 * * The last column represents the number of hours for the specific project 
 *   for the day. The value of 'REMAIN' can be used, but should always be the 
 *   LAST entry for a day (this will assume an 8 hour day).
 * 
 * Entering you time
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
 * in translateTimesheetEntry() below). 
 * The column order is defined in index() below.
 */
 let timesheetData = [
    // Week of 27-May-2023
    ['monday', 'ltg_bank_holiday', 'Bank holiday', 'REMAIN'],
    
    ['tuesday', 'pf_ta_new_eng', 'Scrum', 0.5],
    ['tuesday', 'ltg_training_trainee', 'Onboarding', 'REMAIN'],

    ['wednesday', 'pf_ta_new_eng', 'Scrum', 0.5],
    ['wednesday', 'ltg_training_trainee', 'Onboarding', 'REMAIN'],

    ['thursday', 'pf_ta_new_eng', 'Scrum, all-hands, goals', 2.0],
    ['thursday', 'ltg_training_trainee', 'Onboarding', 'REMAIN'],

    ['friday', 'pf_ta_new_eng', 'Scrum, pfr-5263', 2.0],
    ['friday', 'ltg_training_trainee', 'Onboarding', 'REMAIN'],
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
    $("input.desc:last").val(line[index('desc')]);
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
        // General
        // --------------
        // 00-760 - LTG - Training (trainee)
        // Used for any training such as LTG central compliance training or personal development
        'ltg_training_trainee': '760',
        // 00-761 - LTG - Internal Meeting
        // For non-project related meetings i.e. 1-2-1 meeting with your line manager.
        // Company update meetings etc. Do not use this for meetings regarding progress
        // on roadmap items.
        'ltg_internal_meetings': '761',	
        // 00-751 - LTG - Holiday
        // Any hours spent taken annual leave allowance (vacation, PTO)
        'ltg_holiday': '751',
        // 00-752 - LTG - Sickness
        // Any hours spent out of work due to illness
        'ltg_sick': '752',
        // 00-019 - LTG - Bank Holiday
        // Use this code for bank holidays (paid holiday days off)
        'ltg_bank_holiday': '3368',     // Day off such as Christmas, memorial day
        // 00-759 - LTG - Management
        // For line managers to use when logging time spent in 1-2-1 with team members 
        // or performing general management duties.
        'ltg_management': '759',
        // PFR
        // --------------
        // 03-021 PeopleFluent - TA New - Engineering && Scrum ceremmonies
        // Development of new functionality and Scrum ceremonies
        'pf_ta_new_eng': '10625',         
        // 03-023 PeopleFluent - TA Bug - Engineering
        // Development of bugfixes or customer-reported issues 
        'pf_ta_bug_eng': '10627',         
        // 03-025 PeopleFluent - TA Tech Debt - Engineering
        // Development work on tech debt items, security enhancements and general maintenance
        'pf_ta_tech_debt_eng': '10629',
        // 03-022 PeopleFluent - TA New - QA
        // QA of new functionality or development of new QA frameworks and Scrum ceremonies
        'pf_ta_new_qa': '10626',
        // 03-024 PeopleFluent - TA Bug - QA
        // QA of bugfixes or customer-reported issues
        'pf_ta_bug_qa': '10628',
        // 03-026 PeopleFluent - TA Tech Debt - QA
        // QA tech debt work such as maintenance of automated frameworks
        'pf_ta_tech_debt_qa': '10630',
        // 03-027 PeopleFluent - TA - QA  Performance
        // Performance testing
        'pf_ta_qa_performance': '10631',
        // TM
        // --------------
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
        line[inputIndex('desc')],
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
        'desc': 3,
        'hours': 4
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
        'desc': 2,
        'hours': 3
    }
    return columnToIndex[which];
}
