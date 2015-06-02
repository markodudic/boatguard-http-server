package com.boatguard.boatguard.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Util {

	public static double gps2m(float lat_a, float lng_a, float lat_b, float lng_b) {
		float pk = (float) (180/3.14169);
		float a1 = lat_a / pk;
		float a2 = lng_a / pk;
		float b1 = lat_b / pk;
		float b2 = lng_b / pk;
		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
		double t3 = Math.sin(a1) * Math.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);
		//System.out.println(6366000*tt);
		return 6366000*tt;
	}	
	
	public static float transform(float x) {
		double x_ = Math.floor(x/100);
		double x__ = (x/100 - x_)/0.6;
		x = (float) (x_ + x__);
		return x;
	}
	
    public static String formatDate(long dateToConvert)
    {
    	Date d = new Date(dateToConvert);
    	DateFormat df = new SimpleDateFormat("MMM/dd/yyyy HH:mm");
		return df.format(d).toUpperCase();
    }	

    public static long hexaToDec(String value) {  
		long A = Integer.parseInt(value.substring(0,1), 16) * (16*16*16);
		long B = Integer.parseInt(value.substring(1,2), 16) * (16*16);
		long C = Integer.parseInt(value.substring(2,3), 16) * (16);
		long D = Integer.parseInt(value.substring(3,4), 16);
		
		return (A+B+C+D);
    }

    public static long hexaToDecLong(String value) {  
		long A = Integer.parseInt(value.substring(0,1), 16) * (16*16*16*16*16*16*16);
		long B = Integer.parseInt(value.substring(1,2), 16) * (16*16*16*16*16*16);
		long C = Integer.parseInt(value.substring(2,3), 16) * (16*16*16*16*16);
		long D = Integer.parseInt(value.substring(3,4), 16) * (16*16*16*16);
		long E = Integer.parseInt(value.substring(4,5), 16) * (16*16*16);
		long F = Integer.parseInt(value.substring(5,6), 16) * (16*16);
		
		return (A+B+C+D+E+F);
    }

    public static int nthOccurrence(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos+1);
        return pos;
    }
}
