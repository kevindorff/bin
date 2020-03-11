function getData() {
    return [
        [days('monday'), clients('ltg'), projects('ltg_meetings'),  .5],
        [days('monday'), clients('pf'),  projects('pf_tmNewEng'), 7.5],
    
        [days('tuesday'), clients('ltg'), projects('ltg_meetings'),  .5],
        [days('tuesday'), clients('pf'),  projects('pf_tmNewEng'), 7.5],
    
        [days('wednesday'), clients('ltg'), projects('ltg_meetings'),  1.5],
        [days('wednesday'), clients('pf'),  projects('pf_tmNewEng'), 6.5],
    
        [days('thursday'), clients('ltg'), projects('ltg_meetings'),  1],
        [days('thursday'), clients('pf'),  projects('pf_tmNewEng'), 7],
    
        [days('friday'), clients('ltg'), projects('ltg_meetings'),  .5],
        [days('friday'), clients('pf'),  projects('pf_tmNewEng'), 7.5],
    ];

}

/**
 * Clear any data that already exists in the timesheet. 
 * Populate the timesheet as defined by getData().
 */
clearTimesheet();
getData().forEach(timesheetEntry); 
function timesheetEntry(line, i) {
    $("select.client:last option[value='" + line[index('client')] + "']").attr("selected", true).change();
    $("select.project:last option[value='" + line[index('project')] + "']").attr("selected", true);
    $("input.hours:last").val(line[index('hours')]);
    $("select.days:last option[value='" + line[index('day')] + "']").attr("selected", true);  
    timesheet.save();
    console.log("Saved ", line);
}

function clearTimesheet() {
    $('input.delete:checkbox').each(function(){
        $(this).attr('checked',true);
    })               
    timesheet.save();
}

// ------- CONSTANTS -------
function clients(which) {
    return {
        ltg: "671",
        pf: "2212"
    }[which];
}
function projects(which) {
    return {
        ltg_meetings: "761",
        pf_tmNewEng: "10634"
    }[which];
} 
function days(which) {
    return {
        saturday: "1",
        sunday: "2",
        monday: "3",
        tuesday: "4",
        wednesday: "5",
        thursday: "6",
        friday: "7"
    }[which];
}
function index(which) {
    return {
        day: 0,
        client: 1,
        project: 2,
        hours: 3
    }[which];
}
