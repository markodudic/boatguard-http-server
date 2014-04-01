//TMS-SW: Traffic Management System - Software
//Copyright (C) 2004-2005, Asobi d.o.o. (www.asobi.si).
//All rights reserved.
package si.noemus.boatguard.servlet;

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
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import si.bisoft.commons.dbpool.DbManager;
import si.bisoft.commons.dbpool.DbPoolingConfig;
import si.noemus.boatguard.comm.MailClient;
import si.noemus.boatguard.dao.Cache;

public class InitServlet extends HttpServlet implements javax.servlet.Servlet {

	private static Log log = LogFactory.getLog(InitServlet.class);
  
	public static String realPath = "WebContent/";
	public static Properties mainSettings;
	public static MailClient mailClient;

	
	public void init(ServletConfig conf) throws ServletException {
		try {
			super.init(conf);
			
			String realPath = getServletContext().getRealPath("/");
			String pf = "WebContent/";
			String log4jFile = getInitParameter("config-log4j-file");
			if (!new File("WebContent/"+log4jFile).exists()) {
				pf = realPath;
			}
			PropertyConfigurator.configure(pf+log4jFile);
			
			
			String df = "WebContent/";
			String configFile = getInitParameter("config-init-file");
			if (!new File("WebContent/"+configFile).exists()) {
				df = realPath;
			}
			Properties settings = new Properties();
			settings.load(new FileInputStream(pf + configFile));
			
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

			MailClient mailClient = new MailClient();
			mailClient.smtpServer=settings.getProperty("mail.smtpServer");
			mailClient.user=settings.getProperty("mail.user");
			mailClient.pass=settings.getProperty("mail.pass");
			mailClient.smtp_auth=settings.getProperty("mail.smtp_auth");
			mailClient.smtp_port=settings.getProperty("mail.smtp_port");
			mailClient.smtp_socketFactory_port=settings.getProperty("mail.smtp_socketFactory_port");
			mailClient.smtp_starttls_enable=settings.getProperty("mail.smtp_starttls_enable");
			mailClient.transport_protocol=settings.getProperty("mail.transport_protocol");
			mailClient.use_ssl=settings.getProperty("mail.use_ssl");
			
					
			System.out.println(cfg);
			// setting DB pooling manager
			DbManager.init("config",cfg);
			
			Cache.initCache();				
			
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

