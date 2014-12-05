package com.boatguard.boatguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import si.bisoft.commons.dbpool.DbManager;

import com.boatguard.boatguard.dao.Cache;
import com.boatguard.boatguard.dao.ObuData;
import com.boatguard.boatguard.objects.AlarmData;
import com.boatguard.boatguard.objects.AppSetting;
import com.boatguard.boatguard.objects.Component;
import com.boatguard.boatguard.objects.StateData;
import com.boatguard.boatguard.util.Constant;
import com.boatguard.boatguard.util.HttpLog;
import com.google.gson.Gson;



public class GetObuDataSMSServlet extends HttpServlet {

	Locale locale = Locale.getDefault();
	
	//static Logger log = Logger.getLogger(ObuSettingsServlet.class.getName());
	private static Log log = LogFactory.getLog(GetObuDataSMSServlet.class);

	public void init() throws ServletException
	{
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
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET POST");		

		HttpLog.afterHttp(request, null);

		String originator = (String) request.getParameter("originator");
		String text = (String) request.getParameter("text");
		String message = "BOATGUARD:: ";
		
		ObuData obuData = new ObuData();
		
		int obuid = obuData.getObuFromUser(originator, text);
		if (obuid > 0) {
			LinkedHashMap<Integer, StateData> stateData = obuData.getObuData(obuid);
			Iterator it = stateData.entrySet().iterator();
			//List<StateData> stateDataList = new ArrayList<StateData>();
			//LinkedHashMap<Integer, Component> components = Cache.components;
			LinkedHashMap<String, AppSetting> appSettings =  Cache.appSettings;
			boolean disc = false;
			
			while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        StateData sd = (StateData)pairs.getValue();
		        if (sd.getId_state() == Constant.STATE_ACCU_DISCONNECTED_VALUE) {
		        	if (sd.getValue().equals(Constant.ACCU_DISCONNECT_VALUE+"")) {
		        		message += "BATTERY: " + appSettings.get(Constant.STATE_ACCU_DISCONNECTED).getDesc() + "; ";
		        		disc = true;
		        	}
		        }
		        else if (!disc && (sd.getId_state() == Constant.STATE_ACCU_NAPETOST_VALUE)) {
		        	message += "BATTERY: " + sd.getValue() + "; ";
		        } 
		        else if (sd.getId_state() == Constant.STATE_PUMP_STATE_VALUE) {
		        	if (sd.getValue().equals(Constant.PUMP_OK_VALUE+"")) {
		        		message += "PUMP: " + appSettings.get(Constant.PUMP_OK).getDesc() + "; ";
		        	}
		        	else if (sd.getValue().equals(Constant.PUMP_PUMPING_VALUE+"")) {
		        		message += "PUMP: " + appSettings.get(Constant.PUMP_PUMPING).getDesc() + "; ";
		        	}
		        	else if (sd.getValue().equals(Constant.PUMP_CLODGED_VALUE+"")) {
		        		message += "PUMP: " + appSettings.get(Constant.PUMP_CLODGED).getDesc() + "; ";
		        	}
		        	else if (sd.getValue().equals(Constant.PUMP_DEMAGED_VALUE+"")) {
		        		message += "PUMP: " + appSettings.get(Constant.PUMP_DEMAGED).getDesc() + "; ";
		        	}
		        } 
		        else if (sd.getId_state() == Constant.STATE_GEO_FENCE_VALUE) {
		        	if (sd.getValue().equals(Constant.GEO_FENCE_DISABLED_VALUE+"")) {
		        		message += "GEO FENCE: " + appSettings.get(Constant.GEO_FENCE_DISABLED).getDesc() + "; ";
		        	}
		        	else if (sd.getValue().equals(Constant.GEO_FENCE_ENABLED_VALUE+"")) {
		        		message += "GEO FENCE: " + appSettings.get(Constant.GEO_FENCE_ENABLED).getDesc() + "; ";
		        	}
		        	else if (sd.getValue().equals(Constant.GEO_FENCE_ALARM_VALUE+"")) {
		        		message += "GEO FENCE: " + appSettings.get(Constant.GEO_FENCE_ALARM).getDesc() + "; ";
		        	}
		        } 
			}
			
		}
		else {
			message += "WRONG MESSAGE";
		}

		sendSMS(originator, message);
		
    	OutputStream out = null;
    	response.setContentType("text/plain");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write(message.getBytes());
		out.flush();
		out.close();    	
	
	}
	
	private void sendSMS(String originator, String message) {
		Connection con = null;
		Statement stmt = null;

	    try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
			
			//posljem nov sms samo ce ni ze
	    	String	sql = "insert into smsserver_out (recipient, text, create_date) " + 
	    				"VALUES (" + originator + ",'" + message + "', now())";
	    		
    		//System.out.println("sql="+sql);
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
