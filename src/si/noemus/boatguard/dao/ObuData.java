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
import si.noemus.boatguard.comm.MailClient;
import si.noemus.boatguard.comm.SmsClient;
import si.noemus.boatguard.util.Constant;
import si.noemus.boatguard.util.Util;

public class ObuData {
	
	private static Map<Integer, StateData> obuPrevious = new HashMap<Integer, StateData>();
	
	public static Map<Integer, ObuSetting> getSettings(int obuid, String gsmnum, String serial) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Map<Integer, ObuSetting> obuSettings = new HashMap<Integer, ObuSetting>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select obu_settings.* "
	    			+ "from obus left join obu_settings on (obus.id = obu_settings.id_obu) "
	    			+ "where number = '" + gsmnum + "' or serial_number = '" + serial + "' or id = " + obuid + " "
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
		Map<Integer, State> obuStates = getStates(obu.getId());
		obuPrevious = getStateData(obu.getId());
    	
	    try {
	    	String[] states = data.split(",");
	    	String dateState = states[Constant.OBU_DATE_VALUE];
	    	Map<Integer, StateData> stateDataLast = getStateData(obu.getId());
	    	
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
 
	    	String	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    				"values (" + Constant.STATE_ROW_STATE_VALUE + ", " + obu.getId() + ", '" + data + "', " + dateState + ")";
	    		
	    	stmt.executeUpdate(sql);
	    	
	    	for (int i=0;i<states.length;i++) {
	    		if (Cache.states.get(i) != null) {
	    			String stateValue = states[i];
	    			State state = obuStates.get(Cache.states.get(i).getId());
		    		if (state.getId() == Constant.STATE_ACCU_TOK_VALUE){
		    			Double stateTok = Double.parseDouble(Integer.parseInt(stateValue, 16)+"");
		    			if (stateTok <= Constant.APP_SETTINGS_NAPETOST_TOK_MIN_VALUE) {
	    					stateTok = 0.0;
	    				} else {
	    					stateTok = (3.0 / Constant.APP_SETTINGS_NAPETOST_TOK_MAX_VALUE) * stateTok;
	    				}
	    				stateValue = stateTok+"";	    				
	    			}
	    			if (state.getId() == Constant.STATE_ACCU_NAPETOST_VALUE){
	    				int stateNapetost = Integer.parseInt(stateValue, 16);
	    				int stateTok = Integer.parseInt(states[Constant.OBU_ACCU_AH_VALUE], 16);
	    				//ce je tok<APP_SETTING_TOK_MIN je napetost zadnja od takrat ko je tok>APP_SETTING_TOK_MIN
	    				if ((stateDataLast.get(Constant.STATE_ACCU_NAPETOST)!= null) && (stateTok <= Constant.APP_SETTINGS_NAPETOST_TOK_MIN_VALUE)) {
	    					stateValue = Integer.parseInt(stateDataLast.get(Constant.STATE_ACCU_NAPETOST_VALUE).getValue()) + "";
	    				} else {
	    					stateValue = Math.round((stateNapetost / Constant.APP_SETTINGS_NAPETOST_KOEF1_VALUE) * Constant.APP_SETTINGS_NAPETOST_KOEF2_VALUE)+"";
	    				}
	    			}
	    			if (state.getId() == Constant.STATE_ACCU_AH_VALUE){
	    				int stateAh = Integer.parseInt(stateValue, 16);
	    				int stateAhLast = Constant.APP_SETTINGS_NAPETOST_TOK_MAX_VALUE;
	    				if (stateDataLast.get(Constant.STATE_ROW_STATE) != null) {
	    					String raw_state_last = stateDataLast.get(Constant.STATE_ROW_STATE).getValue();
	    					stateAhLast = Integer.parseInt(raw_state_last.split(",")[1], 16);
	    					//int stateAhLast = Integer.parseInt(stateDataLast.get(Constant.STATE_ACCU_AH_VALUE;
	    				}
	    				double stateNapetost = Integer.parseInt(states[Constant.OBU_ACCU_TOK_VALUE], 16) / Constant.APP_SETTINGS_NAPETOST_KOEF1_VALUE;
	    				
	    				int napetost_percent = (int) Math.round(stateNapetost * Constant.APP_SETTINGS_NAPETOST_KOEF2_VALUE);
	    				Double energija = (double) ((napetost_percent/100) * Constant.APP_SETTINGS_ENERGIJA_VALUE) - ((0.01/10240)*(stateAh-stateAhLast));
	    				if (energija<0) {
	    					stateValue = "0";
	    				} else {
	    					stateValue = energija + "";
	    				}
	    			}
	    			sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    	    		"values ('" + state.getId() + "', " + obu.getId() + ", '" + stateValue + "', " + dateState + ")";
	    	    	stmt.executeUpdate(sql);
		    			
	    		}
	    	}
	    
	    	//geo fence
	    	Map<Integer, String> obuSettings = obu.getSettings();
    		float lat1 = Util.transform(Float.parseFloat(states[Constant.OBU_LAT_VALUE]));
    		float lon1 = Util.transform(Float.parseFloat(states[Constant.OBU_LON_VALUE]));
    		float lat2 = Util.transform(Float.parseFloat(obuSettings.get(Constant.OBU_SETTINGS_LAT_VALUE)));
    		float lon2 = Util.transform(Float.parseFloat(obuSettings.get(Constant.OBU_SETTINGS_LON_VALUE)));
    		int distance = (int) Math.round(Util.gps2m(lat1, lon1, lat2, lon2));
	    	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
    	    		"values (" + Cache.appSettings.get(Constant.OBU_SETTINGS_GEO_DISTANCE).getValue() + ", " + obu.getId() + ", '" + distance + "', " + dateState + ")";
	    		
   	    	stmt.executeUpdate(sql);
	    	
	    	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
    	    		"values (" + Cache.appSettings.get(Constant.OBU_SETTINGS_GEO_FENCE).getValue() + ", " + obu.getId() + ", '" + Integer.parseInt(obuSettings.get(Constant.OBU_SETTINGS_GEO_FENCE_VALUE)) + "', " + dateState + ")";
	    		
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

	
	public static void calculateAlarms(int obuid) {
		List<ObuAlarm> obuAlarms = getAlarms(obuid);
		
		Map<Integer, StateData> obuLast = getStateData(obuid);
		Customer customer = getCustomer(obuid);
		
		Iterator it = obuLast.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        StateData stateData = (StateData)pairs.getValue();
	        Integer stateId = (Integer)pairs.getKey();
	        
	        /*Iterator ita = Cache.alarms.entrySet().iterator();
	        while (ita.hasNext()) {
		        Map.Entry pairsa = (Map.Entry)ita.next();
		        Alarm alarm = (Alarm)pairsa.getValue();*/
		    
		    for (int i=0; i< obuAlarms.size(); i++) {    
		        ObuAlarm obuAlarm = obuAlarms.get(i);
		        Alarm alarm = Cache.alarms.get(obuAlarm.getId_alarm());
		        String state = stateData.getValue();
		        		
		        if (stateData.getId_state() == alarm.getId_state()) {
		        	int alarmValue;
		        	if (alarm.getValue().equals("obu_settings")) {
	        			Map<Integer, ObuSetting> obuSettings = ObuData.getSettings(obuid, null, null);
	        			state = ((ObuSetting)obuSettings.get(Constant.OBU_SETTINGS_GEO_FENCE_VALUE)).getValue();
	        			if (Integer.parseInt(state) == Constant.GEO_FENCE_DISABLED_VALUE){
        					continue;
	        			}
	        			alarmValue = Integer.parseInt(((ObuSetting)obuSettings.get(Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE)).getValue());
	        		} else {
	        			alarmValue = Integer.parseInt(alarm.getValue());
	        		}
		        	
		        	if (alarm.getType().equals("N")) {
		        		boolean setAlarm = false;
			        	if (alarm.getOperand().equals("=")){
		            		if (Integer.parseInt(stateData.getValue()) == alarmValue) {
		            			setAlarm = true;
		            		}
			        	} else if (alarm.getOperand().equals("!=")){
		            		if (Integer.parseInt(stateData.getValue()) != alarmValue) {
		            			setAlarm = true;
		            		}
			        	} else if (alarm.getOperand().equals(">")){
		            		if (Integer.parseInt(stateData.getValue()) > alarmValue) {
		            			setAlarm = true;
		            		}
			        	} else if (alarm.getOperand().equals("<")){
			            	if (Integer.parseInt(stateData.getValue()) < alarmValue) {
		            			setAlarm = true;
		            		}
			        	}
			        	//check previous
			        	if (setAlarm && (alarm.getPrevious()!=null)) {
			        		StateData stateDataPrevious = obuPrevious.get(stateId);
			        		if (stateDataPrevious != null) {
				        		if (alarm.getPrevious().equals("=")){
				            		if (Integer.parseInt(stateDataPrevious.getValue()) != alarmValue) {
				            			setAlarm = false;
				            		}
					        	} else if (alarm.getPrevious().equals("!=")){
				            		if (Integer.parseInt(stateDataPrevious.getValue()) == alarmValue) {
				            			setAlarm = false;
				            		}
					        	} else if (alarm.getPrevious().equals(">")){
				            		if (Integer.parseInt(stateDataPrevious.getValue()) < alarmValue) {
				            			setAlarm = false;
				            		}
					        	} else if (alarm.getPrevious().equals("<")){
					            	if (Integer.parseInt(stateDataPrevious.getValue()) > alarmValue) {
				            			setAlarm = false;
				            		}
					        	}
			        		}
			        	}
			        	if (setAlarm) {
				        	if (alarm.getValue().equals("obu_settings") && setAlarm) {
				        		state = Constant.GEO_FENCE_ALARM_VALUE+"";
				        	}
	            			setAlarm(alarm.getId(), obuid, state, alarm.getMessage(), alarm.getMessage_short(), alarm.getTitle(), alarm.getAction(), obuAlarm.getSound(), obuAlarm.getVibrate(), obuAlarm.getSend_email(), obuAlarm.getSend_customer(), obuAlarm.getSend_friends(), stateData.getDateState(), obuAlarm.getActive(), customer.getEmail());
			        	}
		        	}
		        }
			}
		}
	}

	
	public static void setAlarm(int alarmid, int obuid, String stateValue, String message, String messageShort, String title, String action, int sound, int vibrate, int sendEmail, int sendCustomer, int sendFriends, Timestamp date_alarm, int active, String email_to) {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

	    	String	sql = "select * "
	    				+ "from alarm_data "
	    				+ "where id_alarm = " + alarmid + " and id_obu = " + obuid + " and confirmed=0";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	
	    	if (rs.next()) {
	    	} else {
				String msg = getMessage(message, obuid);
				
				if ((sendCustomer == 1) && (active == 1)) {
					SmsClient.sendSMSCustomer(obuid, msg);
				}
				if ((sendFriends == 1) && (active == 1)) {
					SmsClient.sendSMSFriends(obuid, msg);
				}
				if ((sendEmail == 1) && (active == 1)) {
					MailClient.sendMail(email_to, title, msg);
				}	
				
		    	sql = "insert into alarm_data (id_alarm, id_obu, value, message, message_short, title, action, sound, vibrate, send_customer, send_friends, date_alarm, active) " + 
		    			"values (" + alarmid + ", " + obuid + ", '" + stateValue + "', '" + msg + "', '" + messageShort + "', '" + title + "', '" + action + "', " + sound + ", " + vibrate + ", " + sendCustomer + ", " + sendFriends + ", '" + date_alarm + "', " + active + ")";
		    		
		    	stmt.executeUpdate(sql);
	    	}
	    	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }
	}	
	
	public static String getMessage(String message, int obuid) {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
	    try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

	    	String	sql = "select obus.name obus_name, customers.name customers_name, customers.surname customers_surname, customers.number customers_number "
    				+ "from obus left join customers on (obus.id = customers.id_obu)" +
    				" where obus.id = " + obuid;
	    		
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

	public static List<AlarmData> getAlarmData(int obuid) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    List<AlarmData> alarmData = new ArrayList<AlarmData>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from alarm_data "
	    			+ "where id_obu = " + obuid + " and confirmed = 0";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		AlarmData alarm = new AlarmData();
	    		alarm.setId_alarm(rs.getInt("id_alarm"));
	    		alarm.setId_obu(rs.getInt("id_obu"));
	    		alarm.setValue(rs.getString("value"));
	    		alarm.setMessage(rs.getString("message"));
	    		alarm.setMessage_short(rs.getString("message_short"));
	    		alarm.setTitle(rs.getString("title"));
	    		alarm.setAction(rs.getString("action"));
	    		alarm.setSound(rs.getInt("sound"));
	    		alarm.setVibrate(rs.getInt("vibrate"));
	    		alarm.setType(rs.getString("type"));
	    		alarm.setDate_alarm(rs.getTimestamp("date_alarm"));
	    		alarm.setConfirmed(rs.getInt("confirmed"));
	    		alarm.setActive(rs.getInt("active"));
	    		alarmData.add(alarm);
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }	
		
    	return alarmData;
	}	
	
	public static List<ObuAlarm> getAlarms(int obuid) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    List<ObuAlarm> obuAlarms = new ArrayList<ObuAlarm>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from obu_alarms "
	    			+ "where id_obu = " + obuid;
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		ObuAlarm obuAlarm = new ObuAlarm();
	    		obuAlarm.setId_obu(rs.getInt("id_obu"));
	    		obuAlarm.setId_alarm(rs.getInt("id_alarm"));
	    		obuAlarm.setSound(rs.getInt("sound"));
	    		obuAlarm.setVibrate(rs.getInt("vibrate"));
	    		obuAlarm.setSend_email(rs.getInt("send_email"));
	    		obuAlarm.setSend_customer(rs.getInt("send_customer"));
	    		obuAlarm.setSend_friends(rs.getInt("send_friends"));
	    		obuAlarm.setActive(rs.getInt("active"));
	    		obuAlarms.add(obuAlarm);
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }	
		
    	return obuAlarms;
	}		

	public static Map<Integer, State> getStates(int obuid) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Map<Integer, State> states = new HashMap<Integer, State>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select states.* "
	    			+ "from obu_states left join states on (obu_states.id_state = states.id) "
	    			+ "where id_obu = " + obuid + " and obu_states.active = 1";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		State state = new State();
	    		state.setId(rs.getInt("id"));
	    		state.setId_component(rs.getInt("id_component"));
	    		state.setName(rs.getString("name"));
	    		state.setValues(rs.getString("values"));
	    		state.setPosition(rs.getInt("position"));
	    		state.setType(rs.getString("type"));
	    		state.setActive(rs.getInt("active"));
	    		states.put(rs.getInt("id"), state);
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }	
		
    	return states;
	}		

	public static Customer getCustomer(int obuid) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Customer customer = new Customer();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from customers "
	    			+ "where id_obu = " + obuid;
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		customer.setId(rs.getInt("id"));
	    		customer.setId_obu(rs.getInt("id_obu"));
	    		customer.setName(rs.getString("name"));
	    		customer.setSurname(rs.getString("surname"));
	    		customer.setUsername(rs.getString("username"));
	    		customer.setPassword(rs.getString("password"));
	    		customer.setNumber(rs.getString("number"));
	    		customer.setEmail(rs.getString("email"));
	    		customer.setRegister_date(rs.getTimestamp("register_date"));
	    		customer.setLast_visited(rs.getTimestamp("last_visited"));
	    		customer.setApp_version(rs.getString("app_version"));
	    		customer.setPhone_model(rs.getString("phone_model"));
	    		customer.setPhone_platform(rs.getString("phone_platform"));
	    		customer.setHome_network(rs.getString("home_network"));
	    		customer.setActive(rs.getString("active"));
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
			} catch (Exception e) {}
	    }	
		
    	return customer;
	}	
	
	public static void confirmAlarm(int alarmid, int obuid) {
		Connection con = null;
		Statement stmt = null;
		
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
 
			String	sql = "update alarm_data " + 
	    				"set confirmed = 1 " +
	    				"where id_alarm = " + alarmid + " and id_obu = " + obuid;
	    		
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
