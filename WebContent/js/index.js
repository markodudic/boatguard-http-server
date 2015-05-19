  function login() {
	 var username = document.getElementById('username').value;
     var password = document.getElementById('password').value;
     $.ajax({
          url: "/boatguard/loginuser?username="+username+"&password="+password,
          type: 'POST',
          contentType: "application/json; charset=utf-8",
          success: function (res) {
        	  var data = JSON.parse(res);
    		  if (data.error === null) {
            	  localStorage.setItem("logged", true);
            	  localStorage.setItem("sessionid", data.sessionId);
            	  location.reload();
              }
              else {
                  alert('Wrong login');
              }
          }
      });	      
  }

  function logout() {
	  localStorage.setItem("logged", false);
	  location.reload();
  }
  
  function all_locations() {
	  window.location.replace("/boatguard/location_all.html");
  }
  
  
  $(document).ready(function () {
   
  var
    nestedObjects =[],
    container = document.getElementById('example'),
    exampleConsole = document.getElementById('exampleconsole'),
    load = document.getElementById('load'),
    hot,
    logIn=document.getElementById('login'),
  	obuData=document.getElementById('obu_data');
  
  if (localStorage.getItem("logged") == 'true') {
	  logIn.style.display='none';
	  logIn.style.visibility='hidden'; 
	  obuData.style.display='block';
	  obuData.style.visibility='visible'; 
  }
  else {
	  logIn.style.display='block';
	  logIn.style.visibility='visible'; 
	  obuData.style.display='none';
	  obuData.style.visibility='hidden'; 
  }
    
  function alarmRenderer(instance, td, row, col, prop, value, cellProperties) {
	  Handsontable.renderers.TextRenderer.apply(this, arguments);

	  if ((col == 0) && (value!=undefined)) {
		  td.innerHTML = "<a href='/boatguard/conf.html?id_obu="+value+"&sessionid="+localStorage.getItem("sessionid")+"'>conf</a>";
	  }
	  else if ((col == 1) && (value!=undefined)) {
		  td.innerHTML = "<a href='/boatguard/alarm.html?id_obu="+value+"&sessionid="+localStorage.getItem("sessionid")+"'>alarm</a>";
	  }
	  else if ((col == 2) && (value!=undefined)) {
		  td.innerHTML = "<a href='/boatguard/location.html?id_obu="+value+"&sessionid="+localStorage.getItem("sessionid")+"'>loc</a>";
	  }
	  
	  if ((col == 10) && (value == "2-ALARM")) {
		  td.style.background = '#F00';
	  }
	  else if ((col == 10) && (value == "1-HOME")) {
		  td.style.background = '#0F0';
	  }
	  else if ((col == 16) && ((value == "1-PUMPING") || (value == "2-CLODGED") || (value == "3-DEMAGED") || (value == "4-SERVIS"))) {
		  td.style.background = '#F00';
	  }
	  else if ((col == 20) && (value == "0-NO")) {
		  td.style.background = '#F00';
	  }
	  else if ((col == 20) && (value == "1-YES")) {
		  td.style.background = '#0F0';
	  }
	  else if ((col == 21) && (value == "0-NO")) {
		  td.style.background = '#0F0';
	  }
	  else if ((col == 21) && (value == "1-YES")) {
		  td.style.background = '#F00';
	  }
	  else if ((col == 23) && (value == "0-HOME")) {
		  td.style.background = '#0F0';
	  }
	  else if ((col == 23) && (value == "1-ALARM")) {
		  td.style.background = '#F00';
	  }
	}

	
  Handsontable.renderers.registerRenderer('alarmRenderer', alarmRenderer);
  
  hot = new Handsontable(container, {
    data: nestedObjects,
    columnSorting: true,
    manualColumnResize: true,
    //fixedColumnsLeft: 4,
    colHeaders: ['Conf', 'Alarm', 'Loc',
                 'Id', 'Serial', 'Name', 'Active', 
                 'Last Update', 'Clean Start', 'PIA Update', 
                 'GeoFence', 'Lat', 'Lon', 'Geo Distance', 'GPS Fix', 'Sat Num', 
                 'Pump State',
                 'Battery %','Battery A', 'Battery V', 'Battery Connected', 'Battery Empty',
                 'Anchor State', 'Anchor Drifting',
                 'Lights', 'Fans', 'Doors'
                 ],
    columns: [
      {data: params()},
      {data: params()},
      {data: params()},
      {data: 'obu.uid'},
      {data: 'obu.serial_number'},
      {data: 'obu.name'},
      {data: 'obu.active'},
      {data: property(1)},
      {data: property(2)},
      {data: property(3)},
      {data: property(10)},
      {data: property(11)},
      {data: property(12)},
      {data: property(13)},
      {data: property(14)},
      {data: property(15)},
      {data: property(20)},
      {data: property(30)},
      {data: property(31)},
      {data: property(32)},
      {data: property(33)},
      {data: property(34)},
      {data: property(40)},
      {data: property(41)},
      {data: property(50)},
      {data: property(60)},
      {data: property(70)}
    ],
    cells: function(r,c, prop) {
        var cellProperties = {};
        cellProperties.readOnly = true;
        cellProperties.renderer = "alarmRenderer";
        return cellProperties;        
    },    
    minSpareRows: 1
  });
  
  update();
  
  function update() {
	  ajax('/boatguard/getdata', 'GET', '', function(res) {
		  var data = JSON.parse(res.response);
		   
		  hot.loadData(data);
		  exampleConsole.innerText = 'Data updated: ' + new Date();
	  });
  }
    
  function params() {
      return function (row, value) {
          if (row.states == undefined) return "";
          var l;
          for (i = 0; i < 30; i++) { 
      		if ((row.states[i] != undefined) && (row.states[i].id_state == 11)) {
              	l = "" + row.states[i].id_obu
      			l += "&lat="+row.states[i].value
            }
      		if ((row.states[i] != undefined) && (row.states[i].id_state == 12)) {
              	l += "&lon="+row.states[i].value
            }
          }
          return l;
      }
  }
  
  function property(attr) {
        return function (row, value) {
            if (row.states == undefined) return "";
            for (i = 0; i < 30; i++) { 
        		if ((row.states[i] != undefined) && (row.states[i].id_state == attr)) {
                    if (attr == 1) {
                    	return row.states[i].dateString
                    }
                    else if (attr == 10) {
                    	if (row.states[i].value == 0) return "0-DISABLED"
                    	else if (row.states[i].value == 1) return "1-HOME"
                    	else if (row.states[i].value == 2) return "2-ALARM"
                    }
                    else if (attr == 20) {
                    	if (row.states[i].value == 0) return "0-NORMAL"
                    	else if (row.states[i].value == 1) return "1-PUMPING"
                    	else if (row.states[i].value == 2) return "2-CLODGED"
                    	else if (row.states[i].value == 3) return "3-DEMAGED"
                    	else if (row.states[i].value > 3) return "4-SERVIS"
                    }
                    else if ((attr == 2) || (attr == 33) || (attr == 34)) {
                    	if (row.states[i].value == 0) return "0-NO"
                    	else if (row.states[i].value == 1) return "1-YES"
                    }
                    else if (attr == 40) {
                    	if (row.states[i].value == 0) return "0-OFF"
                    	else if (row.states[i].value == 1) return "1-ON"
                    }
                    else if (attr == 41) {
                    	if (row.states[i].value == 0) return "0-HOME"
                    	else if (row.states[i].value == 1) return "1-ALARM"
                    }
                    else 
                    {
            			return row.states[i].value
                    }
                 } 
            }
        }

    }

  Handsontable.Dom.addEvent(load, 'click', function() {
	  update();
	});
  
/*  function bindDumpButton() {
  
      Handsontable.Dom.addEvent(document.body, 'click', function (e) {
  
        var element = e.target || e.srcElement;
  
        if (element.nodeName == "BUTTON" && element.name == 'dump') {
          var name = element.getAttribute('data-dump');
          var instance = element.getAttribute('data-instance');
          var hot = window[instance];
          console.log('data of ' + name, hot.getData());
        }
      });
    }
  bindDumpButton();*/

});