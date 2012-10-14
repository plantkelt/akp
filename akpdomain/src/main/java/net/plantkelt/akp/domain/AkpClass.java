package net.plantkelt.akp.domain;

public class AkpClass {

	private int xid;
	private String name;
	private String comments;
	private AkpClass parent;
	private String synonyms;
	private int order;
	private int level;

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public AkpClass getParent() {
		return parent;
	}

	public void setParent(AkpClass parent) {
		this.parent = parent;
	}

	public String getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
