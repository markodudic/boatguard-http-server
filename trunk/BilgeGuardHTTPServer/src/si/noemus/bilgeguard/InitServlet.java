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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;

import si.bisoft.commons.dbpool.DbManager;
import si.bisoft.commons.dbpool.DbPoolingConfig;
import si.noemus.boatguard.dao.AppSetting;
import si.noemus.boatguard.dao.State;
import si.noemus.boatguard.util.Util;

public class InitServlet extends HttpServlet implements javax.servlet.Servlet {

	private static Log log = LogFactory.getLog(InitServlet.class);
  
	public static InitServlet instance;
	public static String realPath = "WebContent/";
	public static Properties mainSettings;

	public InitServlet() {
		super();
		instance = this;
	}  
	
	public void init(ServletConfig conf) throws ServletException {
		log.debug("!LOADING ConfigServlet");
		try {
			super.init(conf);
			
			String realPath = getServletContext().getRealPath("/");
			System.out.println("realPath="+realPath);
			String pf = "WebContent/WEB-INF/";
			if (!new File("WebContent/WEB-INF/log4j.properties").exists()) {
				pf = realPath+"WEB-INF/";
			}
			PropertyConfigurator.configure(pf+"log4j.properties");
			
			
			String df = "WebContent/WEB-INF/";
			if (!new File("WebContent/WEB-INF/config.properties").exists()) {
				df = realPath+"WEB-INF/";
			}
			Properties settings = new Properties();
			settings.load(new FileInputStream(pf + "config.properties"));
			
			/*realPath = getServletContext().getRealPath("/");
			// STD db
			String file = getInitParameter("config-init-file");
			Properties settings = new Properties();
			log.debug(realPath + file);
			if (file != null) {
				settings.load(new FileInputStream(realPath + file));
			}*/
			log.debug("INIT settings");
			log.debug(settings);
			
			DbPoolingConfig cfg = new DbPoolingConfig();
			cfg.driver=settings.getProperty("db.jdbc.driver");
			cfg.url=settings.getProperty("db.jdbc.url");
			cfg.username=settings.getProperty("db.jdbc.username");
			cfg.password=settings.getProperty("db.jdbc.password");
			
			cfg.minPoolSize=settings.getProperty("db.pool.minPoolSize");
			cfg.acquireIncrement=settings.getProperty("db.pool.acquireIncrement");
			cfg.maxPoolSize=settings.getProperty("db.pool.maxPoolSize");
			cfg.maxStatements=settings.getProperty("db.pool.maxStatements");
			
			System.out.println(cfg);
			// setting DB pooling manager
			DbManager.init("config",cfg);
			
			//Cache.initCache();				
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error("Config file ne obstaja!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}     

	
}

