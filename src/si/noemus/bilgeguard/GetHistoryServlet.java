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


public class GetHistoryServlet extends InitServlet implements Servlet {

	Locale locale = Locale.getDefault();
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public GetHistoryServlet() {
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
	    String interval = (String) jObj.get("interval");

		JSONArray result = getHistory(user, interval);
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
	

	private JSONArray getHistory(String user, String interval) {
    	ResultSet rs = null;
	    Statement stmt = null;
    	//JSONArray results = new JSONArray();
	    JSONArray history = new JSONArray();
	    try {
	    	connectionMake();

	    	String	sql = "select date_format(message_date, '%d.%m.%Y %k:%i:%s') as date, text " +
						"from smsserver_in left join (select obu from users where name='"+user+"') as user on (originator = obu) " +
						"where obu is not null and text like '#bg:%' and " +
						"	message_date > now() - INTERVAL 1 " + interval +
						" order by message_date desc " +
						"limit 100";
	    		
    		System.out.println("sql="+sql);
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	while (rs.next()) {
	    	    JSONObject current = new JSONObject();
	    		current.put("date", rs.getString("date"));

	    		if (!rs.getString("text").split(":")[0].equalsIgnoreCase("#bg") || rs.getString("text").split(":").length == 1) continue;
		    	String data = rs.getString("text").split(":")[1];
	    		String[] dataA = data.split(",");
	    		current.put("pumpa", dataA[0]);
	    		current.put("baterija_as", dataA[1]);
	    		current.put("baterija_napetost", dataA[2]);
	    		current.put("baterija_tok", dataA[3]);
	    		history.add(current);
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

	    return history;
	}
	
	
	

}
