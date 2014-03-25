package si.noemus.boatguard.dao;

import java.util.HashMap;
import java.util.Map;

public class Obu {
	
	private int id;
	private String name;
	private String number;
	private String pin;
	private String puk;
	private String serial_number;
	private int active;
	public Map<Integer, String> settings = new HashMap<Integer, String>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
		settings = settings;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
