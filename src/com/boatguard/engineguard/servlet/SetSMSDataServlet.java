package com.boatguard.engineguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boatguard.boatguard.dao.ObuData;
import com.boatguard.boatguard.objects.Obu;
import com.boatguard.boatguard.objects.ObuSetting;
import com.boatguard.boatguard.util.Constant;
import com.boatguard.boatguard.util.HttpLog;
import com.boatguard.engineguard.dao.EngineGuardData;



public class SetSMSDataServlet extends HttpServlet {

	Locale locale = Locale.getDefault();
	
	private static Log log = LogFactory.getLog(SetSMSDataServlet.class);

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
		System.out.println("SetSMSDataServlet POST:"+request.getSession().getId());		
		
		request.getSession().setMaxInactiveInterval(-1);
		String sessionId = request.getSession().getId();
		
		HttpLog.afterHttp(request, null);

		String from = (String) request.getParameter("From");
		String body = (String) request.getParameter("Body");
				
		System.out.println("FROM="+from);
		System.out.println("BODY="+body);
		
		//String gsmnum = (String) request.getParameter("gsmnum");
		String serial = body.substring(body.indexOf("serial=")+7, body.indexOf("&"));
		String data = body.substring(body.indexOf("data=")+5);
		
		System.out.println("serial="+serial);
		System.out.println("data="+data);
		
		ObuData obuData = new ObuData();
		boolean isAdd = obuData.setData(null, serial, data);
		
		List<ObuSetting> obuSettings = obuData.getObuSettingsForObu(null, null, serial);
		String settings = "";
		
		for (int i=0; i<obuSettings.size(); i++) {
			ObuSetting obuSetting = obuSettings.get(i);
	        settings += obuSetting.getValue();
		}
		//dodam se 000 in 000 za SteviloInputov in SteviloOutputov
		settings += "0000";
		
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
