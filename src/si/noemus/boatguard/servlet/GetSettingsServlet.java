package si.noemus.boatguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

import si.noemus.boatguard.dao.Alarm;
import si.noemus.boatguard.dao.AlarmData;
import si.noemus.boatguard.dao.AppSetting;
import si.noemus.boatguard.dao.Cache;
import si.noemus.boatguard.dao.ObuData;
import si.noemus.boatguard.dao.StateData;
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
		
		String data = "{\"alarms\":"+alarmsJson+",\"app_settings\":"+appSettingsJson+"}";
		
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
