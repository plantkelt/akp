package net.plantkelt.akp.domain;

import java.util.Date;

public class AkpUser {

	private String login;
	private String md5;
	private String lastbib;
	private int profile;
	private int lang;
	private Date expire;
	private String name;
	private String email;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getLastbib() {
		return lastbib;
	}

	public void setLastbib(String lastbib) {
		this.lastbib = lastbib;
	}

	public int getProfile() {
		return profile;
	}

	public void setProfile(int profile) {
		this.profile = profile;
	}

	public int getLang() {
		return lang;
	}

	public void setLang(int lang) {
		this.lang = lang;
	}

	public Date getExpire() {
		return expire;
	}

	public void setExpire(Date expire) {
		this.expire = expire;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean isAdmin() {
		return profile == 3;
	}
}
