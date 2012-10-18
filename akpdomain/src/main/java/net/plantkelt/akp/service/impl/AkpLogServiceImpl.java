package net.plantkelt.akp.service.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpLogService;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class AkpLogServiceImpl implements AkpLogService {

	public static final int LOG_TYPE_TAXON_DELETION = 1;
	public static final int LOG_TYPE_TAXON_UPDATE = 2;
	public static final int LOG_TYPE_PLANT_CREATION = 3;
	public static final int LOG_TYPE_PLANT_DELETION = 4;
	public static final int LOG_TYPE_VERNA_CREATION = 5;
	public static final int LOG_TYPE_VERNA_DELETION = 6;
	public static final int LOG_TYPE_VERNA_UPDATE = 7;
	public static final int LOG_TYPE_VERNA_BIB_REF_CREATION = 8;
	public static final int LOG_TYPE_VERNA_BIB_REF_DELETION = 10;
	public static final int LOG_TYPE_TAXON_CREATION = 11;
	public static final int LOG_TYPE_PLANT_COMMENT_UPDATE = 12;
	public static final int LOG_TYPE_VERNA_COMMENT_UPDATE = 13;
	public static final int LOG_TYPE_LEXGRP_CREATION = 14;
	public static final int LOG_TYPE_VERNA_PLANT_REF_DELETION = 16;
	public static final int LOG_TYPE_VERNA_PLANT_REF_CREATION = 17;
	public static final int LOG_TYPE_TAG_UPDATE = 18;
	public static final int LOG_TYPE_TAG_DELETION = 19;
	public static final int LOG_TYPE_TAG_CREATION = 20;
	public static final int LOG_TYPE_PLANT_REF_CREATION = 22;
	public static final int LOG_TYPE_PLANT_REF_DELETION = 23;

	@Inject
	private Provider<Session> sessionProvider;

	private LoginGetter loginGetter;

	private Session getSession() {
		return sessionProvider.get();
	}

	private void logNewEntry(int type, Integer plantId, Integer taxonId,
			Integer lexicalGroupId, Integer vernacularNameId, String oldValue,
			String newValue) {
		AkpLogEntry logEntry = new AkpLogEntry();
		logEntry.setType(type);
		logEntry.setDate(new Date());
		logEntry.setLogin(loginGetter.getCurrentLogin());
		logEntry.setPlantId(plantId);
		logEntry.setTaxonId(taxonId);
		logEntry.setLexicalGroupId(lexicalGroupId);
		logEntry.setVernacularNameId(vernacularNameId);
		logEntry.setOldValue(oldValue);
		logEntry.setNewValue(newValue);
		getSession().save(logEntry);
	}

	@Override
	public void setLoginGetter(LoginGetter loginGetter) {
		if (this.loginGetter != null)
			throw new IllegalArgumentException();
		this.loginGetter = loginGetter;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AkpLogEntry> getPlantLogs(Integer plantId) {
		return (List<AkpLogEntry>) getSession()
				.createCriteria(AkpLogEntry.class)
				.add(Restrictions.eq("plantId", plantId))
				.addOrder(Order.desc("date")).list();
	}

	@Override
	@Transactional
	public void logPlantCreation(AkpPlant plant) {
		logNewEntry(LOG_TYPE_PLANT_CREATION, plant.getXid(), null, null, null,
				null, plant.getAkpClass().getName());
	}

	@Override
	@Transactional
	public void logPlantDeletion(AkpPlant plant) {
		logNewEntry(LOG_TYPE_PLANT_DELETION, plant.getXid(), null, null, null,
				plant.getAkpClass().getName(), null);
	}

	@Override
	@Transactional
	public void logPlantCommentsUpdate(AkpPlant plant, String oldValue) {
		logNewEntry(LOG_TYPE_PLANT_COMMENT_UPDATE, plant.getXid(), null, null,
				null, oldValue, plant.getComments());
	}

	@Override
	@Transactional
	public void logPlantRefCreation(AkpPlant plant, AkpPlant targetPlant) {
		logNewEntry(LOG_TYPE_PLANT_REF_CREATION, plant.getXid(), null, null,
				null, null, targetPlant.getMainName().getName());
	}

	@Override
	@Transactional
	public void logPlantRefDeletion(AkpPlant plant, AkpPlant targetPlant) {
		logNewEntry(LOG_TYPE_PLANT_REF_DELETION, plant.getXid(), null, null,
				null, targetPlant.getMainName().getName(), null);
	}

	@Override
	@Transactional
	public void logPlantTagCreation(AkpPlantTag tag) {
		logNewEntry(LOG_TYPE_TAG_CREATION, tag.getPlant().getXid(), null, null,
				null, null, tag.getType() + "/");
	}

	@Override
	@Transactional
	public void logPlantTagUpdate(AkpPlantTag tag) {
		logNewEntry(LOG_TYPE_TAG_UPDATE, tag.getPlant().getXid(), null, null,
				null, null, tag.getType() + "/" + tag.getValue());
	}

	@Override
	@Transactional
	public void logPlantTagDeletion(AkpPlantTag tag) {
		logNewEntry(LOG_TYPE_TAG_DELETION, tag.getPlant().getXid(), null, null,
				null, tag.getType() + "/" + tag.getValue(), null);
	}

	@Override
	@Transactional
	public void logTaxonCreation(AkpTaxon taxon) {
		logNewEntry(LOG_TYPE_TAXON_CREATION, taxon.getPlant().getXid(),
				taxon.getXid(), null, null, null, taxon.getName());
	}

	@Override
	@Transactional
	public void logTaxonUpdate(AkpTaxon taxon, String oldName) {
		logNewEntry(LOG_TYPE_TAXON_UPDATE, taxon.getPlant().getXid(),
				taxon.getXid(), null, null, oldName, taxon.getName());
	}

	@Override
	@Transactional
	public void logTaxonDeletion(AkpTaxon taxon) {
		logNewEntry(LOG_TYPE_TAXON_DELETION, taxon.getPlant().getXid(),
				taxon.getXid(), null, null, taxon.getName(), null);
	}

	@Override
	@Transactional
	public void logLexicalGroupCreation(AkpLexicalGroup lexicalGroup) {
		logNewEntry(LOG_TYPE_LEXGRP_CREATION, lexicalGroup.getPlant().getXid(),
				null, lexicalGroup.getXid(), null, null, lexicalGroup.getLang()
						.getXid() + "/" + lexicalGroup.getCorrect());
	}

	@Override
	@Transactional
	public void logVernacularNameCreation(AkpVernacularName vernacularName) {
		logNewEntry(LOG_TYPE_VERNA_CREATION, vernacularName.getLexicalGroup()
				.getPlant().getXid(), null, vernacularName.getLexicalGroup()
				.getXid(), vernacularName.getXid(), null,
				vernacularName.getName());
	}

	@Override
	@Transactional
	public void logVernacularNameDeletion(AkpVernacularName vernacularName) {
		logNewEntry(LOG_TYPE_VERNA_DELETION, vernacularName.getLexicalGroup()
				.getPlant().getXid(), null, vernacularName.getLexicalGroup()
				.getXid(), vernacularName.getXid(), vernacularName.getName(),
				null);
	}

	@Override
	public void logVernacularNameCommentsUpdate(
			AkpVernacularName vernacularName, String oldComments) {
		logNewEntry(LOG_TYPE_VERNA_COMMENT_UPDATE, vernacularName
				.getLexicalGroup().getPlant().getXid(), null, vernacularName
				.getLexicalGroup().getXid(), vernacularName.getXid(),
				oldComments, vernacularName.getComments());
	}

	@Override
	public void logVernacularNameNameUpdate(AkpVernacularName vernacularName,
			String oldName) {
		logNewEntry(LOG_TYPE_VERNA_UPDATE, vernacularName.getLexicalGroup()
				.getPlant().getXid(), null, vernacularName.getLexicalGroup()
				.getXid(), vernacularName.getXid(), oldName,
				vernacularName.getName());
	}

	@Override
	public void logVernacularNameBibAddition(AkpVernacularName vernacularName,
			AkpBib bib) {
		logNewEntry(LOG_TYPE_VERNA_BIB_REF_CREATION, vernacularName
				.getLexicalGroup().getPlant().getXid(), null, vernacularName
				.getLexicalGroup().getXid(), vernacularName.getXid(), null,
				bib.getXid());
	}

	@Override
	public void logVernacularNameBibRemoval(AkpVernacularName vernacularName,
			AkpBib bib) {
		logNewEntry(LOG_TYPE_VERNA_BIB_REF_DELETION, vernacularName
				.getLexicalGroup().getPlant().getXid(), null, vernacularName
				.getLexicalGroup().getXid(), vernacularName.getXid(),
				bib.getXid(), null);
	}

	@Override
	public void logVernacularNamePlantRefAddition(
			AkpVernacularName vernacularName, AkpPlant plant) {
		logNewEntry(LOG_TYPE_VERNA_PLANT_REF_CREATION, vernacularName
				.getLexicalGroup().getPlant().getXid(), null, vernacularName
				.getLexicalGroup().getXid(), vernacularName.getXid(), null,
				plant.getMainName().getName());
	}

	@Override
	public void logVernacularNamePlantRefRemoval(
			AkpVernacularName vernacularName, AkpPlant plant) {
		logNewEntry(LOG_TYPE_VERNA_PLANT_REF_DELETION, vernacularName
				.getLexicalGroup().getPlant().getXid(), null, vernacularName
				.getLexicalGroup().getXid(), vernacularName.getXid(), plant
				.getMainName().getName(), null);
	}
}
