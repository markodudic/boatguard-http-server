package com.boatguard.engineguard.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.RandomStringUtils;

import si.bisoft.commons.dbpool.DbManager;

import com.boatguard.boatguard.dao.Error;
import com.boatguard.boatguard.dao.ObuData;
import com.boatguard.boatguard.objects.StateData;
import com.boatguard.boatguard.util.Util;
import com.google.gson.Gson;

public class EngineGuardData {

	
	public String getCode(String engineguard, String number, String sessionId) {
		
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Gson gson = new Gson();
		String errorS = null;
		String id_engineguard = null;
		
 	    try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from engineguards "
	    			+ "where UPPER(serial_number) = UPPER('" + engineguard + "') AND (UPPER(gsm_number) = UPPER('" + number + "') OR (gsm_number is null))";
	    		
	    	//System.out.println(sql);
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	if (rs.next()) {
	    		id_engineguard = rs.getInt("uid")+"";
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
		
		String result = "{\"id_engineguard\":\""+id_engineguard+"\",\"sessionId\":\""+sessionId+"\",\"error\":"+errorS+"}";
		System.out.println(result);
		return result;
	}


	public String verifyCode(String code, String number, String id_engineguard, String sessionId) {
		
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Gson gson = new Gson();
		String errorS = null;
		String gsm_number = null;
		String email = null;
		String refresh_time = null;
		
 	    try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select sms_codes.*, engineguards.* "
	    			+ "from sms_codes left join engineguards on (engineguards.uid = id_engineguard) "
	    			+ "where UPPER(code) = UPPER('" + code + "') AND id_engineguard = " + id_engineguard + " AND status = 0";
	    		
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	if (rs.next()) {
	    		id_engineguard = rs.getInt("id_engineguard")+"";
	    		gsm_number = rs.getString("gsm_number");
	    		email = rs.getString("email");
	    		refresh_time = rs.getInt("refresh_time")+"";
	    		
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
		
		String result = "{\"id_engineguard\":\""+id_engineguard+"\",\"gsm_number\":\""+(gsm_number==null?"":gsm_number)+"\",\"email\":\""+(email==null?"":email)+"\",\"refresh_time\":\""+refresh_time+"\",\"error\":"+errorS+"}";
		System.out.println(result);
		return result;
	}

	
	public LinkedHashMap<Integer, StateData> getEngineGuardData(int id) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    LinkedHashMap<Integer, StateData> statesData = new LinkedHashMap<Integer, StateData>();
		try {
    		con = DbManager.getConnection("config");
    	    
	    	String	sql = "select eg_states_data.*, states.ord "
	    			+ "from eg_states_data left join states on (eg_states_data.id_state = states.id) ,  "
	    			+ "	(select max(date_state) as d from eg_states_data where id_engineguard = " + id + ") as max_date "
	    			+ "where id_engineguard = " + id + "  and date_state = max_date.d "
	    			+ "order by ord";
	    		
    		stmt = con.createStatement();   	
    		rs = stmt.executeQuery(sql);
			
	    	while (rs.next()) {
	    		StateData stateData = new StateData();
	    		stateData.setId_state(rs.getInt("id_state"));
	    		stateData.setId_obu(rs.getInt("id_engineguard"));
	    		stateData.setValue(rs.getString("value"));	
	    		//workaround za test account
	    		Timestamp tsf = rs.getTimestamp("date_state");
	    		if (id==1) {
	    			java.util.Date date= new java.util.Date();
	    			Timestamp ts = new Timestamp(date.getTime());
	    			tsf = new Timestamp(ts.getTime() - (100 * 1000L));
	    			stateData.setDateState(tsf);
	    		}
	    		else {
	    			stateData.setDateState(rs.getTimestamp("date_state"));	
	    		}
	    		stateData.setDateString(Util.formatDate(tsf.getTime()));
	    		statesData.put(rs.getInt("id_state"), stateData);
	    		//System.out.println(rs.getInt("id_state")+"+"+rs.getString("value"));
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
		
		return statesData;
	}

}
