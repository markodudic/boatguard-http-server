package si.noemus.bilgeguard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class obu extends InitServlet implements Servlet {

	Locale locale = Locale.getDefault();
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public obu() {
		super();
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
		System.out.println("SERVLET POST="+request.getRemoteAddr()+";"+request.getRemoteHost());		
		
		String ac = request.getParameter("ac");
		String or = request.getParameter("or");
		String bg = request.getParameter("bg");

		if ((or!=null) && (bg!=null)) {
			setState(or, bg);
		}
		
		PrintWriter  out = null;
    	response.setContentType("text/plain;charset=utf-8");
		response.setHeader("cache-control", "no-cache");
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getWriter();
		if ((ac!=null) && (ac.equals("1"))) {
			out.write("#SRV");
		} else {
			out.write("OK");
		}
		out.flush();
		out.close();
	
	}	
	
	private void setState(String or, String bg) {
    	Statement stmt = null;

	    try {
	    	connectionMake();
			stmt = con.createStatement();   	

	    	String	sql = "insert into smsserver_in (originator, message_date, receive_date, text, gateway_id) " +
	    				  " values ('" + or + "', now(), now(), '" + bg + "', 'gprs')";
	    		
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
