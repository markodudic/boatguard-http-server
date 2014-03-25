package si.noemus.boatguard.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import si.bisoft.commons.dbpool.DbManager;
import si.noemus.boatguard.sms.SmsClient;
import si.noemus.boatguard.util.Constant;
import si.noemus.boatguard.util.Util;

public class ObuData {

	public static Map<Integer, ObuSetting> getSettings(int obuId, String gsmnum, String serial) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Map<Integer, ObuSetting> obuSettings = new HashMap<Integer, ObuSetting>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select obu_settings.* "
	    			+ "from obus left join obu_settings on (obus.id = obu_settings.id_obu) "
	    			+ "where number = '" + gsmnum + "' or serial_number = '" + serial + "' or id = " + obuId + " "
	    			+ "order by id_setting";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		ObuSetting obuSetting = new ObuSetting();
	    		obuSetting.setId_setting(rs.getInt("id_setting"));
	    		obuSetting.setValue(rs.getString("value"));
	    		obuSetting.setType(rs.getString("type"));
	    		obuSettings.put(rs.getInt("id_setting"), obuSetting);
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }	
		
    	return obuSettings;
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
	public static int setData(String gsmnum, String serial, String data) {
		Connection con = null;
		Statement stmt = null;
		Obu obu = getObu(gsmnum, serial);
    	
	    try {
	    	String[] states = data.split(",");
	    	String dateState = states[6];
	    	Map<Integer, StateData> stateDataLast = getStateData(obu.getId());
	    	
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
 
	    	String	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    				"values (" + Constant.STATE_ROW_STATE + ", " + obu.getId() + ", '" + data + "', " + dateState + ")";
	    		
	    	stmt.executeUpdate(sql);
	    	
	    	for (int i=0;i<states.length;i++) {
	    		if (Cache.states.get(i) != null) {
	    			String stateValue = states[i];
	    			State state = Cache.states.get(i);
		    		if (state.getId() == Constant.STATE_ACCU_TOK){
	    				int stateTok = Integer.parseInt(stateValue, 16);
	    				if (stateTok <= Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTING_TOK_MIN).getValue())) {
	    					stateTok = 0;
	    				} else {
	    					stateTok = (3 / Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTINGS_NAPETOST_TOK_MAX).getValue())) * stateTok;
	    				}
	    				stateValue = stateTok+"";	    				
	    			}
	    			if (state.getId() == Constant.STATE_ACCU_NAPETOST){
	    				int stateNapetost = Integer.parseInt(stateValue, 16);
	    				int stateTok = Integer.parseInt(states[3], 16);
	    				//ce je tok<APP_SETTING_TOK_MIN je napetost zadnja od takrat ko je tok>APP_SETTING_TOK_MIN
	    				if ((stateDataLast.get(Constant.STATE_ACCU_NAPETOST)!= null) && (stateTok <= Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTING_TOK_MIN).getValue()))) {
	    					stateValue = Integer.parseInt(stateDataLast.get(Constant.STATE_ACCU_NAPETOST).getValue()) + "";
	    				} else {
	    					stateValue = Math.round((stateNapetost / Double.parseDouble(Cache.appSettings.get(Constant.APP_SETTING_NAPETOST_KOEF1).getValue())) * Double.parseDouble(Cache.appSettings.get(Constant.APP_SETTING_NAPETOST_KOEF2).getValue()))+"";
	    				}
	    			}
	    			if (state.getId() == Constant.STATE_ACCU_AH){
	    				int stateAh = Integer.parseInt(stateValue, 16);
	    				int stateAhLast = Integer.parseInt(Cache.appSettings.get(Constant.NAPETOST_TOK_MAX).getValue());
	    				if (stateDataLast.get(Constant.STATE_ROW_STATE) != null) {
	    					String raw_state_last = stateDataLast.get(Constant.STATE_ROW_STATE).getValue();
	    					stateAhLast = Integer.parseInt(raw_state_last.split(",")[1], 16);
	    					//int stateAhLast = Integer.parseInt(stateDataLast.get(Constant.STATE_ACCU_AH).getValue());
	    				}
	    				int stateNapetost = Integer.parseInt(states[2], 16);
	    				//System.out.println(stateAh+"-"+stateAhLast);
	    				
	    				int napetost_percent = (int) Math.round(stateNapetost * Double.parseDouble(Cache.appSettings.get(Constant.APP_SETTING_NAPETOST_KOEF2).getValue()));
	    				Double energija = (double) ((napetost_percent/100) * Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTING_ENERGIJA).getValue())) - ((0.01/10240)*(stateAh-stateAhLast));
	    				
	    				stateValue = energija + "";
	    			}
	    			sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    	    		"values ('" + state.getId() + "', " + obu.getId() + ", '" + stateValue + "', " + dateState + ")";
		    		
	    	    	stmt.executeUpdate(sql);
		    			
	    		}
	    	}
	    
	    	Map<Integer, String> settings = obu.getSettings();
    		float lat1 = Util.transform(Float.parseFloat(states[4]));
    		float lon1 = Util.transform(Float.parseFloat(states[5]));
    		float lat2 = Util.transform(Float.parseFloat(settings.get(Constant.SETTINGS_LON)));
    		float lon2 = Util.transform(Float.parseFloat(settings.get(Constant.SETTINGS_LAT)));
    		int distance = (int) Math.round(Util.gps2m(lat1, lon1, lat2, lon2));
	    	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
    	    		"values ('" + Constant.STATE_GEO_DIST + "', " + obu.getId() + ", '" + distance + "', " + dateState + ")";
	    		
   	    	stmt.executeUpdate(sql);
	    	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }
		
		return obu.getId();
	}
	
	
	//todo : cache obu
	public static Obu getObu(String gsmnum, String serial) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	Obu obu = new Obu();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * from obus where number = '" + gsmnum + "' or serial_number = '" + serial + "'";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	if (rs.next()) {
	    		obu.setId(rs.getInt("id"));
	    		obu.setName(rs.getString("name"));
	    		obu.setNumber(rs.getString("number"));
	    		obu.setPin(rs.getString("pin"));
	    		obu.setPuk(rs.getString("puk"));
	    		obu.setSerial_number(rs.getString("serial_number"));
	    		obu.setActive(rs.getInt("active"));	    		
	    	}
	    	
    		if (obu.getId() > 0) {
		    	sql = "select * from obu_settings where id_obu = " + obu.getId() + "";
	    		
	    		rs = stmt.executeQuery(sql);
	    		Map<Integer, String> settings = obu.getSettings();
	    		settings.clear();
    			
	    		while (rs.next()) {
	    			//System.out.println(rs.getInt("id_setting")+":"+rs.getString("value"));
	    			settings.put(rs.getInt("id_setting"), rs.getString("value"));
	    		}
    		}
	    	
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }	
		
    	return obu;
	}	

	
	public static Map<Integer, StateData> getStateData(int id) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Map<Integer, StateData> statesData = new HashMap<Integer, StateData>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from states_data,  (select max(date_state) as d from states_data where id_obu = 1) as max_date "
	    			+ "where id_obu = " + id + "  and date_state = max_date.d";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	
	    	
	    	while (rs.next()) {
	    		StateData stateData = new StateData();
	    		stateData.setId_state(rs.getInt("id_state"));
	    		stateData.setId_obu(rs.getInt("id_obu"));
	    		stateData.setValue(rs.getString("value"));
	    		stateData.setType(rs.getString("type"));
	    		stateData.setDateState(rs.getTimestamp("date_state"));	
	    		statesData.put(rs.getInt("id_state"), stateData);
	    		
	    		//System.out.println(rs.getInt("id_state")+"+"+rs.getString("value"));
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }
		
    	return statesData;
	}

	
	public static void calculateAlarms(int obuId) {
		Map<Integer, Alarm> alarms = Cache.alarms;
		
		Map<Integer, StateData> obuLast = getStateData(obuId);
		Iterator it = obuLast.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        StateData stateData = (StateData)pairs.getValue();
	        
	        Iterator ita = Cache.alarms.entrySet().iterator();
	        while (ita.hasNext()) {
		        Map.Entry pairsa = (Map.Entry)ita.next();
		        Alarm alarm = (Alarm)pairsa.getValue();
		        
		        if (stateData.getId_state() == alarm.getId_state()) {
		        	int alarmValue;
	        		if (alarm.getValue().equals("obu_settings")) {
	        			Map<Integer, ObuSetting> obuSettings = ObuData.getSettings(obuId, null, null);
	        			if (Integer.parseInt(((ObuSetting)obuSettings.get(Constant.SETTINGS_GEO_FENCE)).getValue()) == 0){
	        					continue;
	        			}
	        			alarmValue = Integer.parseInt(((ObuSetting)obuSettings.get(stateData.getId_state())).getValue());
	        		} else {
	        			alarmValue = Integer.parseInt(alarm.getValue());
	        		}
		        	
		        	if (alarm.getType().equals("N")) {
			        	if (alarm.getOperand().equals("=")){
		            		if (Integer.parseInt(stateData.getValue()) == alarmValue) {
		            			setAlarm(alarm.getId(), obuId, stateData.getValue(), alarm.getMessage(), stateData.getDateState());
		            		}
			        	} else if (alarm.getOperand().equals(">")){
		            		if (Integer.parseInt(stateData.getValue()) > alarmValue) {
		            			setAlarm(alarm.getId(), obuId, stateData.getValue(), alarm.getMessage(), stateData.getDateState());
		            		}
			        	} else if (alarm.getOperand().equals("<")){
			            	if (Integer.parseInt(stateData.getValue()) < alarmValue) {
		            			setAlarm(alarm.getId(), obuId, stateData.getValue(), alarm.getMessage(), stateData.getDateState());
		            		}
			        	}
		        	}
		        }
			}
		}
	}

	
	public static void setAlarm(int alarmId, int obuId, String stateValue, String message, Timestamp date_alarm) {
		Connection con = null;
		Statement stmt = null;
		
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
 
			String msg = getMessage(message, obuId);
			
			SmsClient.sendSMSCustomer(obuId, msg);
	    	SmsClient.sendSMSFriends(obuId, msg);

	    	String	sql = "insert into alarm_data (id_alarm, id_obu, value, message, date_alarm, send_customer, send_friends) " + 
	    				"select " + alarmId + ", " + obuId + ", '" + stateValue + "', '" + msg + "', '" + date_alarm + "', 1, 1 from dual " +
	    				"where not exists (select * from alarm_data where id_alarm = " + alarmId + " and id_obu = " + obuId + " and confirmed=0) limit 1";
	    		
	    	stmt.executeUpdate(sql);
	    	
	    	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }
	}	
	
	public static String getMessage(String message, int obuId) {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
	    try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

	    	String	sql = "select obus.name obus_name, customers.name customers_name, customers.surname customers_surname, customers.number customers_number "
    				+ "from obus left join customers on (obus.id = customers.id_obu)" +
    				" where obus.id = " + obuId;
	    		
    		System.out.println("sql="+sql);
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	
	    	
	    	if (rs.next()) {
	    		String[] messageStr = message.split("%");
	    		if (messageStr.length > 1) {
	    			for (int i=1; i<messageStr.length; i++) {
	    				String par = messageStr[i];
	    				message = message.replaceAll("%"+par+"%", rs.getString(par));
	    				i++;
	    			}
	    		}
	    		
	    	}
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }
	    
		return message;
	}
	
	public static List<AlarmData> getAlarms(int obuId) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    List<AlarmData> alarms = new ArrayList<AlarmData>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from alarm_data "
	    			+ "where id_obu = " + obuId + " and confirmed = 0";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		AlarmData alarm = new AlarmData();
	    		alarm.setId_alarm(rs.getInt("id_alarm"));
	    		alarm.setId_obu(rs.getInt("id_obu"));
	    		alarm.setValue(rs.getString("value"));
	    		alarm.setMessage(rs.getString("message"));
	    		alarm.setType(rs.getString("type"));
	    		alarm.setDate_alarm(rs.getTimestamp("date_alarm"));
	    		alarm.setConfirmed(rs.getInt("confirmed"));
	    		alarms.add(alarm);
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }	
		
    	return alarms;
	}		
	
	public static void confirmAlarm(int alarmId, int obuId) {
		Connection con = null;
		Statement stmt = null;
		
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
 
			String	sql = "update alarm_data " + 
	    				"set confirmed = 1 " +
	    				"where id_alarm = " + alarmId + " and id_obu = " + obuId;
	    		
	    	stmt.executeUpdate(sql);
	    	
	    	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }
	}		
}
