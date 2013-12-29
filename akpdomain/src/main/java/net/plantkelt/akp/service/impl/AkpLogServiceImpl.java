package net.plantkelt.akp.service.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpUserLogEntry;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpLogService;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class AkpLogServiceImpl implements AkpLogService, Serializable {
	private static final long serialVersionUID = 1L;

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
	public static final int LOG_TYPE_LEXGRP_DELETION = 15;
	public static final int LOG_TYPE_VERNA_PLANT_REF_DELETION = 16;
	public static final int LOG_TYPE_VERNA_PLANT_REF_CREATION = 17;
	public static final int LOG_TYPE_TAG_UPDATE = 18;
	public static final int LOG_TYPE_TAG_DELETION = 19;
	public static final int LOG_TYPE_TAG_CREATION = 20;
	public static final int LOG_TYPE_PLANT_REF_CREATION = 22;
	public static final int LOG_TYPE_PLANT_REF_DELETION = 23;
	public static final int LOG_TYPE_PLANT_CLASS_MOVE = 24;

	public static final int USERLOG_TYPE_LOGIN = 1;
	public static final int USERLOG_TYPE_LOGOUT = 2;

	private static final int OLD_NEW_VALUE_LEN = 2048;

	@Inject
	private Provider<Session> sessionProvider;

	private LoginGetter loginGetter;

	private Session getSession() {
		return sessionProvider.get();
	}

	@Transactional
	private void logNewEntry(int type, Integer plantId, Integer taxonId,
			Integer lexicalGroupId, Integer vernacularNameId, String oldValue,
			String newValue) {
		if (oldValue != null && oldValue.equals(newValue))
			return;
		AkpLogEntry logEntry = new AkpLogEntry();
		logEntry.setType(type);
		logEntry.setDate(new Date());
		logEntry.setLogin(loginGetter.getCurrentLogin());
		logEntry.setPlantId(plantId);
		logEntry.setTaxonId(taxonId);
		logEntry.setLexicalGroupId(lexicalGroupId);
		logEntry.setVernacularNameId(vernacularNameId);
		if (oldValue != null && oldValue.length() > OLD_NEW_VALUE_LEN)
			oldValue = oldValue.substring(0, OLD_NEW_VALUE_LEN);
		logEntry.setOldValue(oldValue);
		if (newValue != null && newValue.length() > OLD_NEW_VALUE_LEN)
			newValue = newValue.substring(0, OLD_NEW_VALUE_LEN);
		logEntry.setNewValue(newValue);
		getSession().save(logEntry);
		getSession().flush();
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
	public void logPlantDeletion(AkpPlant plant) {
		logNewEntry(LOG_TYPE_PLANT_DELETION, plant.getXid(), null, null, null,
				plant.getAkpClass().getName(), null);
	}

	@Override
	public void logPlantMove(AkpPlant plant, AkpClass oldClass,
			AkpClass newClass) {
		logNewEntry(LOG_TYPE_PLANT_CLASS_MOVE, plant.getXid(), null, null,
				null, oldClass.getTextName(), newClass.getTextName());
	}

	@Override
	public void logPlantCommentsUpdate(AkpPlant plant, String oldValue) {
		logNewEntry(LOG_TYPE_PLANT_COMMENT_UPDATE, plant.getXid(), null, null,
				null, oldValue, plant.getComments());
	}

	@Override
	public void logPlantRefCreation(AkpPlant plant, AkpPlant targetPlant) {
		logNewEntry(LOG_TYPE_PLANT_REF_CREATION, plant.getXid(), null, null,
				null, null, targetPlant.getMainName().getName());
	}

	@Override
	public void logPlantRefDeletion(AkpPlant plant, AkpPlant targetPlant) {
		logNewEntry(LOG_TYPE_PLANT_REF_DELETION, plant.getXid(), null, null,
				null, targetPlant.getMainName().getName(), null);
	}

	@Override
	public void logPlantTagCreation(AkpPlantTag tag) {
		logNewEntry(LOG_TYPE_TAG_CREATION, tag.getPlant().getXid(), null, null,
				null, null, tag.getType() + "/");
	}

	@Override
	public void logPlantTagUpdate(AkpPlantTag tag) {
		logNewEntry(LOG_TYPE_TAG_UPDATE, tag.getPlant().getXid(), null, null,
				null, null, tag.getType() + "/" + tag.getValue());
	}

	@Override
	public void logPlantTagDeletion(AkpPlantTag tag) {
		logNewEntry(LOG_TYPE_TAG_DELETION, tag.getPlant().getXid(), null, null,
				null, tag.getType() + "/" + tag.getValue(), null);
	}

	@Override
	public void logTaxonCreation(AkpTaxon taxon) {
		logNewEntry(LOG_TYPE_TAXON_CREATION, taxon.getPlant().getXid(),
				taxon.getXid(), null, null, null, taxon.getName());
	}

	@Override
	public void logTaxonUpdate(AkpTaxon taxon, String oldName) {
		logNewEntry(LOG_TYPE_TAXON_UPDATE, taxon.getPlant().getXid(),
				taxon.getXid(), null, null, oldName, taxon.getName());
	}

	@Override
	public void logTaxonDeletion(AkpTaxon taxon) {
		logNewEntry(LOG_TYPE_TAXON_DELETION, taxon.getPlant().getXid(),
				taxon.getXid(), null, null, taxon.getName(), null);
	}

	@Override
	public void logLexicalGroupCreation(AkpLexicalGroup lexicalGroup) {
		logNewEntry(LOG_TYPE_LEXGRP_CREATION, lexicalGroup.getPlant().getXid(),
				null, lexicalGroup.getXid(), null, null, lexicalGroup.getLang()
						.getXid() + "/" + lexicalGroup.getCorrect());
	}

	@Override
	public void logLexicalGroupDeletion(AkpLexicalGroup lexicalGroup) {
		logNewEntry(LOG_TYPE_LEXGRP_DELETION, lexicalGroup.getPlant().getXid(),
				null, lexicalGroup.getXid(), null, lexicalGroup.getLang()
						.getXid() + "/" + lexicalGroup.getCorrect(), null);
	}

	@Override
	public void logVernacularNameCreation(AkpVernacularName vernacularName) {
		logNewEntry(LOG_TYPE_VERNA_CREATION, vernacularName.getLexicalGroup()
				.getPlant().getXid(), null, vernacularName.getLexicalGroup()
				.getXid(), vernacularName.getXid(), null,
				vernacularName.getName());
	}

	@Override
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

	@Transactional
	private void userLogNewEntry(int operation, String login, String value) {
		AkpUserLogEntry userLogEntry = new AkpUserLogEntry();
		userLogEntry.setOperation(operation);
		userLogEntry.setDate(new Date());
		userLogEntry.setValue(value);
		userLogEntry.setLogin(login == null ? loginGetter.getCurrentLogin()
				: login);
		userLogEntry.setRemoteAddr(loginGetter.getCurrentRemoteAddr());
		getSession().save(userLogEntry);
	}

	@Override
	public void userLogLogin(String login) {
		userLogNewEntry(USERLOG_TYPE_LOGIN, login, null);
	}

	@Override
	public void userLogLogout() {
		userLogNewEntry(USERLOG_TYPE_LOGOUT, null, null);
	}

	@Override
	public byte[] getActivityGraph(int width, int height) {
		// TODO Cache the generated image in memory
		Query query = getSession().getNamedQuery("activityPerWeek");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.WEEK_OF_YEAR, -30);
		query.setDate("past", cal.getTime());
		@SuppressWarnings("unchecked")
		List<Object[]> objects = query.list();

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
		for (Object[] obj : objects) {
			Date date = (Date) obj[0];
			Number count = (Number) obj[1];
			dataset.setValue(count, "Modifications", df.format(date));
		}
		BarRenderer.setDefaultShadowsVisible(false);
		JFreeChart chart = ChartFactory.createBarChart("", "Week",
				"Modifications", dataset, PlotOrientation.VERTICAL, false,
				true, false);
		chart.setBackgroundPaint(Color.WHITE);
		chart.getTitle().setPaint(Color.BLACK);
		CategoryPlot p = chart.getCategoryPlot();
		CategoryAxis xAxis = (CategoryAxis) p.getDomainAxis();
		xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		BarRenderer barRenderer = ((BarRenderer) p.getRenderer());
		barRenderer.setShadowVisible(false);
		barRenderer.setSeriesPaint(0, Color.BLUE);
		barRenderer.setBarPainter(new StandardBarPainter());
		p.setRangeGridlinePaint(Color.RED);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ChartUtilities.writeChartAsPNG(baos, chart, width, height);
		} catch (IOException e) {
			// Should not happen
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}
}
