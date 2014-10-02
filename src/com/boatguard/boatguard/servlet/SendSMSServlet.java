package com.boatguard.boatguard.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import si.bisoft.commons.dbpool.DbManager;


public class SendSMSServlet extends HttpServlet {

	Locale locale = Locale.getDefault();
	
	//static Logger log = Logger.getLogger(ObuSettingsServlet.class.getName());
	private static Log log = LogFactory.getLog(GetObuComponentsServlet.class);

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
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET POST");		

		String user = (String) request.getParameter("user");
		String message = (String) request.getParameter("message");

    	sendSMS(user, message);
    	
    	OutputStream out = null;
    	response.setContentType("text/html");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write("ok.".getBytes());
		out.flush();
		out.close();    	
	
	}	
	

	private void sendSMS(String user, String message) {
		Connection con = null;
		Statement stmt = null;

	    try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
			
			//posljem nov sms samo ce ni ze
	    	String	sql = "insert into smsserver_out (recipient, text, create_date) " + 
	    				"select obu, '#SRV#', now() "
	    				+ "from users "
	    				+ "where name = '" + user + "' and "
	    				+ "		not exists (select * from smsserver_out where recipient = users.obu and status != 'S')";
	    		
    		System.out.println("sql="+sql);
	    	stmt.executeUpdate(sql);
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) {
	    			stmt.close();
	    		}
			} catch (Exception e) {
			}
	    }	
		
		return;
	}


}
