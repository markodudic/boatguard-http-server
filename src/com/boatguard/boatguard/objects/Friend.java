package com.boatguard.boatguard.objects;

import java.sql.Timestamp;

public class Friend {
		
		private int uid;
		private int id_customer;
		private String name;
		private String surname;
		private String number;
		private String email;
		private String active;
		
		public int getUid() {
			return uid;
		}
		public void setUid(int uid) {
			this.uid = uid;
		}
		public int getId_customer() {
			return id_customer;
		}
		public void setId_customer(int id_customer) {
			this.id_customer = id_customer;
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
		public String getNumber() {
			return number;
		}
		public void setNumber(String number) {
			this.number = number;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getActive() {
			return active;
		}
		public void setActive(String active) {
			this.active = active;
		}
		

	}
