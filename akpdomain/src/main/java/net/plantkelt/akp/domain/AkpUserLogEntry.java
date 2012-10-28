package net.plantkelt.akp.domain;

import java.util.Date;

public class AkpUserLogEntry {

	private Integer xid;
	private Date date;
	private String login;
	private String remoteAddr;
	private Integer operation;
	private String value;

	public Integer getXid() {
		return xid;
	}

	public void setXid(Integer xid) {
		this.xid = xid;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public Integer getOperation() {
		return operation;
	}

	public void setOperation(Integer operation) {
		this.operation = operation;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
