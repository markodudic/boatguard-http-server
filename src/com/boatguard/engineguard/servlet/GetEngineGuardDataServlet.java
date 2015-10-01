package com.boatguard.engineguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boatguard.boatguard.objects.AlarmData;
import com.boatguard.boatguard.objects.StateData;
import com.boatguard.engineguard.dao.EngineGuardData;
import com.google.gson.Gson;



public class GetEngineGuardDataServlet extends HttpServlet {

	Locale locale = Locale.getDefault();
	
	//static Logger log = Logger.getLogger(ObuSettingsServlet.class.getName());
	private static Log log = LogFactory.getLog(GetEngineGuardDataServlet.class);

	public void init(ServletConfig conf) throws ServletException
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
		System.out.println("GetEngineGuardDataServlet GET");		
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
		System.out.println("GetEngineGuardDataServlet POST");		
		
		String egid = (String) request.getParameter("egid");
		String data =  "{"+getObuData(egid)+"}";		
		
    	OutputStream out = null;
    	response.setContentType("text/plain");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write(data.getBytes());
		out.flush();
		out.close();    	
	
	}
	
	private String getObuData (String egid) {
		EngineGuardData egData = new EngineGuardData();
		LinkedHashMap<Integer, StateData> stateData = egData.getEngineGuardData(Integer.parseInt(egid));
		Iterator it = stateData.entrySet().iterator();
		List<StateData> stateDataList = new ArrayList<StateData>();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        stateDataList.add((StateData)pairs.getValue());
		}
		Gson gson = new Gson();
		String states = gson.toJson(stateDataList);
		
		return "\"states\":"+states;		
	}
	
}
