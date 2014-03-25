package si.noemus.boatguard.dao;

import java.sql.Timestamp;

public class AlarmData {
	
	private int id_alarm;
	private int id_obu;
	private String value;
	private String message;
	private String type;
	private Timestamp date_alarm;
	private int confirmed;
	
	public int getId_alarm() {
		return id_alarm;
	}
	public void setId_alarm(int id_alarm) {
		this.id_alarm = id_alarm;
	}
	public int getId_obu() {
		return id_obu;
	}
	public void setId_obu(int id_obu) {
		this.id_obu = id_obu;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Timestamp getDate_alarm() {
		return date_alarm;
	}
	public void setDate_alarm(Timestamp date_alarm) {
		this.date_alarm = date_alarm;
	}
	public int getConfirmed() {
		return confirmed;
	}
	public void setConfirmed(int confirmed) {
		this.confirmed = confirmed;
	}
	

}
