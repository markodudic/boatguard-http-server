package com.boatguard.boatguard.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import si.bisoft.commons.dbpool.DbManager;

import com.boatguard.boatguard.objects.Alarm;
import com.boatguard.boatguard.objects.AppSetting;
import com.boatguard.boatguard.objects.BatterySetting;
import com.boatguard.boatguard.objects.Component;
import com.boatguard.boatguard.objects.Setting;
import com.boatguard.boatguard.objects.State;
import com.boatguard.boatguard.util.Constant;

public class Cache {
	private static Log log = LogFactory.getLog(Cache.class); 
	public static LinkedHashMap<Integer, Alarm> alarms = new LinkedHashMap<Integer, Alarm>();
	public static LinkedHashMap<Integer, State> states = new LinkedHashMap<Integer, State>();
	public static LinkedHashMap<String, State> statesByCode = new LinkedHashMap<String, State>();
	public static LinkedHashMap<String, AppSetting> appSettings = new LinkedHashMap<String, AppSetting>();
	public static LinkedHashMap<String, Setting> settings = new LinkedHashMap<String, Setting>();
	public static LinkedHashMap<Integer, Component> components = new LinkedHashMap<Integer, Component>();
	public static LinkedHashMap<Integer, HashMap<String, BatterySetting>> batterySettings = new LinkedHashMap<Integer, HashMap<String, BatterySetting>>();

	
	public static boolean initCache() {
		try {
			resetCache();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	public static boolean resetCache() {
		try {
			cacheStates();
			cacheAppSettings();
			cacheAlarms();
			cacheSettings();
			cacheBatterySettings();
			log.debug("STOP RESET CACHE ");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static void cacheAlarms() {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * from alarms order by id_state, id";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	alarms.clear();	
	    	
	    	while (rs.next()) {
	    		Alarm alarm = new Alarm();
	    		alarm.setId(rs.getInt("id"));
	    		alarm.setId_state(rs.getInt("id_state"));
	    		alarm.setName(rs.getString("name"));
	    		alarm.setCode(rs.getString("code"));
	    		alarm.setValue(rs.getString("value"));
	    		alarm.setOperand(rs.getString("operand"));
	    		alarm.setPrevious(rs.getString("previous"));
	    		alarm.setMessage(rs.getString("message"));
	    		alarm.setMessage_short(rs.getString("message_short"));
	    		alarm.setTitle(rs.getString("title"));
	    		alarm.setAction(rs.getString("action"));
	    		alarm.setType(rs.getString("type"));
	    		alarm.setActive(rs.getInt("active"));
	    		alarm.setIcon(rs.getString("icon"));
	    		alarm.setFormat(rs.getString("format"));
	    		alarms.put(rs.getInt("id"), alarm);
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
	
	private static void cacheStates() {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * from states order by id_component, id";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	states.clear();	
	    	statesByCode.clear();
	    	
	    	while (rs.next()) {
	    		State state = new State();
	    		state.setId(rs.getInt("id"));
	    		state.setId_component(rs.getInt("id_component"));
	    		state.setName(rs.getString("name"));
	    		state.setCode(rs.getString("code"));
	    		state.setValues(rs.getString("values"));
	    		state.setPosition(rs.getInt("position"));
	    		state.setType(rs.getString("type"));
	    		state.setActive(rs.getInt("active"));
	    		states.put(rs.getInt("position"), state);
	    		statesByCode.put(rs.getString("code"), state);
	    	}
	    	
		    Constant.STATE_ROW_STATE_VALUE = Cache.statesByCode.get(Constant.STATE_ROW_STATE).getId();
	    	Constant.STATE_PUMP_STATE_VALUE = Cache.statesByCode.get(Constant.STATE_PUMP_STATE).getId();
	    	Constant.STATE_ACCU_TOK_VALUE = Cache.statesByCode.get(Constant.STATE_ACCU_TOK).getId();
	    	Constant.STATE_ACCU_NAPETOST_VALUE = Cache.statesByCode.get(Constant.STATE_ACCU_NAPETOST).getId();
	    	Constant.STATE_ACCU_AH_VALUE = Cache.statesByCode.get(Constant.STATE_ACCU_AH).getId();
	    	Constant.STATE_ACCU_EMPTY_VALUE = Cache.statesByCode.get(Constant.STATE_ACCU_EMPTY).getId();
	    	Constant.STATE_GEO_FIX_VALUE = Cache.statesByCode.get(Constant.STATE_GEO_FIX).getId();
	    	Constant.STATE_ACCU_DISCONNECTED_VALUE = Cache.statesByCode.get(Constant.STATE_ACCU_DISCONNECTED).getId();
	    	Constant.STATE_ANCHOR_VALUE = Cache.statesByCode.get(Constant.STATE_ANCHOR).getId();
	    	
	    	Constant.OBU_SETTINGS_GEO_FENCE_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_GEO_FENCE).getId();
	    	Constant.OBU_SETTINGS_LON_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_LON).getId();
	    	Constant.OBU_SETTINGS_LAT_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_LAT).getId();
	    	Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_GEO_DISTANCE).getId();	    	
	    	Constant.OBU_SETTINGS_ANCHOR_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_ANCHOR).getId();	    	
	    	Constant.OBU_SETTINGS_ANCHOR_DRIFTING_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_ANCHOR_DRIFTING).getId();	    	
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
	
	private static void cacheAppSettings() {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * from app_settings order by id";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	appSettings.clear();	
	    	
	    	while (rs.next()) {
	    		AppSetting appSetting = new AppSetting();
	    		appSetting.setId(rs.getInt("id"));
	    		appSetting.setName(rs.getString("name"));
	    		appSetting.setValue(rs.getString("value"));
	    		appSetting.setType(rs.getString("type"));
	    		appSetting.setDesc(rs.getString("desc"));
	    		appSettings.put(rs.getString("name"), appSetting);
	    	}
	    	
	    	/*
		    Constant.STATE_ROW_STATE_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.STATE_ROW_STATE).getValue());
	    	Constant.STATE_PUMP_STATE_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.STATE_PUMP_STATE).getValue());
	    	Constant.STATE_ACCU_TOK_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.STATE_ACCU_TOK).getValue());
	    	Constant.STATE_ACCU_NAPETOST_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.STATE_ACCU_NAPETOST).getValue());
	    	Constant.STATE_ACCU_AH_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.STATE_ACCU_AH).getValue());
	    	
	    	Constant.OBU_SETTINGS_GEO_FENCE_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_SETTINGS_GEO_FENCE).getValue());
	    	Constant.OBU_SETTINGS_LON_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_SETTINGS_LON).getValue());
	    	Constant.OBU_SETTINGS_LAT_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_SETTINGS_LAT).getValue());
	    	Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_SETTINGS_GEO_DISTANCE).getValue());
	    	*/
	    	Constant.APP_SETTINGS_NAPETOST_TOK_MAX_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTINGS_NAPETOST_TOK_MAX).getValue());
	    	Constant.APP_SETTINGS_NAPETOST_TOK_MIN_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTINGS_NAPETOST_TOK_MIN).getValue());
	    	Constant.APP_SETTINGS_ENERGIJA_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.APP_SETTINGS_ENERGIJA).getValue());
	    	Constant.APP_SETTINGS_NAPETOST_KOEF1_VALUE = Double.parseDouble(Cache.appSettings.get(Constant.APP_SETTINGS_NAPETOST_KOEF1).getValue());
	    	Constant.APP_SETTINGS_NAPETOST_KOEF2_VALUE = Double.parseDouble(Cache.appSettings.get(Constant.APP_SETTINGS_NAPETOST_KOEF2).getValue());
	    	Constant.APP_SETTINGS_NAPETOST_KOEF3_VALUE = Double.parseDouble(Cache.appSettings.get(Constant.APP_SETTINGS_NAPETOST_KOEF3).getValue());
	    	
	    	Constant.OBU_PUMP_STATE_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_PUMP_STATE).getValue());
	    	Constant.OBU_ACCU_NAPETOST_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_ACCU_NAPETOST).getValue());
	    	Constant.OBU_ACCU_TOK_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_ACCU_TOK).getValue());
	    	Constant.OBU_ACCU_AH_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_ACCU_AH).getValue());
	    	Constant.OBU_LAT_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_LAT).getValue());
	    	Constant.OBU_LON_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_LON).getValue());
	    	//Constant.OBU_DATE_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_DATE).getValue());
	    	Constant.OBU_GEO_FIX_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_GEO_FIX).getValue());
	    	
	    	Constant.GEO_FENCE_DISABLED_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.GEO_FENCE_DISABLED).getValue());
	    	Constant.GEO_FENCE_ALARM_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.GEO_FENCE_ALARM).getValue());
	    	Constant.GEO_FENCE_ENABLED_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.GEO_FENCE_ENABLED).getValue());
	    	Constant.GEO_FIX_OK_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.GEO_FIX_OK).getValue());
	    	Constant.BATTERY_EMPTY_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.BATTERY_EMPTY).getValue());
	    	Constant.ACCU_DISCONNECT_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.ACCU_DISCONNECT).getValue());
	    	Constant.PUMP_OK_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.PUMP_OK).getValue());
	    	Constant.PUMP_PUMPING_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.PUMP_PUMPING).getValue());
	    	Constant.PUMP_CLODGED_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.PUMP_CLODGED).getValue());
	    	Constant.PUMP_DEMAGED_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.PUMP_DEMAGED).getValue());
	    	Constant.ANCHOR_ENABLED_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.ANCHOR_ENABLED).getValue());

	
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

	private static void cacheSettings() {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * from settings order by id_component, id";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	settings.clear();	
	    	
	    	while (rs.next()) {
	    		Setting setting = new Setting();
	    		setting.setId(rs.getInt("id"));
	    		setting.setName(rs.getString("code"));
	    		setting.setValue(rs.getString("value"));
	    		setting.setType(rs.getString("type"));
	    		settings.put(rs.getString("code"), setting);
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
	
	private static void cacheComponents() {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * from components order by id";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	components.clear();	
	    	
	    	while (rs.next()) {
	    		Component component = new Component();
	    		component.setId(rs.getInt("id"));
	    		component.setName(rs.getString("name"));
	    		component.setType(rs.getString("type"));
	    		components.put(rs.getInt("id"), component);
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
	
	private static void cacheBatterySettings() {
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select * from battery_settings order by id";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	batterySettings.clear();	
	    	HashMap<String, BatterySetting> batterySetting = new HashMap<String, BatterySetting>();
	    	
	    	int id = -1;
	    	int id_prev = -1;
	    	boolean prvic = true;
	    	
	    	while (rs.next()) {
	    		id = rs.getInt("id");
	    		BatterySetting bs = new BatterySetting();
	    		bs.setId(id);
	    		bs.setKoef(rs.getDouble("koef"));
	    		bs.setPercent(rs.getInt("percent"));
	    		bs.setValue(rs.getString("value"));
	    		bs.setVolt(rs.getDouble("volt"));
	    		
	    		batterySetting.put(rs.getString("value"), bs);
	    		if (id != id_prev) {
	    	    	if (!prvic) {
	    	    		batterySettings.put(id_prev, batterySetting);
	    	    		batterySetting.clear();
	    	    	}
	    	    	id_prev = id;
	    	    	prvic = false;
	    		}
	    	}
	    	
	    	batterySettings.put(id, batterySetting);
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
}
