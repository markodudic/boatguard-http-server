package com.boatguard.boatguard.objects;

public class BatterySetting {
	
	private int id;
	private String value;
	private double koef;
	private int percent;
	private double volt;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public double getKoef() {
		return koef;
	}
	public void setKoef(double koef) {
		this.koef = koef;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
	}
	public double getVolt() {
		return volt;
	}
	public void setVolt(double volt) {
		this.volt = volt;
	}
	

}
