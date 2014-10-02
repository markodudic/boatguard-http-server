package com.boatguard.boatguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import si.noemus.boatguard.dao.Cache;
import si.noemus.boatguard.objects.Alarm;
import si.noemus.boatguard.objects.AppSetting;
import si.noemus.boatguard.objects.Setting;
import si.noemus.boatguard.objects.State;
import si.noemus.boatguard.util.HttpLog;

import com.google.gson.Gson;



public class GetSettingsServlet extends HttpServlet {

	Locale locale = Locale.getDefault();
	
	//static Logger log = Logger.getLogger(ObuSettingsServlet.class.getName());
	private static Log log = LogFactory.getLog(GetSettingsServlet.class);

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
		Gson gson = new Gson();
		 
		Map<Integer, Alarm> alarms = Cache.alarms;
		Iterator it = alarms.entrySet().iterator();
		List<Alarm> alarmsList = new ArrayList<Alarm>();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        alarmsList.add((Alarm)pairs.getValue());
		}
		String alarmsJson = gson.toJson(alarmsList);
		
		Map<String, AppSetting> appSettings = Cache.appSettings;
		it = appSettings.entrySet().iterator();
		List<AppSetting> appSettingsList = new ArrayList<AppSetting>();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        appSettingsList.add((AppSetting)pairs.getValue());
		}
		String appSettingsJson = gson.toJson(appSettingsList);
		
		Map<String, State> states = Cache.statesByCode;
		it = states.entrySet().iterator();
		List<State> statesList = new ArrayList<State>();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        statesList.add((State)pairs.getValue());
		}
		String statesJson = gson.toJson(statesList);
		
		Map<String, Setting> settings = Cache.settings;
		it = settings.entrySet().iterator();
		List<Setting> settingsList = new ArrayList<Setting>();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        settingsList.add((Setting)pairs.getValue());
		}
		String settingsJson = gson.toJson(settingsList);

		
		String data = "{\"alarms\":"+alarmsJson+",\"app_settings\":"+appSettingsJson+",\"states\":"+statesJson+",\"settings\":"+settingsJson+"}";
		
    	OutputStream out = null;
    	response.setContentType("text/plain");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write(data.getBytes());
		out.flush();
		out.close();    	
	
	}
	
}
