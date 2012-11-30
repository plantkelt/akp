package net.plantkelt.akp.service;

import java.io.Serializable;
import java.util.List;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpVernacularName;

public interface AkpLogService {

	public interface LoginGetter extends Serializable {

		public String getCurrentLogin();

		public String getCurrentRemoteAddr();
	}

	public abstract void setLoginGetter(LoginGetter loginGetter);

	public abstract List<AkpLogEntry> getPlantLogs(Integer plantId);

	public abstract void logPlantCreation(AkpPlant plant);

	public abstract void logPlantDeletion(AkpPlant plant);

	public abstract void logPlantCommentsUpdate(AkpPlant plant, String oldValue);

	public abstract void logPlantRefCreation(AkpPlant plant,
			AkpPlant targetPlant);

	public abstract void logPlantRefDeletion(AkpPlant plant,
			AkpPlant targetPlant);

	public abstract void logPlantTagCreation(AkpPlantTag tag);

	public abstract void logPlantTagUpdate(AkpPlantTag tag);

	public abstract void logPlantTagDeletion(AkpPlantTag tag);

	public abstract void logTaxonCreation(AkpTaxon taxon);

	public abstract void logTaxonUpdate(AkpTaxon taxon, String oldName);

	public abstract void logTaxonDeletion(AkpTaxon taxon);

	public abstract void logLexicalGroupCreation(AkpLexicalGroup lexicalGroup);

	public abstract void logVernacularNameCreation(
			AkpVernacularName vernacularName);

	public abstract void logVernacularNameDeletion(
			AkpVernacularName vernacularName);

	public abstract void logVernacularNameCommentsUpdate(
			AkpVernacularName vernacularName, String oldComments);

	public abstract void logVernacularNameNameUpdate(
			AkpVernacularName vernacularName, String oldName);

	public abstract void logVernacularNameBibAddition(
			AkpVernacularName vernacularName, AkpBib bib);

	public abstract void logVernacularNameBibRemoval(
			AkpVernacularName vernacularName, AkpBib bib);

	public abstract void logVernacularNamePlantRefAddition(
			AkpVernacularName vernacularName, AkpPlant plant);

	public abstract void logVernacularNamePlantRefRemoval(
			AkpVernacularName vernacularName, AkpPlant plant);

	public abstract void userLogLogin(String login);

	public abstract void userLogLogout();

	public abstract byte[] getActivityGraph(int width, int height);
}
