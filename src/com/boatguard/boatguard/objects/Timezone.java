package com.boatguard.boatguard.objects;



public class Timezone {

	public Timezone(){
		
	}	
	

	private int dstOffset;
	private int rawOffset;
	private String status;
	private String timeZoneId;
	private String timeZoneName;
	
	public int getDstOffset() {
		return dstOffset;
	}
	public void setDstOffset(int dstOffset) {
		this.dstOffset = dstOffset;
	}
	public int getRawOffset() {
		return rawOffset;
	}
	public void setRawOffset(int rawOffset) {
		this.rawOffset = rawOffset;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTimeZoneId() {
		return timeZoneId;
	}
	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}
	public String getTimeZoneName() {
		return timeZoneName;
	}
	public void setTimeZoneName(String timeZoneName) {
		this.timeZoneName = timeZoneName;
	}

	
}
