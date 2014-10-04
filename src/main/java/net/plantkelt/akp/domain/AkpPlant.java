package net.plantkelt.akp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

public class AkpPlant implements Comparable<AkpPlant> {

	private Integer xid;
	private AkpClass akpClass;
	private String comments;
	private SortedSet<AkpPlantTag> tags;
	private List<AkpTaxon> taxons;
	private List<AkpLexicalGroup> lexicalGroups;
	private List<AkpPlant> plantRefs;

	private transient AkpTaxon mainName;
	private transient List<AkpTaxon> synonyms;

	public AkpPlant() {
	}

	public AkpPlant(AkpClass owningClass, AkpTaxon mainName) {
		this.akpClass = owningClass;
		mainName.setType(AkpTaxon.TYPE_MAIN);
		mainName.setPlant(this);
		taxons = new ArrayList<AkpTaxon>();
		taxons.add(mainName);
		lexicalGroups = Collections.emptyList();
		plantRefs = Collections.emptyList();
		comments = "";
	}

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
		this.comments = comments == null ? "" : comments;
	}

	public SortedSet<AkpPlantTag> getTags() {
		return tags;
	}

	public void setTags(SortedSet<AkpPlantTag> tags) {
		this.tags = tags;
	}

	public synchronized List<AkpTaxon> getTaxons() {
		return taxons;
	}

	public synchronized void removeTaxon(AkpTaxon taxon) {
		if (taxon.getType() == AkpTaxon.TYPE_MAIN)
			throw new IllegalArgumentException(
					"Cannot remove main name from plant");
		mainName = null;
		synonyms = null;
		this.taxons.remove(taxon);
	}

	public synchronized void addTaxon(AkpTaxon taxon) {
		if (taxon.getType() == AkpTaxon.TYPE_MAIN)
			throw new IllegalArgumentException(
					"Cannot add main name from plant");
		mainName = null;
		synonyms = null;
		this.taxons.add(taxon);
	}

	public synchronized void setTaxons(List<AkpTaxon> taxons) {
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
		Collections.sort(synonyms);
		return synonyms;
	}

	public List<AkpLexicalGroup> getLexicalGroups() {
		return lexicalGroups;
	}

	public void setLexicalGroups(List<AkpLexicalGroup> lexicalGroups) {
		this.lexicalGroups = lexicalGroups;
	}

	public List<AkpLexicalGroup> getSortedLexicalGroups() {
		Collections.sort(lexicalGroups);
		return lexicalGroups;
	}

	public List<AkpPlant> getPlantRefs() {
		return plantRefs;
	}

	public void setPlantRefs(List<AkpPlant> plantRefs) {
		this.plantRefs = plantRefs;
	}

	private void updateTaxons() {
		synonyms = new ArrayList<AkpTaxon>(getTaxons().size() - 1);
		for (AkpTaxon taxon : getTaxons()) {
			switch (taxon.getType()) {
			case AkpTaxon.TYPE_MAIN:
				mainName = taxon;
				break;
			case AkpTaxon.TYPE_SYNONYM:
				synonyms.add(taxon);
				break;
			default:
				throw new RuntimeException("Invalid taxon type: "
						+ taxon.getType());
			}
		}
	}

	@Override
	public int hashCode() {
		return getXid() != null ? getXid().hashCode() : 0;
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
