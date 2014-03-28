package si.bisoft.commons.dbpool;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;


 
public class DbPoolingConfig {

	public String driver;
	public String url;
	public String username;
	public String password;
	
	public String minPoolSize;
	public String acquireIncrement;
	public String maxPoolSize;
	public String preferredTestQuery;
	public String maxStatements;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public String toString() {
		return "{url:"+url+",username:"+username+",password:"+password+",driver:"+driver
		+",minPoolSize:"+minPoolSize+",acquireIncrement:"+acquireIncrement+",maxPoolSize:"+maxPoolSize+",maxStatements:"+maxStatements+",preferredTestQuery:"+preferredTestQuery+",}";
	}
	
	public static void config(String driver, String url, String username, String password) throws Exception {
		DbPoolingConfig cfg = new DbPoolingConfig();
		cfg.driver=driver;
		cfg.url=url;
		cfg.username=username;
		cfg.password=password;
		
		cfg.minPoolSize="2";
		cfg.acquireIncrement="5";
		cfg.maxPoolSize="50";
		cfg.maxStatements="180";

		///System.out.println(cfg);
		// setting DB pooling manager
		DbManager.init("config",cfg);
		
	}
	
	public static void config() throws Exception {
		Properties settings = new Properties();
		settings.load(new FileInputStream("WebContent/WEB-INF/config.properties"));

		DbPoolingConfig cfg = new DbPoolingConfig();
		cfg.driver=settings.getProperty("db.jdbc.driver");
		cfg.url=settings.getProperty("db.jdbc.url");
		cfg.username=settings.getProperty("db.jdbc.username");
		cfg.password=settings.getProperty("db.jdbc.password");
		
		cfg.minPoolSize=settings.getProperty("db.pool.minPoolSize");
		cfg.acquireIncrement=settings.getProperty("db.pool.acquireIncrement");
		cfg.maxPoolSize=settings.getProperty("db.pool.maxPoolSize");
		cfg.preferredTestQuery=settings.getProperty("db.pool.preferredTestQuery");
		cfg.maxStatements=settings.getProperty("db.pool.maxStatements");

		//System.out.println(cfg);
		// setting DB pooling manager
		DbManager.init("config",cfg);
		
	}
	
	public static Connection getConnection() throws Exception {
		if (DbManager.dbManagers.get("config") == null) {
			config();
		}
		return DbManager.getConnection("config");
	}
	
	public static Connection getConnection(String driver, String url, String username, String password) throws Exception {
		config(driver, url, username, password);
		return DbManager.getConnection("config");
	}

}
