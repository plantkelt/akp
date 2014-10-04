package net.plantkelt.akp.domain;

import java.io.Serializable;
import java.util.List;

public class AkpLangGroup implements Serializable, Comparable<AkpLangGroup> {
	private static final long serialVersionUID = 1L;

	private int xid;
	private int order;
	private String code;
	private String name;
	private List<AkpLang> langs;

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<AkpLang> getLangs() {
		return langs;
	}

	public void setLangs(List<AkpLang> langs) {
		this.langs = langs;
	}

	@Override
	public String toString() {
		return String.format("[AkpLangGroup %d %s]", getXid(), getCode());
	}

	@Override
	public int compareTo(AkpLangGroup o) {
		return getOrder() - o.getOrder();
	}

}
