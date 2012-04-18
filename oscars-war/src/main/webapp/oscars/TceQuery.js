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
    dojo.connect(pathGrid, "onRowClick", oscars.TceQuery.onPathRowSelect);
};


// select path based on grid row select
oscars.TceQuery.onPathRowSelect = function (/*Event*/ evt) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    var tceQueryPane = dijit.byId("tceQueryPane");
    var pathGrid = dijit.byId("tceQueryResultGrid");
    // get path id
    var item = evt.grid.selection.getFirstSelected();
    var pathId = dijit.byId("hiddenPathId");
    pathId.innerHTML = pathGrid.store.getValues(item, "id");
    var pathBandwidth = dijit.byId("hiddenPathBandwidth");
    pathBandwidth.innerHTML = pathGrid.store.getValues(item, "bandwidth");
    var pathSrcVlan = dijit.byId("hiddenPathSrcVlan");
    pathSrcVlan.innerHTML = pathGrid.store.getValues(item, "srcVlan");
    var pathDstVlan = dijit.byId("hiddenPathDstVlan");
    pathDstVlan.innerHTML = pathGrid.store.getValues(item, "dstVlan");
    var pathDialog = dijit.byId("pathSelectionDialog");
    pathDialog.show();
    var pathIdText = dojo.byId("pathSelectionDialogPathId");
    pathIdText.innerHTML = "<br>Path : <b>" + pathId.innerHTML + "</b><br>";
    // init pathHopsGrid
    var pathHopsGrid = dojo.byId("pathSelectionDialogHopsGrid");
    var hopsData = pathGrid.store.getValues(item, "hops");
    var data1 = {
        identifier: 'id',
        label: 'id',
        items: hopsData
    };
    var store1 = new dojo.data.ItemFileWriteStore({data: data2});
    pathHopsGrid.setStore(store1);
    // init pathScheduleGrid
    var pathScheduleGrid = dijit.byId("pathSelectionDialogScheduleGrid");
    var scheduleData = pathGrid.store.getValues(item, "schedules");
    var data2 = {
        identifier: 'id',
        label: 'id',
        items: scheduleData
    };
    var store2 = new dojo.data.ItemFileWriteStore({data: data2});
    pathScheduleGrid.setStore(store2);
    dojo.connect(pathScheduleGrid, "onRowClick", oscars.TceQuery.onPathScheduleSelect);
};

// select schedule from dialog
oscars.TceQuery.onPathScheduleSelect = function (/*Event*/ evt) {
    var pathScheduleGrid = dijit.byId("pathSelectionDialogScheduleGrid");
    // get schedule id
    item = evt.grid.selection.getFirstSelected();
    // record selection on rowClick
    var schduleStartDate = dijit.byId("hiddenScheduleStartDate");
    var schduleStartTime = dijit.byId("hiddenScheduleStartTime");
    var schduleEndDate = dijit.byId("hiddenScheduleEndDate");
    var schduleEndTime = dijit.byId("hiddenScheduleEndTime");
    schduleStartDate.innerHTML = pathGrid.store.getValues(item, "startDate");
    schduleStartTime.innerHTML = pathGrid.store.getValues(item, "startTime");
    schduleEndDate.innerHTML = pathGrid.store.getValues(item, "endDate");
    schduleEndTime.innerHTML = pathGrid.store.getValues(item, "endTime");
};

// go reservationCreate
oscars.TceQuery.reserveSelectedPath = function (dialogFields) {
    var pathDialog = dijit.byId("pathSelectionDialog");
    pathDialog.hide();

    // fill reservationCrate tab
    oscars.ReservationCreate.resetFields();
    var node = dijit.byId("hiddenPathId");
    dijit.byId("reservationDescription").setValue("Reservation for "+node.innerHTML);
    node = dojo.byId("hiddenPathBandwdidth");
    dijit.byId("bandwidth").setValue(node.innerHTML);
    node = dojo.byId("tceSource");
    dijit.byId("source").setValue(node.value);
    node = dojo.byId("tceDestination");
    dijit.byId("destination").setValue(node.value);
    // tagged VLAN only
    var taggedSrcVlan = dojo.byId("taggedSrcVlan");
    taggedSrcVlan.selectedIndex = 0;
    var taggedDstVlan = dojo.byId("taggedDstVlan");
    taggedDstVlan.selectedIndex = 0;
    // set src VLAN
    node = dojo.byId("hiddenPathSrcVlan");
    var srcVlan = node.innerHTML;
    dijit.byId("srcVlan").setValue(node.innerHTML);
    // set dst VLAN
    var cb = dijit.byId("sameVlan");
    var nodes = dojo.query(".destVlan");
    node = dojo.byId("hiddenPathDstVlan");
    if (node.innerHTML == srcVlan) {
        nodes[0].style.display = "none";
        cb.setAttribute('Ded', true);
        dijit.byId("destVlan").setValue("");
    } else {
        nodes[0].style.display = "";
        cb.setAttribute('checked', false);
        dijit.byId("destVlan").setValue(node.innerHTML);
    }
    // set schedule
    node = dojo.byId("hiddenScheduleStartDate");
    dijit.byId("startDate").setValue(node.innerHTML)
    node = dojo.byId("hiddenScheduleStartTime");
    dijit.byId("startTime").setValue(node.innerHTML)
    node = dojo.byId("hiddenScheduleEndDate");
    dijit.byId("endDate").setValue(node.innerHTML)
    node = dojo.byId("hiddenScheduleEndTime");
    dijit.byId("endTime").setValue(node.innerHTML)
    // TODO: set explicit path ??
    // switch to reservationCrate tab
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
    var node = dojo.byId("hiddenTceSchedules");
    node.value = "";
    node = dijit.byId("hiddenPathId");
    node.innerHTML = "";
    node = dijit.byId("hiddenPathBandwidth");
    node.innerHTML = "";
    node = dijit.byId("hiddenPathSrcVlan");
    node.innerHTML = "";
    node = dijit.byId("hiddenPathDstVlan");
    node.innerHTML = "";
    node = dijit.byId("hiddenScheduleStartDate");
    node.innerHTML = "";
    node = dijit.byId("hiddenScheduleStartTime");
    node.innerHTML = "";
    node = dijit.byId("hiddenScheduleEndDate");
    node.innerHTML = "";
    node = dijit.byId("hiddenScheduleEndTime");
    node.innerHTML = "";
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
    // add to grid
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
