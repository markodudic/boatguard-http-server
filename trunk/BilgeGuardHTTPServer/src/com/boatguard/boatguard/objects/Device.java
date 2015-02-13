package com.boatguard.boatguard.objects;

import java.sql.Timestamp;

public class Device {
		
		private int uid;
		private int id_obu;
		private String gcm_registration_id;
		private Timestamp last_visited;
		private String app_version;
		private String phone_number;
		private String phone_model;
		private String phone_platform;
		private String phone_platform_version;
		private String phone_uuid;
		private String home_network;
		
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
		public String getGcm_registration_id() {
			return gcm_registration_id;
		}
		public void setGcm_registration_id(String gcm_registration_id) {
			this.gcm_registration_id = gcm_registration_id;
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
		
	}
