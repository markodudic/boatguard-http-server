package com.boatguard.boatguard.servlet;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.bisoft.commons.dbpool.DbManager;
import com.boatguard.boatguard.dao.ObuData;
import com.boatguard.boatguard.objects.ObuSetting;


public class CopyOldDataServlet extends HttpServlet {

	static Logger log = Logger.getLogger(CopyOldDataServlet.class.getName());
    private String scheduler_pattern;
    private Timestamp lastDate;

	public void init() throws ServletException
	{
        scheduler_pattern = (String) getServletConfig().getInitParameter("scheduler_pattern");
        
        Scheduler s = new Scheduler();
	  	  s.schedule(scheduler_pattern, new Runnable() {
	  		  public void run() {
	  			scheduleRun();
	  	  	}
	  	  });
	  	  s.start();            
	}

    public void scheduleRun() {
    	Calendar runTime = Calendar.getInstance();
	    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
	    SimpleDateFormat dfTime = new SimpleDateFormat("kk:mm:ss");
		SimpleDateFormat dfYear=new SimpleDateFormat("yyyy");
		//System.out.println("Copy old data start at: " + df.format(runTime.getTime()));
		
		
		Connection con = null;
		ResultSet rs = null;
	    Statement stmt = null;
	    Map<Integer, ObuSetting> obuSettings = new HashMap<Integer, ObuSetting>();
    	try {
    		con = DbManager.getConnection("config");

	    	String	sql = "select message_date, text "
	    			+ "from smsserver_in "
	    			+ "where text like '#BG:%'"
	    			+ "order by message_date desc "
	    			+ "limit 1";
	    		
    		stmt = con.createStatement();   	
	    	rs = stmt.executeQuery(sql);
    		
	    	//STARO: #BG:0,15F519,2C8,00A,1426.626404,4601.989670,20140707125241
	    	//NOVO:  1234.1222,12345.1111,1,0,0,1,0,0,2F50,1A1B1C,2F50,00,00
	    	
	    	if (rs.next()) {
	    		String data = rs.getString("text").replace("#BG:", "");
	    		SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	            Date date = sdfSource.parse(rs.getString("message_date"));
	            Timestamp tsDS = new java.sql.Timestamp(date.getTime());
	            
	            if (lastDate == null) lastDate = tsDS;
	            	
	            if (tsDS.after(lastDate)) {
		            /*SimpleDateFormat sdfDestination = new SimpleDateFormat("yyyyMMddHHmmss");
		            String strDate = sdfDestination.format(date);
		            String newData = data.substring(0, data.lastIndexOf(",")+1) + strDate;*/
		    		String [] dataList = data.split(",");
		    		String newData = dataList[4]+","+dataList[5]+",1,"+dataList[0]+",0,0,0,0,"+dataList[2]+","+dataList[1]+","+dataList[3];
		    		System.out.println("copy old data="+newData);
		    		
		    		ObuData obuData = new ObuData();
		    		boolean isAdd = obuData.setData(null, "123456", newData);
		    		if (isAdd) {
		    			obuData.calculateAlarms(null, "123456");
		    		}
		    		lastDate = tsDS;
	    		}
	    	}
	
	    } catch (Exception theException) {
	    	theException.printStackTrace();
	    } finally {
	    	try {
	    		if (rs != null) rs.close();
	    		if (stmt != null) stmt.close();
	    		if (con != null) con.close();
			} catch (Exception e) {}
	    }	
		
		
		
		
    	runTime = Calendar.getInstance();
		//System.out.println("Copy old data end at: " + df.format(runTime.getTime()));
		
    }	
		
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET GET");		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET POST");		
   
	
	}	
	

	
	
}
