package net.plantkelt.akp.domain;

public class AkpAuthor implements Comparable<AkpAuthor> {

	private String xid;
	private String name;
	private String source;
	private String dates;

	public String getXid() {
		return xid;
	}

	public void setXid(String xid) {
		this.xid = xid;
	}

	public String getName() {
		return name;
	}

	public String getTextName() {
		return name.replaceAll("<.*?>", "");
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDates() {
		return dates;
	}

	public void setDates(String dates) {
		this.dates = dates;
	}

	@Override
	public boolean equals(Object another) {
		if (another == null)
			return false;
		if (another instanceof AkpAuthor) {
			AkpAuthor anotherAuthor = (AkpAuthor) another;
			return anotherAuthor.getXid().equals(getXid());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getXid().hashCode();
	}

	@Override
	public int compareTo(AkpAuthor o) {
		return getXid().compareTo(o.getXid());
	}

}
