package com.boatguard.boatguard.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boatguard.boatguard.dao.ObuData;
import com.boatguard.boatguard.util.HttpLog;


public class SetObuSettingsServlet extends InitServlet {

	static Logger log = Logger.getLogger(SetObuSettingsServlet.class.getName());

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
	 *      http://localhost:8080/bg/data?gsmnum=&serial=123456&data=0,150031,2D0,00B,1403.452026,4626.050656,20140321093336

	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET POST");		
		super.doPost(request, response);

		StringBuilder sb = new StringBuilder();
	    BufferedReader reader = request.getReader();
	    try {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            sb.append(line);
	        }
	    } finally {
	        reader.close();
	    }
	    ObuData obuData = new ObuData();
		obuData.setObuSettings(sb.toString());
		
    	OutputStream out = null;
    	response.setContentType("text/plain");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write("ok".getBytes());
		out.flush();
		out.close();    
	
	}	
	

	
	
}
