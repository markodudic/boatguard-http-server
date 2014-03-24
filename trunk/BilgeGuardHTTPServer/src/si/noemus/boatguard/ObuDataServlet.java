package si.noemus.boatguard;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import si.noemus.bilgeguard.InitServlet;
import si.noemus.boatguard.objects.Obu;
import si.noemus.boatguard.objects.State;
import si.noemus.boatguard.util.Constant;


public class ObuDataServlet extends InitServlet implements Servlet {

	Locale locale = Locale.getDefault();
	
	static Logger log = Logger.getLogger(ObuDataServlet.class.getName());
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public ObuDataServlet() {
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
	 *      
	 *      http://localhost:8080/bg/data?gsmnum=&serial=123456&data=0,150031,2D0,00B,1403.452026,4626.050656,20140321093336

	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET POST");		

		String gsmnum = (String) request.getParameter("gsmnum");
		String serial = (String) request.getParameter("serial");
		String data = (String) request.getParameter("data");

    	setObuData(gsmnum, serial, data);
    	
    	OutputStream out = null;
    	response.setContentType("text/plain");
		response.setHeader("Content-disposition", null);
		response.setHeader("Access-Control-Allow-Origin", "*");
		out = response.getOutputStream();
		out.write("1".getBytes());
		out.flush();
		out.close();    	
	
	}	
	
	/*
	0,02F3,01F3,00E,15.1,46.5,20130523181121.000
	0.-STANJE PUMPE (0-NE PUMPA, 2-PUMPA, 3-ZAMA�ENA)
	1.-PRETECENE As
	2.-STANJE NAPETOSTI
	3.-TRENUTNI TOK
	4.-LONGITUDE
	5.-LATITUDE
	6.-UTC TIME
	*/
	private void setObuData(String gsmnum, String serial, String data) {
    	Statement stmt = null;
    	
	    try {
	    	Obu obu = getObuId(gsmnum, serial);
	    	String[] states = data.split(",");
	    	String dateState = states[6];
	    	
	    	connectionMake();
			stmt = con.createStatement();   	

	    	String	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    				"values (" + Constant.STATE_ROW_STATE + ", " + obu.getId() + ", '" + data + "', " + dateState + ")";
	    		
	    	log.info(sql.replaceAll("'", ""));
	    	stmt.executeUpdate(sql);

	    	for (int i=0;i<states.length;i++) {
	    		if (statesByPosition.get(i) != null) {
	    			String stateValue = states[i];
	    			State state = statesByPosition.get(i);
	    	    	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    	    		"values ('" + state.getId() + "', " + obu.getId() + ", '" + stateValue + "', " + dateState + ")";
		    		
	    	    	stmt.executeUpdate(sql);
		    			
	    		}
	    	}
	    
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
	
	
	private Obu getObuId(String gsmnum, String serial) {
    	ResultSet rs = null;
	    Statement stmt = null;
    	Obu obu = new Obu();
    	try {
	    	connectionMake();

	    	String	sql = "select * from obus where number = '" + gsmnum + "' or serial_number = '" + serial + "'";
	    		
    		System.out.println("sql="+sql);
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	if (rs.next()) {
	    		obu.setId(rs.getInt("id"));
	    		obu.setNumber(rs.getString("number"));
	    		obu.setPin(rs.getString("pin"));
	    		obu.setPuk(rs.getString("puk"));
	    		obu.setSerial_number(rs.getString("serial_number"));
	    		obu.setActive(rs.getInt("active"));
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) {
	    			rs.close();
	    		}

	    		if (stmt != null) {
	    			stmt.close();
	    		}
			} catch (Exception e) {
			}
	    }	
		
    	return obu;
	}	

}
