package si.noemus.boatguard.objects;



public class ObuState {

	public ObuState(){
		
	}	
	
	private int id_state;
	private int id_obu;
	private String value;
	private String dateState;
	
	@Override
	public String toString(){
		return "OBUSTATE: idState: " + this.id_state + ", idObu:" + this.id_obu + ", value:" + this.value + ", dateState" + this.dateState;
	}

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

	public String getDateState() {
		return dateState;
	}

	public void setDateState(String dateState) {
		this.dateState = dateState;
	}


}
