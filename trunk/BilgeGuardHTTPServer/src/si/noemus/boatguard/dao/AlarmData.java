package si.noemus.boatguard.dao;

import java.sql.Timestamp;

public class AlarmData {
	
	private int id_alarm;
	private int id_obu;
	private String value;
	private String message;
	private String message_short;
	private String title;
	private String action;
	private int sound;
	private int vibrate;
	private String type;
	private Timestamp date_alarm;
	private int confirmed;
	private int active;
	
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
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getSound() {
		return sound;
	}
	public void setSound(int sound) {
		this.sound = sound;
	}
	public int getVibrate() {
		return vibrate;
	}
	public void setVibrate(int vibrate) {
		this.vibrate = vibrate;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getMessage_short() {
		return message_short;
	}
	public void setMessage_short(String message_short) {
		this.message_short = message_short;
	}
	
}
