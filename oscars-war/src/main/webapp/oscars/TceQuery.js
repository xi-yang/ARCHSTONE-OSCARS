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

    var pathGrid = dijit.byId("tceQueryResultGrid");
    var data = {
        identifier: 'id',
        label: 'id',
        items: responseObject.pathData
    };
    var store =
        new dojo.data.ItemFileWriteStore({data: data});
    pathGrid.setStore(store);
    oscarsState.pathGridInitialized = true;
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "success";
    oscarsStatus.innerHTML = "TCEQuery reply path list";
    dojo.connect(pathGrid, "onRowClick", oscars.TceQuery.onResvRowSelect);
};


// select path based on grid row select
oscars.TceQuery.onResvRowSelect = function (/*Event*/ evt) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    var tceQueryPane = dijit.byId("tceQueryPane");
    var pathGrid = dijit.byId("resvGrid");
    // get path id
    var item = evt.grid.selection.getFirstSelected();
    var pathId = pathGrid.store.getValues(item, "id");
    var hopText = pathGrid.store.getValues(item, "hopText");
    var pathDialog = dijit.byId("pathSelectionDialog");
    pathDialog.show();
    var pathIdText = dojo.byId("pathSelectionDialogPathId");
    pathIdText.innerHTML = pathId;
    var pathHopsText = dojo.byId("pathSelectionDialogHops");
    pathHopsText.innerHTML = hopText; // TODO: break into multiple lines
    // init pathSelectGrid     var pathScheduleGrid = dijit.byId("pathSelectionDialogScheduleGrid");
    var scheduleData = pathGrid.store.getValues(item, "schedules");
    var data = {
        identifier: 'id',
        label: 'id',
        items: scheduleData
    };
    var store = new dojo.data.ItemFileWriteStore({data: data});
    pathScheduleGrid.setStore(store);
    dojo.connect(pathScheduleGrid, "onRowClick", oscars.TceQuery.onPathScheduleSelect);
};

// select schedule from dialog
oscars.TceQuery.onPathScheduleSelect = function (/*Event*/ evt) {
    var pathScheduleGrid = dijit.byId("pathSelectionDialogScheduleGrid");
    // get schedule id
    var item = evt.grid.selection.getFirstSelected();
    var scheduleId = pathGrid.store.getValues(item, "id");
    // TODO: record selection on rowClick
};

// go to reservation create pane
oscars.TceQuery.reserveSelectedPath = function (dialogFields) {
    var pathDialog = dijit.byId("pathSelectionDialog");
    pathDialog.hide();    
    // TODO: fill reservationCrate tab
    var mainTabContainer = dijit.byId("mainTabContainer");
    var reservationCreatePane = dijit.byId("reservationCreatePane");
    mainTabContainer.selectChild(reservationCreatePane);
}

// take action when this tab is clicked on
oscars.TceQuery.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    oscarsStatus.innerHTML = "TCE query form";
    var pathGrid = dijit.byId("tceQueryResultGrid");
    if (pathGrid && !oscarsState.pathGridInitialized) {
        oscarsStatus.className = "success";
        oscarsStatus.innerHTML = "TCEQuery reply path list";
    }
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
        var maxBw = dijit.byId("maxBandwidth");
        maxBw.value = "0";
        var minBw = dijit.byId("minBandwidth");
        minBw.value = "0";
        
    }
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
    hiddenTceSchedules.value = hiddenTceSchedules.value + "--";
    hiddenTceSchedules.value = hiddenTceSchedules.value + endDate.toString();
    hiddenTceSchedules.value = hiddenTceSchedules.value + endTime.toString();
};
