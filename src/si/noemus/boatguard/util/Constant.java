package si.noemus.boatguard.util;

import java.text.SimpleDateFormat;

public class Constant {

	public static final String STATE_ROW_STATE 					= "ROW_STATE";
	public static Integer STATE_ROW_STATE_VALUE					= 1;
	public static final String STATE_PUMP_STATE 				= "PUMP_STATE";
	public static Integer STATE_PUMP_STATE_VALUE				= 20;
	public static final String STATE_ACCU_TOK 					= "ACCU_TOK";
	public static Integer STATE_ACCU_TOK_VALUE					= 30;
	public static final String STATE_ACCU_NAPETOST 				= "ACCU_NAPETOST";
	public static Integer STATE_ACCU_NAPETOST_VALUE				= 31;
	public static final String STATE_ACCU_AH 					= "ACCU_AH";
	public static Integer STATE_ACCU_AH_VALUE					= 32;
	public static final String STATE_LAT 						= "LAT";
	public static Integer STATE_LAT_VALUE						= 11;
	public static final String STATE_LON	 					= "LON";
	public static Integer STATE_LON_VALUE						= 12;
	public static final String STATE_GEO_FIX 					= "GEO_FIX";
	public static Integer STATE_GEO_FIX_VALUE					= 14;

	public static final String OBU_SETTINGS_GEO_FENCE 			= "GEO_FENCE";
	public static Integer OBU_SETTINGS_GEO_FENCE_VALUE			= 10;
	public static final String OBU_SETTINGS_LON 				= "LON";
	public static Integer OBU_SETTINGS_LON_VALUE				= 12;
	public static final String OBU_SETTINGS_LAT 				= "LAT";
	public static Integer OBU_SETTINGS_LAT_VALUE				= 11;
	public static final String OBU_SETTINGS_GEO_DISTANCE		= "GEO_DISTANCE";
	public static Integer OBU_SETTINGS_GEO_DISTANCE_VALUE		= 13;

	public static final String APP_SETTINGS_NAPETOST_TOK_MAX 	= "NAPETOST_TOK_MAX";
	public static Integer APP_SETTINGS_NAPETOST_TOK_MAX_VALUE	= 1023;
	public static final String APP_SETTINGS_NAPETOST_TOK_MIN 	= "NAPETOST_TOK_MIN";
	public static Integer APP_SETTINGS_NAPETOST_TOK_MIN_VALUE	= 15;
	public static final String APP_SETTINGS_ENERGIJA 			= "ENERGIJA";
	public static Integer APP_SETTINGS_ENERGIJA_VALUE			= 30;
	public static final String APP_SETTINGS_NAPETOST_KOEF1 		= "STATE_NAPETOST_KOEF1";
	public static Double APP_SETTINGS_NAPETOST_KOEF1_VALUE		= 127.875;
	public static final String APP_SETTINGS_NAPETOST_KOEF2 		= "STATE_NAPETOST_KOEF2";
	public static Double APP_SETTINGS_NAPETOST_KOEF2_VALUE		= 12.5;

	public static final String OBU_PUMP_STATE 					= "OBU_PUMP_STATE";
	public static Integer OBU_PUMP_STATE_VALUE					= 3;
	public static final String OBU_ACCU_NAPETOST 				= "OBU_ACCU_NAPETOST";
	public static Integer OBU_ACCU_NAPETOST_VALUE				= 10;
	public static final String OBU_ACCU_TOK 					= "OBU_ACCU_TOK";
	public static Integer OBU_ACCU_TOK_VALUE					= 8;
	public static final String OBU_ACCU_AH 						= "OBU_ACCU_AH";
	public static Integer OBU_ACCU_AH_VALUE						= 9;
	public static final String OBU_LAT 							= "OBU_LAT";
	public static Integer OBU_LAT_VALUE							= 0;
	public static final String OBU_LON 							= "OBU_LON";
	public static Integer OBU_LON_VALUE							= 1;
	//public static final String OBU_DATE 						= "OBU_DATE";
	//public static Integer OBU_DATE_VALUE						= -1;
	public static final String OBU_GEO_FIX 						= "OBU_GEO_FIX";
	public static Integer OBU_GEO_FIX_VALUE						= 2;

	public static final String GEO_FENCE_DISABLED 				= "GEO_FENCE_DISABLED";
	public static Integer GEO_FENCE_DISABLED_VALUE				= 0;
	public static final String GEO_FENCE_ENABLED 				= "GEO_FENCE_ENABLED";
	public static Integer GEO_FENCE_ENABLED_VALUE				= 1;
	public static final String GEO_FENCE_ALARM 					= "GEO_FENCE_ALARM";
	public static Integer GEO_FENCE_ALARM_VALUE					= 2;
	public static final String GEO_FIX_OK						= "GEO_FIX";
	public static Integer GEO_FIX_OK_VALUE						= 1;

}
