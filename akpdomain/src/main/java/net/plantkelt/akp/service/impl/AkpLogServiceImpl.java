package net.plantkelt.akp.service.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;
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
	public static final int LOG_TYPE_BIBREF_CREATION = 8;
	public static final int LOG_TYPE_BIBREF_UPDATE = 9;
	public static final int LOG_TYPE_BIBREF_DELETE = 10;
	public static final int LOG_TYPE_TAXON_CREATION = 11;
	public static final int LOG_TYPE_PLANT_COMMENT_UPDATE = 12;
	public static final int LOG_TYPE_VERNA_COMMENT_UPDATE = 13;
	public static final int LOG_TYPE_LEXGRP_CREATION = 14;
	public static final int LOG_TYPE_15 = 15;
	public static final int LOG_TYPE_VERNA_PLANT_REF_DELETION = 16;
	public static final int LOG_TYPE_VERNA_PLANT_REF_CREATION = 17;
	public static final int LOG_TYPE_TAG_UPDATE = 18;
	public static final int LOG_TYPE_TAG_DELETION = 19;
	public static final int LOG_TYPE_TAG_CREATION = 20;
	public static final int LOG_TYPE_20 = 21;
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

}
