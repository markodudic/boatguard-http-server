package si.noemus.boatguard.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.bisoft.commons.dbpool.DbManager;
import si.noemus.boatguard.dao.ObuData;
import si.noemus.boatguard.objects.ObuSetting;
import it.sauronsoftware.cron4j.Scheduler;


public class CopyOldDataServlet extends HttpServlet {

	static Logger log = Logger.getLogger(CopyOldDataServlet.class.getName());
    private String scheduler_pattern;

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
		System.out.println("Copy old data start at: " + df.format(runTime.getTime()));
		
		
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
    		
	    	if (rs.next()) {
	    		String data = rs.getString("text").replace("#BG:", "");
	    		System.out.println("Old data: " + data);
	    		ObuData obuData = new ObuData();
	    		int obuid = obuData.setData(null, "123456", data);
	    		obuData.calculateAlarms(obuid);
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
		System.out.println("Copy old data end at: " + df.format(runTime.getTime()));
		
    }	
		
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET GET");		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SERVLET POST");		
   
	
	}	
	

	
	
}
