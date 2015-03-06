package com.boatguard.boatguard.servlet;

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
import com.boatguard.boatguard.objects.AlarmData;
import com.boatguard.boatguard.util.HttpLog;

import com.google.gson.Gson;



public class LoginRegisterServlet extends HttpServlet {

	Locale locale = Locale.getDefault();
	
	//static Logger log = Logger.getLogger(ObuSettingsServlet.class.getName());
	private static Log log = LogFactory.getLog(LoginRegisterServlet.class);

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
		System.out.println("LoginRegisterServlet POST:"+request.getSession().getId());		
		
		request.getSession().setMaxInactiveInterval(-1);
		String sessionId = request.getSession().getId();
		
		HttpLog.afterHttp(request, null);

		String type = (String) request.getParameter("type");
		String username = (String) request.getParameter("username");
		String password = (String) request.getParameter("password");
		String obuSn = (String) request.getParameter("obu_sn");
		/*String deviceName = (String) request.getParameter("device_name");
		String devicePlatform = (String) request.getParameter("device_platform");
		String deviceVersion = (String) request.getParameter("device_version");
		String deviceUuid = (String) request.getParameter("device_uuid");
		String phoneNumber = (String) request.getParameter("phone_number");
		String appVersion = (String) request.getParameter("app_version");*/

				
				
		ObuData obuData = new ObuData();
		String result = "";
		if (type.equalsIgnoreCase("login")) {
			result = obuData.login(username, password, obuSn, sessionId);
		} else {
			result = obuData.register(username, password, obuSn, sessionId);
		}

		OutputStream out = null;
    	response.setContentType("text/plain");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write(result.getBytes());
		out.flush();
		out.close();    	
	
	}
	
}
