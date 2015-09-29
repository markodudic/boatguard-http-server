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
	     $.ajax({
	          url: "/boatguard/verifycode?code="+encodeURIComponent(code) + "&sessionid="+localStorage.getItem("sessionid"),
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
	    			  
	            	  localStorage.setItem("eglogged", true);
	              }
	              else {
	            	  localStorage.setItem("eglogged", false);
	                  alert('Wrong code. Try again.');
	              }
	          }
	      });	      
  }
  
  $(document).ready(function () {


  });