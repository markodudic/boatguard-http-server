package si.noemus.boatguard.dao;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import si.bisoft.commons.dbpool.DbManager;
import si.noemus.boatguard.comm.MailClient;
import si.noemus.boatguard.comm.SmsClient;
import si.noemus.boatguard.objects.Alarm;
import si.noemus.boatguard.objects.AlarmData;
import si.noemus.boatguard.objects.Customer;
import si.noemus.boatguard.objects.Obu;
import si.noemus.boatguard.objects.ObuAlarm;
import si.noemus.boatguard.objects.ObuComponent;
import si.noemus.boatguard.objects.ObuSetting;
import si.noemus.boatguard.objects.State;
import si.noemus.boatguard.objects.StateData;
import si.noemus.boatguard.util.Constant;
import si.noemus.boatguard.util.Util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ObuData {
	
	private static Map<Integer, StateData> lastStateData = new HashMap<Integer, StateData>();
	
	public ObuData(){

	}
	
	public Map<Integer, ObuSetting> getObuSettings(String obuid, String gsmnum, String serial) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Map<Integer, ObuSetting> obuSettings = new HashMap<Integer, ObuSetting>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select obu_settings.*, settings.code "
	    			+ "from obus left join "
	    			+ "		obu_settings on (obus.uid = obu_settings.id_obu) left join "
	    			+ "		settings on (obu_settings.id_setting = settings.id) "
	    			+ "where number = '" + gsmnum + "' or serial_number = '" + serial + "' or obus.uid = " + obuid + " "
	    			+ "order by id_setting";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		ObuSetting obuSetting = new ObuSetting();
	    		obuSetting.setId_setting(rs.getInt("id_setting"));
	    		obuSetting.setValue(rs.getString("value"));
	    		obuSetting.setType(rs.getString("type"));
	    		obuSetting.setCode(rs.getString("code"));
	    		obuSettings.put(rs.getInt("id_setting"), obuSetting);
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
		
    	return obuSettings;
	}
	
	public List<ObuSetting> getObuSettingsForObu(String obuid, String gsmnum, String serial) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    List<ObuSetting> obuSettings = new ArrayList<ObuSetting>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select obu_settings.*, settings.code, app_settings.value "
	    				+ "from obus left join "
	    				+ "	obu_settings on (obus.uid = obu_settings.id_obu) left join "
	    				+ "	settings on (obu_settings.id_setting = settings.id)  left join "
	    				+ "	app_settings on (settings.code = app_settings.name) "
						+ "where (number = '" + gsmnum + "' or serial_number = '" + serial + "' or obus.uid = " + obuid + ") and app_settings.value is not null "
						+ "order by app_settings.value";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		ObuSetting obuSetting = new ObuSetting();
	    		obuSetting.setId_setting(rs.getInt("id_setting"));
	    		obuSetting.setValue(rs.getString("value"));
	    		obuSetting.setType(rs.getString("type"));
	    		obuSetting.setCode(rs.getString("code"));
	    		obuSettings.add(obuSetting);
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
		
    	return obuSettings;
	}	

	public void setObuSettings(String obuid, String data) {
		Gson gson = new Gson();
		System.out.println("DATA="+data);
		Type listType = new TypeToken<List<ObuSetting>>(){}.getType();
		List obuSettings = gson.fromJson(data, listType);
		
		Connection con = null;
		Statement stmt = null;
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

			for (int i = 0; i < obuSettings.size(); i++) {
				ObuSetting obuSetting = (ObuSetting) obuSettings.get(i);
				
		    	String sql = "update obu_settings " + 
			    		" set value = '" + obuSetting.getValue() + "'" +
			    		" where id_obu = " + obuid + " and id_setting = " + obuSetting.getId_setting();
					
				stmt.executeUpdate(sql);
					    	
			}
			
			//set current location
			String sqls = "update obu_settings " +
						"set value = (select value " +
									"from states_data " +
									"where id_obu = " + obuid +
									"		and id_state = " + Constant.OBU_SETTINGS_LAT_VALUE +
									" order by date_state desc " +
									"limit 1) " +
						"where id_setting = " + Constant.OBU_SETTINGS_LAT_VALUE;
			
			stmt.executeUpdate(sqls);

			sqls = "update obu_settings " +
					"set value = (select value " +
								"from states_data " +
								"where id_obu = " + obuid +
								"		and id_state = " + Constant.OBU_SETTINGS_LON_VALUE +
								" order by date_state desc " +
								"limit 1) " +
					"where id_setting = " + Constant.OBU_SETTINGS_LON_VALUE;
		
			stmt.executeUpdate(sqls);
			
			//updetam lokacijo
		} catch (Exception theException) {
			theException.printStackTrace();
		} finally {
			try {
				if (stmt != null) stmt.close();
				if (con != null) con.close();
			} catch (Exception e) {}
		}		
        
		
	}
	
	public Map<Integer, ObuComponent> getComponents(String obuid, String gsmnum, String serial) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Map<Integer, ObuComponent> obuComponents = new HashMap<Integer, ObuComponent>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select obu_components.id_component, components.name, components.type, components.show "
	    			+ "from obus left join "
	    			+ "		obu_components on (obus.uid = obu_components.id_obu) left join "
	    			+ "		components on (obu_components.id_component = components.id) "
	    			+ "where number = '" + gsmnum + "' or serial_number = '" + serial + "' or obus.uid = " + obuid + " "
	    			+ "order by obu_components.id_component";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		ObuComponent obuComponent = new ObuComponent();
	    		obuComponent.setId_component(rs.getInt("id_component"));
	    		obuComponent.setName(rs.getString("name"));
	    		obuComponent.setType(rs.getString("type"));
	    		obuComponent.setShow(rs.getInt("show"));
	    		obuComponents.put(rs.getInt("id_component"), obuComponent);
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
		
    	return obuComponents;
	}	
	 
	/*
	1234.1222,12345.1111,1,0,0,1,0,0,2F50,1A1B1C,2F50,00,00
	0.-LATITUDE
	1.-LONGITUDE
	2.-GPS FIX
	3.-STANJE PUMPE (0-NE PUMPA, 1-PUMPA, 1-ZAMAsENA, 2-POKVARJENA)
	4.-DEVIŠKI START
	5.-ANCHOR DRIFTING STATE
	6.-ANCHOR DRIFTING ALARM
	7.-ACCU DISCONNECT
	8.-TOK
	9.-ENERGIJA
	10.-NAPETOST
	11.-OUTPUT COUNT
	12.-OUTPUTS x OUTPUT COUNT
	13.-INPUT COUNT
	14.-INPUTS x INPUT COUNT
	*/
	public boolean setData(String gsmnum, String serial, String data) {
		Connection con = null;
		Statement stmt = null;
		Obu obu = getObu(gsmnum, serial);
		Map<Integer, State> obuStates = getStates(obu.getUid());
		lastStateData = getStateData(obu.getUid());
		boolean isAdd = false;
		
	    try {
	    	String[] states = data.split(",");
	    	//String dateState = states[Constant.OBU_DATE_VALUE];
	    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
	        //Date parsedDate = dateFormat.parse(dateState);
	        //Timestamp tsDS = new java.sql.Timestamp(parsedDate.getTime());
	    	
	    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	Date today = Calendar.getInstance().getTime();        
	    	String dateState = df.format(today);
	    	//if (lastStateData.get(Constant.STATE_ROW_STATE_VALUE)==null || tsDS.after(lastStateData.get(Constant.STATE_ROW_STATE_VALUE).getDateState())) {
	    		//Map<Integer, StateData> stateDataLast = getStateData(obu.getId());
	    		isAdd = true;
		    	
		    	con = DbManager.getConnection("config");
				stmt = con.createStatement();   	
	 
		    	String	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
		    				"values (" + Constant.STATE_ROW_STATE_VALUE + ", " + obu.getUid() + ", '" + data + "', '" + dateState + "')";
		    		
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
			    		else if (state.getId() == Constant.STATE_ACCU_NAPETOST_VALUE){
		    				int stateNapetost = Integer.parseInt(stateValue, 16);
		    				int stateTok = Integer.parseInt(states[Constant.OBU_ACCU_AH_VALUE], 16);
		    				//ce je tok<APP_SETTING_TOK_MIN je napetost zadnja od takrat ko je tok>APP_SETTING_TOK_MIN
		    				if ((lastStateData.get(Constant.STATE_ACCU_NAPETOST)!= null) && (stateTok <= Constant.APP_SETTINGS_NAPETOST_TOK_MIN_VALUE)) {
		    					stateValue = Integer.parseInt(lastStateData.get(Constant.STATE_ACCU_NAPETOST_VALUE).getValue()) + "";
		    				} else {
		    					stateValue = Math.round((stateNapetost / Constant.APP_SETTINGS_NAPETOST_KOEF1_VALUE) * Constant.APP_SETTINGS_NAPETOST_KOEF2_VALUE)+"";
		    				}
		    			}
			    		else if (state.getId() == Constant.STATE_ACCU_AH_VALUE){
		    				int stateAh = Integer.parseInt(stateValue, 16);
		    				int stateAhLast = Constant.APP_SETTINGS_NAPETOST_TOK_MAX_VALUE;
		    				if (lastStateData.get(Constant.STATE_ROW_STATE) != null) {
		    					String raw_state_last = lastStateData.get(Constant.STATE_ROW_STATE_VALUE).getValue();
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
		    	    		"values ('" + state.getId() + "', " + obu.getUid() + ", '" + stateValue + "', '" + dateState + "')";
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
	    	    		"values (" + Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE + ", " + obu.getUid() + ", '" + distance + "', '" + dateState + "')";
	    			
	   	    	stmt.executeUpdate(sql);
		    	
		    	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    	    		"values (" + Constant.OBU_SETTINGS_GEO_FENCE_VALUE + ", " + obu.getUid() + ", '" + obuSettings.get(Constant.OBU_SETTINGS_GEO_FENCE_VALUE) + "', '" + dateState + "')";
	    			
	   	    	stmt.executeUpdate(sql);
	    	//}
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) stmt.close();
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }
		
		return isAdd;
	}
	
	
	//todo : cache obu
	public Obu getObu(String gsmnum, String serial) {
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
	    		obu.setUid(rs.getInt("uid"));
	    		obu.setName(rs.getString("name"));
	    		obu.setNumber(rs.getString("number"));
	    		obu.setPin(rs.getString("pin"));
	    		obu.setPuk(rs.getString("puk"));
	    		obu.setSerial_number(rs.getString("serial_number"));
	    		obu.setActive(rs.getInt("active"));	    		
	    	}
	    	
    		if (obu.getUid() > 0) {
		    	sql = "select * from obu_settings where id_obu = " + obu.getUid() + "";
	    		
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
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }	
		
    	return obu;
	}	

	
	public Map<Integer, StateData> getStateData(int id) {
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
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }
		
		return statesData;
	}

	
	public void calculateAlarms(String gsmnum, String serial) {
		Obu obu = getObu(gsmnum, serial);
		List<ObuAlarm> obuAlarms = getAlarms(obu.getUid());
    	
		Map<Integer, StateData> obuLast = getStateData(obu.getUid());
		Customer customer = getCustomer(obu.getUid());
		
		Iterator it = obuLast.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        StateData stateData = (StateData)pairs.getValue();
	        Integer stateId = (Integer)pairs.getKey();
	        
		    for (int i=0; i< obuAlarms.size(); i++) {    
		        ObuAlarm obuAlarm = obuAlarms.get(i);
		        Alarm alarm = Cache.alarms.get(obuAlarm.getId_alarm());
		        String state = stateData.getValue();
		        		
		        if (stateData.getId_state() == alarm.getId_state()) {
		        	int alarmValue;
		        	if (alarm.getValue().equals("obu_settings")) {
		        		ObuData obuData = new ObuData();
		        		Map<Integer, ObuSetting> obuSettings = obuData.getObuSettings(obu.getUid()+"", null, null);
	        			state = ((ObuSetting)obuSettings.get(Constant.OBU_SETTINGS_GEO_FENCE_VALUE)).getValue();
	        			if (Integer.parseInt(state) == Constant.GEO_FENCE_DISABLED_VALUE){
        					continue;
	        			}
	        			alarmValue = Integer.parseInt(((ObuSetting)obuSettings.get(Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE)).getValue());
	        		} else {
	        			alarmValue = Integer.parseInt(alarm.getValue());
	        		}
		        	
		        	if (alarm.getFormat().equals("N")) {
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
			        		StateData stateDataPrevious = lastStateData.get(stateId);
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
	            			setAlarm(alarm.getId(), obu.getUid(), state, alarm.getMessage(), alarm.getMessage_short(), alarm.getTitle(), alarm.getAction(), alarm.getType(), obuAlarm.getSound(), obuAlarm.getVibrate(), obuAlarm.getSend_email(), obuAlarm.getSend_customer(), obuAlarm.getSend_friends(), stateData.getDateState(), obuAlarm.getActive(), customer.getEmail());
			        	}
		        	}
		        }
			}
		}
	}

	
	public void setAlarm(int alarmid, int obuid, String stateValue, String message, String messageShort, String title, String action, String type, int sound, int vibrate, int sendEmail, int sendCustomer, int sendFriends, Timestamp date_alarm, int active, String email_to) {
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
				
		    	sql = "insert into alarm_data (id_alarm, id_obu, value, message, message_short, title, action, type, sound, vibrate, send_customer, send_friends, date_alarm, active) " + 
		    			"values (" + alarmid + ", " + obuid + ", '" + stateValue + "', '" + msg + "', '" + messageShort + "', '" + title + "', '" + action + "', '" + type + "', " + sound + ", " + vibrate + ", " + sendCustomer + ", " + sendFriends + ", '" + date_alarm + "', " + active + ")";
		    		
		    	stmt.executeUpdate(sql);
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
	}	
	
	public String getMessage(String message, int obuid) {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
	    try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

	    	String	sql = "select obus.name obus_name, customers.name customers_name, customers.surname customers_surname, customers.phone_number customers_number "
    				+ "from obus left join customers on (obus.uid = customers.id_obu)" +
    				" where obus.uid = " + obuid;
	    		
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
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }
	    
		return message;
	}

	public List<AlarmData> getAlarmData(int obuid) {
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
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }	
		
    	return alarmData;
	}	
	
	public List<ObuAlarm> getAlarms(int obuid) {
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
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }	
		
    	return obuAlarms;
	}		

	public Map<Integer, State> getStates(int obuid) {
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
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }	
		
    	return states;
	}		

	public Customer getCustomer(int obuid) {
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
	    		customer.setUid(rs.getInt("uid"));
	    		customer.setId_obu(rs.getInt("id_obu"));
	    		customer.setName(rs.getString("name"));
	    		customer.setSurname(rs.getString("surname"));
	    		customer.setUsername(rs.getString("username"));
	    		customer.setPassword(rs.getString("password"));
	    		customer.setEmail(rs.getString("email"));
	    		customer.setRegister_date(rs.getTimestamp("register_date"));
	    		customer.setLast_visited(rs.getTimestamp("last_visited"));
	    		customer.setApp_version(rs.getString("app_version"));
	    		customer.setPhone_number(rs.getString("phone_number"));
	    		customer.setPhone_model(rs.getString("phone_model"));
	    		customer.setPhone_platform(rs.getString("phone_platform"));
	    		customer.setPhone_platform_version(rs.getString("phone_platform_version"));
	    		customer.setPhone_uuid(rs.getString("phone_uuid"));
	    		customer.setHome_network(rs.getString("home_network"));
	    		customer.setActive(rs.getString("active"));
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
		
    	return customer;
	}	
	
	public String login(String username, String password, String obuSerialNumber, String deviceName, String devicePlatform, String deviceVersion, String deviceUuid, String phoneNumber, String appVersion) {
		
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
		Gson gson = new Gson();
		String obuS = null;
		String errorS = null;
		
 	    try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select customers.uid customer_id, obus.* "
	    			+ "from customers inner join obus on (customers.id_obu = obus.uid) "
	    			+ "where UPPER(username) = UPPER('" + username + "') AND UPPER(password) = UPPER('" + password + "')";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		System.out.println("SQL="+sql);

	    	if (rs.next()) {
	    		Obu obu= new Obu();
	    		obu.setUid(rs.getInt("uid"));
	    		obu.setName(rs.getString("name"));
	    		obu.setNumber(rs.getString("number"));
	    		obu.setPin(rs.getString("pin"));
	    		obu.setPuk(rs.getString("puk"));
	    		obu.setSerial_number(rs.getString("serial_number"));
	    		obu.setActive(rs.getInt("active"));
				obuS = gson.toJson(obu);

	    		setLoginData(username, deviceName, devicePlatform, deviceVersion, deviceUuid, phoneNumber, appVersion);
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
		
		String result = "{\"obu\":"+obuS+",\"error\":"+errorS+"}";
    	return result;

		
	}

	public String register(String username, String password, String obuSerialNumber, String deviceName, String devicePlatform, String deviceVersion, String deviceUuid, String phoneNumber, String appVersion) {
		
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
		Gson gson = new Gson();
		String errorS = null;
    	String result = "";
		
 	    try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select customers.uid customer_id, obus.* "
	    			+ "from customers left join obus on (customers.id_obu = obus.uid) "
	    			+ "where (UPPER(username) = UPPER('" + username + "') and serial_number = " + obuSerialNumber + " and obus.active=1) OR "
	    					+ "NOT EXISTS (select uid from obus where serial_number = " + obuSerialNumber + ")";
	    		
	    	System.out.println("SQL="+sql);
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	
    		
	    	if (rs.next()) {
	    		Error error = new Error(Error.REGISTER_ERROR, Error.REGISTER_ERROR_CODE, Error.REGISTER_ERROR_MSG);
				errorS = gson.toJson(error);
				result = "{\"error\":"+errorS+"}";
	    	} else {
		    	sql = "insert into customers (username, password, register_date, active, id_obu) " 
		    			+ "select '" + username + "', '" + password + "', now(), 1, uid "
		    			+ "from obus "
		    			+ "where serial_number = " + obuSerialNumber;
		    	stmt.executeUpdate(sql);
	    		
		    	sql = "update obus " + 
		    			"set active = 1 " +
		    			"where serial_number = " + obuSerialNumber;
		    	stmt.executeUpdate(sql);

		    	result = login(username, password, obuSerialNumber, deviceName, devicePlatform, deviceVersion, deviceUuid, phoneNumber, appVersion);
	    	}
			
	
	    } catch (Exception theException) {
    		Error error = new Error(Error.REGISTER_ERROR, Error.REGISTER_ERROR_CODE, Error.REGISTER_ERROR_MSG);
			errorS = gson.toJson(error);
			result = "{\"error\":"+errorS+"}";
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }	
		
		return result;

		
	}

	
	public void setLoginData(String username, String deviceName, String devicePlatform, String deviceVersion, String deviceUuid, String phoneNumber, String appVersion){
		Connection con = null;
		Statement stmt = null;
		
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
 
			String	sql = "update customers " + 
	    				"set phone_model = '" + deviceName + "', " +
	    				"	phone_platform = '" + devicePlatform + "', " +
	    				"	phone_platform_version = '" + deviceVersion + "', " +
	    				"	phone_uuid = '" + deviceUuid + "', " +
	    				//"	phone_number = '" + phoneNumber + "', " +
	    				"	app_version = '" + appVersion + "', " +
	    				"	last_visited = now() " +
	    				"where UPPER(username) = UPPER('" + username + "')";
			System.out.println("SQL="+sql);
	
	    	stmt.executeUpdate(sql);
	    	
	    	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) stmt.close();
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }
	}

	public void confirmAlarm(int alarmid, int obuid) {
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
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }
	}	
	

}
