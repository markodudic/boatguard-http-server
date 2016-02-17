package com.boatguard.boatguard.objects;

public class ObuComponent {
	
	private int id_obu;
	private int id_component;
	private String name;
	private String label;
	private String type;
	private int show;
	
	@Override
	public String toString(){
		return "OBUCOMPONENT: id_component: " + this.id_component + ", id_obu:" + this.id_obu + ", name:" + this.name + ", label:" + this.label + ", show:" + this.show + ", type" + this.type;
	}
	
	public int getId_obu() {
		return id_obu;
	}
	public void setId_obu(int id_obu) {
		this.id_obu = id_obu;
	}

	public int getId_component() {
		return id_component;
	}
	public void setId_component(int id_component) {
		this.id_component = id_component;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getShow() {
		return show;
	}
	public void setShow(int show) {
		this.show = show;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
