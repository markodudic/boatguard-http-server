package si.noemus.boatguard.dao;

public class ObuComponent {
	
	private int id_component;
	private String name;
	private String type;
	private int show;
	
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
}
