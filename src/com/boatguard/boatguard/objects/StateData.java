package com.boatguard.boatguard.objects;

import java.sql.Timestamp;

public class StateData {
	
	private int id_state;
	private int id_obu;
	private String value;
	private String type;
	private Timestamp dateState;
	private String dateString;
	
	public int getId_state() {
		return id_state;
	}
	public void setId_state(int id_state) {
		this.id_state = id_state;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Timestamp getDateState() {
		return dateState;
	}
	public void setDateState(Timestamp dateState) {
		this.dateState = dateState;
	}
	public String getDateString() {
		return dateString;
	}
	public void setDateString(String dateString) {
		this.dateString = dateString;
	}
}
