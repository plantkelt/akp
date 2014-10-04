package net.plantkelt.akp.domain;

public class AkpLang implements Comparable<AkpLang> {

	private String xid;
	private int order;
	private int level;
	private String code;
	private String name;
	private String desc;
	private AkpLangGroup langGroup;

	public String getXid() {
		return xid;
	}

	public void setXid(String xid) {
		this.xid = xid;
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

	public String getCode() {
		if (code == null)
			code = xid;
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

	public String getDesc() {
		if (desc == null)
			desc = "";
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public AkpLangGroup getLangGroup() {
		return langGroup;
	}

	public void setLangGroup(AkpLangGroup langGroup) {
		this.langGroup = langGroup;
	}

	@Override
	public String toString() {
		return String.format("[AkpLang %s %s]", getXid(), getName());
	}

	@Override
	public int compareTo(AkpLang o) {
		return getCode().compareToIgnoreCase(o.getCode());
	}
}
