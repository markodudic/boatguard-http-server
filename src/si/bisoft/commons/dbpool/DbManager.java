package si.bisoft.commons.dbpool;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
public abstract class DbManager {
	private static Log log = LogFactory.getLog(DbManager.class); 
	/**
	 * @param args
	 */
	public static Map<String,DbManager> dbManagers = new HashMap<String,DbManager>(); 
	public static Map<String,DbPoolingConfig> dbConfigs = new HashMap<String,DbPoolingConfig>(); 
	
	public static void init(String name, DbPoolingConfig cnf) throws Exception {
		if (cnf.driver.indexOf("oracle") != -1) {
			//dbManager = new DbcpDbManager(cnf);
		} else {
			//log.debug(name);
			log.debug(cnf);
			//BoneCpManager cdbm = new BoneCpManager(cnf);
			C3P0DbManager cdbm = new C3P0DbManager(cnf);
			//log.debug(cdbm);
			dbConfigs.put(name, cnf);
			dbManagers.put(name, cdbm);
			//log.debug(dbManagers);
		}
        //log.debug(dbManagers);
		log.debug("DB pool started.");
	}
	private static void reinit(String name) throws Exception {
		DbPoolingConfig cnf = dbConfigs.get(name);
		if (cnf.driver.indexOf("oracle") != -1) {
			//dbManager = new DbcpDbManager(cnf);
		} else {
			//log.debug(name);
			//log.debug(cnf);
			C3P0DbManager cdbm = new C3P0DbManager(cnf);
			//log.debug(cdbm);
			dbManagers.put(name, cdbm);
			//log.debug(dbManagers);
		}
        //log.debug(dbManagers);
		log.debug("DB pool restarted.");
	}
	
	/*public static DataSource getDS(String ds) {
		//log.debug(dbManagers.get(ds));
		DataSource rds = null;
		for (int i = 0; i < 20; i++) {
			Connection con = null;
			try {
				rds = dbManagers.get(ds).getDS();
				log.debug(rds);
				con = rds.getConnection();
				if (!con.isClosed()) { break; }
			} catch (Exception e) {
			} finally {
				try {if (con!= null) con.close();} catch (SQLException e) {}
			}
			try {
				Thread.sleep(500);
			} catch (Exception e) {}
		}
		return rds;
	}*/
	public static Connection getConnection(String ds) throws SQLException {
		//log.debug(dbManagers.get(ds));
		Connection con = null;
		try {
			if (dbManagers==null) {
				log.info("RECREATE DS");
				dbManagers = new HashMap<String,DbManager>();
			}
			if (dbManagers.get(ds)==null) {
				log.info("REINIT DS"+dbConfigs.get(ds));
				reinit(ds);
			}
			con = dbManagers.get(ds).getConnection();
			return con;
		} catch (Throwable e) {
			log.error(e);
			try {if (con!= null) con.close();} catch (SQLException e1) {}
		}

		throw new SQLException("Napaka pri poolingu");
	}
	
	public static void autocommitOff(Connection con) {
		try {
			con.setAutoCommit(false);
		} catch (SQLException e) {e.printStackTrace();}
	}
	public static void commit(Connection con) {
		try {
			con.commit();
		} catch (SQLException e) {e.printStackTrace();}
		try {
			con.setAutoCommit(true);
		} catch (SQLException e) {e.printStackTrace();}
		
	}
	
	public abstract Connection getConnection() throws Exception ;
	public abstract void destroy() throws SQLException;
}
