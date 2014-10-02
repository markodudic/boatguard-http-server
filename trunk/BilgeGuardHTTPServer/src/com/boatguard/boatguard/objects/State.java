package com.boatguard.boatguard.objects;



public class State {

	public State(){
		
	}	
	
	private int id;
	private int id_component;
	private String name;
	private String code;
	private int position;
	private String type;
	private int active;
	private String values;
	
	@Override
	public String toString(){
		return "STATE: id: " + this.id + ", idComponent:" + this.id_component + ", name:" + this.name + ", code:" + this.code + 
				", type" + this.type + ", active" + this.active;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public int getIdComponent() {
		return id_component;
	}

	public void setIdComponent(int id_component) {
		this.id_component = id_component;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
	}

	public int getId_component() {
		return id_component;
	}

	public void setId_component(int id_component) {
		this.id_component = id_component;
	}

	public String getValues() {
		return values;
	}

	public void setValues(String values) {
		this.values = values;
	}


}
