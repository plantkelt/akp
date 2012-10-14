package net.plantkelt.akp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AkpPlant implements Comparable<AkpPlant> {

	private Integer xid;
	private AkpClass akpClass;
	private String comments;
	private List<AkpTaxon> taxons;

	private transient AkpTaxon mainName;
	private transient List<AkpTaxon> synonyms;

	public Integer getXid() {
		return xid;
	}

	public void setXid(Integer xid) {
		this.xid = xid;
	}

	public AkpClass getAkpClass() {
		return akpClass;
	}

	public void setAkpClass(AkpClass akpClass) {
		this.akpClass = akpClass;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public synchronized List<AkpTaxon> getTaxons() {
		return taxons;
	}

	public synchronized void setTaxons(List<AkpTaxon> taxons) {
		mainName = null;
		synonyms = null;
		this.taxons = taxons;
	}

	public synchronized AkpTaxon getMainName() {
		if (mainName == null)
			updateTaxons();
		return mainName;
	}

	public synchronized List<AkpTaxon> getSynonyms() {
		if (synonyms == null)
			updateTaxons();
		return synonyms;
	}

	private void updateTaxons() {
		synonyms = new ArrayList<AkpTaxon>(getTaxons().size() - 1);
		for (AkpTaxon taxon : getTaxons()) {
			switch (taxon.getType()) {
			case 0:
				mainName = taxon;
				break;
			case 2:
				synonyms.add(taxon);
				break;
			default:
				throw new RuntimeException("Invalid taxon type: "
						+ taxon.getType());
			}
		}
		Collections.sort(synonyms);
	}

	@Override
	public String toString() {
		return String.format("[AkpPlant #%d]", getXid());
	}

	@Override
	public int compareTo(AkpPlant other) {
		return getMainName().compareTo(other.getMainName());
	}

}
