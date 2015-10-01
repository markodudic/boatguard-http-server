package com.boatguard.engineguard.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.lang3.RandomStringUtils;

import si.bisoft.commons.dbpool.DbManager;

import com.boatguard.boatguard.dao.Error;
import com.boatguard.boatguard.dao.ObuData;
import com.google.gson.Gson;

public class EngineGuardData {

	
	public String getCode(String engineguard, String number, String sessionId) {
		
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Gson gson = new Gson();
		String errorS = null;
		
 	    try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from engineguards "
	    			+ "where UPPER(serial_number) = UPPER('" + engineguard + "') AND (UPPER(gsm_number) = UPPER('" + number + "') OR (gsm_number is null))";
	    		
	    	//System.out.println(sql);
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	if (rs.next()) {
	    		int id_engineguard = rs.getInt("uid");
	    		String code = RandomStringUtils.randomAlphanumeric(5).toUpperCase();
	    		
				String sql1 = "insert into sms_codes (id_engineguard, code, status) " + 
			    		" values (" + id_engineguard + ", '" + code + "', 0)" +
						" on duplicate key update " +
			    		"	code = '" + code + "', " +
			    		"	status = 0";
			
				//System.out.println(sql1);
				stmt.executeUpdate(sql1);
		    
	    		//send code
				ObuData obuData = new ObuData();
				//obuData.sendSMS(number, code);
	    	} else {
	    		Error error = new Error(Error.LOGIN_ERROR, Error.LOGIN_ERROR_CODE, Error.LOGIN_ERROR_MSG);
				errorS = gson.toJson(error);
	    	}
			
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }	
		
		String result = "{\"sessionId\":\""+sessionId+"\",\"error\":"+errorS+"}";
		System.out.println(result);
		return result;
	}


	public String verifyCode(String code, String number, String sessionId) {
		
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Gson gson = new Gson();
		String errorS = null;
		String id_engineguard = null;
		
 	    try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from sms_codes "
	    			+ "where UPPER(code) = UPPER('" + code + "') AND status = 0";
	    		
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	if (rs.next()) {
	    		id_engineguard = rs.getInt("id_engineguard")+"";
	    		
				String sql1 = "update sms_codes " +
			    		"	set status = 1 " +
						"	where id_engineguard = " + id_engineguard;
			
				stmt.executeUpdate(sql1);

				sql1 = "update engineguards " +
			    		"	set gsm_number = '" + number + "'" +
						"	where uid = " + id_engineguard;
			
				System.out.println(sql1);
				stmt.executeUpdate(sql1);
	    	} else {
	    		Error error = new Error(Error.LOGIN_ERROR, Error.LOGIN_ERROR_CODE, Error.LOGIN_ERROR_MSG);
				errorS = gson.toJson(error);
	    	}
			
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }	
		
		String result = "{\"id_engineguard\":\""+id_engineguard+"\",\"error\":"+errorS+"}";
		System.out.println(result);
		return result;

		
	}

}
