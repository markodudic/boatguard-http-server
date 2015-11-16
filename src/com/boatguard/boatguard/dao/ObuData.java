package com.boatguard.boatguard.dao;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import si.bisoft.commons.dbpool.DbManager;

import com.boatguard.boatguard.comm.MailClient;
import com.boatguard.boatguard.objects.Alarm;
import com.boatguard.boatguard.objects.AlarmData;
import com.boatguard.boatguard.objects.BatterySetting;
import com.boatguard.boatguard.objects.Customer;
import com.boatguard.boatguard.objects.Device;
import com.boatguard.boatguard.objects.Friend;
import com.boatguard.boatguard.objects.Obu;
import com.boatguard.boatguard.objects.ObuAlarm;
import com.boatguard.boatguard.objects.ObuComponent;
import com.boatguard.boatguard.objects.ObuSetting;
import com.boatguard.boatguard.objects.State;
import com.boatguard.boatguard.objects.StateData;
import com.boatguard.boatguard.objects.Timezone;
import com.boatguard.boatguard.util.Constant;
import com.boatguard.boatguard.util.Util;
import com.boatguard.engineguard.dao.EngineGuardData;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;


public class ObuData {
	
	private static Map<Integer, StateData> lastStateData = new HashMap<Integer, StateData>();
	public static final String ACCOUNT_SID = "AC112ea33cdbaeca1f35082386731fb9d0"; 
	public static final String AUTH_TOKEN = "c9a002cfe476f7cf233d6ef382c99f18";
	public static final String FROM = "+447903596041";
	
	public static final String GCM = "AIzaSyCqFRqsD4W9SC1urN5k5njvIzUKDmAM46Y";
	//public static final String TZA = "AIzaSyAi1_iBErRoU5bIfNEMy8aR06sHWBu6xCI";
	public static final String TZ_URL = "https://maps.googleapis.com/maps/api/timezone/json";
	public static int SERVER_ZONE = 3600;
	
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
	    		obuSetting.setId_obu(rs.getInt("id_obu"));
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

	
	/*
	0d00500112340000
	Če je število vseh bajtov po Y recimo 3F (63 desetiško) je YH=3(33) in YL=F(46) 
	0.-NUM BYTES (2 mesta)
	1.-PIA PRISOTNA (1)
	2.-URE IN MINUTE (2)
	3.-RESET ENERGY (1)
	4.-PUMP CALIBRATION (1)
	5.-ANCHOR DRIFTING ON (1)
	6.-ANCHOR DRIFTING DISTANCE (4)
	7.-OUTPUT COUNT (2)
	8.-OUTPUTS x OUTPUT COUNT
	9.-INPUT COUNT (2)
	10.-INPUTS x INPUT COUNT
	*/
			
	public List<ObuSetting> getObuSettingsForObu(String obuid, String gsmnum, String serial) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    List<ObuSetting> obuSettings = new ArrayList<ObuSetting>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select obu_settings.id_obu, obu_settings.id_setting, max(obu_settings.value) value, obu_settings.type, settings.code, app_settings.value value1 "
    				+ "from obus left join "
    				+ "	obu_settings on (obus.uid = obu_settings.id_obu) left join "
    				+ "	settings on (obu_settings.id_setting = settings.id)  left join "
    				+ "	app_settings on (settings.code = app_settings.name) "
					+ "where (number = '" + gsmnum + "' or serial_number = '" + serial + "' or obus.uid = " + obuid + ") and app_settings.value is not null "
					+ "group by app_settings.value"
					+ " order by app_settings.value";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		obuid = rs.getInt("id_obu")+"";
	    		ObuSetting obuSetting = new ObuSetting();
	    		obuSetting.setId_setting(rs.getInt("id_setting"));
	    		String v = rs.getString("value");
    			if (rs.getInt("id_setting") == Constant.OBU_SETTINGS_REFRESH_TIME_VALUE) {
    				if (hasAlarm(Integer.parseInt(obuid))) {
    					v = (Constant.APP_SETTINGS_ALARM_REFRESH_TIME_VALUE / 60 / 1000) + "";
    				}
	    			while (v.length()<2) {
	    				v = '0' + v;
	    			}
	    		}
    			/*if (rs.getInt("id_setting") == Constant.OBU_SETTINGS_BATTERY_ENERGY_RESET_VALUE) {
	    			//za KITO konverzija v D/N
    				if (v.equals("0")) v = "N";
	    			else v = "D";
	    		}*/
    			if ((rs.getInt("id_setting") == Constant.OBU_SETTINGS_ANCHOR_DRIFTING_VALUE) || 
    				(rs.getInt("id_setting") == Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE)) {
	    			v = (Integer.parseInt(v) * 10000 / 1850) + "";
    				while (v.length()<4) {
	    				v = '0' + v;
	    			}
    	    	}
	    		obuSetting.setValue(v);
	    		obuSetting.setType(rs.getString("type"));
	    		obuSetting.setCode(rs.getString("code"));
	    		obuSettings.add(obuSetting);
	    	}
	    	
			//reset energy reset - za test zakomentirano da lahko kita testira
	    	setEnergy(obuid, 0);
	
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

	private boolean hasAlarm(int obuid) {
		Map<Integer, StateData> obuStates = getObuData(obuid);
		return (Integer.parseInt(obuStates.get(Constant.STATE_GEO_FENCE_VALUE).getValue()) == 2 ||
				Integer.parseInt(obuStates.get(Constant.STATE_PUMP_STATE_VALUE).getValue()) > 0 ||
				Integer.parseInt(obuStates.get(Constant.STATE_ACCU_EMPTY_VALUE).getValue()) == 1 ||
				Integer.parseInt(obuStates.get(Constant.STATE_ANCHOR_DRIFTING_VALUE).getValue()) == 2
				);
		
	}
	
	private void setEnergy(String obuid, int set) {
		Connection con = null;
		Statement stmt = null;
	    try {
    		con = DbManager.getConnection("config");
    		stmt = con.createStatement();   	
	    	//reset energy reset - za test zakomentirano da lahko kita testira
    		String sqls = "update obu_settings " +
						"set value = " + set + " " +
						"where id_obu = " + obuid + " and " +
						"	id_setting = " + Constant.OBU_SETTINGS_BATTERY_ENERGY_RESET_VALUE;
    		stmt.executeUpdate(sqls);
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) stmt.close();
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }			
	}
	
	public void setObuSettings(String data) {
		Gson gson = new Gson();
		Type listType = new TypeToken<List<ObuSetting>>(){}.getType();
		List obuSettings = gson.fromJson(data, listType);
		
		Connection con = null;
		Statement stmt = null;
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	
			int obuId = 0;
			boolean setLocation = false;
			for (int i = 0; i < obuSettings.size(); i++) {
				ObuSetting obuSetting = (ObuSetting) obuSettings.get(i);
				if (obuSetting.getId_obu() == 0) continue;
				
				if  (obuSetting.getCode().equals(Constant.OBU_SETTINGS_LAT)) {
					setLocation = obuSetting.getValue().equals("SET");
				}
				if  (obuSetting.getCode().equals(Constant.OBU_SETTINGS_LAT) || obuSetting.getCode().equals(Constant.OBU_SETTINGS_LON)) {
					continue;
				}
				obuId = obuSetting.getId_obu();
				
		    	String sql = "update obu_settings " + 
			    		" set value = '" + obuSetting.getValue() + "'" +
			    		" where id_obu = " + obuId + " and id_setting = " + obuSetting.getId_setting();
					
				stmt.executeUpdate(sql);
					    	
			}
			
			if (setLocation) {
				//set current location
				String sqls = "update obu_settings " +
							"set value = (select value " +
										"from states_data " +
										"where id_obu = " + obuId +
										"		and id_state = " + Constant.OBU_SETTINGS_LAT_VALUE +
										" order by date_state desc " +
										"limit 1) " +
							"where id_setting = " + Constant.OBU_SETTINGS_LAT_VALUE +
							"		and id_obu = " + obuId;
				
				stmt.executeUpdate(sqls);
	
				sqls = "update obu_settings " +
						"set value = (select value " +
									"from states_data " +
									"where id_obu = " + obuId +
									"		and id_state = " + Constant.OBU_SETTINGS_LON_VALUE +
									" order by date_state desc " +
									"limit 1) " +
						"where id_setting = " + Constant.OBU_SETTINGS_LON_VALUE +
						"		and id_obu = " + obuId;
			
				stmt.executeUpdate(sqls);
			}
		} catch (Exception theException) {
			theException.printStackTrace();
		} finally {
			try {
				if (stmt != null) stmt.close();
				if (con != null) con.close();
			} catch (Exception e) {}
		}		
        
		
	}
	
	public LinkedHashMap<Integer, ObuComponent> getComponents(String obuid, String gsmnum, String serial) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    LinkedHashMap<Integer, ObuComponent> obuComponents = new LinkedHashMap<Integer, ObuComponent>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select obu_components.id_component, components.name, components.type, components.show "
	    			+ "from obus left join "
	    			+ "		obu_components on (obus.uid = obu_components.id_obu) left join "
	    			+ "		components on (obu_components.id_component = components.id) "
	    			+ "where number = '" + gsmnum + "' or serial_number = '" + serial + "' or obus.uid = " + obuid + " "
	    			+ "order by components.ord";
	    		
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
	1234.1222,N,12345.1111,E,1,5,0,N,N,N,D,2F50,1A1B1C,2F50,00,00
	0.-LATITUDE
	1.-N/S INDICATOR
	2.-LONGITUDE
	3.-E/W INDICATOR
	4.-GPS FIX
	5.-SAT NUM
	6.-PIA STATE
	7.-STANJE PUMPE (0-NE PUMPA, 1-PUMPA, 2-ZAMAsENA, 3-POKVARJENA)
	8.-DEVIŠKI START
	9.-ANCHOR DRIFTING STATE
	10.-ANCHOR DRIFTING ALARM
	11.-ACCU CONNECT
	12-TOK
	13.-ENERGIJA
	14.-NAPETOST
	15.-OUTPUT COUNT
	16.-OUTPUTS x OUTPUT COUNT
	17.-INPUT COUNT
	18.-INPUTS x INPUT COUNT
	*/
	public boolean setData(String gsmnum, String serial, String data) {
		Connection con = null;
		Statement stmt = null;
		Obu obu = getObu(gsmnum, serial);
		Map<Integer, State> obuStates = getStates(obu.getUid());
		lastStateData = getObuData(obu.getUid());
		boolean isAdd = false;
		boolean isTok = false;
		
	    try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

			//fake zaradi kite, ko posilja takale podatke
	    	//0000000000000000000000000000000,0,N,N,N,D,0091,002E95,0391,00,00,
	    	data = data.replace("0000000000000000000000000000000", "0,N,0,E,0,0,0");
	    	
	    	String[] states = data.split(",");
	    	String nsIndicator = states[Constant.OBU_N_S_INDICATOR_VALUE];
			String ewIndicator = states[Constant.OBU_E_W_INDICATOR_VALUE];		
				    	
	    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	Calendar cal = Calendar.getInstance();
	    	

			//date state popravim glede na time zone
	    	//zaradi api quote preverim samo ob polnoci in zapisem v setting obuja
	    	if (cal.get(Calendar.HOUR_OF_DAY) == 0) {
		    	float lat = Util.transform(Float.parseFloat(states[Constant.OBU_LAT_VALUE]));
	    		float lon = Util.transform(Float.parseFloat(states[Constant.OBU_LON_VALUE]));
				if (ewIndicator.equals("W")) {
					lat = -lat;
				}
				if (nsIndicator.equals("S")) {
					lon = -lon;
				}
				
				String url = TZ_URL + "?location="+lon+","+lat+"&timestamp="+cal.getTimeInMillis()/1000+"&key="+GCM;
				HttpContext localContext = new BasicHttpContext();
				HttpPost httpPost = new HttpPost(url);
				System.out.println("url="+url);
				HttpClient httpClient = new DefaultHttpClient();
				   
				String text = null;
				HttpResponse response = httpClient.execute(httpPost, localContext);
				HttpEntity entity = response.getEntity();
				text = getASCIIContentFromEntity(entity);
				System.out.println("text="+text);
		    	
				Gson gson = new Gson();
				Timezone timezone = gson.fromJson(text, Timezone.class);
				if (timezone.getStatus().equalsIgnoreCase("OK")) {
					int diff = timezone.getRawOffset() - SERVER_ZONE;
					cal.add(Calendar.SECOND, diff);
					
			    	String sql = "update obus " + 
				    		" set timezone = " + diff +
				    		" where uid = " + obu.getUid();
			    	stmt.executeUpdate(sql);
				}
	    	} else {
	    		cal.add(Calendar.SECOND, obu.getTimezone());
	    	}
	    	Date today = cal.getTime();        
	    	String dateState = df.format(today);
	    	
	    	//if (lastStateData.get(Constant.STATE_ROW_STATE_VALUE)==null || tsDS.after(lastStateData.get(Constant.STATE_ROW_STATE_VALUE).getDateState())) {
	    		//Map<Integer, StateData> stateDataLast = getStateData(obu.getId());
	    		isAdd = true;
    			
	 
		    	String	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
		    				"values (" + Constant.STATE_ROW_STATE_VALUE + ", " + obu.getUid() + ", '" + data + "', '" + dateState + "')";
		    		
		    	stmt.executeUpdate(sql);
    			
		    	for (int i=0;i<states.length;i++) {
					boolean insert = true;
		    		if (Cache.states.get(i) != null) {
		    			String stateValue = states[i];
		    			//System.out.println("stateValue="+stateValue);
		    			
		    			//TUKAJ JE SAM PREMAPIRANJE ZARADI KITE IZ D/N V 1/0 ZA PODATKE OD 7-10
		    			if ((i>7) && (i<12)) {
		    				stateValue = stateValue.replace("D", "1").replace("N", "0");
		    			}
		    			
		    			State state = obuStates.get(Cache.states.get(i).getId());
		    			if (state == null) continue;
		    			
			    		if (state.getId() == Constant.STATE_ACCU_TOK_VALUE){
			    			String prefix = "-";
			    			if (Integer.parseInt(stateValue.substring(1,2)) == 8) {
			    				stateValue = stateValue.substring(0,1) + "0" + stateValue.substring(2,4);
			    				prefix = "+";
			    			}
			    			stateValue = Util.hexaToDec(stateValue)/Constant.APP_SETTINGS_TOK_KOEF1_VALUE + ""; 	
			    			stateValue = new DecimalFormat("#.##").format(Float.parseFloat(stateValue));
			    			if (stateValue.equals("0")) {
			    				setEnergy(obu.getUid()+"", 1);
			    			}
			    			else {
				    			stateValue = prefix + stateValue;
				    			isTok = true;
			    			}
		    			}
			    		else if (state.getId() == Constant.STATE_ACCU_NAPETOST_VALUE){
			    			/*if (Integer.parseInt(stateValue.substring(0,1)) > 7) {
			    				//ce je A>=8 je negativen tok (polnjenje) in prikazem tok=0
			    				stateValue = "0";
			    			}
			    			else {*/
			    				HashMap<String, BatterySetting> batterySetting = Cache.batterySettings.get(obu.getId_battery_settings());
			    				long value = Util.hexaToDec(stateValue);
			    				if (batterySetting.get(stateValue) == null) {
				    				//interpoliram vrednost hexa
				    				Set<String> keys = batterySetting.keySet();
				    				long min_diff = 99999;
				    				String newValue = stateValue;
				    				Iterator it = keys.iterator();
			    			        while (it.hasNext()) {
				    					String keyS = (String)it.next();
				    					long key = Util.hexaToDec(keyS);
					    				
				    			        if (Math.abs(value - key) < min_diff) {
			    							newValue = keyS;
			    							min_diff = Math.abs(value - key);
			    						}
				    				}
				    				stateValue = newValue;
				    			}
				    			//String stateNapetost = stateValue;
				    			//stateValue = Util.hexaToDec(stateValue)/batterySetting.get(stateValue).getKoef() +"";
				    			
				    			//procente izracunam iz napetosti in ne iz porabe. če je tok=0
			    				if (!isTok) {
					    			String procent = batterySetting.get(stateValue).getPercent() +"";
					    			Map<Integer, String> obuSettings = obu.getSettings();
					    			
					    			int empty = Integer.parseInt(obuSettings.get(Constant.OBU_SETTINGS_BATTERY_ALARM_LEVEL_VALUE));
					    			sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
							    	   		"values ('" + Constant.STATE_ACCU_EMPTY_VALUE + "', " + obu.getUid() + ", '" + (Integer.parseInt(procent)<empty?Constant.BATTERY_EMPTY_VALUE:"0") + "', '" + dateState + "')";
								    stmt.executeUpdate(sql);		        				
						    		sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
						    	    		"values ('" + Constant.STATE_ACCU_AH_VALUE + "', " + obu.getUid() + ", '" + procent + "', '" + dateState + "')";
						    		stmt.executeUpdate(sql);
			    				}
			    				
			    				if (value > Constant.APP_SETTINGS_NAPETOST_MAX_VALUE) {
				    				stateValue = "MAX";			    				
				    		    	setEnergy(obu.getUid()+"", 1);
				    			}
				    			else {
				    				stateValue = batterySetting.get(stateValue).getVolt() + "";
				    				stateValue = new DecimalFormat("#.##").format(Float.parseFloat(stateValue));
				    			}
			    			//}
		    			}
			    		else if (state.getId() == Constant.STATE_ACCU_AH_VALUE){
			    			insert = isTok;
			    			if (isTok) {				    			
				    			Double stateUsedEnergy = Util.hexaToDecLong(stateValue)/(3600 * Constant.APP_SETTINGS_TOK_KOEF1_VALUE);
				    			
				    			Map<Integer, String> obuSettings = obu.getSettings();
				    			stateValue = "0";
				    			if (stateUsedEnergy > 0) {
				    				//long value = (100 - Math.round((100 * stateUsedEnergy) / Float.parseFloat(obuSettings.get(Constant.OBU_SETTINGS_BATTERY_CAPACITY_VALUE))));
				    				long value = Math.round((100 * stateUsedEnergy) / Float.parseFloat(obuSettings.get(Constant.OBU_SETTINGS_BATTERY_CAPACITY_VALUE)));
				    				if (value < 0) stateValue = "0";
					    			else stateValue = value + "";
				    			}
				    			
				    			int stateValueDiff = Integer.parseInt(lastStateData.get(state.getId()).getValue()) - Integer.parseInt(stateValue);
				    			if (stateValueDiff < 0) stateValue = "0";
				    			else if (stateValueDiff > 100) stateValue = "100";
				    			else stateValue = stateValueDiff + "";
				    			
				    			int empty = Integer.parseInt(obuSettings.get(Constant.OBU_SETTINGS_BATTERY_ALARM_LEVEL_VALUE));
					    		sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
							       		"values ('" + Constant.STATE_ACCU_EMPTY_VALUE + "', " + obu.getUid() + ", '" + (Integer.parseInt(stateValue)<empty&&!isTok?Constant.BATTERY_EMPTY_VALUE:"0") + "', '" + dateState + "')";
								stmt.executeUpdate(sql);	
			    			}
		    			}
			    		else if ((state.getId() == Constant.STATE_LON_VALUE) || (state.getId() == Constant.STATE_LAT_VALUE)) {
			    			Integer geoFixValue = Integer.parseInt(states[Constant.OBU_GEO_FIX_VALUE]);
			    			
			    			//ce je geo_fix=0 prepisem stare koordinate
			    			if (geoFixValue != Constant.GEO_FIX_OK_VALUE) {
			    				if (lastStateData.get(state.getId()) != null) {
				    				stateValue = lastStateData.get(state.getId()).getValue();
				    				if (state.getId() == Constant.STATE_LON_VALUE) {
				    					states[Constant.OBU_LON_VALUE] = stateValue;
				    				}
				    				else if (state.getId() == Constant.STATE_LAT_VALUE) {
				    					states[Constant.OBU_LAT_VALUE] = stateValue;
				    				}
			    				}
			    			} else 	{
			    				stateValue = Util.transform(Float.parseFloat(stateValue)) +"";
				    			if (state.getId() == Constant.STATE_LON_VALUE) {
			    					if (nsIndicator.equalsIgnoreCase("S")) {
			    						stateValue = "-" +stateValue;
			    					}
				    			}
			    				if (state.getId() == Constant.STATE_LAT_VALUE) {
			    					if (ewIndicator.equalsIgnoreCase("W")) {
			    						stateValue = "-" +stateValue;
			    					}
			    				}
			    			}
			    		}
			    		else if (state.getId() == Constant.STATE_ANCHOR_VALUE){
			    			Map<Integer, String> obuSettings = obu.getSettings();
			    			if (obuSettings.get(Constant.OBU_SETTINGS_ANCHOR_VALUE).equals(Constant.ANCHOR_ENABLED_VALUE+"")) {
								stateValue = Constant.ANCHOR_ENABLED_VALUE + "";
			    			}
			    		}
			    		
			    		//INPUT DOOR
			    		if (obu.getFirmware() == Constant.FIRMWARE_2) {
			    			if (i == 17) {
			    				try {
				    				if (Integer.parseInt(stateValue) == 1) { 
					    				stateValue = "1";
					    			}
					    			else {
					    				stateValue = "0";
					    			}
				    				
			    				} catch (Exception e) {
			    					continue;
			    				}
				    		}			    		
			    		}	
			    		
			    		if (insert) {
				    		sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
				    	    		"values ('" + state.getId() + "', " + obu.getUid() + ", '" + stateValue + "', '" + dateState + "')";
				    		stmt.executeUpdate(sql);
			    		}
		    		}
		    	}
		    
		    	//geo fence distance
		    	Map<Integer, String> obuSettings = obu.getSettings();
	    		float lat1 = Util.transform(Float.parseFloat(states[Constant.OBU_LAT_VALUE]));
				if (ewIndicator.equalsIgnoreCase("W")) {
					lat1 = -lat1;
				}
	    		float lon1 = Util.transform(Float.parseFloat(states[Constant.OBU_LON_VALUE]));
				if (nsIndicator.equalsIgnoreCase("S")) {
					lon1 = -lon1;
				}
				float lat2 = Float.parseFloat(obuSettings.get(Constant.OBU_SETTINGS_LAT_VALUE));
				float lon2 = Float.parseFloat(obuSettings.get(Constant.OBU_SETTINGS_LON_VALUE));
				int distance = (int) Math.round(Util.gps2m(lat1, lon1, lat2, lon2));
	    		sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
	    	    		"values (" + Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE + ", " + obu.getUid() + ", '" + distance + "', '" + dateState + "')";
					
	   	    	stmt.executeUpdate(sql);
	   	    	
		    	//geo fence status prepisem
	   	    	if (distance > Integer.parseInt( obuSettings.get(Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE)) &&
	   	    			Integer.parseInt(obuSettings.get(Constant.OBU_SETTINGS_GEO_FENCE_VALUE)) == Constant.GEO_FENCE_ENABLED_VALUE) {
		   	    	sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
		    	    		"values (" + Constant.OBU_SETTINGS_GEO_FENCE_VALUE + ", " + obu.getUid() + ", '" + Constant.GEO_FENCE_ALARM_VALUE + "', '" + dateState + "')";
		   	    }
	   	    	else {
	   	    		sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
		    	    		"values (" + Constant.OBU_SETTINGS_GEO_FENCE_VALUE + ", " + obu.getUid() + ", '" + obuSettings.get(Constant.OBU_SETTINGS_GEO_FENCE_VALUE) + "', '" + dateState + "')";
	   	    	}
	   	    	stmt.executeUpdate(sql);
	    			
	    		//LIGHT & FAN STATE skopiram iz settinga
	    		if (obu.getFirmware() == Constant.FIRMWARE_2) {
	   	    		sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
		    	    		"values (" + Constant.OBU_SETTINGS_LIGHT_VALUE + ", " + obu.getUid() + ", '" + obuSettings.get(Constant.OBU_SETTINGS_LIGHT_VALUE) + "', '" + dateState + "')";
		   	    	stmt.executeUpdate(sql);
	   	    		sql = "insert into states_data (id_state, id_obu, value, date_state) " + 
		    	    		"values (" + Constant.OBU_SETTINGS_FAN_VALUE + ", " + obu.getUid() + ", '" + obuSettings.get(Constant.OBU_SETTINGS_FAN_VALUE) + "', '" + dateState + "')";
		   	    	stmt.executeUpdate(sql);
	    		}
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
	    		obu.setFirmware(rs.getInt("firmware"));
	    		obu.setId_battery_settings(rs.getInt("id_battery_settings"));
	    		obu.setTimezone(rs.getInt("timezone"));
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

	public LinkedHashMap<Integer, Obu> getObus() {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    LinkedHashMap<Integer, Obu> obus = new LinkedHashMap<Integer, Obu>();
		
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * from obus order by serial_number";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		Obu obu = new Obu();
	    		obu.setUid(rs.getInt("uid"));
	    		obu.setName(rs.getString("name"));
	    		obu.setNumber(rs.getString("number"));
	    		obu.setPin(rs.getString("pin"));
	    		obu.setPuk(rs.getString("puk"));
	    		obu.setSerial_number(rs.getString("serial_number"));
	    		obu.setFirmware(rs.getInt("firmware"));
	    		obu.setId_battery_settings(rs.getInt("id_battery_settings"));
	    		obu.setActive(rs.getInt("active"));	
	    		obus.put(rs.getInt("uid"), obu);
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
		
    	return obus;
	}	

	public int getObuFromUser(String originator, String text) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	int id_obu = 0;
    	try {
    		con = DbManager.getConnection("config");

    		if (text.contains("/")) {
	    		String[] data = text.split("/");
		    	String	sql = "select id_obu "
		    			+ "from customers "
		    			+ "where phone_number = '+" + originator + "' and upper(username) = '" + data[0].toUpperCase() + "' and upper(password) = '" + data[1].toUpperCase() + "'";
		    		
		    	stmt = con.createStatement();   	
		    	rs = stmt.executeQuery(sql);
	    		
		    	if (rs.next()) {
		    		id_obu = rs.getInt("id_obu");
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
		
    	return id_obu;
	}	

	protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
		InputStream in = entity.getContent();

		StringBuffer out = new StringBuffer();
		int n = 1;
		while (n>0) {
			byte[] b = new byte[4096];
			n =  in.read(b);
	
			if (n>0) out.append(new String(b, 0, n));
		}
		return out.toString();
	}
	
	public LinkedHashMap<Integer, StateData> getObuData(int id) {
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
	    		/*if ((rs.getInt("id_state") == Constant.STATE_ACCU_NAPETOST_VALUE) || 
	    			(rs.getInt("id_state") == Constant.STATE_ACCU_TOK_VALUE) ||
	    			(rs.getInt("id_state") == Constant.STATE_ACCU_AH_VALUE)) {
		    		String f = new DecimalFormat("#.##").format(Float.parseFloat(rs.getString("value")));
	    			stateData.setValue(f);	
	    		}
	    		else {
	    			stateData.setValue(rs.getString("value"));	
	    		}*/
	    		stateData.setValue(rs.getString("value"));	
	    		stateData.setType(rs.getString("type"));
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

	public List<List<StateData>> getObuHistoryData(int id) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    List<StateData> statesData = new ArrayList<StateData>();
	    String statesDataWithoutDate = "";
	    String statesDataWithoutDateLast = "";
	    List<List<StateData>> historyData = new ArrayList<List<StateData>>();
		
	    Timestamp lastD = null;
		try {
    		con = DbManager.getConnection("config");
    	    
	    	String	sql = "select states_data.*, states.ord "
	    			+ "from states_data left join states on (states_data.id_state = states.id) "
	    			+ "where id_obu = " + id + " and history = 1 and date_state > date_add(now(), interval -1 week)" 
	    			+ "order by date_state desc, ord asc ";
	    			//+ "limit 5000";
	    	
    		stmt = con.createStatement();   	
    		rs = stmt.executeQuery(sql);
			
	    	while (rs.next()) {
	    		Timestamp d = rs.getTimestamp("date_state");
	    		if (lastD==null || !d.equals(lastD)) {
	    			if (lastD!=null) {
	    				if (!statesDataWithoutDateLast.equals(statesDataWithoutDate)) {
		    				historyData.add(statesData);
		    				statesDataWithoutDateLast = statesDataWithoutDate;
	    	    		}
	    			}
		    		statesData = new ArrayList<StateData>();
		    		statesDataWithoutDate = "";
	    			lastD = d;
	    		}
	    		StateData stateData = new StateData();
	    		stateData.setId_state(rs.getInt("id_state"));
	    		stateData.setId_obu(rs.getInt("id_obu"));
	    		/*if ((rs.getInt("id_state") == Constant.STATE_ACCU_TOK_VALUE) || (rs.getInt("id_state") == Constant.STATE_ACCU_NAPETOST_VALUE)) {
	    			stateData.setValue(Math.round(Float.parseFloat(rs.getString("value")))+"");	    			
	    		}
	    		else {
	    			stateData.setValue(rs.getString("value"));
	    		}*/
	    		stateData.setValue(rs.getString("value"));
	    		stateData.setDateState(rs.getTimestamp("date_state"));	
	    		statesData.add(stateData);
	    		
	    		if ((rs.getInt("id_state") != Constant.STATE_LAT_VALUE) && (rs.getInt("id_state") != Constant.STATE_LON_VALUE)) {
	    			statesDataWithoutDate += stateData.getId_state() + ":" + stateData.getValue() +";";
	    		}
	    		
	    	}
			/*if (lastD!=null) {
				historyData.add(statesData);
			}*/
		} catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }
		
		return historyData;
	}
	
	public List<StateData> getObuHistoryRawData(int id) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    StateData stateData = new StateData();
		String statesDataWithoutDate = "";
	    String statesDataWithoutDateLast = "";
	    List<StateData> historyData = new ArrayList<StateData>();
		
	    Timestamp lastD = null;
		try {
    		con = DbManager.getConnection("config");
    	    
	    	String	sql = "select states_data.*"
	    			+ "from states_data "
	    			+ "where id_obu = " + id + " and id_state = 1 and date_state > date_add(now(), interval -1 week)" 
	    			+ "order by date_state desc ";
	    			//+ "limit 5000";
	    	
    		stmt = con.createStatement();   	
    		rs = stmt.executeQuery(sql);
			
	    	while (rs.next()) {
	    		Timestamp d = rs.getTimestamp("date_state");
	    		
	    		if (lastD==null || !d.equals(lastD)) {
	    			if (lastD!=null) {
	    				if (!statesDataWithoutDateLast.equals(statesDataWithoutDate)) {
	    	    			historyData.add(stateData);
		    				statesDataWithoutDateLast = statesDataWithoutDate;
	    	    		}
	    			}
	    			stateData = new StateData();
		    		statesDataWithoutDate = "";
	    			lastD = d;
	    		}
	    		String raw = rs.getString("value");
	    		stateData.setId_state(rs.getInt("id_state"));
	    		stateData.setId_obu(rs.getInt("id_obu"));
	    		stateData.setValue(raw);
	    		stateData.setDateState(rs.getTimestamp("date_state"));	
	    		
	    		int index = Util.nthOccurrence(raw, ',', 5);
	    		statesDataWithoutDate = raw.substring(index);
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
		
		return historyData;
	}

	
	
	public List<ObuAlarm> getObuAlarms(int obuid) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    List<ObuAlarm> obuAlarms = new ArrayList<ObuAlarm>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from obu_alarms left join alarms on (obu_alarms.id_alarm = alarms.id) "
	    			+ "where id_obu = " + obuid;
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		ObuAlarm obuAlarm = new ObuAlarm();
	    		obuAlarm.setId_alarm(rs.getInt("id_alarm"));
	    		obuAlarm.setId_obu(rs.getInt("id_obu"));
	    		obuAlarm.setValue(rs.getString("value"));
	    		obuAlarm.setMessage(getMessage(rs.getString("message"), obuid));
	    		obuAlarm.setMessage_short(rs.getString("message_short"));
	    		obuAlarm.setTitle(rs.getString("title"));
	    		obuAlarm.setAction(rs.getString("action"));
	    		obuAlarm.setSound(rs.getInt("sound"));
	    		obuAlarm.setVibrate(rs.getInt("vibrate"));
	    		obuAlarm.setType(rs.getString("type"));
	    		obuAlarm.setActive(rs.getInt("active"));
	    		obuAlarm.setSend_email(rs.getInt("send_email"));
	    		obuAlarm.setSend_customer(rs.getInt("send_customer"));
	    		obuAlarm.setSend_friends(rs.getInt("send_friends"));
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
	
	
	public void setObuAlarms(String data) {
		Gson gson = new Gson();
		Type listType = new TypeToken<List<ObuAlarm>>(){}.getType();
		List obuAlarms = gson.fromJson(data, listType);
		
		Connection con = null;
		Statement stmt = null;
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

			for (int i = 0; i < obuAlarms.size(); i++) {
				ObuAlarm obuAlarm = (ObuAlarm) obuAlarms.get(i);
				
		    	String sql = "update obu_alarms " + 
			    		" set active = '" + obuAlarm.getActive() + "'," +
			    		" 	send_email = '" + obuAlarm.getSend_email() + "'," +
			    		" 	send_friends = '" + obuAlarm.getSend_friends() + "'" +
			    		" where id_obu = " + obuAlarm.getId_obu() + " and id_alarm = " + obuAlarm.getId_alarm();
		    	stmt.executeUpdate(sql);
					    	
			}
			
		} catch (Exception theException) {
			theException.printStackTrace();
		} finally {
			try {
				if (stmt != null) stmt.close();
				if (con != null) con.close();
			} catch (Exception e) {}
		}		
	}
	
	
	public void calculateAlarms(String gsmnum, String serial) {
		Obu obu = getObu(gsmnum, serial);
		List<ObuAlarm> obuAlarms = getAlarms(obu.getUid());
    	
		Map<Integer, StateData> obuLast = getObuData(obu.getUid());
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
		        	
		        	int alarmValue = 0;
		        	if (alarm.getValue().equals("obu_settings")) {
		        		ObuData obuData = new ObuData();
		        		Map<Integer, ObuSetting> obuSettings = obuData.getObuSettings(obu.getUid()+"", null, null);
	        			state = ((ObuSetting)obuSettings.get(Constant.OBU_SETTINGS_GEO_FENCE_VALUE)).getValue();
	        			if (stateData.getId_state() == Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE){
		        			if (Integer.parseInt(state) == Constant.GEO_FENCE_DISABLED_VALUE){
	        					continue;
		        			}
		        			alarmValue = Integer.parseInt(((ObuSetting)obuSettings.get(Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE)).getValue());	        				
	        			}
	        			else if (stateData.getId_state() == Constant.STATE_ACCU_AH_VALUE) {
		        			alarmValue = Integer.parseInt(((ObuSetting)obuSettings.get(Constant.OBU_SETTINGS_BATTERY_ALARM_LEVEL_VALUE)).getValue());	
		        			//if (Float.parseFloat(obuLast.get(Constant.STATE_ACCU_TOK_VALUE).getValue()) != 0) {
		        			//	continue;
		        			//}
	        			}
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
				            		if (Integer.parseInt(stateDataPrevious.getValue()) <= alarmValue) {
				            			setAlarm = false;
				            		}
					        	} else if (alarm.getPrevious().equals("<")){
					            	if (Integer.parseInt(stateDataPrevious.getValue()) >= alarmValue) {
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
			if (obuid==41 || obuid==45 || obuid==46) {
				EngineGuardData egData = new EngineGuardData();
				egData.setAlarm(obuid+"", null);
				return;
			}
			
			
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
				
				Customer customer = getCustomer(obuid);
				if ((sendCustomer == 1) && (active == 1)) {
					//SmsClient.sendSMSCustomer(obuid, msg);
					String phoneNum = customer.getPhone_number();
				
					if (phoneNum != null) {
						sendSMS(phoneNum, msg);
					}
				}
				if ((sendFriends == 1) && (active == 1)) {
					//SmsClient.sendSMSFriends(obuid, msg);
					List<Friend> friends = getFriends(customer.getUid());
					for (int i=0; i<friends.size(); i++) {
						Friend friend = friends.get(i);
						String phoneNum = friend.getNumber();
						if (phoneNum != null) {
							sendSMS(phoneNum, msg);
						}
					}
				}
				if ((sendEmail == 1) && (active == 1)) {
					MailClient.sendMail(email_to, title, msg);
				}	
				
		    	sql = "insert into alarm_data (id_alarm, id_obu, value, message, message_short, title, action, type, sound, vibrate, send_customer, send_friends, date_alarm, active) " + 
		    			"values (" + alarmid + ", " + obuid + ", '" + stateValue + "', '" + msg + "', '" + messageShort + "', '" + title + "', '" + action + "', '" + type + "', " + sound + ", " + vibrate + ", " + sendCustomer + ", " + sendFriends + ", '" + date_alarm + "', " + active + ")";
		    		
		    	stmt.executeUpdate(sql);

		    	//GCM
				Sender sender = new Sender(GCM);
				Message gcmMsg = new Message.Builder()
					.addData("alarmid", alarmid+"")
					.addData("title", title)
		    		.addData("message", msg)
				    .addData("date",  date_alarm+"")
				    .addData("sound",  sound+"")
				    .addData("vibrate",  vibrate+"")
				    .build();
				
				sql = "select gcm_registration_id, phone_uuid, max(last_visited) "
    				+ "from devices "
    				+ "where id_obu = " + obuid + " "
    				+ "group by phone_uuid";
				rs = stmt.executeQuery(sql);
		    	
				List<String> devices = new ArrayList<String>(); 
		    	while (rs.next()) {
		    		if (rs.getString("gcm_registration_id") != null && !rs.getString("gcm_registration_id").equals("null")) {
		    			devices.add(rs.getString("gcm_registration_id"));
		    		}
		    	}
		    	
		    	if (devices.size() > 0) {
		    		MulticastResult result = sender.send(gcmMsg, devices, 5);
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
	}	
	
	
	public void sendSMS(String phoneNum, String message) {
		 TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN); 
		 
		 System.out.println("Twillio sms="+phoneNum+":"+FROM);
		 
		 // Build the parameters 
		 List<NameValuePair> params = new ArrayList<NameValuePair>(); 
		 params.add(new BasicNameValuePair("To", phoneNum)); 
		 params.add(new BasicNameValuePair("From", FROM)); 
		 params.add(new BasicNameValuePair("Body", message));   
	 
		 MessageFactory messageFactory = client.getAccount().getMessageFactory(); 
		 try {
			 com.twilio.sdk.resource.instance.Message msg = messageFactory.create(params);
			 System.out.println(msg.getSid()); 	
		 } catch (TwilioRestException e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
		 } 
	}
	
	private String getMessage(String message, int obuid) {
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
	    				if (rs.getString(par) != null) {
	    					message = message.replaceAll("%"+par+"%", rs.getString(par));
	    				}
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

	    	String	sql = "select customers.*, obus.name boat_name, obus.serial_number, obus.boat_manafacturer, obus.boat_model, obus.boat_country "
	    			+ "from customers left join obus on (customers.id_obu = obus.uid) "
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
	    		customer.setBirth_year(rs.getInt("birth_year"));
	    		customer.setCountry(rs.getString("country"));
	    		customer.setRegister_date(rs.getTimestamp("register_date"));
	    		customer.setPhone_number(rs.getString("phone_number"));
	    		/*customer.setLast_visited(rs.getTimestamp("last_visited"));
	    		customer.setApp_version(rs.getString("app_version"));
	    		customer.setPhone_model(rs.getString("phone_model"));
	    		customer.setPhone_platform(rs.getString("phone_platform"));
	    		customer.setPhone_platform_version(rs.getString("phone_platform_version"));
	    		customer.setPhone_uuid(rs.getString("phone_uuid"));
	    		customer.setHome_network(rs.getString("home_network"));*/
	    		customer.setActive(rs.getString("active"));
	    		customer.setSerial_number(rs.getString("serial_number"));
	    		customer.setBoat_name(rs.getString("boat_name"));
	    		customer.setBoat_manafacturer(rs.getString("boat_manafacturer"));
	    		customer.setBoat_model(rs.getString("boat_model"));
	    		customer.setBoat_country(rs.getString("boat_country"));
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

	
	public void setCustomer(String data) {
		Gson gson = new Gson();
		Customer customer = gson.fromJson(data, Customer.class);
		
		Connection con = null;
		Statement stmt = null;
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

			String sql = "update customers " + 
			    		" set name = '" + customer.getName() + "'," +
			    		" 	surname = '" + customer.getSurname() + "'," +
			    		" 	password = '" + customer.getPassword() + "'," +
			    		" 	email = '" + customer.getEmail() + "'," +
			    		" 	birth_year = " + customer.getBirth_year() + "," +
			    		" 	country = '" + customer.getCountry() + "'," +
			    		" 	phone_number = '" + customer.getPhone_number() + "'" +
			    		" where id_obu = " + customer.getId_obu();
			//System.out.println(sql);
		    stmt.executeUpdate(sql);
			
			sql = "update obus " + 
		    		" set name = '" + customer.getBoat_name() + "'," +
		    		" 	boat_manafacturer = '" + customer.getBoat_manafacturer() + "'," +
		    		" 	boat_model = '" + customer.getBoat_model() + "'," +
		    		" 	boat_country = '" + customer.getBoat_country() + "'" +
		    		" where uid = " + customer.getId_obu();
			//System.out.println(sql);
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

	public void setDevice(String data) {
		Gson gson = new Gson();
		Device device = gson.fromJson(data, Device.class);
		
		Connection con = null;
		Statement stmt = null;
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement();   	

			String sql = "insert into devices (id_obu, id_customer, gcm_registration_id, phone_model, phone_platform, phone_platform_version, phone_uuid, app_version, last_visited) " + 
			    		" values (" + device.getId_obu() + ", " + 
			    					device.getId_customer() + ", '" + 
			    					device.getGcm_registration_id() + "', '" +
			    					device.getPhone_model() + "', '" +
			    					device.getPhone_platform() + "', '" +
			    					device.getPhone_platform_version() + "', '" +
			    					device.getPhone_uuid() + "', '" +
			    					device.getApp_version() + "', " +
			    					"now())" +
						" on duplicate key update " +
			    		"	id_obu = " + device.getId_obu() + ", " +
			    		"	id_customer = " + device.getId_customer() + ", " +
	    				"	gcm_registration_id = '" + device.getGcm_registration_id() + "', " +
	    				"	phone_model = '" + device.getPhone_model() + "', " +
	    				"	phone_platform = '" + device.getPhone_platform() + "', " +
	    				"	phone_platform_version = '" + device.getPhone_platform_version() + "', " +
	    				"	phone_uuid = '" + device.getPhone_uuid() + "', " +
	    				"	app_version = '" + device.getApp_version() + "', " +
	    				"	last_visited = now() ";
			
			//System.out.println(sql);
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
	public List<Friend> getFriends(int customerid) {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    List<Friend> friends = new ArrayList<Friend>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * "
	    			+ "from friends "
	    			+ "where id_customer = " + customerid;
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	while (rs.next()) {
	    		Friend friend = new Friend();
	    		friend.setUid(rs.getInt("uid"));
	    		friend.setId_customer(rs.getInt("id_customer"));
	    		friend.setName(rs.getString("name"));
	    		friend.setSurname(rs.getString("surname"));
	    		friend.setNumber(rs.getString("number"));
	    		friend.setEmail(rs.getString("email"));
	    		friend.setActive(rs.getString("active"));
	    		friends.add(friend);
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
		
    	return friends;
	}	
	
	
	public void setFriends(String data) {
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Friend>>(){}.getType();
		List friends = gson.fromJson(data, listType);
		
		Connection con = null;
		Statement stmt = null;
		try {
	    	con = DbManager.getConnection("config");
			stmt = con.createStatement(); 
			String ids = "";

			for (int i = 0; i < friends.size(); i++) {
				Friend friend = (Friend) friends.get(i);
				ids += (i>0?",":"") + friend.getUid();
				
		    	String sql = "REPLACE INTO friends (uid, id_customer, name, surname, number, email, active) " +
		    				" VALUES (" + friend.getUid() + ", " + 
		    							friend.getId_customer() + ", '" + 
		    							friend.getName() + "', '" + 
		    							friend.getSurname() + "', '" + 
		    							friend.getNumber() + "', '" + 
		    							friend.getEmail() + "', " + 
		    							friend.getActive() + ")";
		    	stmt.executeUpdate(sql);
			}
			
			String sql = "DELETE FROM friends WHERE uid NOT IN (" + ids + ")";
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
	
	
	public String login(String username, String password, String obuSerialNumber, String sessionId) {
		
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

	    		//setLoginData(username, deviceName, devicePlatform, deviceVersion, deviceUuid, phoneNumber, appVersion);
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
		
		String result = "{\"obu\":"+obuS+",\"sessionId\":\""+sessionId+"\",\"error\":"+errorS+"}";
    	return result;

		
	}

	public String register(String username, String password, String obuSerialNumber, String sessionId) {
		
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
	    			+ "where (serial_number = '" + obuSerialNumber + "' and obus.active=1) OR "
	    					+ "NOT EXISTS (select uid from obus where serial_number = '" + obuSerialNumber + "')";
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
		    			+ "where serial_number = '" + obuSerialNumber + "'";
		    	stmt.executeUpdate(sql);
	    		
		    	sql = "update obus " + 
		    			"set active = 1 " +
		    			"where serial_number = '" + obuSerialNumber + "'";
		    	stmt.executeUpdate(sql);

		    	result = login(username, password, obuSerialNumber, sessionId);
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

	public String loginUser(String username, String password, String sessionId) {
		
		Gson gson = new Gson();
		String obuS = null;
		String errorS = null;
		
 	    try {
    		if ((username.equalsIgnoreCase(Constant.SERVER_SETTINGS_USERNAME_VALUE) && (password.equalsIgnoreCase(Constant.SERVER_SETTINGS_PASSWORD_VALUE)))) {
	    		
	    	} else {
	    		Error error = new Error(Error.LOGIN_ERROR, Error.LOGIN_ERROR_CODE, Error.LOGIN_ERROR_MSG);
				errorS = gson.toJson(error);
	    	}
			
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    }	
		
		String result = "{\"sessionId\":\""+sessionId+"\",\"error\":"+errorS+"}";
    	return result;

		
	}	
	/*public void setLoginData(String username, String deviceName, String devicePlatform, String deviceVersion, String deviceUuid, String phoneNumber, String appVersion){
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
			stmt.executeUpdate(sql);
	    	
	    	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (stmt != null) stmt.close();
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }
	}*/

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
