package net.plantkelt.akp.webapp.wicket;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class AkpSessionData implements Serializable {
	private static final long serialVersionUID = 1L;

	private String defaultLangXid = null;
	private boolean synonymsDefaultOpen = false;
	private Set<String> lexicalGroupDefaultOpen = new HashSet<String>();

	public String getDefaultLangXid() {
		return defaultLangXid;
	}

	public void setDefautLangXid(String langXid) {
		defaultLangXid = langXid;
	}

	public boolean isSynonymsDefaultOpen() {
		return synonymsDefaultOpen;
	}

	public void setSynonymsDefaultOpen(boolean open) {
		synonymsDefaultOpen = open;
	}

	public boolean isLexicalGroupDefaultOpen(String langXid, Integer correct) {
		String key = langXid + ":" + correct.toString();
		return lexicalGroupDefaultOpen.contains(key);
	}

	public void setLexicalGroupDefaultOpen(String langXid, Integer correct,
			boolean open) {
		String key = langXid + ":" + correct.toString();
		if (open)
			lexicalGroupDefaultOpen.add(key);
		else
			lexicalGroupDefaultOpen.remove(key);
	}

}
