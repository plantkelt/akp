package net.plantkelt.akp.domain;

public class AkpPlant {

	private int xid;
	private AkpClass akpClass;
	private String comments;

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public AkpClass getAkpClass() {
		return akpClass;
	}

	public void setAkpClass(AkpClass akpClass) {
		this.akpClass = akpClass;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
