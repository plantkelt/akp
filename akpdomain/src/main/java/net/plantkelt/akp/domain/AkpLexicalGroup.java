package net.plantkelt.akp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AkpLexicalGroup {

	private int xid;
	private int correct;
	private AkpLang lang;
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

	public void setCorrect(int correct) {
		this.correct = correct;
	}

	public AkpLang getLang() {
		return lang;
	}

	public void setLang(AkpLang lang) {
		this.lang = lang;
	}

	public synchronized List<AkpVernacularName> getVernacularNames() {
		return vernacularNames;
	}

	public synchronized List<AkpVernacularName> getRootVernacularNames() {
		return rootVernacularNames;
	}

	public synchronized void setVernacularNames(
			List<AkpVernacularName> vernacularNames) {
		this.vernacularNames = vernacularNames;
		updateVernacularNamesTree();
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

}
