/*
TceQuery.js:   Handles TCE Query.
Xi Yang
*/


dojo.provide("oscars.TceQuery");

// posts request to server to set parameters in initial create reservation form
oscars.TceQuery.init = function () {

};

// posts request to server to create reservation
oscars.TceQuery.sendQuery = function () {
    var legalDates = oscars.TceQuery.checkDateTimes();
    // status bar shows error message
    if (!legalDates) {
        return;
    }
    // check validity of rest of fields
    var valid = dijit.byId("tceQueryForm").validate();
    if (!valid) {
        return;
    } 
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "inprocess";
    oscarsStatus.innerHTML = "Processing TCE Query...";
    dojo.xhrPost({
      url: 'servlet/QueryTCE',
      handleAs: "json",
      load: oscars.TceQuery.handleReply,
      error: oscars.Form.handleError,
      form: dijit.byId("TceQueryForm").domNode
    });
};

// handles replies from servlets having to do with TCE query
oscars.TceQuery.handleReply = function (responseObject, ioArgs) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    // TODO: Display query status message
    // TODO: If successful, add a new "tceQueryResult_n" Tab and transision to that
    /*
    var formNode = dijit.byId("reservationDetailsForm").domNode;
    formNode.gri.value = responseObject.gri;
    dojo.xhrPost({
        url: 'servlet/QueryReservation',
        handleAs: "json",
        load: oscars.ReservationDetails.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("reservationDetailsForm").domNode
    });
    // set tab to reservation details
    var resvDetailsPane = dijit.byId("reservationDetailsPane");
    mainTabContainer.selectChild(resvDetailsPane);
   */
};

// take action when this tab is clicked on
oscars.TceQuery.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    oscarsStatus.innerHTML = "TCE query form";
};

// resets all fields, including ones the standard reset doesn't catch
oscars.TceQuery.resetFields = function () {
    var formNode = dijit.byId("TceQueryForm").domNode;
    // do the standard ones first
    formNode.reset();
    // if layer 3 fields are displayed, display layer 2 ones again since
    // that button is rechecked on reset
    oscars.TceQuery.toggleLayer("layer2");
    // set whether VLAN's are tagged back to default (Tagged)
    var taggedSrcVlan = dojo.byId("taggedSrcVlan");
    taggedSrcVlan.selectedIndex = 0;
    var taggedDestVlan = dojo.byId("taggedDestVlan");
    taggedDestVlan.selectedIndex = 0;
};

oscars.TceQuery.flexibleBandwidthToggler = function (/*Event*/ evt) {
    var cb = dijit.byId("flexibleBandwidth");
    // only one
    var nodes = dojo.query(".flexibleBandwidthDisplay");
    if (cb.checked) {
        nodes[0].style.display = "";
    } else {
        nodes[0].style.display = "none";
    }
};

//TODO: modify this for TCE query (request topology)
oscars.TceQuery.checkDateTimes = function () {
    var currentDate = new Date();
    var msg;
    var startSeconds =
        oscars.DigitalClock.convertDateTime(currentDate, "startDate",
                                            "startTime", true);
    // default is 4 minutes in the future
    var endDate = new Date(startSeconds*1000 + 60*4*1000);
    var endSeconds =
            oscars.DigitalClock.convertDateTime(endDate, "endDate", "endTime",
                                                true);
    // additional checks for legality
    // check for start time more than four minutes in the past
    if (startSeconds < (currentDate.getTime()/1000 - 240)) {
        msg = "Start time is more than four minutes in the past";
    } else if (startSeconds > endSeconds) {
        msg = "End time is before start time";
    } else if (startSeconds == endSeconds) {
        msg = "End time is the same as start time";
    }
    if (msg) {
        var oscarsStatus = dojo.byId("oscarsStatus");
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = msg;
        return false;
    }
    var startSecondsN = dojo.byId("hiddenStartSeconds");
    // set hidden field value, which is what servlet uses
    startSecondsN.value = startSeconds;
    var endSecondsN = dojo.byId("hiddenEndSeconds");
    endSecondsN.value = endSeconds;
    return true;
};

// Add candidate schedule time window to data grid
oscars.TceQuery.addSchedule = function () {
    var scheduleGrid = dijit.byId("tceQueryScheduleGrid");
    var store = scheduleGrid.getStore();
    // parse paratmers
    var item = {col1: startDate, col2: startTime, col3: endDate, col4: endTime, col5: duration};
    store.items.push(item);
    scheduleGrid.sort();
};
