package com.boatguard.boatguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boatguard.boatguard.dao.ObuData;
import com.boatguard.boatguard.objects.Obu;
import com.boatguard.boatguard.objects.ObuSetting;
import com.boatguard.boatguard.util.Constant;


public class SetObuDataServlet extends InitServlet {

	static Logger log = Logger.getLogger(SetObuDataServlet.class.getName());

	public void init(ServletConfig conf) throws ServletException
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
		super.doPost(request, response);

		String gsmnum = (String) request.getParameter("gsmnum");
		String serial = (String) request.getParameter("serial");
		if (serial == null) {
			//zaradi kitine slamparije iščem "SKOK2=Dserial" če obstaja
			serial = (String) request.getParameter("SKOK2");
			serial = serial.replace("Dserial=", "");
		}
		String data = (String) request.getParameter("data");
		
		ObuData obuData = new ObuData();
		boolean isAdd = obuData.setData(gsmnum, serial, data);
		if (isAdd) {
			obuData.calculateAlarms(gsmnum, serial);
		}
		
		List<ObuSetting> obuSettings = obuData.getObuSettingsForObu(null, gsmnum, serial);
		String settings = "";
		
		Obu obu = obuData.getObu(gsmnum, serial);
		if (obu.getFirmware() == Constant.FIRMWARE_1) {
			for (int i=0; i<obuSettings.size(); i++) {
				ObuSetting obuSetting = obuSettings.get(i);
		        settings += obuSetting.getValue();
			}
			//dodam se 000 in 000 za SteviloInputov in SteviloOutputov
			settings += "0000";
		}
		else if (obu.getFirmware() == Constant.FIRMWARE_3) {
			boolean output1 = false;
			boolean output2 = false;

			for (int i=0; i<obuSettings.size(); i++) {
				ObuSetting obuSetting = obuSettings.get(i);
		        if (obuSetting.getId_setting() == Constant.OBU_SETTINGS_OUTPUT1_VALUE) {
		        	output1 = obuSetting.getValue().equals("1"); 
		        }
		        else if (obuSetting.getId_setting() == Constant.OBU_SETTINGS_OUTPUT2_VALUE) {
		        	output2 = obuSetting.getValue().equals("1"); 
		        }
		        else {
		        	settings += obuSetting.getValue();
		        }
			}
			String outputValues = "0";
			if (output1 && output2) outputValues = "3";
			else if (output1) outputValues = "1";
			else if (output2) outputValues = "2";
			
			settings = settings.substring(0, 4) + outputValues + settings.substring(5);
			settings += "0000";
		}
		else {
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
		        	outputValues = "FF0000FF";
		        	fan = obuSetting.getValue().equals("1"); 
		        }
		        else {
		        	settings += obuSetting.getValue();
		        }
			}
			
			if (light && fan) outputValues = "FF0003FC";
			else if (light) outputValues = "FF0002FD";
			else if (fan) outputValues = "FF0001FE";
			
			//dodam se 00 in 00 za SteviloInputov in SteviloOutputov
			settings += output + outputValues;			
		}
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
