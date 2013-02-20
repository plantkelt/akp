package net.plantkelt.akp.domain;

import java.util.HashSet;
import java.util.Set;

public class AkpTaxon implements Comparable<AkpTaxon> {

	public static final int TYPE_MAIN = 0;
	public static final int TYPE_SYNONYM = 2;

	private static final String[] EPSILON_LIST = { "sect.", "subsect.",
			"subsp.", "[subsp.]", "nothosubsp.", "proles", "nothoproles",
			"var.", "[var.]", "nothovar.", "subvar.", "subgen.", "fa.",
			"subfa.", "convar.", "cv.", "infrasp." };
	private static final String[] EPSILON2_LIST = { " non ", " nec ", " ex ",
			" & ", " an ", " excl. ", " incl. ", " emend. ", " fide ", " vel ",
			" ser. ", " apud ", " lusus ", "<i>orth. var.</i>", "<i>p. p.</i>",
			"<i>p. max. p.</i>", "<i>p. min. p.</i>", "<i>lapsus calami ?</i>" };
	private static final String[] EPSILON3_LIST = { "<i>sensu</i>",
			"<i>pro sp.</i>", "<i>pro hybr.</i>" };

	private static final String[] escapedEpsilonList;

	static {
		escapedEpsilonList = new String[EPSILON_LIST.length];
		for (int i = 0; i < EPSILON_LIST.length; i++) {
			escapedEpsilonList[i] = EPSILON_LIST[i].replace(".", "\\.")
					.replace("[", "\\[").replace("]", "\\]");
		}
	}

	private Integer xid;
	private AkpPlant plant;
	private String name;
	private int type;

	private transient String sortKey;
	private transient String xName;

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
		return getXName().replace("<a>", "<span class='author'>").replace(
				"</a>", "</span>");
	}

	public String[] getNameElements() {
		return getXName().split("</?a>");
	}

	public Set<String> getReferencedAuthorIds() {
		String[] elems = getNameElements();
		Set<String> retval = new HashSet<String>(elems.length / 2 + 1);
		for (int i = 1; i < elems.length; i += 2) {
			retval.add(elems[i]);
		}
		return retval;
	}

	private synchronized String getXName() {
		if (xName == null) {
			xName = name.replace("<x>", "×").replace("<+>", "+")
					.replace("<urseurtad>", " - 1 seurtad:").replace("<l>", "")
					.replace("</l>", "").replace("<y>", "<span class='txy'>")
					.replace("</y>", "</span>");
			if (type == TYPE_MAIN) {
				xName = xName.replace("<b>", "<b><i>")
						.replace("</b>", "</i></b>").replace("<e>", "<b><i>")
						.replace("</e>", "</i></b>");
			} else {
				xName = xName.replace("<b>", "<i>").replace("</b>", "</i>")
						.replace("<e>", "<i>").replace("</e>", "</i>");
			}
			for (String eps : EPSILON_LIST) {
				xName = xName.replace(" " + eps + " ",
						" <span class='epsilon'>" + eps + "</span> ");
			}
			for (String eps : EPSILON2_LIST) {
				xName = xName.replace(" " + eps + " ",
						" <span class='epsilon'>" + eps + "</span> ");
			}
			for (String eps : EPSILON3_LIST) {
				xName = xName.replace(" " + eps + " ",
						" <span class='epsilon3'>" + eps + "</span> ");
			}
		}
		return xName;
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

	public synchronized String getSortKey() {
		if (sortKey == null) {
			sortKey = name;
			sortKey = sortKey
					.replace("spp.", "")
					.replace(", non ", "  ")
					.replaceAll(
							"^(<l><b>.*?</b>)(((?!\\s\\[=).)*?((<e>.*?</e>)|(cv\\.)).*)",
							"$1@$2");
			for (String epsilon : escapedEpsilonList) {
				sortKey = sortKey.replaceAll("\\s(" + epsilon
						+ ")\\s<e>([\\w-]+)</e>", " ~$2 $1");
			}
			sortKey = sortKey
					.replaceAll("\\s(cv\\.)\\s'?([\\w-]+)'?", " ~$2 $1")
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
