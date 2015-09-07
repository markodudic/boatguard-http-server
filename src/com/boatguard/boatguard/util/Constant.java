package com.boatguard.boatguard.util;

import java.text.SimpleDateFormat;

public class Constant {

	//STATE ID
	public static final String STATE_ROW_STATE 					= "ROW_STATE";
	public static Integer STATE_ROW_STATE_VALUE					= 1;
	public static final String STATE_PUMP_STATE 				= "PUMP_STATE";
	public static Integer STATE_PUMP_STATE_VALUE				= 20;
	public static final String STATE_ACCU_NAPETOST 				= "ACCU_NAPETOST";
	public static Integer STATE_ACCU_NAPETOST_VALUE				= 32;
	public static final String STATE_ACCU_TOK 					= "ACCU_TOK";
	public static Integer STATE_ACCU_TOK_VALUE					= 31;
	public static final String STATE_ACCU_AH 					= "ACCU_AH";
	public static Integer STATE_ACCU_AH_VALUE					= 30;
	public static final String STATE_ACCU_EMPTY					= "ACCU_EMPTY";
	public static Integer STATE_ACCU_EMPTY_VALUE				= 34;
	public static final String STATE_ACCU_DISCONNECTED			= "ACCU_DISCONNECT";
	public static Integer STATE_ACCU_DISCONNECTED_VALUE			= 33;
	public static final String STATE_GEO_FENCE					= "GEO_FENCE";
	public static Integer STATE_GEO_FENCE_VALUE					= 10;
	public static final String STATE_LAT 						= "LAT";
	public static Integer STATE_LAT_VALUE						= 11;
	public static final String STATE_LON	 					= "LON";
	public static Integer STATE_LON_VALUE						= 12;
	public static final String STATE_GEO_FIX 					= "GEO_FIX";
	public static Integer STATE_GEO_FIX_VALUE					= 14;
	public static final String STATE_ANCHOR 					= "ANCHOR";
	public static Integer STATE_ANCHOR_VALUE					= 40;
	public static final String STATE_ANCHOR_DRIFTING 			= "ANCHOR_DRIFTING";
	public static Integer STATE_ANCHOR_DRIFTING_VALUE			= 41;

	//OBU SETTINGS ID
	public static final String OBU_SETTINGS_REFRESH_TIME 		= "REFRESH_TIME";
	public static Integer OBU_SETTINGS_REFRESH_TIME_VALUE		= 1;
	public static final String OBU_SETTINGS_GEO_FENCE 			= "GEO_FENCE";
	public static Integer OBU_SETTINGS_GEO_FENCE_VALUE			= 10;
	public static final String OBU_SETTINGS_LON 				= "LON";
	public static Integer OBU_SETTINGS_LON_VALUE				= 12;
	public static final String OBU_SETTINGS_LAT 				= "LAT";
	public static Integer OBU_SETTINGS_LAT_VALUE				= 11;
	public static final String OBU_SETTINGS_GEO_DISTANCE		= "GEO_DISTANCE";
	public static Integer OBU_SETTINGS_GEO_DISTANCE_VALUE		= 13;
	public static final String OBU_SETTINGS_BATTERY_ENERGY_RESET= "BATTERY_ENERGY_RESET";
	public static Integer OBU_SETTINGS_BATTERY_ENERGY_RESET_VALUE= 31;
	public static final String OBU_SETTINGS_BATTERY_CAPACITY	= "BATTERY_CAPACITY";
	public static Integer OBU_SETTINGS_BATTERY_CAPACITY_VALUE	= 32;
	public static final String OBU_SETTINGS_BATTERY_ALARM_LEVEL = "BATTERY_ALARM_LEVEL";
	public static Integer OBU_SETTINGS_BATTERY_ALARM_LEVEL_VALUE= 33;
	public static final String OBU_SETTINGS_ANCHOR				= "ANCHOR";
	public static Integer OBU_SETTINGS_ANCHOR_VALUE				= 40;
	public static final String OBU_SETTINGS_ANCHOR_DRIFTING		= "ANCHOR_DRIFTING";
	public static Integer OBU_SETTINGS_ANCHOR_DRIFTING_VALUE	= 41;
	public static final String OBU_SETTINGS_LIGHT				= "LIGHT";
	public static Integer OBU_SETTINGS_LIGHT_VALUE				= 50;
	public static final String OBU_SETTINGS_FAN					= "FAN";
	public static Integer OBU_SETTINGS_FAN_VALUE				= 60;

	//PARAMETRI
	public static final String APP_SETTINGS_NAPETOST_TOK_MAX 	= "NAPETOST_TOK_MAX";
	public static Integer APP_SETTINGS_NAPETOST_TOK_MAX_VALUE	= 1023;
	public static final String APP_SETTINGS_NAPETOST_TOK_MIN 	= "NAPETOST_TOK_MIN";
	public static Integer APP_SETTINGS_NAPETOST_TOK_MIN_VALUE	= 15;
	public static final String APP_SETTINGS_ENERGIJA 			= "ENERGIJA";
	public static Integer APP_SETTINGS_ENERGIJA_VALUE			= 30;
	public static final String APP_SETTINGS_NAPETOST_KOEF1 		= "STATE_NAPETOST_KOEF1";
	public static Double APP_SETTINGS_NAPETOST_KOEF1_VALUE		= 83.25;
	public static final String APP_SETTINGS_NAPETOST_KOEF2 		= "STATE_NAPETOST_KOEF2";
	public static Double APP_SETTINGS_NAPETOST_KOEF2_VALUE		= 12.5;
	public static final String APP_SETTINGS_NAPETOST_KOEF3 		= "STATE_NAPETOST_KOEF3";
	public static Double APP_SETTINGS_NAPETOST_KOEF3_VALUE		= 11.29;
	public static final String APP_SETTINGS_TOK_KOEF1 			= "STATE_TOK_KOEF1";
	public static Double APP_SETTINGS_TOK_KOEF1_VALUE			= 20.7;
	public static final String APP_SETTINGS_NAPETOST_MAX		= "STATE_NAPETOST_MAX";
	public static Integer APP_SETTINGS_NAPETOST_MAX_VALUE		= 784;
	public static final String APP_SETTINGS_ALARM_REFRESH_TIME	= "ALARM_REFRESH_TIME";
	public static Integer APP_SETTINGS_ALARM_REFRESH_TIME_VALUE	= 120000;
	
	//POZICIJE V PODATKIH IZ OBUJA
	public static final String OBU_PUMP_STATE 					= "OBU_PUMP_STATE";
	public static Integer OBU_PUMP_STATE_VALUE					= 4;
	public static final String OBU_ACCU_NAPETOST 				= "OBU_ACCU_NAPETOST";
	public static Integer OBU_ACCU_NAPETOST_VALUE				= 11;
	public static final String OBU_ACCU_TOK 					= "OBU_ACCU_TOK";
	public static Integer OBU_ACCU_TOK_VALUE					= 9;
	public static final String OBU_ACCU_AH 						= "OBU_ACCU_AH";
	public static Integer OBU_ACCU_AH_VALUE						= 10;
	public static final String OBU_LAT 							= "OBU_LAT";
	public static Integer OBU_LAT_VALUE							= 0;
	public static final String OBU_LON 							= "OBU_LON";
	public static Integer OBU_LON_VALUE							= 2;
	public static final String OBU_N_S_INDICATOR 				= "OBU_N_S_INDICATOR";
	public static Integer OBU_N_S_INDICATOR_VALUE				= 1;
	public static final String OBU_E_W_INDICATOR 				= "OBU_E_W_INDICATOR";
	public static Integer OBU_E_W_INDICATOR_VALUE				= 3;
	//public static final String OBU_DATE 						= "OBU_DATE";
	//public static Integer OBU_DATE_VALUE						= -1;
	public static final String OBU_GEO_FIX 						= "OBU_GEO_FIX";
	public static Integer OBU_GEO_FIX_VALUE						= 4;

	//ALARMI
	public static final String GEO_FENCE_DISABLED 				= "GEO_FENCE_DISABLED";
	public static Integer GEO_FENCE_DISABLED_VALUE				= 0;
	public static final String GEO_FENCE_ENABLED 				= "GEO_FENCE_ENABLED";
	public static Integer GEO_FENCE_ENABLED_VALUE				= 1;
	public static final String GEO_FENCE_ALARM 					= "GEO_FENCE_ALARM";
	public static Integer GEO_FENCE_ALARM_VALUE					= 2;
	public static final String GEO_FIX_OK						= "GEO_FIX";
	public static Integer GEO_FIX_OK_VALUE						= 1;
	public static final String BATTERY_EMPTY					= "BATTERY_EMPTY";
	public static Integer BATTERY_EMPTY_VALUE					= 1;
	public static final String ACCU_DISCONNECT					= "ACCU_DISCONNECT";
	public static Integer ACCU_DISCONNECT_VALUE					= 0;
	public static final String PUMP_OK							= "PUMP_OK";
	public static Integer PUMP_OK_VALUE							= 0;
	public static final String PUMP_PUMPING						= "PUMP_PUMPING";
	public static Integer PUMP_PUMPING_VALUE					= 1;
	public static final String PUMP_CLODGED						= "PUMP_CLODGED";
	public static Integer PUMP_CLODGED_VALUE					= 2;
	public static final String PUMP_DEMAGED						= "PUMP_DEMAGED";
	public static Integer PUMP_DEMAGED_VALUE					= 3;
	public static final String ANCHOR_ENABLED					= "ANCHOR_ENABLED";
	public static Integer ANCHOR_ENABLED_VALUE					= 1;

	//Firmware
	public static final int FIRMWARE_1							= 1;
	public static final int FIRMWARE_2							= 2;
	
	//Server
	public static final String SERVER_SETTINGS_USERNAME			= "USERNAME";
	public static String SERVER_SETTINGS_USERNAME_VALUE			= "boat";
	public static final String SERVER_SETTINGS_PASSWORD			= "PASSWORD";
	public static String SERVER_SETTINGS_PASSWORD_VALUE			= "guard";
}
