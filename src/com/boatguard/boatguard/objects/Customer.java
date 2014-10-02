package com.boatguard.boatguard.objects;

import java.sql.Timestamp;

public class Customer {
		
		private int uid;
		private int id_obu;
		private String name;
		private String surname;
		private String username;
		private String password;
		private String email;
		private Timestamp register_date;
		private Timestamp last_visited;
		private String app_version;
		private String phone_number;
		private String phone_model;
		private String phone_platform;
		private String phone_platform_version;
		private String phone_uuid;
		private String home_network;
		private String active;
		private String serial_number;
		private String boat_name;
		
		public int getUid() {
			return uid;
		}
		public void setUid(int uid) {
			this.uid = uid;
		}
		public int getId_obu() {
			return id_obu;
		}
		public void setId_obu(int id_obu) {
			this.id_obu = id_obu;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getSurname() {
			return surname;
		}
		public void setSurname(String surname) {
			this.surname = surname;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public Timestamp getRegister_date() {
			return register_date;
		}
		public void setRegister_date(Timestamp register_date) {
			this.register_date = register_date;
		}
		public Timestamp getLast_visited() {
			return last_visited;
		}
		public void setLast_visited(Timestamp last_visited) {
			this.last_visited = last_visited;
		}
		public String getApp_version() {
			return app_version;
		}
		public void setApp_version(String app_version) {
			this.app_version = app_version;
		}
		public String getPhone_model() {
			return phone_model;
		}
		public void setPhone_model(String phone_model) {
			this.phone_model = phone_model;
		}
		public String getPhone_platform() {
			return phone_platform;
		}
		public void setPhone_platform(String phone_platform) {
			this.phone_platform = phone_platform;
		}
		public String getHome_network() {
			return home_network;
		}
		public void setHome_network(String home_network) {
			this.home_network = home_network;
		}
		public String getActive() {
			return active;
		}
		public void setActive(String active) {
			this.active = active;
		}
		public String getPhone_number() {
			return phone_number;
		}
		public void setPhone_number(String phone_number) {
			this.phone_number = phone_number;
		}
		public String getPhone_platform_version() {
			return phone_platform_version;
		}
		public void setPhone_platform_version(String phone_platform_version) {
			this.phone_platform_version = phone_platform_version;
		}
		public String getPhone_uuid() {
			return phone_uuid;
		}
		public void setPhone_uuid(String phone_uuid) {
			this.phone_uuid = phone_uuid;
		}
		public String getSerial_number() {
			return serial_number;
		}
		public void setSerial_number(String serial_number) {
			this.serial_number = serial_number;
		}
		public String getBoat_name() {
			return boat_name;
		}
		public void setBoat_name(String boat_name) {
			this.boat_name = boat_name;
		}
		
	}
