package si.noemus.boatguard.sms;

import java.sql.Connection;
import java.sql.Statement;

import si.bisoft.commons.dbpool.DbManager;

public class SmsClient {

	
	public static void sendSMSCustomer(int obuId, String message) {
		Connection con = null;
		Statement stmt = null;

	    try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

	    	String	sql = "insert into smsserver_out (recipient, text, create_date) " + 
    				"select customers.number, '" + message + "', now() "
    				+ "from customers " +
    				"where customers.id_obu = " + obuId;
	    		
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
	
	public static void sendSMSFriends(int obuId, String message) {
		Connection con = null;
		Statement stmt = null;

	    try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

	    	String	sql = "insert into smsserver_out (recipient, text, create_date) " + 
	    				"select friends.number, '" + message + "', now() "
	    				+ "from customers left join friends on (friends.id_customer = customers.id) " +
	    				"where customers.id_obu = " + obuId;
	    		
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
