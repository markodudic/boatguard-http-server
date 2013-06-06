package si.noemus.bilgeguard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class SetSettingsServlet extends InitServlet implements Servlet {

	Locale locale = Locale.getDefault();
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public SetSettingsServlet() {
		super();
	}
 
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest arg0,
	 *      HttpServletResponse arg1)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("SERVLET GET");		
		doPost(request, response);
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest arg0,
	 *      HttpServletResponse arg1)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET POST");		

		StringBuilder sb = new StringBuilder();
	    BufferedReader br = request.getReader();
	    String str;
	    while( (str = br.readLine()) != null ){
	        sb.append(str);
	    }
	    
	    JSONArray jArray = (JSONArray)JSONValue.parse(sb.toString());
	    JSONObject jObj=(JSONObject)jArray.get(1);
	    String user = (String) jObj.get("user");
	    jObj=(JSONObject)jArray.get(2);
	    String radius = (String) jObj.get("radius");
	    
	    setSettings(user, radius);
    	
    	OutputStream out = null;
    	response.setContentType("text/html");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write("ok.".getBytes());
		out.flush();
		out.close();    	
	
	}	
	

	private void setSettings(String user, String radius) {
    	Statement stmt = null;

	    try {
	    	JSONObject jObj = getLocation(user);
	    	
	    	connectionMake();
			stmt = con.createStatement();   	

	    	String	sql = "update users " +
	    				  " set radius=" + radius + ", x_geo_fence=" + jObj.get("lon") + ",y_geo_fence=" + jObj.get("lat") +
	    				  " where name = '" + user + "'";
	    		
    		System.out.println("sql="+sql);
	    	stmt.executeUpdate(sql);
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) {
	    			stmt.close();
	    		}
			} catch (Exception e) {
			}
	    }	
		
		return;
	}
	
	
	

}
