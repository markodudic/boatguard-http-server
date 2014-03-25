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
	public static Map<Integer, State> states = new HashMap<Integer, State>();
	public static Map<String, AppSetting> appSettings = new HashMap<String, AppSetting>();

	
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
			log.debug("STOP RESET CACHE ");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
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


}
