//TMS-SW: Traffic Management System - Software
//Copyright (C) 2004-2005, Asobi d.o.o. (www.asobi.si).
//All rights reserved.
package si.noemus.bilgeguard;

/*
 * Created on 2004.11.12
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author MarkoDudic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.servlet.http.HttpServlet;

public class InitServlet extends HttpServlet {

  static Connection con   = null;

  private String driver;
  private String url;
  private String user;
  private String pass;
/*
  public void init(ServletConfig conf) throws ServletException {
	  System.out.println("INIT servlet");
		try {
			super.init(conf);
			// setting DB pooling manager
			
		    driver = getServletContext().getInitParameter("driver");
		    url = getServletContext().getInitParameter("conn");
		    user = getServletContext().getInitParameter("user");
		    pass = getServletContext().getInitParameter("pass");
			System.out.println("driver="+driver);
			System.out.println("url="+url);
			System.out.println("user="+user);
			System.out.println("pass="+pass);

			DbPoolingConfig cfg = new DbPoolingConfig();
			cfg.driver=driver;
			cfg.username=user;
			cfg.password=pass;
			cfg.url=url;

			cfg.minPoolSize = "2";
			cfg.acquireIncrement = "2";
			cfg.maxPoolSize = "10";
			cfg.preferredTestQuery = "select * from enote";
			cfg.maxStatements = "100";

			
		    DbManager.init(cfg);

		} catch (Exception e) {
			e.printStackTrace();
			destroy();
		}
	}
	*/	
  public Connection connectionMake()
  {
    driver = getServletContext().getInitParameter("driver");
    url = getServletContext().getInitParameter("conn");
    user = getServletContext().getInitParameter("user");
    pass = getServletContext().getInitParameter("pass");
    
    try
    {
        //System.out.println( "connectionMake:" + con);
	    if ((con == null) || (con.isClosed()))
	    {
	        //System.out.println("INIT="+ url+" "+user+" "+pass);
	        try { 
				Class.forName(driver);
	        	con = DriverManager.getConnection(url,user,pass);
	        }
	        catch (Exception e) {
	            System.out.println( "Napaka:" + e.toString());
	            e.printStackTrace();
	        }
	    }
    } catch (Exception e)
    {
        System.out.println( "Napaka:" + e.toString());
        e.printStackTrace();
    }
    
    return con;
  }

	public void disableTriggers() {
    	ResultSet rs = null;
    	Statement stmt = null;

	    try {
	    	connectionMake();
			stmt = con.createStatement();   	
	    	rs = stmt.executeQuery("SET @disable_triggers = 1;");
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) {
	    			rs.close();
	    		}
	    		if (stmt != null) {
	    			stmt.close();
	    		}
			} catch (Exception e) {
			}
	    }
	}					

	public void enableTriggers() {
    	ResultSet rs = null;
    	Statement stmt = null;

	    try {
	    	connectionMake();
			stmt = con.createStatement();   	
	    	rs = stmt.executeQuery("SET @disable_triggers = NULL;");
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) {
	    			rs.close();
	    		}
	    		if (stmt != null) {
	    			stmt.close();
	    		}
			} catch (Exception e) {
			}
	    }
	}					
}

