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

import org.json.simple.JSONObject;

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
	
	public JSONObject getLocation(String user) {
    	ResultSet rs = null;
	    Statement stmt = null;
    	//JSONArray results = new JSONArray();
	    JSONObject current = new JSONObject();
	    try {
	    	connectionMake();

	    	String	sql = "select date_format(message_date, '%d.%m.%Y %k:%i:%s') as date, text, x_geo_fence, y_geo_fence, radius, active " +
						"from smsserver_in left join (select obu, x_geo_fence, y_geo_fence, radius, active " +
						"								from users " +
						"								where name='"+user+"') as user " +
						"on (originator = obu) " +
						"where obu is not null and text like '#bg:%' " +
						"order by message_date desc " +
						"limit 1";
	    		
    		System.out.println("sql="+sql);
	    	stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
	    	while (rs.next()) {
	    		current.put("date", rs.getString("date"));
	    	
	    		/*
	    		#BG:00,02F3,01F3,00E,15.1,46.5,20130523181121.000
	    		1.-UVOD
	    		2.-STANJE PUMPE (00-NE PUMPA, 01-PUMPA, 10-ZAMA�ENA)
	    		3.-PRETE�ENE As
	    		4.-STANJE NAPETOSTI
	    		5.-TRENUTNI TOK( ENOTE �E NE VEM)
	    		6.-LONGITUDE
	    		7.-LATITUDE
	    		8.-UTC TIME
	    		*/
	    		String data = rs.getString("text").split(":")[1];
	    		String[] dataA = data.split(",");
	    		current.put("pumpa", dataA[0]);
	    		current.put("baterija_as", dataA[1]);
	    		current.put("baterija_napetost", dataA[2]);
	    		current.put("baterija_tok", dataA[3]);
	    		current.put("lon", dataA[4]);
	    		current.put("lat", dataA[5]);
	    		
	    		if (rs.getString("active").equals("1")) {
		    		float lat1 = transform(Float.parseFloat(dataA[4]));
		    		float lon1 = transform(Float.parseFloat(dataA[5]));
		    		float lat2 = transform(Float.parseFloat(rs.getString("x_geo_fence")));
		    		float lon2 = transform(Float.parseFloat(rs.getString("y_geo_fence")));
		    		int radius = Integer.parseInt(rs.getString("radius"));
		    		double distance = gps2m(lat1, lon1, lat2, lon2);
		    		if (distance <= radius) {
			    		current.put("geofence", "1"); //home
		    		} else {
			    		current.put("geofence", "2"); //alarm
		    		}
	    		} else {
		    		current.put("geofence", "0"); //ni vklopljen
	    		}
	    		
	    	}
	    	
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

	    return current;
	}
	
	
	private double gps2m(float lat_a, float lng_a, float lat_b, float lng_b) {
		float pk = (float) (180/3.14169);
		float a1 = lat_a / pk;
		float a2 = lng_a / pk;
		float b1 = lat_b / pk;
		float b2 = lng_b / pk;
		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
		double t3 = Math.sin(a1) * Math.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);
		System.out.println(6366000*tt);
		return 6366000*tt;
	}	
	
	private float transform(float x) {
		double x_ = Math.floor(x/100);
		double x__ = (x/100 - x_)/0.6;
		x = (float) (x_ + x__);
		return x;
	}
}

