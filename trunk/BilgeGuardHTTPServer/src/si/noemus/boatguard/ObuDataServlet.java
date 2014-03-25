package si.noemus.boatguard;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import si.bisoft.commons.dbpool.DbManager;
import si.noemus.bilgeguard.InitServlet;
import si.noemus.boatguard.dao.Cache;
import si.noemus.boatguard.dao.Obu;
import si.noemus.boatguard.dao.State;
import si.noemus.boatguard.dao.StateData;
import si.noemus.boatguard.util.Constant;
import si.noemus.boatguard.util.HttpLog;
import si.noemus.boatguard.util.Util;


public class ObuDataServlet implements Servlet {

	Locale locale = Locale.getDefault();
	
	static Logger log = Logger.getLogger(ObuDataServlet.class.getName());
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public ObuDataServlet() {
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
	 *      
	 *      http://localhost:8080/bg/data?gsmnum=&serial=123456&data=0,150031,2D0,00B,1403.452026,4626.050656,20140321093336

	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET POST");		

		HttpLog.afterHttp(request, null);

		String gsmnum = (String) request.getParameter("gsmnum");
		String serial = (String) request.getParameter("serial");
		String data = (String) request.getParameter("data");
		
		setObuData(gsmnum, serial, data);
		
    	OutputStream out = null;
    	response.setContentType("text/plain");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write("1".getBytes());
		out.flush();
		out.close();    	
	
	}	
	
	/*
	0,02F3,01F3,00E,15.1,46.5,20130523181121.000
	0.-STANJE PUMPE (0-NE PUMPA, 2-PUMPA, 3-ZAMA�ENA)
	1.-PRETECENE As
	2.-STANJE NAPETOSTI
	3.-TRENUTNI TOK
	4.-LONGITUDE
	5.-LATITUDE
	6.-UTC TIME
	*/
	private void setObuData(String gsmnum, String serial, String data) {
		Connection con = null;
		Statement stmt = null;
    	
	    try {
	    	Obu obu = getObu(gsmnum, serial);
	    	String[] states = data.split(",");
	    	String dateState = states[6];
	    	Map<Integer, StateData> stateDataLast = getObuLast(obu.getId());
	    	
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
 
	    	String	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    				"values (" + Constant.STATE_ROW_STATE + ", " + obu.getId() + ", '" + data + "', " + dateState + ")";
	    		
	    	log.info(sql.replaceAll("'", ""));
	    	stmt.executeUpdate(sql);
	    	
	    	for (int i=0;i<states.length;i++) {
	    		if (Cache.states.get(i) != null) {
	    			String stateValue = states[i];
	    			State state = Cache.states.get(i);
	    			if (state.getId() == Constant.STATE_ACCU_TOK){
	    				int stateTok = Integer.parseInt(stateValue, 16);
	    				if (stateTok <= Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTING_TOK_MIN).getValue())) {
	    					stateTok = 0;
	    				} else {
	    					stateTok = (3 / Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTINGS_NAPETOST_TOK_MAX).getValue())) * stateTok;
	    				}
	    				stateValue = stateTok+"";	    				
	    			}
	    			if (state.getId() == Constant.STATE_ACCU_NAPETOST){
	    				int stateNapetost = Integer.parseInt(stateValue, 16);
	    				int stateTok = Integer.parseInt(states[3], 16);
	    				//ce je tok<APP_SETTING_TOK_MIN je napetost zadnja od takrat ko je tok>APP_SETTING_TOK_MIN
	    				if ((stateDataLast.get(Constant.STATE_ACCU_NAPETOST)!= null) && (stateTok <= Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTING_TOK_MIN).getValue()))) {
	    					stateValue = Integer.parseInt(stateDataLast.get(Constant.STATE_ACCU_NAPETOST).getValue()) + "";
	    				} else {
	    					stateValue = Math.round((stateNapetost / Double.parseDouble(Cache.appSettings.get(Constant.APP_SETTING_NAPETOST_KOEF1).getValue())) * Double.parseDouble(Cache.appSettings.get(Constant.APP_SETTING_NAPETOST_KOEF2).getValue()))+"";
	    				}
	    			}
	    			if (state.getId() == Constant.STATE_ACCU_AH){
	    				int stateAh = Integer.parseInt(stateValue, 16);
	    				int stateAhLast = Integer.parseInt(Cache.appSettings.get(Constant.NAPETOST_TOK_MAX).getValue());
	    				if (stateDataLast.get(Constant.STATE_ROW_STATE) != null) {
	    					String raw_state_last = stateDataLast.get(Constant.STATE_ROW_STATE).getValue();
	    					stateAhLast = Integer.parseInt(raw_state_last.split(",")[1], 16);
	    					//int stateAhLast = Integer.parseInt(stateDataLast.get(Constant.STATE_ACCU_AH).getValue());
	    				}
	    				int stateNapetost = Integer.parseInt(states[2], 16);
	    				//System.out.println(stateAh+"-"+stateAhLast);
	    				
	    				int napetost_percent = (int) Math.round(stateNapetost * Double.parseDouble(Cache.appSettings.get(Constant.APP_SETTING_NAPETOST_KOEF2).getValue()));
	    				Double energija = (double) ((napetost_percent/100) * Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTING_ENERGIJA).getValue())) - ((0.01/10240)*(stateAh-stateAhLast));
	    				
	    				stateValue = energija + "";
	    			}
	    			sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    	    		"values ('" + state.getId() + "', " + obu.getId() + ", '" + stateValue + "', " + dateState + ")";
		    		
	    	    	stmt.executeUpdate(sql);
		    			
	    		}
	    	}
	    
	    	Map<Integer, String> settings = obu.getSettings();
    		float lat1 = Util.transform(Float.parseFloat(states[4]));
    		float lon1 = Util.transform(Float.parseFloat(states[5]));
    		float lat2 = Util.transform(Float.parseFloat(settings.get(Constant.SETTINGS_LON)));
    		float lon2 = Util.transform(Float.parseFloat(settings.get(Constant.SETTINGS_LAT)));
    		int distance = (int) Math.round(Util.gps2m(lat1, lon1, lat2, lon2));
	    	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
    	    		"values ('" + Constant.STATE_GEO_DIST + "', " + obu.getId() + ", '" + distance + "', " + dateState + ")";
	    		
   	    	stmt.executeUpdate(sql);
	    	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }
		
		return;
	}
	
	
	//todo : cache obu
	private Obu getObu(String gsmnum, String serial) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	Obu obu = new Obu();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * from obus where number = '" + gsmnum + "' or serial_number = '" + serial + "'";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	if (rs.next()) {
	    		obu.setId(rs.getInt("id"));
	    		obu.setNumber(rs.getString("number"));
	    		obu.setPin(rs.getString("pin"));
	    		obu.setPuk(rs.getString("puk"));
	    		obu.setSerial_number(rs.getString("serial_number"));
	    		obu.setActive(rs.getInt("active"));	    		
	    	}
	    	
    		if (obu.getId() > 0) {
		    	sql = "select * from obu_settings where id_obu = " + obu.getId() + "";
	    		
	    		rs = stmt.executeQuery(sql);
	    		Map<Integer, String> settings = obu.getSettings();
	    		settings.clear();
    			
	    		while (rs.next()) {
	    			//System.out.println(rs.getInt("id_setting")+":"+rs.getString("value"));
	    			settings.put(rs.getInt("id_setting"), rs.getString("value"));
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
		
    	return obu;
	}	

	
	private Map<Integer, StateData> getObuLast(int id) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Map<Integer, StateData> statesData = new HashMap<Integer, StateData>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from states_data,  (select max(date_state) as d from states_data where id_obu = 1) as max_date "
	    			+ "where id_obu = " + id + "  and date_state = max_date.d";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	
	    	
	    	while (rs.next()) {
	    		StateData stateData = new StateData();
	    		stateData.setId_state(rs.getInt("id_state"));
	    		stateData.setId_obu(rs.getInt("id_obu"));
	    		stateData.setValue(rs.getString("value"));
	    		stateData.setType(rs.getString("type"));
	    		stateData.setDateState(rs.getTimestamp("date_state"));	
	    		statesData.put(rs.getInt("id_state"), stateData);
	    		
	    		//System.out.println(rs.getInt("id_state")+"+"+rs.getString("value"));
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }
		
    	return statesData;
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
