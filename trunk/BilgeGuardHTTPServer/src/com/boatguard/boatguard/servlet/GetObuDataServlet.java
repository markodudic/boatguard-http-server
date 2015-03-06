package com.boatguard.boatguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
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

import com.boatguard.boatguard.dao.ObuData;
import com.boatguard.boatguard.objects.AlarmData;
import com.boatguard.boatguard.objects.StateData;
import com.boatguard.boatguard.util.HttpLog;
import com.google.gson.Gson;



public class GetObuDataServlet extends HttpServlet {

	Locale locale = Locale.getDefault();
	
	//static Logger log = Logger.getLogger(ObuSettingsServlet.class.getName());
	private static Log log = LogFactory.getLog(GetObuDataServlet.class);

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
		System.out.println("GetObuDataServlet GET");		
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
		System.out.println("GetObuDataServlet POST:"+request.getSession().getId());		

		HttpLog.afterHttp(request, null);

		String obuid = (String) request.getParameter("obuid");
		String sessionid = (String) request.getParameter("sessionid");
		System.out.println("sessionid="+sessionid);
		
		ObuData obuData = new ObuData();
		LinkedHashMap<Integer, StateData> stateData = obuData.getObuData(Integer.parseInt(obuid));
		Iterator it = stateData.entrySet().iterator();
		List<StateData> stateDataList = new ArrayList<StateData>();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        stateDataList.add((StateData)pairs.getValue());
		}
		Gson gson = new Gson();
		String states = gson.toJson(stateDataList);
		
		List<AlarmData> alarmData = obuData.getAlarmData(Integer.parseInt(obuid));
		String alarms = gson.toJson(alarmData);
		
		String data = "{\"states\":"+states+",\"alarms\":"+alarms+"}";
		
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
