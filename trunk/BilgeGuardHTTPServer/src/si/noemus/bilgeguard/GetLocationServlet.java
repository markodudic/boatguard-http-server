package si.noemus.bilgeguard;

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


public class GetLocationServlet extends InitServlet implements Servlet {

	Locale locale = Locale.getDefault();
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public GetLocationServlet() {
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

		String user = (String) request.getParameter("user");

		JSONObject result = getLocation(user);
		System.out.println("result="+result);
		
		PrintWriter  out = null;
    	response.setContentType("application/json;charset=utf-8");
		response.setHeader("cache-control", "no-cache");
		out = response.getWriter();
		out.write(result.toString());
		out.flush();
		out.close();
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest arg0,
	 *      HttpServletResponse arg1)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET POST");		
    	
	
	}	
	

	private JSONObject getLocation(String user) {
    	ResultSet rs = null;
	    Statement stmt = null;
    	//JSONArray results = new JSONArray();
	    JSONObject current = new JSONObject();
	    try {
	    	connectionMake();

	    	String	sql = "select date_format(message_date, '%d.%m.%Y %k:%i:%s') as date, text " +
						"from smsserver_in left join (select obu from users where name='"+user+"') as user on (originator = obu) " +
						"where obu is not null and text like '#bg:location:%' " +
						"order by message_date desc " +
						"limit 1";
	    		
    		System.out.println("sql="+sql);
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	while (rs.next()) {
	    		
	    		current.put("date", rs.getString("date"));
	    		String loc = rs.getString("text");
	    		String[] locData = loc.split(":");
	    		String lat = locData[2].substring(4);
	    		String lon = locData[3].substring(4);
	    		current.put("lat", lat);
	    		current.put("lon", lon);
	    		//results. add(current);
	    	
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
