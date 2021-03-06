package com.boatguard.engineguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.boatguard.boatguard.util.HttpLog;
import com.boatguard.engineguard.dao.EngineGuardData;



public class GetCodeServlet extends HttpServlet {

	Locale locale = Locale.getDefault();
	
	private static Log log = LogFactory.getLog(GetCodeServlet.class);

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
		System.out.println("GetCodeServlet POST:"+request.getSession().getId());		
		
		request.getSession().setMaxInactiveInterval(-1);
		String sessionId = request.getSession().getId();
		
		HttpLog.afterHttp(request, null);

		String engineguard = (String) request.getParameter("engineguard");
		String number = (String) request.getParameter("number");
				
		EngineGuardData egData = new EngineGuardData();
		String result = egData.getCode(engineguard, number, sessionId);
		
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
