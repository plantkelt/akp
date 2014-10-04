package net.plantkelt.akp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AkpLexicalGroup implements Comparable<AkpLexicalGroup> {

	private static final String[] CORRECT_DISPLAY_CODES = { "=", "≈", "≠", "?" };

	public static final int MAX_CORRECT = 3;

	private int xid;
	private int correct;
	private AkpLang lang;
	private AkpPlant plant;
	private List<AkpVernacularName> vernacularNames;
	private List<AkpVernacularName> rootVernacularNames;

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public int getCorrect() {
		return correct;
	}

	public String getCorrectDisplayCode() {
		return getCorrectDisplayCode(correct);
	}

	public static String getCorrectDisplayCode(int correct) {
		if (correct >= 0 && correct < CORRECT_DISPLAY_CODES.length)
			return CORRECT_DISPLAY_CODES[correct];
		else
			return "" + correct;
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

	public AkpPlant getPlant() {
		return plant;
	}

	public void setPlant(AkpPlant plant) {
		this.plant = plant;
	}

	public synchronized List<AkpVernacularName> getVernacularNames() {
		return vernacularNames;
	}

	public synchronized List<AkpVernacularName> getRootVernacularNames() {
		if (rootVernacularNames == null)
			updateVernacularNamesTree();
		return rootVernacularNames;
	}

	public synchronized void setVernacularNames(
			List<AkpVernacularName> vernacularNames) {
		this.vernacularNames = vernacularNames;
	}

	public synchronized void refreshVernacularNamesTree() {
		updateVernacularNamesTree();
	}

	private void updateVernacularNamesTree() {
		rootVernacularNames = new ArrayList<AkpVernacularName>();
		Map<Integer, AkpVernacularName> vernaMap = new HashMap<Integer, AkpVernacularName>(
				vernacularNames.size());
		for (AkpVernacularName vernaName : vernacularNames) {
			vernaMap.put(vernaName.getXid(), vernaName);
			vernaName.setChildren(new ArrayList<AkpVernacularName>());
		}
		for (AkpVernacularName vernaName : vernacularNames) {
			Integer parentId = vernaName.getParentId();
			if (parentId != null && parentId != 0) {
				AkpVernacularName parentName = vernaMap.get(parentId);
				vernaName.setParent(parentName);
				parentName.getChildren().add(vernaName);
			} else {
				rootVernacularNames.add(vernaName);
			}
		}
		for (AkpVernacularName vernaName : vernacularNames) {
			Collections.sort(vernaName.getChildren());
		}
		Collections.sort(rootVernacularNames);
	}

	@Override
	public String toString() {
		return String.format("[AkpLexicalGroup %d %d]", getXid(), getCorrect());
	}

	@Override
	public int compareTo(AkpLexicalGroup o) {
		AkpLangGroup langGroup = getLang().getLangGroup();
		AkpLangGroup oLangGroup = o.getLang().getLangGroup();
		int cmp = langGroup.getOrder() - oLangGroup.getOrder();
		if (cmp == 0) {
			cmp = getLang().getOrder() - o.getLang().getOrder();
		}
		return cmp;
	}

}