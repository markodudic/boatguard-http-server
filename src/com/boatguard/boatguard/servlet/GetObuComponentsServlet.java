package com.boatguard.boatguard.servlet;

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

import com.boatguard.boatguard.dao.ObuData;
import com.boatguard.boatguard.objects.ObuComponent;
import com.boatguard.boatguard.util.HttpLog;
import com.google.gson.Gson;



public class GetObuComponentsServlet extends InitServlet {

	Locale locale = Locale.getDefault();
	
	//static Logger log = Logger.getLogger(ObuSettingsServlet.class.getName());
	private static Log log = LogFactory.getLog(GetObuComponentsServlet.class);

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

		String gsmnum = (String) request.getParameter("gsmnum");
		String serial = (String) request.getParameter("serial");
		String obuid = (String) request.getParameter("obuid");
		
		ObuData obuData = new ObuData();
		LinkedHashMap<Integer, ObuComponent> obuComponents = obuData.getComponents(obuid, gsmnum, serial);
		List<ObuComponent> obuComponentsList = new ArrayList<ObuComponent>();
		
		Iterator it = obuComponents.entrySet().iterator();
		String settings = "";
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        obuComponentsList.add((ObuComponent)pairs.getValue());
		}
			
		Gson gson = new Gson();
		settings = gson.toJson(obuComponentsList);			

    	
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
