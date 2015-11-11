 var id;
 var map;
 
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

	
  function send() {
	 var engineguard = document.getElementById('engineguard').value;
     var number = document.getElementById('number').value;
     $.ajax({
          url: "/boatguard/getcode?engineguard="+encodeURIComponent(engineguard)+"&number="+encodeURIComponent(number),
          type: 'POST',
          contentType: "application/json; charset=utf-8",
          success: function (res) {
        	  var data = JSON.parse(res);
    		  if (data.error === null) {
    			  var 	logIn=document.getElementById('login'),
    			  		verification=document.getElementById('verification'),
	  					location=document.getElementById('location');
	  			  logIn.style.display='none';
    			  logIn.style.visibility='hidden'; 
    			  verification.style.display='block';
    			  verification.style.visibility='visible'; 
    			  location.style.display='none';
    			  location.style.visibility='hidden'; 
    			  id = data.id_engineguard;
    			  
            	  localStorage.setItem("egsessionid", data.sessionId);
              }
              else {
            	  localStorage.setItem("eglogged", false);
                  alert('Wrong login');
              }
          }
      });	      
  }
  
  function verify() {
		 var code = document.getElementById('code').value;
	     var number = document.getElementById('number').value;
	     $.ajax({
	          url: "/boatguard/verifycode?code="+encodeURIComponent(code)+"&number="+encodeURIComponent(number)+"&egid="+encodeURIComponent(id)+"&sessionid="+localStorage.getItem("sessionid"),
	          type: 'POST',
	          contentType: "application/json; charset=utf-8",
	          success: function (res) {
	        	  var data = JSON.parse(res);
	    		  if (data.error === null) {
	    			  var 	logIn=document.getElementById('login'),
	    			  		verification=document.getElementById('verification'),
  			  				location=document.getElementById('location');
	    			  logIn.style.display='none';
	    			  logIn.style.visibility='hidden'; 
	    			  verification.style.display='none';
	    			  verification.style.visibility='hidden'; 
	    			  location.style.display='block';
	    			  location.style.visibility='visible'; 	 
	    			  location.style.height='100%'; 	 
	    			  
	    			  document.getElementById('set_code').value = data.serial_number;
	    			  document.getElementById('set_number').value = data.gsm_number;
	    			  document.getElementById('set_email').value = data.email;
	    			  document.getElementById('set_refresh_time').value = data.refresh_time;
	    			  
	    			  initialize();
	    			  setInterval(initialize, 60000);

	            	  localStorage.setItem("eglogged", true);
	              }
	              else {
	            	  localStorage.setItem("eglogged", false);
	                  alert('Wrong code. Try again.');
	              }
	          }
	      });	      
  }
  
  function initialize() {
	  ajax('/boatguard/getegdata?egid='+id, 'GET', '', function(res) {
		  	var parsedJSON = JSON.parse(res.response);
		  	var myLatlng;
		  	var infowindow = new google.maps.InfoWindow();
		  	var bounds = new google.maps.LatLngBounds();
		  	
	        if (map == undefined) {
	        	map = new google.maps.Map(document.getElementById('map-canvas'), {
			      zoom: 10,
			      mapTypeId: google.maps.MapTypeId.ROADMAP
			    });
	        }
	        
	        var lat = 0;
            var lon = 0;
            var time = 0;
            
            var states = parsedJSON.states;
            for (var ii=0;ii<states.length;ii++) {
		        if (states[ii].id_state == 11) {
		        	lon = states[ii].value;
		        	time = states[ii].dateString;
		        }
		        if (states[ii].id_state == 12) {
		        	lat = states[ii].value;
		        }		            
		    }
            
            var marker = new google.maps.Marker({
	    	      position: new google.maps.LatLng(lat, lon),
	    	      map: map,
	    	      title: 'Last location'
	    	});  	
            
            marker.setPosition( new google.maps.LatLng(lat, lon) );
            map.panTo( new google.maps.LatLng(lat, lon) );
            
            
			var i = 0;
	        google.maps.event.addListener(marker, 'click', (function(marker, i) {
	          return function() {
	            infowindow.setContent(time);
	            infowindow.open(map, marker);
	          }
	        })(marker, i));
		      
			//bounds.extend(marker.position);	
		  			
		  	//map.fitBounds(bounds);
	  });
	
  }  
  
  
  function deactivate() {
		 $.ajax({
	          url: "/boatguard/setalarm?&egid="+encodeURIComponent(id)+"&sessionid="+localStorage.getItem("sessionid"),
	          type: 'POST',
	          contentType: "application/json; charset=utf-8",
	          success: function (res) {
	        	  var data = JSON.parse(res);
	    		  if (data.error === null) {

	              }
	              else {

	              }
	          }
	      });	      
}
  
  
  $(document).ready(function () {

	  if(QueryString.alarm != undefined) {
		  var 	logIn=document.getElementById('login'),
	  			verification=document.getElementById('verification'),
				location=document.getElementById('location');
		  logIn.style.display='none';
		  logIn.style.visibility='hidden'; 
		  verification.style.display='none';
		  verification.style.visibility='hidden'; 
		  location.style.display='block';
		  location.style.visibility='visible'; 	 
		  location.style.height='100%'; 
		  
		  id = QueryString.id_engineguard;
		  document.getElementById('set_code').value = QueryString.serial_number;
		  document.getElementById('set_number').value = QueryString.gsm_number;
		  document.getElementById('set_email').value = QueryString.email;
		  document.getElementById('set_refresh_time').value = QueryString.refresh_time;
		  
		  initialize();
		  setInterval(initialize, 30000);
	
		  localStorage.setItem("eglogged", true);		  		  
	  }
  });