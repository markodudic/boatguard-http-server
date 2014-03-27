package si.noemus.boatguard.dao;

public class ObuAlarm {
	
	private int id_obu;
	private int id_alarm;
	private int sound;
	private int vibrate;
	private int send_customer;
	private int send_friends;
	private int active;
	
	public int getId_obu() {
		return id_obu;
	}
	public void setId_obu(int id_obu) {
		this.id_obu = id_obu;
	}
	public int getId_alarm() {
		return id_alarm;
	}
	public void setId_alarm(int id_alarm) {
		this.id_alarm = id_alarm;
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
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
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
	
}
