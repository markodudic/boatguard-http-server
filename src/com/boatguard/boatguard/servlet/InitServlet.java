//TMS-SW: Traffic Management System - Software
//Copyright (C) 2004-2005, Asobi d.o.o. (www.asobi.si).
//All rights reserved.
package com.boatguard.boatguard.servlet;

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
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import si.bisoft.commons.dbpool.DbManager;
import si.bisoft.commons.dbpool.DbPoolingConfig;

import com.boatguard.boatguard.comm.MailClient;
import com.boatguard.boatguard.dao.Cache;
import com.boatguard.boatguard.util.HttpLog;

public class InitServlet extends HttpServlet implements javax.servlet.Servlet {

	Locale locale = Locale.getDefault();
	private static Log log = LogFactory.getLog(InitServlet.class);
	
	protected static int outputIndx = 0;
  
	public static String realPath = "WebContent/";
	//public static Properties mainSettings;
	//public static MailClient mailClient;
	private static String sessionID;
	
	public void init(ServletConfig conf) throws ServletException {
		try {
			super.init(conf);
			
			String realPath = getServletContext().getRealPath("/");
			String pf = "WebContent/";
			//String log4jFile = getInitParameter("config-log4j-file");
			String log4jFile = getServletContext().getInitParameter("config-log4j-file");
			
			if (!new File("WebContent/"+log4jFile).exists()) {
				pf = realPath;
			}
			PropertyConfigurator.configure(pf+log4jFile);
			
			
			String df = "WebContent/";
			//String configFile = getInitParameter("config-init-file");
			String configFile = getServletContext().getInitParameter("config-init-file");
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
			
			sessionID=settings.getProperty("session.id");			
					
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("InitServlet POST:"+request.getSession().getId());		
		HttpLog.afterHttp(request, null);

		String sessionid = (String) request.getParameter("sessionid");
		System.out.println("sessionid="+sessionid);
		
		if ((sessionid != null) && (!sessionid.equals(sessionID)) && (!sessionid.equals(request.getSession().getId()))) {
			//login
			String data = "{\"error\":1,\"error_desc\":\"NOT LOGGED\"}";
			
	    	OutputStream out = null;
	    	response.setContentType("text/plain");
			response.setHeader("Content-disposition", null);
			response.setHeader("Access-Control-Allow-Origin", "*");
			out = response.getOutputStream();
			out.write(data.getBytes());
			out.flush();
			out.close();    	
			
		}
	}
	
}

