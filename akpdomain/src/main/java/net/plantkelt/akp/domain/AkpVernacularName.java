package net.plantkelt.akp.domain;

import java.util.List;

public class AkpVernacularName {

	private int xid;
	private int parentId;
//	private AkpVernacularName parent;
//	private List<AkpVernacularName> children;
	private String name;
	private String comment;
	private AkpLexicalGroup lexicalGroup;

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

//	public AkpVernacularName getParent() {
//		return parent;
//	}
//
//	public void setParent(AkpVernacularName parent) {
//		this.parent = parent;
//	}
//
//	public List<AkpVernacularName> getChildren() {
//		return children;
//	}
//
//	public void setChildren(List<AkpVernacularName> children) {
//		this.children = children;
//	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public AkpLexicalGroup getLexicalGroup() {
		return lexicalGroup;
	}

	public void setLexicalGroup(AkpLexicalGroup lexicalGroup) {
		this.lexicalGroup = lexicalGroup;
	}

	@Override
	public String toString() {
		return String.format("[AkpVernacularName %d %s]", getXid(), getName());
	}

}
