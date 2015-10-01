 var id;
 var map;
 
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
	    			  
	    			  document.getElementById('set_code').value = id;
	    			  document.getElementById('set_number').value = data.gsm_number;
	    			  document.getElementById('set_email').value = data.email;
	    			  document.getElementById('set_refresh_time').value = data.refresh_time;
	    			  
	    			  initialize();
	    			  setInterval(initialize, 10000);

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
  
  $(document).ready(function () {


  });