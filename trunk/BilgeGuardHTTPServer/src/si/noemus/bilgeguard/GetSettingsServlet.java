package si.noemus.bilgeguard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class GetSettingsServlet extends InitServlet implements Servlet {

	Locale locale = Locale.getDefault();
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public GetSettingsServlet() {
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
	    
	   JSONObject result = getSettings(user);
		System.out.println("result="+result);
		
		PrintWriter  out = null;
    	response.setContentType("application/json;charset=utf-8");
		response.setHeader("cache-control", "no-cache");
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getWriter();
		out.write(result.toString());
		out.flush();
		out.close();
	
	}	
	

	private JSONObject getSettings(String user) {
    	ResultSet rs = null;
	    Statement stmt = null;

	    JSONObject current = new JSONObject();
	    try {
	    	connectionMake();

	    	String	sql = "select x_geo_fence, y_geo_fence, radius, active " +
						"from users " +
						"where name='"+user+"'";
	    		
    		System.out.println("sql="+sql);
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	while (rs.next()) {
	    		current.put("x_geo_fence", rs.getString("x_geo_fence"));
	    		current.put("y_geo_fence", rs.getString("y_geo_fence"));
	    		current.put("radius", rs.getString("radius"));
	    		current.put("active", rs.getString("active"));
	    	}
	    	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) {
	    			rs.close();
	    		}

	    		if (stmt != null) {
	    			stmt.close();
	    		}
			} catch (Exception e) {
			}
	    }	

	    return current;
	}
	
	
	

}
