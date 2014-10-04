package net.plantkelt.akp.domain;

import java.io.Serializable;
import java.util.Date;

public class AkpUser implements Serializable, Comparable<AkpUser> {
	private static final long serialVersionUID = 1L;

	public static final int PROFILE_USER = 1;
	public static final int PROFILE_ADMIN = 3;

	/* TODO Use ISO 2 letters code for user language */
	public static final int LANG_EN = 0;
	public static final int LANG_FR = 1;
	public static final int LANG_BR = 2;

	private String login;
	private String md5;
	private String lastbib;
	private int profile;
	private int lang;
	private Date expire;
	private String name;
	private String email;
	private Integer requestCount;

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

	public Integer getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(Integer requestCount) {
		this.requestCount = requestCount;
	}

	public synchronized void incRequestCount() {
		if (requestCount == null)
			requestCount = 0;
		requestCount++;
	}

	public boolean isAdmin() {
		return profile == 3;
	}

	@Override
	public boolean equals(Object another) {
		if (another instanceof AkpUser) {
			return getLogin().equals(((AkpUser) another).getLogin());
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("[AkpUser %s]", getLogin());
	}

	@Override
	public int compareTo(AkpUser o) {
		return getLogin().compareToIgnoreCase(o.getLogin());
	}

}
