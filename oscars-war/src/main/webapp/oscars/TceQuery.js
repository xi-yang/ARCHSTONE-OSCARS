/*
TceQuery.js:   Handles TCE Query.
Xi Yang
*/


dojo.provide("oscars.TceQuery");

// posts request to server to set parameters in initial create reservation form
oscars.TceQuery.init = function () {
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.numTceSchdules = 0;
};

// posts request to server to create reservation
oscars.TceQuery.sendQuery = function () {
    var legalDates = oscars.TceQuery.checkDateTimes();
    // status bar shows error message
    if (!legalDates) {
        return;
    }

    // convert data before sending

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
      form: dijit.byId("tceQueryForm").domNode
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
    var formNode = dijit.byId("tceQueryForm").domNode;
    // do the standard ones first
    formNode.reset();
    var nodes = dojo.query(".flexibleBandwidthDisplay");
    nodes[0].style.display = "none";
    var oscarsStatus = dojo.byId("oscarsStatus");
    if (oscarsStatus.numTceSchdules > 0) {
        var scheduleGrid = dijit.byId("tceQueryScheduleGrid");
        scheduleGrid.setStore(null);
        oscarsStatus.numTceSchdules = 0;
    }
    var hiddenTceSchedules = dojo.byId("hiddenTceSchedules");
    hiddenTceSchedules.value = "";
};

oscars.TceQuery.flexibleBandwidthToggler = function (/*Event*/ evt) {
    var cb = dijit.byId("flexibleBandwidth");
    var hiddenFlexibleBw = dojo.byId("hiddenFlexibleBandwidth");
    // only one
    var nodes = dojo.query(".flexibleBandwidthDisplay");
    if (cb.checked) {
        nodes[0].style.display = "";
        hiddenFlexibleBw.value="true";
    } else {
        nodes[0].style.display = "none";
        hiddenFlexibleBw.value="false";
    }
};

//TODO: modify this for TCE query (request topology)
oscars.TceQuery.checkDateTimes = function () {
    var currentDate = new Date();
    var msg;
    var startSeconds =
        oscars.DigitalClock.convertDateTime(currentDate, "startDate", "startTime", true);
    // default is 4 minutes in the future
    var endDate = new Date(startSeconds*1000 + 60*4*1000);
    var endSeconds =
            oscars.DigitalClock.convertDateTime(endDate, "endDate", "endTime",true);

    // TODO: verify timeSchedules 
    // TODO: add default time window to hiddenTceSchedules if empty

    return true;
};

// Add candidate schedule time window to data grid
oscars.TceQuery.addSchedule = function () {
    // parse paratmers
    var startDate = dijit.byId("tceStartDate");
    var startTime = dijit.byId("tceStartTime");
    var endDate = dijit.byId("tceEndDate");
    var endTime = dijit.byId("tceEndTime");
    var scheduleGrid = dijit.byId("tceQueryScheduleGrid");
    // TODO: verify start >= now and (end - start) >= duration 
    // push to grid
    var oscarsStatus = dojo.byId("oscarsStatus");
    var hiddenTceSchedules = dojo.byId("hiddenTceSchedules");
    oscarsStatus.numTceSchdules += 1;
    var item = {'id':oscarsStatus.numTceSchdules ,'startDate':startDate.toString(),'startTime':startTime.toString(),'endDate':endDate.toString(),'endTime':endTime.toString()};
    var store = scheduleGrid.store;
    if (store == null) {
        var data = {
            identifier: 'id',
            label: 'id',
            items: [item]
        };
        store = new dojo.data.ItemFileWriteStore({data: data});
        scheduleGrid.setStore(store);
    } else {
        store.newItem(item);
        hiddenTceSchedules.value = hiddenTceSchedules.value + ";"
    }
    hiddenTceSchedules.value = hiddenTceSchedules.value + startDate.toString();
    hiddenTceSchedules.value = hiddenTceSchedules.value + startTime.toString();
    hiddenTceSchedules.value = hiddenTceSchedules.value + ",";
    hiddenTceSchedules.value = hiddenTceSchedules.value + endDate.toString();
    hiddenTceSchedules.value = hiddenTceSchedules.value + endTime.toString();
};
