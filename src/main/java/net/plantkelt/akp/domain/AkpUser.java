package net.plantkelt.akp.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
	/** Set of roles (rights) for the user */
	private Set<String> roles;
	/**
	 * Lang restriction for verna or lang related role, an empty list means
	 * "all"
	 */
	private Set<AkpLang> langs;

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

	public Set<String> getRoles() {
		if (roles == null || roles.isEmpty()) {
			// Ensure DB migration by adding a default "USER" role
			Set<String> tempRoles = new HashSet<>(10);
			tempRoles.add(AkpUserRoles.ROLE_USER);
			return tempRoles;
		}
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public boolean hasRole(String role) {
		// Admin has *all* roles
		switch (getProfile()) {
		case PROFILE_ADMIN:
			return true;
		case PROFILE_USER:
			return getRoles().contains(role);
		default:
			throw new RuntimeException("Unknown profile: " + getProfile());
		}
	}

	public Set<AkpLang> getLangs() {
		if (langs == null)
			return Collections.emptySet();
		return langs;
	}

	public void setLangs(Set<AkpLang> langs) {
		this.langs = langs;
	}

	public boolean hasLangRight(AkpLang lang) {
		if (langs == null || langs.isEmpty()) {
			return false;
		}
		return langs.contains(lang);
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
