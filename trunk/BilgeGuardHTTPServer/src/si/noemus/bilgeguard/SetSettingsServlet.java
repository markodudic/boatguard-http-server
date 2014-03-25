package si.noemus.bilgeguard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import si.bisoft.commons.dbpool.DbManager;
import si.noemus.boatguard.util.Util;


public class SetSettingsServlet implements Servlet {

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
	    jObj=(JSONObject)jArray.get(3);
	    String active = (String) jObj.get("active");
	    
	    setSettings(user, radius, active);
    	
    	OutputStream out = null;
    	response.setContentType("text/html");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write("ok.".getBytes());
		out.flush();
		out.close();    	
	
	}	
	

	private void setSettings(String user, String radius, String active) {
		Connection con = null;
		Statement stmt = null;

	    try {
	    	JSONObject jObj = getLocation(user);
	    	
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

	    	String	sql = "update users " +
	    				  //" set radius=" + radius + ", x_geo_fence=" + jObj.get("lon") + ",y_geo_fence=" + jObj.get("lat") + ",active=" + active +
	    				  " set radius=" + radius + ",active=" + active +
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
	
	public JSONObject getLocation(String user) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	//JSONArray results = new JSONArray();
	    JSONObject current = new JSONObject();
	    try {
	    	con = DbManager.getConnection("config");;

	    	String	sql = "select date_format(message_date, '%d.%m.%Y %k:%i') as date, text, x_geo_fence, y_geo_fence, radius, active " +
						"from smsserver_in left join (select obu, x_geo_fence, y_geo_fence, radius, active " +
						"								from users " +
						"								where name='"+user+"') as user " +
						"on (originator = obu) " +
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
	    		2.-STANJE PUMPE (00-NE PUMPA, 01-PUMPA, 10-ZAMA�ENA)
	    		3.-PRETE�ENE As
	    		4.-STANJE NAPETOSTI
	    		5.-TRENUTNI TOK( ENOTE �E NE VEM)
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
	    		
	    		if (rs.getString("active").equals("1") && 
	    			Math.round(Float.parseFloat(dataA[4]))!=0 && 
	    			Math.round(Float.parseFloat(dataA[5]))!=0) {
		    		float lat1 = Util.transform(Float.parseFloat(dataA[4]));
		    		float lon1 = Util.transform(Float.parseFloat(dataA[5]));
		    		float lat2 = Util.transform(Float.parseFloat(rs.getString("x_geo_fence")));
		    		float lon2 = Util.transform(Float.parseFloat(rs.getString("y_geo_fence")));
		    		int radius = Integer.parseInt(rs.getString("radius"));
		    		double distance = Util.gps2m(lat1, lon1, lat2, lon2);
		    		if (distance <= radius) {
			    		current.put("geofence", "1"); //home
		    		} else {
			    		current.put("geofence", "2"); //alarm
		    		}
	    		} else {
		    		current.put("geofence", "0"); //ni vklopljen
	    		}
	    		
	    	}
	    	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }

	    return current;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(ServletConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
