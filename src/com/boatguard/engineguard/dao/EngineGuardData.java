package com.boatguard.engineguard.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

	final String ALARM_URL = "http://server.boatguard.com:8080/boatguard/engineguard.html";
	final String GOOGLE_URL = "https://www.googleapis.com/urlshortener/v1/url?fields=id&key=AIzaSyCqFRqsD4W9SC1urN5k5njvIzUKDmAM46Y";
	
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
				obuData.sendSMS(number, code);
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
		String serial_number = null;
		
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
	    		serial_number = rs.getString("serial_number");
	    		
				String sql1 = "update sms_codes " +
			    		"	set status = 1 " +
						"	where id_engineguard = " + id_engineguard;
			
				stmt.executeUpdate(sql1);

				sql1 = "update engineguards " +
			    		"	set gsm_number = '" + number + "'" +
						"	where uid = " + id_engineguard;
			
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
		
		String result = "{\"id_engineguard\":\""+id_engineguard+"\",\"serial_number\":\""+serial_number+"\",\"gsm_number\":\""+(gsm_number==null?"":gsm_number)+"\",\"email\":\""+(email==null?"":email)+"\",\"refresh_time\":\""+refresh_time+"\",\"error\":"+errorS+"}";
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
    	    
	    	String	sql = "select states_data.*, states.ord "
	    			+ "from states_data left join states on (states_data.id_state = states.id) ,  "
	    			+ "	(select max(date_state) as d from states_data where id_obu = " + id + ") as max_date "
	    			+ "where id_obu = " + id + "  and date_state = max_date.d "
	    			+ "order by ord";
	    		
    		stmt = con.createStatement();   	
    		rs = stmt.executeQuery(sql);
			
	    	while (rs.next()) {
	    		StateData stateData = new StateData();
	    		stateData.setId_state(rs.getInt("id_state"));
	    		stateData.setId_obu(rs.getInt("id_obu"));
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
	
	public String setAlarm(String id_engineguard, String sessionId) {
		
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Gson gson = new Gson();
		String errorS = null;

		
 	    try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from engineguards "
	    			+ "where uid = " + id_engineguard;
	    		
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	if (rs.next()) {
	    		String gsm_number = rs.getString("gsm_number");
	    		String email = rs.getString("email");
	    		String refresh_time = rs.getInt("refresh_time")+"";
	    		String serial_number = rs.getString("serial_number");

	    		String url = ALARM_URL + "?alarm=true&gsm_number="+(gsm_number==null?"":gsm_number)+"&id_engineguard="+id_engineguard+"&email="+(email==null?"":email)+"&refresh_time="+rs.getString("refresh_time")+"&serial_number="+rs.getString("serial_number");
	    		System.out.println(url);
				String shortUrl = shorten(url);
	    		System.out.println(shortUrl);
				ObuData obuData = new ObuData();
				obuData.sendSMS(rs.getString("gsm_number"), "ENGINEGUARD: CHECK ALARM: " + shortUrl);

	    		String	sql1 = "update engineguards " +
			    		"	set alarm = 1" +
						"	where uid = " + id_engineguard;;
		    	
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
		
 	    String result = "{\"error\":"+errorS+"}";
		return result;
	}
	
	public String shorten(String longUrl) {
        if (longUrl == null) {
            return longUrl;
        }
        
        StringBuilder sb = null;
        String line = null;
        String urlStr = longUrl;

        try {
            URL url = new URL(GOOGLE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write("{\"longUrl\": \"" + longUrl + "\"}");
            writer.close();

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line + '\n');
            }

            String json = sb.toString();
            //It extracts easily...
            return json.substring(json.indexOf("http"), json.indexOf("\"", json.indexOf("http")));
        } catch (MalformedURLException e) {
        	System.out.println("1="+e.getMessage());
            return longUrl;
        } catch (IOException e) {
        	System.out.println("2="+e.getMessage());
            return longUrl;
        }
    }


}
