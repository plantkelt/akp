package net.plantkelt.akp.domain;

import java.util.Date;

public class AkpLogEntry {

	private Integer xid;
	private Date date;
	private String login;
	private Integer type;
	private Integer plantId;
	private Integer lexicalGroupId;
	private Integer vernacularNameId;
	private Integer taxonId;
	private String oldValue;
	private String newValue;

	public Integer getXid() {
		return xid;
	}

	public void setXid(Integer xid) {
		this.xid = xid;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getPlantId() {
		return plantId;
	}

	public void setPlantId(Integer plantId) {
		this.plantId = plantId;
	}

	public Integer getLexicalGroupId() {
		return lexicalGroupId;
	}

	public void setLexicalGroupId(Integer lexicalGroupId) {
		this.lexicalGroupId = lexicalGroupId;
	}

	public Integer getVernacularNameId() {
		return vernacularNameId;
	}

	public void setVernacularNameId(Integer vernacularNameId) {
		this.vernacularNameId = vernacularNameId;
	}

	public Integer getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(Integer taxonId) {
		this.taxonId = taxonId;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
}
