package si.bisoft.commons.dbpool;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PooledDataSource;
 
public class C3P0DbManager extends DbManager {
	private static Log log = LogFactory.getLog(C3P0DbManager.class); 
	
	private DataSource ds = null;
	private DbPoolingConfig cnf = null;
	
	public C3P0DbManager(DbPoolingConfig cnf) throws IOException {
		this.cnf = cnf;
		init();
	}
	
	private void init() throws IOException {
		try {

			Class.forName(cnf.driver);
			// the settings below are optional -- c3p0 can work with defaults
			Map<String,String> conf = new HashMap<String,String>();
			conf.put("minPoolSize", cnf.minPoolSize);
			conf.put("acquireIncrement", cnf.acquireIncrement);
			conf.put("maxPoolSize", cnf.maxPoolSize);
			conf.put("preferredTestQuery", "SELECT 1");
			
			// AUTO RECONNECT
			// preverjeno
			conf.put("acquireRetryDelay", "1000");
			conf.put("acquireRetryAttempts", "30");
			conf.put("breakAfterAcquireFailure", "false");
			// windows ???

			
			// connection timeout
			conf.put("maxConnectionAge","36000");
			conf.put("maxIdleTime","14400");
			conf.put("maxIdleTimeExcessConnections","0");
			conf.put("propertyCycle","1000");
			conf.put("unreturnedConnectionTimeout","24000");
			// reconnecting and connection testing
			conf.put("automaticTestTable", null);
			conf.put("preferredTestQuery", "SELECT 1");
			//conf.put("connectionCustomizerClassName", null);
			//conf.put("connectionTesterClassName","com.mchange.v2.c3p0.impl.DefaultConnectionTester");
			conf.put("forceIgnoreUnresolvedTransactions","false");
			conf.put("testConnectionOnCheckin","true");
			conf.put("testConnectionOnCheckout","false");
			conf.put("usesTraditionalReflectiveProxies","false");
			conf.put("idleConnectionTestPeriod", "240");
			conf.put("NumHelperThreads", "6");
			conf.put("CheckoutTimeout", "0");   // koliko časa čaka getConnection - 0 neskončno
			
	        String url = cnf.url; // a JDBC url
	        String username = cnf.username;
	        String password = cnf.password;
	        
	       // log.info(url);
	        
			DataSource ds_unpooled = DataSources.unpooledDataSource(url, username, password);
			ds = DataSources.pooledDataSource( ds_unpooled, conf );
			
			Connection con = ds.getConnection();
			
			log.debug(con);
			con.close();
			
		//	log.debug("DB pool started.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException("Problem pri povezavi na bazo.");
		}
	}
	
	public Connection getConnection() throws Exception {
		if (ds==null) {
			try {
				init();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ds.getConnection();
	}
	
	public void destroy() throws SQLException
	{
		try
		{
		// do all kinds of stuff with that sweet pooled DataSource...
		}
		finally
		{
		DataSources.destroy( ds );
		}
	}
	
	public String toString() {
		//		 make sure it's a c3p0 PooledDataSource
		try {
			if ( ds instanceof PooledDataSource)
			{
			PooledDataSource pds = (PooledDataSource) ds;
			//System.out.println("num_connections: " + pds.getNumConnectionsDefaultUser());
			//System.err.println("num_busy_connections: " + pds.getNumBusyConnectionsDefaultUser());
			//System.err.println("num_idle_connections: " + pds.getNumIdleConnectionsDefaultUser());
			//System.err.println();
			return "num_connections: " + pds.getNumConnectionsDefaultUser()+"  "+ds.toString();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ds.toString();
	}

}
