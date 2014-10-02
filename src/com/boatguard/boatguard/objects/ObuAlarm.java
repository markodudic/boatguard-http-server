package com.boatguard.boatguard.objects;

import java.sql.Timestamp;

public class ObuAlarm {
	
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
	private int send_email;
	private int send_customer;
	private int send_friends;
	
	
	@Override
	public String toString(){
		return "OBUALARM: idAlarm: " + this.id_alarm + ", idObu:" + this.id_obu + ", value:" + this.value + ", messageShort" + this.message_short +
				" title: " + this.title + ", action:" + this.action + ", sound:" + this.sound + ", vibrate" + this.vibrate +
				" type: " + this.type + ", dateAlarm:" + this.date_alarm + ", confirmed:" + this.confirmed + ", active" + this.active;
	}

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

	public String getMessage_short() {
		return message_short;
	}

	public void setMessage_short(String message_short) {
		this.message_short = message_short;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
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

	public int getSend_email() {
		return send_email;
	}

	public void setSend_email(int send_email) {
		this.send_email = send_email;
	}

	public int getSend_customer() {
		return send_customer;
	}

	public void setSend_customer(int send_customer) {
		this.send_customer = send_customer;
	}

	public int getSend_friends() {
		return send_friends;
	}

	public void setSend_friends(int send_friends) {
		this.send_friends = send_friends;
	}


	
}
