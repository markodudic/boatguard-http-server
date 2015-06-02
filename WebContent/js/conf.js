$(document).ready(function () {
   
    if (!localStorage.getItem("logged")) {
    	window.location.replace("index.html");   
    }

  var
    nestedObjects =[],
    container = document.getElementById('example'),
    exampleConsole = document.getElementById('exampleconsole'),
    load = document.getElementById('load'),
    hot;

  hot = new Handsontable(container, {
    data: nestedObjects,
    columnSorting: true,
    manualColumnResize: true,
    //fixedColumnsLeft: 4,
    colHeaders: ['Id', 'Code', 'Value'],
    columns: [
      {data: 'id_setting', readOnly: true},
      {data: 'code', readOnly: true},
      {data: 'value'}
    ],
    cells: function(r,c, prop) {
        var cellProperties = {};
        return cellProperties;        
    },    
    minSpareRows: 1
  });

                  
   var QueryString = function () {
	  // This function is anonymous, is executed immediately and 
	  // the return value is assigned to QueryString!
	  var query_string = {};
	  var query = window.location.search.substring(1);
	  var vars = query.split("&");
	  for (var i=0;i<vars.length;i++) {
	    var pair = vars[i].split("=");
	        // If first entry with this name
	    if (typeof query_string[pair[0]] === "undefined") {
	      query_string[pair[0]] = pair[1];
	        // If second entry with this name
	    } else if (typeof query_string[pair[0]] === "string") {
	      var arr = [ query_string[pair[0]], pair[1] ];
	      query_string[pair[0]] = arr;
	        // If third or later entry with this name
	    } else {
	      query_string[pair[0]].push(pair[1]);
	    }
	  } 
	    return query_string;
	} ();
	
	update();
  
  function update() {
	  ajax('/boatguard/getobusettings?obuid='+QueryString.id_obu, 'GET', '', function(res) {
		  var data = JSON.parse(res.response);
		   
		  hot.loadData(data);
		  exampleConsole.innerText = 'Data updated: ' + new Date();
	  });
  }
  
  Handsontable.Dom.addEvent(load, 'click', function() {
	  update();
	});
  
  function bindDumpButton() {
  
	  Handsontable.Dom.addEvent(document.body, 'click', function (e) {
		
	    var element = e.target || e.srcElement;
	
	    if (element.nodeName == "BUTTON" && element.name == 'dump') {
	      $.ajax({
              url: "/boatguard/setobusettings?obuid="+QueryString.id_obu,
              data: JSON.stringify(hot.getData()),
              dataType: 'json',
              type: 'POST',
              contentType: "application/json; charset=utf-8",
              success: function (res) {
                  if (res.result === 'ok') {
                      $console.text('Data saved');
                  }
                  else {
                      $console.text('Save error');
                  }
              }
          });	      
	      /*var name = element.getAttribute('data-dump');
	      var instance = element.getAttribute('data-instance');
	      var hot = window[instance];
	      console.log('data of ' + name, hot);*/
	    }
	  });
	}
	bindDumpButton();
});