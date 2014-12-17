package com.boatguard.boatguard.objects;

import java.util.HashMap;
import java.util.Map;

public class Obu {
	
	private int uid;
	private String name;
	private String number;
	private String boat_manafacturer;
	private String boat_model;
	private String boat_country;
	private String pin;
	private String puk;
	private String serial_number;
	private int active;
	public Map<Integer, String> settings = new HashMap<Integer, String>();
	
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	public String getPuk() {
		return puk;
	}
	public void setPuk(String puk) {
		this.puk = puk;
	}
	public String getSerial_number() {
		return serial_number;
	}
	public void setSerial_number(String serial_number) {
		this.serial_number = serial_number;
	}
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
	}
	public Map<Integer, String> getSettings() {
		return settings;
	}
	public void setSettings(Map<Integer, String> settings) {
		this.settings = settings;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBoat_manafacturer() {
		return boat_manafacturer;
	}
	public void setBoat_manafacturer(String boat_manafacturer) {
		this.boat_manafacturer = boat_manafacturer;
	}
	public String getBoat_model() {
		return boat_model;
	}
	public void setBoat_model(String boat_model) {
		this.boat_model = boat_model;
	}
	public String getBoat_country() {
		return boat_country;
	}
	public void setBoat_country(String boat_country) {
		this.boat_country = boat_country;
	}

}
