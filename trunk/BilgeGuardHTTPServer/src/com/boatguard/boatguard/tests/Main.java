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
import com.boatguard.boatguard.objects.ObuSetting;
import com.boatguard.boatguard.objects.ObuState;
import com.boatguard.boatguard.util.Constant;
import com.google.gson.Gson;


public class Main {

	private final String server = "http://93.103.12.155:8080/boatguard/";
	private final String serialObu = "12345";
	private final String idObu = "1";
	
	private final static int bilgePumpState = Constant.PUMP_CLODGED_VALUE; //0-ok;1-pumping;2-clodged;3-dry;4-servis
	private final static int anchorEnabledState = Constant.ANCHOR_ENABLED_VALUE; //1-da;0-NE
	private final static int anchorDriftingState = Constant.ANCHOR_ENABLED_VALUE; //1-da;0-NE
	
	private final static String geoFenceLat = "01402.5046";
	private final static String geoFenceLon = "4626.0058";
	private final static int geoFenceEnabled = Constant.GEO_FENCE_ENABLED_VALUE; //1-da;0-NE
	private final static int geoFenceResult = Constant.GEO_FENCE_ALARM_VALUE; //0-disabled;1-enabled;2-alarm
   
	private static Main main = new Main();
	private static HashMap<Integer,ObuState> obuStates = new HashMap<Integer,ObuState>(){};
	private static HashMap<Integer,ObuAlarm> obuAlarms = new HashMap<Integer,ObuAlarm>(){};
	private static HashMap<Integer,ObuSetting> obuSettings = new HashMap<Integer,ObuSetting>(){};
		
	
    public static void main(String[] args) {
        System.out.println("START!");
        
        main.setSettings(geoFenceEnabled, geoFenceLat, geoFenceLon);
        main.setStates(bilgePumpState, anchorEnabledState==1?"D":"N", anchorDriftingState==1?"D":"N", geoFenceLat, geoFenceLon); 
        main.getObuData();
		System.out.println("*****TEST RESULTS*****");
        main.checkBilgePump();
        main.checkAnchor();
        main.checkGeoFence();
    }

    private void setStates(int pumpState, String anchorState, String anchorDrifting, String lat, String lon) {
		System.out.println("*****TEST START*****");
    	String url = server + "setdata?serial="+serialObu+"&data="+lon+",N,"+lat+",E,1,16,0,"+pumpState+",N,"+anchorState+","+anchorDrifting+",D,0000,000000,03FF,00,00,";
		main.sendPost(url);
    }

    private void setSettings(int geoFenceEnabled, String geoFenceLat, String geoFenceLon) {
		System.out.println("*****SETTINGS START*****");
    	String url = server + "getobusettings?obuid="+idObu;
    	getSettings(main.sendPost(url));
    }

    private void checkBilgePump() {
    	ObuState obuState = obuStates.get(Constant.STATE_PUMP_STATE_VALUE);
    	if (Integer.parseInt(obuState.getValue()) == Constant.PUMP_OK_VALUE) {
    		System.out.println(bilgePumpState+":"+Constant.PUMP_OK);
    	}
    	else if (Integer.parseInt(obuState.getValue()) == Constant.PUMP_PUMPING_VALUE) {
    		System.out.println(bilgePumpState+":"+Constant.PUMP_PUMPING);
    	}
    	else if (Integer.parseInt(obuState.getValue()) == Constant.PUMP_CLODGED_VALUE) {
    		System.out.println(bilgePumpState+":"+Constant.PUMP_CLODGED);
    	}
    	else if (Integer.parseInt(obuState.getValue()) == Constant.PUMP_DEMAGED_VALUE) {
    		System.out.println(bilgePumpState+":"+Constant.PUMP_DEMAGED);
    	}
    	if (Integer.parseInt(obuState.getValue()) == bilgePumpState) {
    		System.out.println("*****BILGE PUMP TEST OK*****");
    	}
    	else {
    		System.out.println("!!!!!!BILGEBILGE PUMP TEST NI OK!!!!!!");    		
    	}
    }

    private void checkAnchor() {
    	ObuState anchorState = obuStates.get(Constant.STATE_ANCHOR_VALUE);
    	if (Integer.parseInt(anchorState.getValue()) == Constant.ANCHOR_ENABLED_VALUE) {
    		System.out.println(anchorEnabledState+":"+Constant.ANCHOR_ENABLED);
    	}
    	else if (Integer.parseInt(anchorState.getValue()) != Constant.ANCHOR_ENABLED_VALUE) {
    		System.out.println(anchorEnabledState+": NOT " + Constant.ANCHOR_ENABLED);
    	}    	
    	
    	ObuState anchorDrifting = obuStates.get(Constant.STATE_ANCHOR_DRIFTING_VALUE);
    	if (Integer.parseInt(anchorState.getValue()) == Constant.ANCHOR_ENABLED_VALUE) {
    		System.out.println(anchorDriftingState+":"+Constant.STATE_ANCHOR_DRIFTING);
    	}
    	else if (Integer.parseInt(anchorState.getValue()) != Constant.ANCHOR_ENABLED_VALUE) {
    		System.out.println(anchorDriftingState+": NOT " + Constant.STATE_ANCHOR_DRIFTING);
    	}
    	
    	if ((Integer.parseInt(anchorState.getValue()) == anchorEnabledState) && 
    		(Integer.parseInt(anchorDrifting.getValue()) == anchorDriftingState)) {
    		System.out.println("*****ANCHOR TEST OK*****");
    	}
    	else {
    		System.out.println("!!!!!!ANCHOR TEST NI OK!!!!!!");    		
    	}
    }

    private void checkGeoFence() {
    	if (Integer.parseInt(obuSettings.get(Constant.STATE_GEO_FENCE_VALUE).getValue()) == Constant.GEO_FENCE_ENABLED_VALUE) {
    		System.out.println("GEO FENCE SETTING: "+Constant.GEO_FENCE_ENABLED);   		
    	}
    	else {
    		System.out.println("GEO FENCE SETTING: "+Constant.GEO_FENCE_DISABLED);    		
    	}
    	
    	ObuState geoFenceState = obuStates.get(Constant.STATE_GEO_FENCE_VALUE);
    	if (Integer.parseInt(geoFenceState.getValue()) == Constant.GEO_FENCE_ENABLED_VALUE) {
    		System.out.println(geoFenceEnabled+":"+Constant.GEO_FENCE_ENABLED);
    	}
    	else if (Integer.parseInt(geoFenceState.getValue()) == Constant.GEO_FENCE_DISABLED_VALUE) {
    		System.out.println(geoFenceEnabled+": " + Constant.GEO_FENCE_DISABLED);
    	}
    	else if (Integer.parseInt(geoFenceState.getValue()) == Constant.GEO_FENCE_ALARM_VALUE) {
    		System.out.println(geoFenceEnabled+": " + Constant.GEO_FENCE_ALARM);
    	}
    	
    	
    	
    	if (Integer.parseInt(geoFenceState.getValue()) == geoFenceResult) {
    		System.out.println("*****GEO FENCE TEST OK*****");
    	}
    	else {
    		System.out.println("!!!!!!GEO FENCE  TEST NI OK!!!!!!");    		
    	}
    }
    
    private void getObuData() {
    	String url = server + "getdata?obuid="+idObu;
    	getData(main.sendPost(url));
    }    
    
    private void getData(String res) {
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

    private void getSettings(String res) {
		try {
			Gson gson = new Gson();	
            JSONArray jsonObuSettings = (JSONArray)new JSONTokener(res).nextValue();
	   		obuSettings.clear();
	   		for (int i=0; i< jsonObuSettings.length(); i++) {
	   			ObuSetting obuSetting = gson.fromJson(jsonObuSettings.get(i).toString(), ObuSetting.class);
	   			//System.out.println(obuSetting.toString());
	   			obuSettings.put(obuSetting.getId_setting(), obuSetting);
	   		}  	        
	   		
        } catch (Exception e) {
    	   	e.printStackTrace();
        	e.getLocalizedMessage();
	   	}
    }
   
	private String sendPost(String url) {
    	try {
		 
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			//add reuqest header
			con.setRequestMethod("POST");
			
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
			System.out.println("Response : " + response.toString());
			return response.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	} 
}
