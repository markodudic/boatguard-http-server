package com.boatguard.boatguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boatguard.boatguard.dao.ObuData;
import com.boatguard.boatguard.objects.ObuSetting;
import com.boatguard.boatguard.util.Constant;
import com.boatguard.boatguard.util.HttpLog;


public class SetObuDataServlet extends HttpServlet {

	static Logger log = Logger.getLogger(SetObuDataServlet.class.getName());

	public void init() throws ServletException
	{
	}
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest arg0,
	 *      HttpServletResponse arg1)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

		String gsmnum = (String) request.getParameter("gsmnum");
		String serial = (String) request.getParameter("serial");
		String data = (String) request.getParameter("data");
		
		ObuData obuData = new ObuData();
		boolean isAdd = obuData.setData(gsmnum, serial, data);
		if (isAdd) {
			obuData.calculateAlarms(gsmnum, serial);
		}
		
		List<ObuSetting> obuSettings = obuData.getObuSettingsForObu(null, gsmnum, serial);
		
		String settings = "";
		String output = "0000";
		String outputValues = "";
		boolean light = false;
		boolean fan = false;
		
		for (int i=0; i<obuSettings.size(); i++) {
			ObuSetting obuSetting = obuSettings.get(i);
	        if (obuSetting.getId_setting() == Constant.OBU_SETTINGS_LIGHT_VALUE) {
	        	output = "0401";
	        	outputValues = "FF0000FF";
	        	light = obuSetting.getValue().equals("1"); 
	        }
	        else if (obuSetting.getId_setting() == Constant.OBU_SETTINGS_FAN_VALUE) {
	        	output = "0401";
	        	outputValues = "ff0000ff";
	        	fan = obuSetting.getValue().equals("1"); 
	        }
	        else {
	        	settings += obuSetting.getValue();
	        }
		}
		
		if (light && fan) outputValues = "ff0003fc";
		else if (light) outputValues = "ff0002fd";
		else if (fan) outputValues = "ff0001fe";
		
		//dodam se 00 in 00 za SteviloInputov in SteviloOutputov
		settings += output + outputValues;
		//dodam se dolzino v HEXA
		String len = Integer.toHexString(settings.length()).toUpperCase();
		
		settings = (len.length()==1?"0"+len:len) + settings;
		
    	OutputStream out = null;
    	response.setContentType("text/plain");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write(settings.getBytes());
		out.flush();
		out.close();    
	
	}	
	

	
	
}
