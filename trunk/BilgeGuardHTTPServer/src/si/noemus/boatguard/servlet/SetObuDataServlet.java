package si.noemus.boatguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.noemus.boatguard.dao.ObuData;
import si.noemus.boatguard.objects.ObuSetting;
import si.noemus.boatguard.util.HttpLog;


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
		for (int i=0; i<obuSettings.size(); i++) {
			ObuSetting obuSetting = obuSettings.get(i);
	        settings += obuSetting.getValue();
		}
		
		//dodam se 000 in 000 za SteviloInputov in SteviloOutputov
		settings += "000000";
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
