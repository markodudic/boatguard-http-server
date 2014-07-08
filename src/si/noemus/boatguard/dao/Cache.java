package si.noemus.boatguard.dao;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import si.bisoft.commons.dbpool.DbManager;
import si.noemus.boatguard.objects.Alarm;
import si.noemus.boatguard.objects.AppSetting;
import si.noemus.boatguard.objects.Component;
import si.noemus.boatguard.objects.Setting;
import si.noemus.boatguard.objects.State;
import si.noemus.boatguard.util.Constant;

public class Cache {
	private static Log log = LogFactory.getLog(Cache.class); 
	public static Map<Integer, Alarm> alarms = new HashMap<Integer, Alarm>();
	public static Map<Integer, State> states = new HashMap<Integer, State>();
	public static Map<String, State> statesByCode = new HashMap<String, State>();
	public static Map<String, AppSetting> appSettings = new HashMap<String, AppSetting>();
	public static Map<Integer, Setting> settings = new HashMap<Integer, Setting>();
	public static Map<Integer, Component> components = new HashMap<Integer, Component>();

	
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

	    	String	sql = "select * from alarms";
	    		
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

	    	String	sql = "select * from states";
	    		
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
	    	
	    	Constant.OBU_SETTINGS_GEO_FENCE_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_GEO_FENCE).getId();
	    	Constant.OBU_SETTINGS_LON_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_LON).getId();
	    	Constant.OBU_SETTINGS_LAT_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_LAT).getId();
	    	Constant.OBU_SETTINGS_GEO_DISTANCE_VALUE = Cache.statesByCode.get(Constant.OBU_SETTINGS_GEO_DISTANCE).getId();	    	
	
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

	    	String	sql = "select * from app_settings";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	appSettings.clear();	
	    	
	    	while (rs.next()) {
	    		AppSetting appSetting = new AppSetting();
	    		appSetting.setId(rs.getInt("id"));
	    		appSetting.setName(rs.getString("name"));
	    		appSetting.setValue(rs.getString("value"));
	    		appSetting.setType(rs.getString("type"));
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
	    	
	    	Constant.OBU_PUMP_STATE_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_PUMP_STATE).getValue());
	    	Constant.OBU_ACCU_NAPETOST_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_ACCU_NAPETOST).getValue());
	    	Constant.OBU_ACCU_TOK_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_ACCU_TOK).getValue());
	    	Constant.OBU_ACCU_AH_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_ACCU_AH).getValue());
	    	Constant.OBU_LAT_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_LAT).getValue());
	    	Constant.OBU_LON_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_LON).getValue());
	    	//Constant.OBU_DATE_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.OBU_DATE).getValue());
	    	
	    	Constant.GEO_FENCE_DISABLED_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.GEO_FENCE_DISABLED).getValue());
	    	Constant.GEO_FENCE_ALARM_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.GEO_FENCE_ALARM).getValue());
	    	Constant.GEO_FENCE_ENABLED_VALUE = Integer.parseInt(Cache.appSettings.get(Constant.GEO_FENCE_ENABLED).getValue());

	
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

	    	String	sql = "select * from settings";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	settings.clear();	
	    	
	    	while (rs.next()) {
	    		Setting setting = new Setting();
	    		setting.setId(rs.getInt("id"));
	    		setting.setName(rs.getString("name"));
	    		setting.setValue(rs.getString("value"));
	    		setting.setType(rs.getString("type"));
	    		settings.put(rs.getInt("id"), setting);
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

	    	String	sql = "select * from components";
	    		
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
}
