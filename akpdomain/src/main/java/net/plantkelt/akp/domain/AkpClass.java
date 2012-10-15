package net.plantkelt.akp.domain;

import java.util.Collections;
import java.util.List;

public class AkpClass {

	private Integer xid;
	private String name;
	private String comments;
	private AkpClass parent;
	private List<AkpClass> children;
	private String synonyms;
	private int order;
	private int level;
	private List<AkpPlant> plants;

	public Integer getXid() {
		return xid;
	}

	public void setXid(Integer xid) {
		this.xid = xid;
	}

	public String getName() {
		return name;
	}

	public String getHtmlName() {
		return name.replace("<l>", "<b>").replace("</l>", "</b>");
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments == null ? "" : comments;
	}

	public AkpClass getParent() {
		return parent;
	}

	public String getParentPathHtml() {
		AkpClass cls = getParent();
		StringBuffer retval = new StringBuffer();
		while (cls != null) {
			retval.insert(0, " / " + cls.getHtmlName());
			cls = cls.getParent();
		}
		return retval.toString();
	}

	public void setParent(AkpClass parent) {
		this.parent = parent;
	}

	public List<AkpClass> getChildren() {
		return children;
	}

	public void setChildren(List<AkpClass> children) {
		this.children = children;
	}

	public String getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms == null ? "" : synonyms;
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

	public List<AkpPlant> getPlants() {
		if (plants == null)
			return Collections.emptyList();
		return plants;
	}

	public void setPlants(List<AkpPlant> plants) {
		this.plants = plants;
	}

	@Override
	public String toString() {
		return String.format("[AkpClass #%d %s]", getXid(), getName());
	}

}
