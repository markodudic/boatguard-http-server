package si.noemus.bilgeguard;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Statement;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SendSMSServlet extends InitServlet implements Servlet {

	Locale locale = Locale.getDefault();
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public SendSMSServlet() {
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
    	Statement stmt = null;

	    try {
	    	connectionMake();
			stmt = con.createStatement();   	

	    	String	sql = "insert into smsserver_out (recipient, text, create_date) " + 
	    				"select obu, '#SRV#', now() from users where name = '" + user + "'";
	    		
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
