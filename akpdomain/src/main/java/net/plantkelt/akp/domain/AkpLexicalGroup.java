package net.plantkelt.akp.domain;

import java.util.List;

public class AkpLexicalGroup {

	private int xid;
	private int correct;
	private AkpLang lang;
	private List<AkpVernacularName> vernacularNames;

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public int getCorrect() {
		return correct;
	}

	public void setCorrect(int correct) {
		this.correct = correct;
	}

	public AkpLang getLang() {
		return lang;
	}

	public void setLang(AkpLang lang) {
		this.lang = lang;
	}

	public List<AkpVernacularName> getVernacularNames() {
		return vernacularNames;
	}

	public void setVernacularNames(List<AkpVernacularName> vernacularNames) {
		this.vernacularNames = vernacularNames;
	}

	@Override
	public String toString() {
		return String.format("[AkpLexicalGroup %d %d]", getXid(), getCorrect());
	}

}
