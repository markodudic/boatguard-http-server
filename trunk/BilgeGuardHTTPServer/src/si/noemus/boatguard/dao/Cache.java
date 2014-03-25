package si.noemus.boatguard.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import si.bisoft.commons.dbpool.DbManager;

public class Cache {
	private static Log log = LogFactory.getLog(Cache.class); 
	public static Map<Integer, Alarm> alarms = new HashMap<Integer, Alarm>();
	public static Map<Integer, State> states = new HashMap<Integer, State>();
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
	    		alarm.setValue(rs.getString("value"));
	    		alarm.setOperand(rs.getString("operand"));
	    		alarm.setMessage(rs.getString("message"));
	    		alarm.setType(rs.getString("type"));
	    		alarms.put(rs.getInt("id"), alarm);
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
	    	
	    	while (rs.next()) {
	    		State state = new State();
	    		state.setId(rs.getInt("id"));
	    		state.setId_component(rs.getInt("id_component"));
	    		state.setName(rs.getString("name"));
	    		state.setValues(rs.getString("values"));
	    		state.setPosition(rs.getInt("position"));
	    		state.setType(rs.getString("type"));
	    		states.put(rs.getInt("position"), state);
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
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
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
			} catch (Exception e) {}
	    }
		
	}	
}
