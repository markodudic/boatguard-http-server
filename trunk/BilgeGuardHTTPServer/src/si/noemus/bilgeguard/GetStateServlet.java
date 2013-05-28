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


public class GetStateServlet extends InitServlet implements Servlet {

	Locale locale = Locale.getDefault();
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public GetStateServlet() {
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
		response.setHeader("Access-Control-Allow-Origin", "*");
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
						"where obu is not null and text like '#bg:%' " +
						"order by message_date desc " +
						"limit 1";
	    		
    		System.out.println("sql="+sql);
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	while (rs.next()) {
	    		current.put("date", rs.getString("date"));
	    	
	    		/*
	    		#BG:00,02F3,01F3,00E,15.1,46.5,20130523181121.000
	    		1.-UVOD
	    		2.-STANJE PUMPE (00-NE PUMPA, 01-PUMPA, 10-ZAMAŠENA)
	    		3.-PRETEÈENE As
	    		4.-STANJE NAPETOSTI
	    		5.-TRENUTNI TOK( ENOTE ŠE NE VEM)
	    		6.-LONGITUDE
	    		7.-LATITUDE
	    		8.-UTC TIME
	    		*/
	    		String data = rs.getString("text").split(":")[1];
	    		String[] dataA = data.split(",");
	    		current.put("pumpa", dataA[0]);
	    		current.put("baterija_as", dataA[1]);
	    		current.put("baterija_napetost", dataA[2]);
	    		current.put("baterija_tok", dataA[3]);
	    		current.put("lon", dataA[4]);
	    		current.put("lat", dataA[5]);
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
