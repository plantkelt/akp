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
		String html = name.replace("<x>", "×").replace("<+>", "+")
				.replace("<urseurtad>", " - 1 seurtad:").replace("<l>", "")
				.replace("</l>", "").replace("<y>", "<span class='txy'>")
				.replace("</y>", "</span>");
		if (type == TYPE_MAIN) {
			html = html.replace("<b>", "<b><i>").replace("</b>", "</i></b>")
					.replace("<e>", "<b><i>").replace("</e>", "</i></b>");
		} else {
			html = html.replace("<b>", "<i>").replace("</b>", "</i>")
					.replace("<e>", "<i>").replace("</e>", "</i>");
		}
		// TODO Author
		// TODO epsilons
		html = html.replace("<a>", "[").replace("</a>", "]");
		return html;
	}

	public String getTextName() {
		return name.replace("<x>", "×").replace("<+>", "+")
				.replaceAll("<.*?>", "");
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
			sortKey = name;
			sortKey = sortKey
					.replace("spp.", "")
					.replace(", non ", "  ")
					.replaceAll(
							"^(<l><b>.*?</b>)(((?!\\s\\[=).)*?((<e>.*?</e>)|(cv\\.)).*)",
							"\\1@\\2");
			// TODO EPSILON
			// for i in range(len(EPSILONS)):
			// taxon = re.sub(r"\s(" + EPSILONS[i].replace('.', '\.').replace(
			// '[', '\[').replace(']', '\]') + r")\s<e>([\w-]+)</e>",
			// r" ~\2 \1", taxon)
			sortKey = sortKey
					.replaceAll("\\s(cv\\.)\\s'?([\\w-]+)'?", " ~\\2 \\1")
					.replace("<x> ", "").replace("<+> ", "").replace("<x>", "")
					.replace("<+>", "").replaceAll("<.*?>", "")
					.replace("'", "");
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
