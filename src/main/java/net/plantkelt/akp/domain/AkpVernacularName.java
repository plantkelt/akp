package net.plantkelt.akp.domain;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AkpVernacularName implements Comparable<AkpVernacularName> {

	private int xid;
	private Integer parentId;
	private AkpVernacularName parent;
	private List<AkpVernacularName> children;
	private String name;
	private String comments;
	private AkpLexicalGroup lexicalGroup;
	private List<AkpBib> bibs;
	private List<AkpPlant> plantRefs;

	private static Collator defaultCollator = Collator.getInstance();

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public AkpVernacularName getParent() {
		return parent;
	}

	public void setParent(AkpVernacularName parent) {
		this.parent = parent;
	}

	public List<AkpVernacularName> getChildren() {
		return children;
	}

	public void setChildren(List<AkpVernacularName> children) {
		this.children = children;
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
		this.comments = comments == null ? "" : comments;
	}

	public AkpLexicalGroup getLexicalGroup() {
		return lexicalGroup;
	}

	public void setLexicalGroup(AkpLexicalGroup lexicalGroup) {
		this.lexicalGroup = lexicalGroup;
	}

	public List<AkpBib> getBibs() {
		return bibs;
	}
	
	// TODO How to define this using hibernate mapping ?
	public List<AkpBib> getOrderedBibs() {
		List<AkpBib> retval = new ArrayList<AkpBib>(bibs);
		Collections.sort(retval);
		return retval;
	}

	public void setBibs(List<AkpBib> bibs) {
		this.bibs = bibs;
	}

	public List<AkpPlant> getPlantRefs() {
		return plantRefs;
	}

	public void setPlantRefs(List<AkpPlant> plantRefs) {
		this.plantRefs = plantRefs;
	}

	@Override
	public String toString() {
		return String.format("[AkpVernacularName %d %s]", getXid(), getName());
	}

	@Override
	public int compareTo(AkpVernacularName o) {
		if (getName().length() == 0) {
			// Treat empty name special case: we want them at the end
			if (o.getName().length() == 0)
				return 0; // Two empty strings are equals
			return 1;
		}
		if (o.getName().length() == 0) {
			// Empty string is greater than anything else
			return -1;
		}
		int cmp = defaultCollator.compare(getName(), o.getName());
		if (cmp == 0) {
			// We want a stable sort
			cmp = getXid() - o.getXid();
		}
		return cmp;
	}

}
