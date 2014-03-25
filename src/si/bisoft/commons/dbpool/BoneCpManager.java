package si.bisoft.commons.dbpool;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
 
public class BoneCpManager extends DbManager {
	private static Log log = LogFactory.getLog(BoneCpManager.class); 
	
	private BoneCP ds = null;
	private DbPoolingConfig cnf = null;
	
	public BoneCpManager(DbPoolingConfig cnf) throws IOException {
		this.cnf = cnf;
		init();
	}
	
	private void init() throws IOException {
		try {
			
			Class.forName(cnf.driver);
	        BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(cnf.url); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
			config.setUsername(cnf.username); 
			config.setPassword(cnf.password);
			config.setLogStatementsEnabled(false);
			config.setMinConnectionsPerPartition(Integer.parseInt(cnf.minPoolSize));
			config.setMaxConnectionsPerPartition(Integer.parseInt(cnf.maxPoolSize));
			config.setAcquireIncrement(Integer.parseInt(cnf.acquireIncrement));
			config.setPartitionCount(1);
			//config.setIdleMaxAgeInMinutes(720);
			ds = new BoneCP(config); // setup the connection pool

			Connection con = ds.getConnection();
			
			log.debug(con);
			con.close();
			
			/*log.debug(ds.getConfig().getMaxConnectionAge());
			log.debug(ds.getConfig().getIdleMaxAge());
			log.debug(ds.getConfig().getIdleMaxAgeInMinutes());*/
			
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
		ds.close();
		}
	}
	
	public String toString() {
		return ds.toString();
	}

}
