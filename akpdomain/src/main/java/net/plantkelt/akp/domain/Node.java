package net.plantkelt.akp.domain;

import java.util.List;

public class Node {

	private Integer xid;
	private Node parent;
	private List<Node> children;

	public Integer getXid() {
		return xid;
	}

	public void setXid(Integer xid) {
		this.xid = xid;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return String.format("[Node %d, parent %s]", xid,
				parent == null ? "NONE" : parent.toString());
	}
}
