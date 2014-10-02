package com.boatguard.boatguard.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import si.bisoft.commons.dbpool.DbManager;

public class HttpLog {
	public Integer uid = null;
	public String id = null;
	public String url = null;
	public String referer = null;
	public String parameters = null;
	public String headers = null;
	public String user_agent = null;
	public String client_ip = null;
	
	
	public static void afterHttp(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long ts = new Date().getTime();

		HttpParameters hp = parseHttpParameters(request);
		
		HttpLog h = new HttpLog();
		h.id = uuid();
		hp.parameters.put("http_log_id", h.id);
		h.url = hp.url+(hp.query!=null ? "?"+hp.query : "");
		h.headers = hp.headers.toString();
		h.parameters = hp.parameters.toString();
		h.referer = hp.referer;
		h.user_agent = hp.userAgent;
		h.client_ip = hp.clientIP;
		try {
			h.insert();
		} catch (SQLException e1) {
			throw new ServletException(e1);
		}
	} 

	public void get(String id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DbManager.getConnection("config");
			ps = con.prepareStatement("SELECT * FROM log_http WHERE id =?");
			ps.setString(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				this.uid = rs.getInt("uid");
				this.id = rs.getString("id");
				this.url = rs.getString("url");
				this.referer = rs.getString("referer");
				this.parameters = rs.getString("parameters");
				this.headers = rs.getString("headers");
				this.user_agent = rs.getString("user_agent");
				this.client_ip = rs.getString("client_ip");
			}
			rs.close();
			ps.close();
			con.close();
		} finally {
			if (rs != null) {try {rs.close();} catch (SQLException e) {}}
			if (ps != null) {try {ps.close();} catch (SQLException e) {}}
			if (con != null) {try {con.close();} catch (SQLException e) {}}
		}
	}

	public void insert() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DbManager.getConnection("config");
			ps = con.prepareStatement("INSERT INTO log_http(id,url,referer,parameters,headers,user_agent,client_ip)  VALUES (?,?,?,?,?,?,?)");
			ps.setString(1, this.id);
			ps.setString(2, this.url);
			ps.setString(3, this.referer);
			ps.setString(4, this.parameters);
			ps.setString(5, this.headers);
			ps.setString(6, this.user_agent);
			ps.setString(7, this.client_ip);
			ps.executeUpdate();
			ps.close();
			con.close();
		} finally {
			if (rs != null) {try {rs.close();} catch (SQLException e) {}}
			if (ps != null) {try {ps.close();} catch (SQLException e) {}}
			if (con != null) {try {con.close();} catch (SQLException e) {}}
		}
	}

	public String toString() {
        StringBuffer toString = new StringBuffer("[\n");
        toString.append("uid=" + (uid == null ? "null" : ""+uid) + "|");
        toString.append("id=" + (id == null ? "null" : ""+id) + "|");
        toString.append("url=" + (url == null ? "null" : ""+url) + "|");
        toString.append("referer=" + (referer == null ? "null" : ""+referer) + "|");
        toString.append("parameters=" + (parameters == null ? "null" : ""+parameters) + "|");
        toString.append("headers=" + (headers == null ? "null" : ""+headers) + "|");
        toString.append("user_agent=" + (user_agent == null ? "null" : ""+user_agent) + "|");
        toString.append("client_ip=" + (client_ip == null ? "null" : ""+client_ip) + "|");
		toString.append("]\n");
        return toString.toString();
    }
	
	private static List EXCL_HEADERS = new ArrayList();
	static {
		EXCL_HEADERS.add("cookie");
		EXCL_HEADERS.add("referer");
		EXCL_HEADERS.add("user-agent");
	}
	
	private static HttpParameters parseHttpParameters(HttpServletRequest request) {
		String enn;
		Properties props = new Properties();
		for (Enumeration en = request.getParameterNames(); en.hasMoreElements() ;) {
			enn = en.nextElement().toString().trim();
			props.put(enn, request.getParameter(enn));
		}
		Properties heads = new Properties();
		for (Enumeration en = request.getHeaderNames(); en.hasMoreElements() ;) {
			enn = en.nextElement().toString().trim();
			if (!EXCL_HEADERS.contains(enn)) {
				heads.put(enn, request.getHeader(enn));
			}
		}
		String referer = request.getHeader("REFERER");
		String userAgent = request.getHeader("USER-AGENT");
		HttpParameters hp = new HttpParameters();
		hp.parameters = props;
		hp.headers = heads;
		hp.referer = referer;
		hp.userAgent = userAgent;
		hp.method = request.getMethod().toUpperCase();
		hp.cookie = request.getHeader("COOKIE");
		hp.url = request.getRequestURL().toString();
		hp.query = request.getQueryString();
		hp.isSecure = request.isSecure();
		hp.clientIP = request.getRemoteAddr();
		return hp;
	}
	private static String uuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
}