package net.plantkelt.akp.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class AkpSearchData implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum AkpSearchType {
		VERNA, TAXON
	};

	private int limit;
	private String taxonName;
	private boolean includeSynonyms;
	private String plantOrigin;
	private String plantComments;
	private Integer familyXid;
	private String vernacularName;
	private Set<String> langXids = new HashSet<String>();
	private String bibRefXid;
	private String vernacularNameComments;

	public AkpSearchData() {
		limit = 10;
		includeSynonyms = true;
	}

	public AkpSearchType getSearchType() {
		if (vernacularName != null || vernacularNameComments != null
				|| bibRefXid != null)
			return AkpSearchType.VERNA;
		if (taxonName != null || plantComments != null || plantOrigin != null
				|| familyXid != null)
			return AkpSearchType.TAXON;
		return null;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getTaxonName() {
		return taxonName;
	}

	public void setTaxonName(String taxonName) {
		this.taxonName = taxonName;
	}

	public boolean isIncludeSynonyms() {
		return includeSynonyms;
	}

	public void setIncludeSynonyms(boolean includeSynonyms) {
		this.includeSynonyms = includeSynonyms;
	}

	public String getVernacularName() {
		return vernacularName;
	}

	public void setVernacularName(String vernacularName) {
		this.vernacularName = vernacularName;
	}

	public String getPlantComments() {
		return plantComments;
	}

	public void setPlantComments(String plantComments) {
		this.plantComments = plantComments;
	}

	public String getVernacularNameComments() {
		return vernacularNameComments;
	}

	public void setVernacularNameComments(String vernacularNameComments) {
		this.vernacularNameComments = vernacularNameComments;
	}

	public String getPlantOrigin() {
		return plantOrigin;
	}

	public void setPlantOrigin(String plantOrigin) {
		this.plantOrigin = plantOrigin;
	}

	public Integer getFamilyXid() {
		return familyXid;
	}

	public void setFamilyXid(Integer familyXid) {
		this.familyXid = familyXid;
	}

	public Set<String> getLangXids() {
		return langXids;
	}

	public void setLangXids(Set<String> langXids) {
		this.langXids = langXids;
	}

	public String getBibRefXid() {
		return bibRefXid;
	}

	public void setBibRefXid(String bibRefXid) {
		this.bibRefXid = bibRefXid;
	}

}
