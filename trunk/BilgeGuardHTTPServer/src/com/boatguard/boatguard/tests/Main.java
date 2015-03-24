package com.boatguard.boatguard.tests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.boatguard.boatguard.objects.ObuAlarm;
import com.boatguard.boatguard.objects.ObuState;
import com.boatguard.boatguard.util.Constant;
import com.google.gson.Gson;


public class Main {

	private final String USER_AGENT = "Mozilla/5.0";
	private String server = "http://93.103.12.155:8080/boatguard/";
	private String serialObu = "12345";
	private String idObu = "1";
	static Main main = new Main();
	public static HashMap<Integer,ObuState> obuStates = new HashMap<Integer,ObuState>(){};
	public static HashMap<Integer,ObuAlarm> obuAlarms = new HashMap<Integer,ObuAlarm>(){};
	
	private static int bilgePumpState = Constant.PUMP_PUMPING_VALUE;
    
    public static void main(String[] args) {
        System.out.println("START!");
        main.setBilgePump(bilgePumpState); //0-ok;1-pumping;2-clodged;3-dry;4-servis
        
        main.getObuData();
        
        main.checkBilgePump();
    }
    
	private String sendPost(String url) {
    	try {
		 
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			//add reuqest header
			con.setRequestMethod("POST");
			//con.setRequestProperty("User-Agent", USER_AGENT);
			//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	 
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			//wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
	 
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	 
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			//print result
			System.out.println(response.toString());
			return response.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	} 
	
    private void setBilgePump(int state) {
		System.out.println("*****BILGE PUMP TEST*****");
    	String url = server + "setdata?serial="+serialObu+"&data=4519.2591,N,01333.1484,E,1,16,0,"+state+",N,D,N,D,0000,000000,03FF,00,00,";
		main.sendPost(url);
    }
    
    private void checkBilgePump() {
    	ObuState obuState = obuStates.get(Constant.STATE_PUMP_STATE_VALUE);
    	if (Integer.parseInt(obuState.getValue()) == Constant.PUMP_OK_VALUE) {
    		System.out.println(Constant.PUMP_OK);
    	}
    	else if (Integer.parseInt(obuState.getValue()) == Constant.PUMP_PUMPING_VALUE) {
    		System.out.println(Constant.PUMP_PUMPING);
    	}
    	else if (Integer.parseInt(obuState.getValue()) == Constant.PUMP_CLODGED_VALUE) {
    		System.out.println(Constant.PUMP_CLODGED);
    	}
    	else if (Integer.parseInt(obuState.getValue()) == Constant.PUMP_DEMAGED_VALUE) {
    		System.out.println(Constant.PUMP_DEMAGED);
    	}
    	if (Integer.parseInt(obuState.getValue()) == bilgePumpState) {
    		System.out.println("*****BILGE PUMP TEST OK*****");
    	}
    	else {
    		System.out.println("*****BILGEBILGE PUMP TEST NI OK*****");    		
    	}
    }

    private void getObuData() {
    	String url = server + "getdata?obuid="+idObu;
    	getData(main.sendPost(url));
    }    
    
    public void getData(String res) {
		try {
			Gson gson = new Gson();	
			JSONObject jRes = (JSONObject)new JSONTokener(res).nextValue();
	   		JSONArray jsonStates = (JSONArray)jRes.get("states");
    	   	obuStates.clear();
    	   	for (int i=0; i< jsonStates.length(); i++) {
	   			ObuState obuState = gson.fromJson(jsonStates.get(i).toString(), ObuState.class);
	   			obuStates.put(obuState.getId_state(), obuState);
	   		}
    	   	
    	   	JSONArray jsonAlarms = (JSONArray)jRes.get("alarms");
    	   	obuAlarms.clear();
    	   	for (int i=0; i< jsonAlarms.length(); i++) {
	   			ObuAlarm obuAlarm = gson.fromJson(jsonAlarms.get(i).toString(), ObuAlarm.class);
	   			obuAlarms.put(obuAlarm.getId_alarm(), obuAlarm);
	   		}
	   		
        } catch (Exception e) {
    	   	e.printStackTrace();
        	e.getLocalizedMessage();
	   	}
    }

}
