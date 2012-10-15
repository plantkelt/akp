package net.plantkelt.akp.domain;

public class AkpTaxon implements Comparable<AkpTaxon> {

	public static final int TYPE_MAIN = 0;
	public static final int TYPE_SYNONYM = 2;

	private Integer xid;
	private AkpPlant plant;
	private String name;
	private int type;

	private transient String sortKey;

	public Integer getXid() {
		return xid;
	}

	public void setXid(Integer xid) {
		this.xid = xid;
	}

	public AkpPlant getPlant() {
		return plant;
	}

	public void setPlant(AkpPlant plant) {
		this.plant = plant;
	}

	public String getName() {
		return name;
	}

	public String getHtmlName() {
		// TODO compute and cache the returned value
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
		this.sortKey = null;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	private synchronized String getSortKey() {
		if (sortKey == null) {
			// TODO compute sort key based on name
			sortKey = name;
		}
		return sortKey;
	}

	@Override
	public String toString() {
		return String.format("[AkpTaxon #%d %d %s]", getXid(), getType(),
				getName());
	}

	@Override
	public int compareTo(AkpTaxon other) {
		return getSortKey().compareTo(other.getSortKey());
	}

}
