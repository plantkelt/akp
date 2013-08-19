package net.plantkelt.akp.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpAuthor;
import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpLangGroup;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpSearchData;
import net.plantkelt.akp.domain.AkpSearchData.AkpSearchType;
import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.domain.AkpSearchResult.AkpSearchResultColumn;
import net.plantkelt.akp.domain.AkpSearchResult.AkpSearchResultRow;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.utils.MapUtils;
import net.plantkelt.akp.utils.Pair;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class AkpTaxonServiceImpl implements AkpTaxonService, Serializable {
	private static final long serialVersionUID = 1L;

	private static final boolean DEBUG_TAXON_SORT = false;

	private Logger log = Logger.getLogger(AkpTaxonServiceImpl.class);

	@Inject
	private Provider<Session> sessionProvider;
	@Inject
	private AkpLogService akpLogService;
	@Inject
	private AkpLoginService akpLoginService;

	private static Set<Integer> PUBLIC_PLANT_XIDS;

	private String staticIndexLocation = "";

	static {
		PUBLIC_PLANT_XIDS = new HashSet<Integer>();
		PUBLIC_PLANT_XIDS.add(4648);
		PUBLIC_PLANT_XIDS.add(478);
		PUBLIC_PLANT_XIDS.add(4029);
		PUBLIC_PLANT_XIDS.add(2640);
		PUBLIC_PLANT_XIDS.add(3271);
		PUBLIC_PLANT_XIDS.add(1974);
	}

	@Transactional
	@Override
	public AkpClass getClass(Integer xid) {
		if (xid == null) {
			// Root classes: create a dummy container
			@SuppressWarnings("unchecked")
			List<AkpClass> rootClasses = getSession()
					.createCriteria(AkpClass.class)
					.add(Restrictions.isNull("parent")).list();
			AkpClass rootClass = new AkpClass();
			rootClass.setChildren(rootClasses);
			rootClass.setName("/");
			rootClass.setSynonyms("");
			rootClass.setComments("");
			return rootClass;
		} else {
			return (AkpClass) getSession().get(AkpClass.class, xid);
		}
	}

	@Transactional
	@Override
	public List<AkpClass> getFamilies() {
		@SuppressWarnings("unchecked")
		List<AkpClass> families = getSession().createCriteria(AkpClass.class)
				.add(Restrictions.eq("level", AkpClass.LEVEL_FAMILY)).list();
		Collections.sort(families, new Comparator<AkpClass>() {
			@Override
			public int compare(AkpClass o1, AkpClass o2) {
				return o1.getTextName().compareTo(o2.getTextName());
			}
		});
		return families;
	}

	@Transactional
	@Override
	public void moveDownChildClass(AkpClass parentClass,
			int childIndexToMoveDown) {
		// TODO Use hibernate list-index
		List<AkpClass> children = parentClass.getChildren();
		if (childIndexToMoveDown >= children.size() - 1)
			return;
		AkpClass child1 = children.get(childIndexToMoveDown);
		AkpClass child2 = children.get(childIndexToMoveDown + 1);
		int order1 = child1.getOrder();
		int order2 = child2.getOrder();
		child1.setOrder(order2);
		child2.setOrder(order1);
		getSession().update(child1);
		getSession().update(child2);
		getSession().flush();
	}

	@Transactional
	@Override
	public void createNewClass(AkpClass parentClass) {
		AkpClass newClass = new AkpClass();
		newClass.setName("<l>Zzz</l>");
		newClass.setComments("");
		newClass.setSynonyms("");
		newClass.setLevel(parentClass.getLevel() + 1);
		newClass.setParent(parentClass);
		int maxOrder = -1;
		for (AkpClass child : parentClass.getChildren()) {
			if (child.getOrder() > maxOrder)
				maxOrder = child.getOrder();
		}
		newClass.setOrder(maxOrder + 1);
		parentClass.getChildren().add(newClass);
		getSession().save(newClass);
		getSession().update(parentClass);
		getSession().flush();
	}

	@Transactional
	@Override
	public void updateClass(AkpClass akpClass) {
		getSession().update(akpClass);
		getSession().flush();
	}

	@Override
	public boolean canDeleteClass(AkpClass akpClass) {
		if (akpClass.getChildren().size() > 0)
			return false;
		if (akpClass.getPlants().size() > 0)
			return false;
		return true;
	}

	@Transactional
	@Override
	public boolean deleteClass(AkpClass akpClass) {
		if (!canDeleteClass(akpClass))
			return false;
		int order = akpClass.getOrder();
		AkpClass parentClass = akpClass.getParent();
		if (parentClass != null) {
			for (AkpClass sibling : parentClass.getChildren()) {
				if (sibling.getOrder() > order) {
					sibling.setOrder(sibling.getOrder() - 1);
					getSession().update(sibling);
				}
			}
		}
		getSession().delete(akpClass);
		getSession().flush();
		return true;
	}

	@Transactional
	@Override
	public AkpPlant createNewPlant(AkpClass owningClass) {
		AkpTaxon mainTaxon = new AkpTaxon();
		mainTaxon.setType(AkpTaxon.TYPE_MAIN);
		mainTaxon.setName("<l><b>Aaa aaa</b></l>");
		AkpPlant plant = new AkpPlant(owningClass, mainTaxon);
		getSession().save(plant);
		getSession().save(mainTaxon);
		akpLogService.logPlantCreation(plant);
		return plant;
	}

	@Transactional
	@Override
	public AkpPlant getPlant(Integer xid) {
		// return (AkpPlant) getSession().createCriteria(AkpPlant.class)
		// .add(Restrictions.eq("xid", xid))
		// .setFetchMode("lexicalGroups", FetchMode.JOIN).uniqueResult();
		return (AkpPlant) getSession().get(AkpPlant.class, xid);
	}

	@Override
	public Set<Integer> getPublicPlantXids() {
		return PUBLIC_PLANT_XIDS;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AkpPlant> searchPlantFromName(String name) {
		return (List<AkpPlant>) getSession()
				.createCriteria(AkpPlant.class)
				.createCriteria("taxons")
				.add(Restrictions.and(
						Restrictions.eq("type", AkpTaxon.TYPE_MAIN),
						Restrictions.like("name", "%" + name + "%"))).list();
	}

	@Transactional
	@Override
	public void updatePlantComments(AkpPlant plant, String newComments) {
		String oldComments = plant.getComments();
		plant.setComments(newComments);
		getSession().update(plant);
		akpLogService.logPlantCommentsUpdate(plant, oldComments);
	}

	@Transactional
	@Override
	public void addPlantRefToPlant(AkpPlant plant, AkpPlant targetPlant) {
		if (!plant.equals(targetPlant)
				&& !plant.getPlantRefs().contains(targetPlant)) {
			plant.getPlantRefs().add(targetPlant);
			getSession().update(plant);
			akpLogService.logPlantRefCreation(plant, targetPlant);
		}
	}

	@Transactional
	@Override
	public void removePlantRefFromPlant(AkpPlant plant, AkpPlant targetPlant) {
		plant.getPlantRefs().remove(targetPlant);
		getSession().update(plant);
		akpLogService.logPlantRefDeletion(plant, targetPlant);
	}

	@Transactional
	@Override
	public boolean canDeletePlant(AkpPlant plant) {
		return plant.getLexicalGroups().size() == 0
				&& plant.getTags().size() == 0
				&& plant.getSynonyms().size() == 0
				&& plant.getPlantRefs().size() == 0
				&& getPlantBackRefs(plant).size() == 0
				&& getVernacularNameBackRefs(plant).size() == 0;
	}

	@Transactional
	@Override
	public boolean deletePlant(AkpPlant plant) {
		if (!canDeletePlant(plant))
			return false;
		akpLogService.logPlantDeletion(plant);
		getSession().delete(plant.getMainName());
		getSession().delete(plant);
		getSession().flush();
		return true;
	}

	@Transactional
	@Override
	public boolean createNewPlantTag(AkpPlant plant, int tagType) {
		for (AkpPlantTag tag : plant.getTags())
			if (tag.getType() == tagType)
				return false;
		AkpPlantTag tag = new AkpPlantTag(plant, tagType);
		getSession().save(tag);
		plant.getTags().add(tag);
		akpLogService.logPlantTagCreation(tag);
		return true;
	}

	@Transactional
	@Override
	public void updatePlantTag(AkpPlantTag tag) {
		getSession().update(tag);
		akpLogService.logPlantTagUpdate(tag);
	}

	@Transactional
	@Override
	public void deletePlantTag(AkpPlantTag tag) {
		akpLogService.logPlantTagDeletion(tag);
		AkpPlant plant = tag.getPlant();
		plant.getTags().remove(tag);
		getSession().delete(tag);
		getSession().flush();
	}

	@Transactional
	@Override
	public void createNewTaxon(AkpPlant ownerPlant) {
		AkpTaxon taxon = new AkpTaxon();
		taxon.setPlant(ownerPlant);
		taxon.setType(AkpTaxon.TYPE_SYNONYM);
		taxon.setName("<l><b></b></l>");
		ownerPlant.addTaxon(taxon);
		getSession().save(taxon);
		akpLogService.logTaxonCreation(taxon);
	}

	@Transactional
	@Override
	public void updateTaxonName(AkpTaxon taxon, String newName) {
		String oldName = taxon.getName();
		taxon.setName(newName);
		getSession().update(taxon);
		akpLogService.logTaxonUpdate(taxon, oldName);
	}

	private boolean checkOpCl(String str, String op, String cl) {
		int nop = str.split(Pattern.quote(op), -1).length - 1;
		int ncl = str.split(Pattern.quote(cl), -1).length - 1;
		return nop == ncl;
	}

	@Override
	public List<String> checkTaxon(AkpTaxon taxon) {
		List<String> retval = new ArrayList<String>();
		String taxonName = taxon.getName();
		if (!checkOpCl(taxonName, "\\(", "\\)"))
			retval.add("error.parenthesis.count.match");
		if (!checkOpCl(taxonName, "\\[", "\\]"))
			retval.add("error.square.brackets.count.match");
		if (!checkOpCl(taxonName, "<l>", "</l>"))
			retval.add("error.tag.l.count.match");
		if (!checkOpCl(taxonName, "<b>", "</b>"))
			retval.add("error.tag.b.count.match");
		if (!checkOpCl(taxonName, "<i>", "</i>"))
			retval.add("error.tag.i.count.match");
		if (!checkOpCl(taxonName, "<e>", "</e>"))
			retval.add("error.tag.e.count.match");
		if (!checkOpCl(taxonName, "<a>", "</a>"))
			retval.add("error.tag.a.count.match");
		if (taxonName.contains("  "))
			retval.add("error.double.space");
		if (taxonName.matches(".*\\w(\\(|\\[).*"))
			retval.add("error.no.space.before.parenthesis");
		if (taxonName.matches(".*(\\)|\\])\\w.*"))
			retval.add("error.no.space.after.parenthesis");
		if (taxonName.matches(".*/\\w>\\w.*"))
			retval.add("error.no.space.before.opening.tag");
		if (taxonName.matches(".*\\w<\\w.*"))
			retval.add("error.no.space.after.closing.tag");
		if (taxonName.contains(" ."))
			retval.add("error.space.before.dot");
		if (taxonName.contains(" nec ") && !taxonName.contains(" non "))
			retval.add("error.nec.without.non");
		if (taxonName.matches(".*([^\\s\\w]|[^,]\\s)(non|nec)\\s.*"))
			retval.add("error.nec.non.missing.comma");
		if (taxonName.matches(".*\\[\\s+=.*"))
			retval.add("error.space.between.brackets.and.equals");
		if (taxonName.matches(".*=[^\\s].*"))
			retval.add("error.no.space.after.equals");
		Matcher synMatcher = Pattern.compile("<l>.*?</l>").matcher(taxonName);
		for (int i = 1; i <= synMatcher.groupCount(); i++) {
			String syn = synMatcher.group(i);
			if (!syn.matches("^<l><(i|b)>(<(x|\\+)>)??[A-Z][a-z,\\-]+? (<(x|\\+)> )??([a-z,\\-]+?)|(spp\\.)</(i|b)>")) {
				retval.add("error.invalid.taxon.structure");
				break;
			}
		}
		// Unknown author: no need to do since they will be printed in red.
		// Matcher authMatcher = Pattern.compile("<a>(.*?)</a>")
		// .matcher(taxonName);
		// System.out.println("auth group count=" + authMatcher.groupCount());
		// for (int i = 1; i <= authMatcher.groupCount(); i++) {
		// String auth = authMatcher.group(i);
		// System.out.println("auth='" + auth + "'");
		// // TODO
		// // error.unknown.author
		// }
		// TODO error.missing.e.tag.after.epsilon
		return retval;
	}

	@Transactional
	@Override
	public void deleteTaxon(AkpTaxon taxon) {
		AkpPlant ownerPlant = taxon.getPlant();
		ownerPlant.removeTaxon(taxon);
		getSession().delete(taxon);
		getSession().update(ownerPlant);
		akpLogService.logTaxonDeletion(taxon);
	}

	@Transactional
	@Override
	public void addRootVernacularName(AkpLexicalGroup lexicalGroup,
			String defaultBib) {
		AkpVernacularName name = new AkpVernacularName();
		name.setLexicalGroup(lexicalGroup);
		name.setName("");
		name.setComments("");
		name.setParentId(0);
		name.setBibs(new ArrayList<AkpBib>(0));
		lexicalGroup.getVernacularNames().add(name);
		lexicalGroup.refreshVernacularNamesTree();
		getSession().save(name);
		if (defaultBib != null) {
			AkpBib defBib = getBib(defaultBib);
			if (defBib != null) {
				addBibToVernacularName(defBib, name);
			}
		}
	}

	@Transactional
	@Override
	public void addChildVernacularName(AkpVernacularName parentName,
			String defaultBib) {
		AkpLexicalGroup lexicalGroup = parentName.getLexicalGroup();
		AkpVernacularName name = new AkpVernacularName();
		name.setLexicalGroup(lexicalGroup);
		name.setName("");
		name.setComments("");
		name.setParentId(parentName.getXid());
		name.setBibs(new ArrayList<AkpBib>(0));
		lexicalGroup.getVernacularNames().add(name);
		lexicalGroup.refreshVernacularNamesTree();
		getSession().save(name);
		if (defaultBib != null) {
			AkpBib defBib = getBib(defaultBib);
			if (defBib != null) {
				addBibToVernacularName(defBib, name);
			}
		}
	}

	@Transactional
	@Override
	public void updateVernacularNameName(AkpVernacularName vernacularName,
			String newName) {
		String oldName = vernacularName.getName();
		vernacularName.setName(newName);
		vernacularName.getLexicalGroup().refreshVernacularNamesTree();
		if (newName.equals("#"))
			vernacularName.setBibs(new ArrayList<AkpBib>(0));
		getSession().update(vernacularName);
		if (oldName.length() == 0)
			akpLogService.logVernacularNameCreation(vernacularName);
		else
			akpLogService.logVernacularNameNameUpdate(vernacularName, oldName);
	}

	@Transactional
	@Override
	public void updateVernacularNameComments(AkpVernacularName vernacularName,
			String newComments) {
		String oldComments = vernacularName.getComments();
		vernacularName.setComments(newComments);
		getSession().update(vernacularName);
		akpLogService.logVernacularNameCommentsUpdate(vernacularName,
				oldComments);
	}

	@Transactional
	@Override
	public void addBibToVernacularName(AkpBib bib,
			AkpVernacularName vernacularName) {
		if (vernacularName.getBibs().contains(bib))
			return;
		vernacularName.getBibs().add(bib);
		getSession().update(vernacularName);
		akpLogService.logVernacularNameBibAddition(vernacularName, bib);
	}

	@Transactional
	@Override
	public void removeBibFromVernacularName(AkpBib bib,
			AkpVernacularName vernacularName) {
		vernacularName.getBibs().remove(bib);
		getSession().update(vernacularName);
		akpLogService.logVernacularNameBibRemoval(vernacularName, bib);
	}

	@Transactional
	@Override
	public void addPlantRefToVernacularName(AkpPlant targetPlant,
			AkpVernacularName vernacularName) {
		if (!vernacularName.getPlantRefs().contains(targetPlant)) {
			vernacularName.getPlantRefs().add(targetPlant);
			getSession().update(vernacularName);
			akpLogService.logVernacularNamePlantRefAddition(vernacularName,
					targetPlant);
		}
	}

	@Transactional
	@Override
	public void removePlantRefFromVernacularName(AkpPlant targetPlant,
			AkpVernacularName vernacularName) {
		vernacularName.getPlantRefs().remove(targetPlant);
		getSession().update(vernacularName);
		akpLogService.logVernacularNamePlantRefRemoval(vernacularName,
				targetPlant);
	}

	@Transactional
	@Override
	public boolean deleteVernacularName(AkpVernacularName vernacularName) {
		if (vernacularName.getChildren().size() > 0)
			return false;
		AkpLexicalGroup lexicalGroup = vernacularName.getLexicalGroup();
		lexicalGroup.getVernacularNames().remove(vernacularName);
		lexicalGroup.refreshVernacularNamesTree();
		if (vernacularName.getName().length() > 0)
			akpLogService.logVernacularNameDeletion(vernacularName);
		getSession().delete(vernacularName);
		getSession().flush();
		return true;
	}

	@Transactional
	@Override
	public AkpBib createNewBib(String xid) {
		AkpBib bib = new AkpBib();
		bib.setXid(xid);
		bib.setTitle(xid);
		bib.setAuthor("");
		bib.setDate("");
		bib.setIsbn("");
		bib.setComments("");
		bib.setEditor("");
		getSession().save(bib);
		getSession().flush();
		return bib;
	}

	@Transactional
	@Override
	public boolean deleteBib(AkpBib bib) {
		getSession().delete(bib);
		getSession().flush();
		return true;
	}

	@Transactional
	@Override
	public AkpBib getBib(String xid) {
		if (xid == null)
			return null;
		return (AkpBib) getSession().get(AkpBib.class, xid);
	}

	@Transactional
	@Override
	public List<AkpBib> getBibs() {
		@SuppressWarnings("unchecked")
		List<AkpBib> bibs = getSession().createCriteria(AkpBib.class).list();
		Collections.sort(bibs);
		return bibs;
	}

	@Transactional
	@Override
	public List<AkpBib> searchBib(int limit, String xid, String title,
			String author, String date, String isbn, String comments,
			String editor) {
		Criteria criteria = getSession().createCriteria(AkpBib.class);
		criteria.setMaxResults(limit);
		if (xid != null)
			criteria.add(Restrictions.like("xid", "%" + xid + "%"));
		if (title != null)
			criteria.add(Restrictions.like("title", "%" + title + "%"));
		if (author != null)
			criteria.add(Restrictions.like("author", "%" + author + "%"));
		if (date != null)
			criteria.add(Restrictions.like("date", "%" + date + "%"));
		if (isbn != null)
			criteria.add(Restrictions.like("isbn", "%" + isbn + "%"));
		if (comments != null)
			criteria.add(Restrictions.like("comments", "%" + comments + "%"));
		if (editor != null)
			criteria.add(Restrictions.like("editor", "%" + editor + "%"));
		@SuppressWarnings("unchecked")
		List<AkpBib> retval = criteria.list();
		Collections.sort(retval);
		return retval;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<String> searchBibFromId(String id) {
		return getSession().createCriteria(AkpBib.class)
				.add(Restrictions.like("xid", "%" + id + "%"))
				.setProjection(Projections.property("xid")).list();
	}

	@Transactional
	@Override
	public void updateBib(AkpBib bib) {
		getSession().update(getSession().merge(bib));
		getSession().flush();
	}

	@Override
	@Transactional
	public List<AkpLangGroup> getLangGroupList() {
		@SuppressWarnings("unchecked")
		List<AkpLangGroup> groups = getSession().createCriteria(
				AkpLangGroup.class).list();
		Collections.sort(groups);
		return groups;
	}

	@Transactional
	@Override
	public boolean createNewLang(String xid) {
		if (getLang(xid) != null)
			return false;
		AkpLang lang = new AkpLang();
		lang.setXid(xid);
		lang.setLevel(3);
		lang.setName(xid);
		lang.setLangGroup(getLangGroupList().get(0));
		lang.setOrder(-1);
		getSession().save(lang);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AkpLang> getLangList(int level) {
		// Only return lang for the correct profile.
		List<AkpLang> langs = getSession().createCriteria(AkpLang.class)
				.add(Restrictions.le("level", level)).list();
		Collections.sort(langs);
		return langs;
	}

	@Override
	public AkpLang getLang(String xid) {
		return (AkpLang) getSession().get(AkpLang.class, xid);
	}

	@Transactional
	@Override
	public void updateLang(AkpLang lang) {
		getSession().update(lang);
	}

	@Transactional
	@Override
	public boolean canDeleteLang(AkpLang lang) {
		Long count = ((Long) getSession().createCriteria(AkpLexicalGroup.class)
				.add(Restrictions.eq("lang", lang))
				.setProjection(Projections.rowCount()).uniqueResult());
		return count.equals(0L);
	}

	@Transactional
	@Override
	public void deleteLang(AkpLang lang) {
		getSession().delete(lang);
	}

	@Transactional
	@Override
	public boolean createNewLexicalGroup(AkpPlant plant, AkpLang lang,
			Integer correct) {
		for (AkpLexicalGroup grp : plant.getLexicalGroups()) {
			if (grp.getLang().getXid().equals(lang.getXid())
					&& grp.getCorrect() == correct)
				return false;
		}
		AkpLexicalGroup lexicalGroup = new AkpLexicalGroup();
		lexicalGroup.setLang(lang);
		lexicalGroup.setCorrect(correct);
		lexicalGroup.setPlant(plant);
		plant.getLexicalGroups().add(lexicalGroup);
		getSession().save(lexicalGroup);
		akpLogService.logLexicalGroupCreation(lexicalGroup);
		return true;
	}

	@Transactional
	@Override
	public AkpLexicalGroup getLexicalGroup(Integer xid) {
		return (AkpLexicalGroup) getSession().get(AkpLexicalGroup.class, xid);
	}

	@Transactional
	@Override
	public boolean deleteLexicalGroup(AkpLexicalGroup lexicalGroup) {
		if (lexicalGroup.getVernacularNames().size() > 0)
			return false;
		getSession().delete(lexicalGroup);
		akpLogService.logLexicalGroupDeletion(lexicalGroup);
		getSession().flush();
		return true;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AkpPlant> getPlantBackRefs(AkpPlant plant) {
		return getSession().createCriteria(AkpPlant.class)
				.createCriteria("plantRefs", "plantRef")
				.add(Restrictions.eq("xid", plant.getXid())).list();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AkpVernacularName> getVernacularNameBackRefs(AkpPlant plant) {
		Criteria vernaCriteria = getSession().createCriteria(
				AkpVernacularName.class);
		vernaCriteria.setFetchMode("lexicalGroup", FetchMode.JOIN)
				.createCriteria("lexicalGroup")
				.setFetchMode("plant", FetchMode.JOIN);
		Criteria plantRefCriteria = vernaCriteria.createCriteria("plantRefs",
				"plantRef").add(Restrictions.eq("xid", plant.getXid()));
		return plantRefCriteria.list();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AkpVernacularName> getVernacularNameRefsFromBib(AkpBib bib) {
		List<AkpVernacularName> retval = getSession()
				.createCriteria(AkpVernacularName.class)
				.setFetchMode("bibs", FetchMode.SELECT)
				.setFetchMode("lexicalGroup", FetchMode.JOIN)
				.setFetchMode("lexicalGroup.plant", FetchMode.JOIN)
				.createCriteria("bibs", "bib")
				.add(Restrictions.eq("xid", bib.getXid())).list();
		Collections.sort(retval);
		return retval;
	}

	@Transactional
	@Override
	public AkpAuthor createNewAuthor(String xid) {
		AkpAuthor author = new AkpAuthor();
		author.setXid(xid);
		author.setName(xid);
		author.setSource("");
		author.setDates("");
		getSession().save(author);
		getSession().flush();
		return author;
	}

	@Transactional
	@Override
	public AkpAuthor getAuthor(String xid) {
		return (AkpAuthor) getSession().get(AkpAuthor.class, xid);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Map<String, AkpAuthor> getAuthors(Set<String> xids) {
		if (xids.size() == 0)
			return Collections.emptyMap();
		List<AkpAuthor> authors = getSession().createCriteria(AkpAuthor.class)
				.add(Restrictions.in("xid", xids)).list();
		Map<String, AkpAuthor> retval = new HashMap<String, AkpAuthor>(
				authors.size());
		for (AkpAuthor author : authors) {
			retval.put(author.getXid(), author);
		}
		return retval;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Map<String, Set<AkpAuthor>> getAuthorFromSources(Set<String> oldXids) {
		if (oldXids.size() == 0)
			return Collections.emptyMap();
		Criteria criteria = getSession().createCriteria(AkpAuthor.class);
		List<Criterion> likes = new ArrayList<Criterion>(oldXids.size());
		for (String oldXid : oldXids) {
			likes.add(Restrictions.like("source", oldXid, MatchMode.ANYWHERE));
			likes.add(Restrictions.eq("xid", oldXid));
		}
		criteria.add(Restrictions.or(likes.toArray(new Criterion[likes.size()])));
		List<AkpAuthor> authors = criteria.list();
		Map<String, Set<AkpAuthor>> retval = new HashMap<String, Set<AkpAuthor>>(
				authors.size());
		for (AkpAuthor author : authors) {
			String[] oldXids2 = author.getSource().split(";");
			// Add source
			for (String oldXid2 : oldXids2) {
				oldXid2 = oldXid2.trim();
				if (oldXids.contains(oldXid2)) {
					Set<AkpAuthor> authList = retval.get(oldXid2);
					if (authList == null) {
						authList = new HashSet<AkpAuthor>();
						retval.put(oldXid2, authList);
					}
					authList.add(author);
				}
			}
			// Add XID
			if (oldXids.contains(author.getXid())) {
				Set<AkpAuthor> authList = retval.get(author.getXid());
				if (authList == null) {
					authList = new HashSet<AkpAuthor>();
					retval.put(author.getXid(), authList);
				}
				authList.add(author);
			}
		}
		return retval;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AkpAuthor> getAuthors() {
		return getSession().createCriteria(AkpAuthor.class).list();
	}

	@Transactional
	@Override
	public List<AkpAuthor> searchAuthor(int limit, String xid, String name,
			String dates, String source) {
		Criteria criteria = getSession().createCriteria(AkpAuthor.class);
		criteria.setMaxResults(limit);
		if (xid != null)
			criteria.add(Restrictions.or(
					Restrictions.like("xid", "%" + xid + "%"),
					Restrictions.like("source", "%" + xid + "%")));
		if (name != null)
			criteria.add(Restrictions.like("name", "%" + name + "%"));
		if (dates != null)
			criteria.add(Restrictions.like("dates", "%" + dates + "%"));
		if (source != null)
			criteria.add(Restrictions.like("source", "%" + source + "%"));
		@SuppressWarnings("unchecked")
		List<AkpAuthor> retval = criteria.list();
		Collections.sort(retval);
		return retval;
	}

	@Transactional
	@Override
	public List<AkpTaxon> getTaxonsForAuthor(int limit, AkpAuthor author) {
		@SuppressWarnings("unchecked")
		List<AkpTaxon> taxons = getSession()
				.createCriteria(AkpTaxon.class)
				.setMaxResults(limit)
				.add(Restrictions.like("name", "%<a>" + author.getXid()
						+ "</a>%")).list();
		Collections.sort(taxons);
		return taxons;
	}

	@Transactional
	@Override
	public void updateAuthor(AkpAuthor author) {
		getSession().update(author);
		getSession().flush();
	}

	@Transactional
	@Override
	public boolean deleteAuthor(AkpAuthor author) {
		getSession().delete(author);
		getSession().flush();
		return true;
	}

	@Transactional
	@Override
	public AkpSearchResult search(AkpUser user, AkpSearchData searchData) {
		Map<String, Set<String>> authorRenameMap = renameAuthors(searchData);
		AkpSearchResult result;
		AkpSearchType searchType = searchData.getSearchType();
		boolean incRequestCount = true;
		if (searchType == AkpSearchType.TAXON) {
			result = searchTaxon(searchData);
		} else if (searchType == AkpSearchType.VERNA) {
			result = searchVerna(searchData);
		} else {
			result = new AkpSearchResult();
			incRequestCount = false;
		}
		result.setAuthorRenameMap(authorRenameMap);
		if (incRequestCount) {
			akpLoginService.incUserRequestCount(user);
		}
		return result;
	}

	private Map<String, Set<String>> renameAuthors(AkpSearchData searchData) {
		String taxonSearch = searchData.getTaxonName();
		if (taxonSearch == null)
			return null;
		Map<String, Set<String>> retval = new HashMap<String, Set<String>>();
		// Scan input taxon search for potential authors
		String[] elements = taxonSearch.split("</?a>");
		Set<String> authors = new HashSet<String>(elements.length / 2 + 1);
		for (int i = 1; i < elements.length; i += 2) {
			authors.add(elements[i]);
		}
		Map<String, Set<AkpAuthor>> renameSet = getAuthorFromSources(authors);
		for (Map.Entry<String, Set<AkpAuthor>> kv : renameSet.entrySet()) {
			if (taxonSearch.contains("<a>" + kv.getKey() + "</a>")) {
				if (kv.getValue().size() == 1) {
					// Auto-replace only if ONE match.
					taxonSearch = taxonSearch
							.replace("<a>" + kv.getKey() + "</a>", "<a>"
									+ kv.getValue().iterator().next().getXid()
									+ "</a>");
				}
			}
			Set<String> xids = new HashSet<String>();
			for (AkpAuthor auth : kv.getValue())
				xids.add(auth.getXid());
			retval.put(kv.getKey(), xids);
		}
		searchData.setTaxonName(taxonSearch);
		if (retval.isEmpty())
			return null;
		return retval;
	}

	private AkpSearchResult searchTaxon(AkpSearchData searchData) {
		// Create criterias
		AkpSearchResult results = new AkpSearchResult();
		Criteria taxonCriteria = getSession().createCriteria(AkpTaxon.class);
		taxonCriteria.setMaxResults(searchData.getLimit());
		if (searchData.getTaxonName() != null)
			taxonCriteria.add(Restrictions.like("name",
					"%" + searchData.getTaxonName() + "%"));
		taxonCriteria.setFetchMode("plant", FetchMode.JOIN);
		if (!(searchData.isIncludeSynonyms() && searchData.getTaxonName() != null))
			taxonCriteria.add(Restrictions.eq("type", AkpTaxon.TYPE_MAIN));
		if (searchData.getPlantComments() != null) {
			Criteria plantCriteria = taxonCriteria.createCriteria("plant");
			plantCriteria.add(Restrictions.like("comments",
					"%" + searchData.getPlantComments() + "%"));
		}
		if (searchData.getPlantOrigin() != null) {
			Criteria tagCriteria = taxonCriteria.createCriteria("plant.tags",
					"tag");
			tagCriteria
					.add(Restrictions.eq("type", AkpPlantTag.TAGTYPE_ORIGIN));
			tagCriteria.add(Restrictions.like("stringValue",
					"%" + searchData.getPlantOrigin() + "%"));
		}
		if (searchData.getFamilyXid() != null) {
			Criteria plantCriteria = taxonCriteria.createCriteria("plant");
			plantCriteria.add(Restrictions.eq("akpClass.xid",
					searchData.getFamilyXid()));
		}
		// Search
		@SuppressWarnings("unchecked")
		List<AkpTaxon> taxons = taxonCriteria.list();
		Collections.sort(taxons);
		// Build result
		if (DEBUG_TAXON_SORT) {
			results.addHeaderKey("result.column.sortkey");
		}
		if (searchData.isIncludeSynonyms() && searchData.getTaxonName() != null)
			results.addHeaderKey("result.column.synonym");
		results.addHeaderKey("result.column.plantname");
		if (searchData.getPlantComments() != null)
			results.addHeaderKey("result.column.plant.comments");
		if (searchData.getPlantOrigin() != null)
			results.addHeaderKey("result.column.plant.origin");
		for (AkpTaxon taxon : taxons) {
			AkpPlant plant = taxon.getPlant();
			AkpSearchResultRow result = new AkpSearchResultRow(plant.getXid(),
					null, null);
			if (DEBUG_TAXON_SORT) {
				result.addColumn(new AkpSearchResultColumn(plant.getMainName()
						.getSortKey().replace(" ", "_ "), "sortkey"));
			}
			if (searchData.isIncludeSynonyms()
					&& searchData.getTaxonName() != null)
				result.addColumn(new AkpSearchResultColumn(taxon.getHtmlName(),
						"taxon"));
			result.addColumn(new AkpSearchResultColumn(plant.getMainName()
					.getHtmlName(), "taxon"));
			if (searchData.getPlantComments() != null)
				result.addColumn(new AkpSearchResultColumn(plant.getComments(),
						"comments"));
			if (searchData.getPlantOrigin() != null) {
				String origin = "?";
				for (AkpPlantTag tag : plant.getTags()) {
					if (tag.getType() == AkpPlantTag.TAGTYPE_ORIGIN)
						origin = tag.getValue();
				}
				result.addColumn(new AkpSearchResultColumn(origin, "tag"));
			}
			results.addRow(result);
		}
		return results;
	}

	private AkpSearchResult searchVerna(AkpSearchData searchData) {
		// Create criterias
		AkpSearchResult results = new AkpSearchResult();
		Criteria vernaCriteria = getSession().createCriteria(
				AkpVernacularName.class);
		vernaCriteria.setMaxResults(searchData.getLimit());
		vernaCriteria.setFetchMode("lexicalGroup", FetchMode.JOIN);
		vernaCriteria.setFetchMode("lexicalGroup.plant", FetchMode.JOIN);
		if (searchData.getVernacularName() != null)
			vernaCriteria.add(Restrictions.like("name",
					"%" + searchData.getVernacularName() + "%"));
		if (searchData.getTaxonName() != null) {
			Criteria taxonCriteria = vernaCriteria.createCriteria(
					"lexicalGroup.plant.taxons", "taxon");
			taxonCriteria.add(Restrictions.like("taxon.name",
					"%" + searchData.getTaxonName() + "%"));
			taxonCriteria
					.add(Restrictions.eq("taxon.type", AkpTaxon.TYPE_MAIN));
		}
		if (searchData.getBibRefXid() != null) {
			Criteria bibCriteria = vernaCriteria.createCriteria("bibs", "bib");
			bibCriteria.add(Restrictions.eq("bib.xid",
					searchData.getBibRefXid()));
		}
		if (searchData.getLangXids().size() > 0) {
			Criteria lexgrpCriteria = vernaCriteria
					.createCriteria("lexicalGroup");
			lexgrpCriteria.add(Restrictions.in("lang.xid",
					searchData.getLangXids()));
		}
		if (searchData.getVernacularNameComments() != null) {
			vernaCriteria.add(Restrictions.like("comments",
					"%" + searchData.getVernacularNameComments() + "%"));
		}
		if (searchData.getPlantComments() != null) {
			Criteria plantCriteria = vernaCriteria.createCriteria(
					"lexicalGroup.plant", "plant");
			plantCriteria.add(Restrictions.like("comments",
					"%" + searchData.getPlantComments() + "%"));
		}
		if (searchData.getPlantOrigin() != null) {
			Criteria tagCriteria = vernaCriteria.createCriteria(
					"lexicalGroup.plant.tags", "tag");
			tagCriteria
					.add(Restrictions.eq("type", AkpPlantTag.TAGTYPE_ORIGIN));
			tagCriteria.add(Restrictions.like("stringValue",
					"%" + searchData.getPlantOrigin() + "%"));
		}
		if (searchData.getFamilyXid() != null) {
			Criteria plantCriteria = vernaCriteria
					.createCriteria("lexicalGroup.plant");
			plantCriteria.add(Restrictions.eq("akpClass.xid",
					searchData.getFamilyXid()));
		}
		// Search
		@SuppressWarnings("unchecked")
		List<AkpVernacularName> vernacularNames = vernaCriteria.list();
		Collections.sort(vernacularNames);
		// Build result
		results.addHeaderKey("result.column.vernaname");
		results.addHeaderKey("result.column.lang");
		results.addHeaderKey("result.column.plantname");
		if (searchData.getVernacularNameComments() != null)
			results.addHeaderKey("result.column.vernaname.comments");
		if (searchData.getPlantComments() != null)
			results.addHeaderKey("result.column.plant.comments");
		if (searchData.getPlantOrigin() != null)
			results.addHeaderKey("result.column.plant.origin");
		for (AkpVernacularName vernacularName : vernacularNames) {
			AkpLexicalGroup lexicalGroup = vernacularName.getLexicalGroup();
			AkpPlant plant = lexicalGroup.getPlant();
			AkpSearchResultRow result = new AkpSearchResultRow(plant.getXid(),
					lexicalGroup.getLang().getXid(), lexicalGroup.getCorrect());
			result.addColumn(new AkpSearchResultColumn(
					vernacularName.getName(), "verna"));
			result.addColumn(new AkpSearchResultColumn(lexicalGroup.getLang()
					.getXid()
					+ (lexicalGroup.getCorrect() != 0 ? " "
							+ lexicalGroup.getCorrectDisplayCode() : ""), null));
			result.addColumn(new AkpSearchResultColumn(plant.getMainName()
					.getHtmlName(), "taxon"));
			if (searchData.getVernacularNameComments() != null)
				result.addColumn(new AkpSearchResultColumn(vernacularName
						.getComments(), "comments"));
			if (searchData.getPlantComments() != null)
				result.addColumn(new AkpSearchResultColumn(plant.getComments(),
						"comments"));
			if (searchData.getPlantOrigin() != null) {
				String origin = "?";
				for (AkpPlantTag tag : plant.getTags()) {
					if (tag.getType() == AkpPlantTag.TAGTYPE_ORIGIN)
						origin = tag.getValue();
				}
				result.addColumn(new AkpSearchResultColumn(origin, "tag"));
			}
			results.addRow(result);
		}
		return results;
	}

	@Override
	public Date getLastUpdate() {
		return (Date) getSession().createCriteria(AkpLogEntry.class)
				.setProjection(Projections.max("date")).uniqueResult();
	}

	@Override
	public Map<String, Long> getObjectCount() {
		Map<String, Long> retval = new HashMap<String, Long>(12);
		final Class<?> CLASSES[] = { AkpPlant.class, AkpTaxon.class,
				AkpVernacularName.class, AkpBib.class, AkpAuthor.class };
		for (Class<?> clazz : CLASSES) {
			retval.put(clazz.getSimpleName(), (Long) getSession()
					.createCriteria(clazz)
					.setProjection(Projections.rowCount()).uniqueResult());
		}
		retval.put(
				"AkpBibRef",
				(Long) getSession().createCriteria(AkpVernacularName.class)
						.createCriteria("bibs", "bib")
						.setProjection(Projections.rowCount()).uniqueResult());
		return retval;
	}

	@Override
	public Map<AkpLang, Long> getVernacularNameCountPerLanguage() {
		Map<AkpLang, Long> retval = new HashMap<AkpLang, Long>();
		@SuppressWarnings("unchecked")
		List<Object[]> list = getSession()
				.createCriteria(AkpVernacularName.class)
				.createAlias("lexicalGroup", "lexicalGroup")
				.setProjection(
						Projections
								.projectionList()
								.add(Projections.rowCount())
								.add(Projections
										.groupProperty("lexicalGroup.lang")))
				.list();
		for (Object[] objs : list) {
			retval.put((AkpLang) objs[1], (Long) objs[0]);
		}
		return retval;
	}

	@Override
	public AkpSearchResult getDuplicatedVernacularNames() {
		Query query = getSession().getNamedQuery("duplicatedVernacularName");
		@SuppressWarnings("unchecked")
		List<AkpVernacularName> duplicatedNames = query.list();
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.vernaname");
		retval.addHeaderKey("result.column.lang");
		retval.addHeaderKey("result.column.plantname");
		for (AkpVernacularName vernacularName : duplicatedNames) {
			AkpLexicalGroup lexicalGroup = vernacularName.getLexicalGroup();
			AkpPlant plant = lexicalGroup.getPlant();
			AkpSearchResultRow result = new AkpSearchResultRow(plant.getXid(),
					lexicalGroup.getLang().getXid(), lexicalGroup.getCorrect());
			result.addColumn(new AkpSearchResultColumn(
					vernacularName.getName(), "verna"));
			result.addColumn(new AkpSearchResultColumn(lexicalGroup.getLang()
					.getXid()
					+ (lexicalGroup.getCorrect() != 0 ? " "
							+ lexicalGroup.getCorrectDisplayCode() : ""), null));
			result.addColumn(new AkpSearchResultColumn(plant.getMainName()
					.getHtmlName(), "taxon"));
			result.setSortKey(vernacularName.getName());
			retval.addRow(result);
		}
		return retval;
	}

	@Override
	public AkpSearchResult getDuplicatedTaxonNames() {
		Query query = getSession().getNamedQuery("duplicatedTaxonName");
		@SuppressWarnings("unchecked")
		List<AkpTaxon> duplicatedTaxons = query.list();
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.synonym");
		retval.addHeaderKey("result.column.plantname");
		for (AkpTaxon taxon : duplicatedTaxons) {
			AkpPlant plant = taxon.getPlant();
			AkpSearchResultRow result = new AkpSearchResultRow(plant.getXid(),
					null, null);
			result.addColumn(new AkpSearchResultColumn(taxon.getHtmlName(),
					"taxon"));
			result.addColumn(new AkpSearchResultColumn(plant.getMainName()
					.getHtmlName(), "taxon"));
			result.setSortKey(taxon.getSortKey());
			retval.addRow(result);
		}
		return retval;
	}

	@Override
	@Transactional
	public AkpSearchResult getTaxonSyntaxErrors() {
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.synonym");
		retval.addHeaderKey("result.column.plantname");
		retval.addHeaderKey("result.column.syntaxerror");
		ScrollableResults taxons = getSession().createCriteria(AkpTaxon.class)
				.setFetchSize(100).setReadOnly(true).setLockMode(LockMode.NONE)
				.scroll();
		while (taxons.next()) {
			AkpTaxon taxon = (AkpTaxon) taxons.get(0);
			List<String> errors = checkTaxon(taxon);
			if (!errors.isEmpty()) {
				AkpPlant plant = taxon.getPlant();
				for (String error : errors) {
					AkpSearchResultRow result = new AkpSearchResultRow(
							plant.getXid(), null, null);
					result.addColumn(new AkpSearchResultColumn(taxon
							.getHtmlName(), "taxon"));
					result.addColumn(new AkpSearchResultColumn(plant
							.getMainName().getHtmlName(), "taxon"));
					AkpSearchResultColumn col3 = new AkpSearchResultColumn(
							error, "comment", true);
					col3.setEscape(true);
					result.addColumn(col3);
					result.setSortKey(taxon.getSortKey());
					retval.addRow(result);
				}
			}
		}
		return retval;
	}

	@Override
	@Transactional
	public AkpSearchResult getAuthorWithoutTags() {
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.synonym");
		retval.addHeaderKey("result.column.author");
		List<AkpAuthor> authors = getAuthors();
		Set<String> authorXids = new HashSet<String>();
		for (AkpAuthor author : authors) {
			authorXids.add(author.getXid());
		}
		ScrollableResults taxons = getSession().createCriteria(AkpTaxon.class)
				.setFetchSize(100).setReadOnly(true).setLockMode(LockMode.NONE)
				.scroll();
		while (taxons.next()) {
			AkpTaxon taxon = (AkpTaxon) taxons.get(0);
			String taxonName = taxon.getName();
			taxonName = taxonName.replaceAll("<b>.*?</b>", "");
			taxonName = taxonName.replaceAll("<i>.*?</i>", "");
			taxonName = taxonName.replaceAll("\\[.*?\\]", "");
			String[] elements = taxonName.split("[\\s]");
			for (String elem : elements) {
				if (authorXids.contains(elem)) {
					AkpSearchResultRow result = new AkpSearchResultRow(taxon
							.getPlant().getXid(), null, null);
					result.addColumn(new AkpSearchResultColumn(taxon
							.getHtmlName(), "taxon"));
					result.addColumn(new AkpSearchResultColumn(elem, "comment"));
					result.setSortKey(taxon.getSortKey());
					retval.addRow(result);
				}
			}
		}
		return retval;
	}

	@Override
	@Transactional
	public AkpSearchResult getImpreciseVernaWithoutPlantRef() {
		@SuppressWarnings("unchecked")
		List<AkpVernacularName> wrongNames = getSession()
				.createCriteria(AkpVernacularName.class)
				.add(Restrictions.or(Restrictions.eq("parentId", 0),
						Restrictions.isNull("parentId")))
				.add(Restrictions.isEmpty("plantRefs"))
				.createAlias("lexicalGroup", "lexGrp")
				.add(Restrictions.ne("lexGrp.correct", 0)).list();
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.vernaname");
		retval.addHeaderKey("result.column.lang");
		retval.addHeaderKey("result.column.plantname");
		for (AkpVernacularName vernacularName : wrongNames) {
			AkpLexicalGroup lexicalGroup = vernacularName.getLexicalGroup();
			AkpPlant plant = lexicalGroup.getPlant();
			AkpSearchResultRow result = new AkpSearchResultRow(plant.getXid(),
					lexicalGroup.getLang().getXid(), lexicalGroup.getCorrect());
			result.addColumn(new AkpSearchResultColumn(
					vernacularName.getName(), "verna"));
			result.addColumn(new AkpSearchResultColumn(lexicalGroup.getLang()
					.getXid()
					+ (lexicalGroup.getCorrect() != 0 ? " "
							+ lexicalGroup.getCorrectDisplayCode() : ""), null));
			result.addColumn(new AkpSearchResultColumn(plant.getMainName()
					.getHtmlName(), "taxon"));
			result.setSortKey(plant.getMainName().getSortKey());
			retval.addRow(result);
		}
		return retval;
	}

	@Override
	@Transactional
	public AkpSearchResult getAuthorRefCount() {
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.author");
		retval.addHeaderKey("result.column.count");
		ScrollableResults taxons = getSession().createCriteria(AkpTaxon.class)
				.setFetchSize(100).setReadOnly(true).setLockMode(LockMode.NONE)
				.scroll();
		Map<String, Integer> usageCount = new HashMap<String, Integer>();
		for (AkpAuthor author : getAuthors())
			usageCount.put(author.getXid(), 0);
		while (taxons.next()) {
			AkpTaxon taxon = (AkpTaxon) taxons.get(0);
			String taxonName = taxon.getName();
			Matcher m = Pattern.compile("<a>(.*?)</a>").matcher(taxonName);
			while (m.find()) {
				String authXid = m.group(1);
				Integer count = usageCount.get(authXid);
				if (count != null)
					usageCount.put(authXid, count + 1);
			}
		}
		Map<String, Integer> sortedUsageCount = MapUtils.sortByValue(
				usageCount, true);
		for (Map.Entry<String, Integer> kv : sortedUsageCount.entrySet()) {
			AkpSearchResultRow result = new AkpSearchResultRow(null, null, null);
			result.addColumn(new AkpSearchResultColumn(kv.getKey(), "author"));
			result.addColumn(new AkpSearchResultColumn("" + kv.getValue(),
					"comment"));
			retval.addRow(result);
		}
		return retval;
	}

	@Override
	public AkpSearchResult getPlantsWithoutVerna() {
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.plantname");
		retval.addHeaderKey("result.column.lang");
		// 1. Get plants with no lexical groups
		ScrollableResults plants = getSession().createCriteria(AkpPlant.class)
				.add(Restrictions.isEmpty("lexicalGroups"))
				.addOrder(Order.asc("xid")).setFetchSize(100).setReadOnly(true)
				.setLockMode(LockMode.NONE).scroll();
		while (plants.next()) {
			AkpPlant plant = (AkpPlant) plants.get(0);
			AkpSearchResultRow result = new AkpSearchResultRow(plant.getXid(),
					null, null);
			result.addColumn(new AkpSearchResultColumn(plant.getMainName()
					.getHtmlName(), "taxon"));
			result.addColumn(new AkpSearchResultColumn("", null));
			result.setSortKey(plant.getMainName().getSortKey());
			retval.addRow(result);
		}
		// 2. Get lexical groups with no vernacular names
		ScrollableResults lexicalGroups = getSession()
				.createCriteria(AkpLexicalGroup.class)
				.add(Restrictions.isEmpty("vernacularNames"))
				.addOrder(Order.asc("xid")).setFetchSize(100).setReadOnly(true)
				.setLockMode(LockMode.NONE).scroll();
		while (lexicalGroups.next()) {
			AkpLexicalGroup lexicalGroup = (AkpLexicalGroup) lexicalGroups
					.get(0);
			AkpPlant plant = lexicalGroup.getPlant();
			AkpSearchResultRow result = new AkpSearchResultRow(plant.getXid(),
					lexicalGroup.getLang().getXid(), lexicalGroup.getCorrect());
			result.addColumn(new AkpSearchResultColumn(plant.getMainName()
					.getHtmlName(), "taxon"));
			result.addColumn(new AkpSearchResultColumn(lexicalGroup.getLang()
					.getXid()
					+ (lexicalGroup.getCorrect() != 0 ? " "
							+ lexicalGroup.getCorrectDisplayCode() : ""), null));
			result.setSortKey(plant.getMainName().getSortKey());
			retval.addRow(result);
		}
		return retval;
	}

	@Override
	public AkpSearchResult getPlantsXRefs() {
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.plantname");
		retval.addHeaderKey("result.column.plantname");
		// 1. Get plants with no lexical groups
		ScrollableResults plants = getSession().createCriteria(AkpPlant.class)
				.add(Restrictions.isNotEmpty("plantRefs")).setFetchSize(100)
				.addOrder(Order.asc("xid")).setReadOnly(true)
				.setLockMode(LockMode.NONE).scroll();
		while (plants.next()) {
			AkpPlant plant1 = (AkpPlant) plants.get(0);
			for (AkpPlant plant2 : plant1.getPlantRefs()) {
				AkpSearchResultRow result = new AkpSearchResultRow(
						plant1.getXid(), null, null);
				result.addColumn(new AkpSearchResultColumn(plant1.getMainName()
						.getHtmlName(), "taxon"));
				AkpSearchResultColumn col2 = new AkpSearchResultColumn("⇒ "
						+ plant2.getMainName().getHtmlName(), "taxon");
				col2.setPlantXid(plant2.getXid());
				result.addColumn(col2);
				result.setSortKey(plant1.getMainName().getSortKey());
				retval.addRow(result);
			}
		}
		return retval;
	}

	@Override
	public AkpSearchResult getHybridParents() {
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.taxon");
		retval.addHeaderKey("result.column.taxon");
		ScrollableResults taxons = getSession().createCriteria(AkpTaxon.class)
				.setFetchSize(100).setReadOnly(true).setLockMode(LockMode.NONE)
				.scroll();
		Pattern pattern1 = Pattern.compile("(<l>.*?</l>).*?(\\[.*\\]).*");
		Pattern pattern2 = Pattern.compile("<l>.*?</l>");
		Map<String, AkpTaxon> hybridMap = new HashMap<String, AkpTaxon>();
		Map<String, AkpTaxon> parentMap = new HashMap<String, AkpTaxon>();
		while (taxons.next()) {
			AkpTaxon taxon = (AkpTaxon) taxons.get(0);
			Matcher matcher1 = pattern1.matcher(taxon.getName());
			if (matcher1.matches()) {
				String hybrid = matcher1.group(1);
				String parents = matcher1.group(2);
				hybridMap.put(hybrid, taxon);
				Matcher matcher2 = pattern2.matcher(parents);
				while (matcher2.find()) {
					String parent = matcher2.group();
					parentMap.put(parent, taxon);
				}
			}
		}
		for (Map.Entry<String, AkpTaxon> kv : parentMap.entrySet()) {
			String parent = kv.getKey();
			AkpTaxon taxon1 = kv.getValue();
			AkpTaxon taxon2 = hybridMap.get(parent);
			if (taxon2 != null) {
				AkpSearchResultRow result = new AkpSearchResultRow(taxon1
						.getPlant().getXid(), null, null);
				result.addColumn(new AkpSearchResultColumn(
						taxon1.getHtmlName(), "taxon"));
				AkpSearchResultColumn col2 = new AkpSearchResultColumn(
						taxon2.getHtmlName(), "taxon");
				col2.setPlantXid(taxon2.getPlant().getXid());
				result.addColumn(col2);
				retval.addRow(result);
			}
		}
		return retval;
	}

	@Override
	public AkpSearchResult getEqualsSynonyms() {
		AkpSearchResult retval = new AkpSearchResult();
		retval.addHeaderKey("result.column.taxon");
		retval.addHeaderKey("result.column.taxon");
		// Scroll first time to extract all "[= ... ]" names
		ScrollableResults taxons = getSession().createCriteria(AkpTaxon.class)
				.setFetchSize(100).setReadOnly(true).setLockMode(LockMode.NONE)
				.scroll();
		Pattern pattern = Pattern.compile("\\[=\\s*(.*?)\\]");
		Map<String, AkpTaxon> equalsMap = new HashMap<String, AkpTaxon>();
		while (taxons.next()) {
			AkpTaxon taxon = (AkpTaxon) taxons.get(0);
			Matcher matcher = pattern.matcher(taxon.getName());
			while (matcher.find()) {
				String equals = AkpTaxon.getTextName(matcher.group(1));
				equalsMap.put(equals, taxon);
			}
		}
		// Scroll again for all synonyms and check if they are in "=" list
		taxons = getSession().createCriteria(AkpTaxon.class).setFetchSize(100)
				.setReadOnly(true).setLockMode(LockMode.NONE).scroll();
		while (taxons.next()) {
			AkpTaxon taxon1 = (AkpTaxon) taxons.get(0);
			if (taxon1.getType() != AkpTaxon.TYPE_SYNONYM)
				continue;
			AkpTaxon taxon2 = equalsMap.get(taxon1.getTextName());
			if (taxon2 != null) {
				AkpSearchResultRow result = new AkpSearchResultRow(taxon1
						.getPlant().getXid(), null, null);
				result.addColumn(new AkpSearchResultColumn(
						taxon1.getHtmlName(), "taxon"));
				AkpSearchResultColumn col2 = new AkpSearchResultColumn(
						taxon2.getHtmlName(), "taxon");
				col2.setPlantXid(taxon2.getPlant().getXid());
				result.addColumn(col2);
				retval.addRow(result);
			}
		}
		return retval;
	}

	@Override
	@Transactional
	public void mergeLang(String langId1, String langId2) {
		AkpLang lang1 = getLang(langId1);
		AkpLang lang2 = getLang(langId2);
		Session session = getSession();
		ScrollableResults plants = session
				.createCriteria(AkpLexicalGroup.class)
				.addOrder(Order.asc("xid")).add(Restrictions.eq("lang", lang2))
				.setFetchSize(100).setReadOnly(false)
				.setLockMode(LockMode.NONE).scroll();
		while (plants.next()) {
			AkpLexicalGroup lex2 = (AkpLexicalGroup) plants.get(0);
			AkpPlant plant = lex2.getPlant();
			AkpLexicalGroup lex1 = null;
			for (AkpLexicalGroup lex : plant.getLexicalGroups()) {
				if (lex.getLang().equals(lang1)) {
					lex1 = lex;
					break;
				}
			}
			if (lex1 == null) {
				System.out.println("Updating " + plant + " [" + lex2 + "]");
				// No target lang, simply transform lex2 to use lex1.lang
				lex2.setLang(lang1);
				session.update(lex2);
			} else {
				System.out.println("Merging " + plant);
				// Merge lex2 into lex1
				// Get existing lex2 names to merge names
				Map<String, AkpVernacularName> names1 = new HashMap<String, AkpVernacularName>();
				for (AkpVernacularName name1 : lex1.getVernacularNames()) {
					if (name1.getName().equals("#"))
						throw new RuntimeException(
								"Can't merge non flat languages!");
					names1.put(name1.getName(), name1);
				}
				for (AkpVernacularName name2 : lex2.getVernacularNames()) {
					AkpVernacularName name1 = names1.get(name2.getName());
					if (name1 != null) {
						// Name exists, merge bib ref
						System.out.println("Merging name: " + name2.getName());
						Set<AkpBib> allBibs = new HashSet<AkpBib>();
						allBibs.addAll(name1.getBibs());
						allBibs.addAll(name2.getBibs());
						name1.setBibs(new ArrayList<AkpBib>(allBibs));
						session.update(name1);
						if (name2.getComments() != null
								&& !name2.getComments().equals(
										name1.getComments()))
							System.out
									.println("Warning: comment will be removed!"
											+ name2.getComments());
						session.delete(name2);
					} else {
						// Name does not exists, move it
						System.out.println("Moving name: " + name2.getName());
						name2.setLexicalGroup(lex1);
						session.save(name2);
					}
				}
				session.delete(lex2);
			}
		}
	}

	@Override
	@Transactional
	public void addAuthNameAsSource() {
		ScrollableResults authors = getSession()
				.createCriteria(AkpAuthor.class).setFetchSize(1000)
				.setReadOnly(false).setLockMode(LockMode.NONE).scroll();
		while (authors.next()) {
			AkpAuthor author = (AkpAuthor) authors.get(0);
			String name = author.getName();
			// Remove <?>, [] and ()
			name = name.replaceAll("\\(.*?\\)", "");
			name = name.replaceAll("\\[.*?\\]", "");
			name = name.replaceAll("<.*?>", "");
			String[] elements = name.split("\\s+");
			String lastName = elements[elements.length - 1].trim();
			String sources[] = author.getSource().split(";");
			boolean ok = !lastName.equalsIgnoreCase(author.getXid());
			for (String source : sources) {
				if (source.trim().equalsIgnoreCase(lastName)) {
					ok = false;
				}
			}
			String firstLetter = author.getXid().substring(0, 1);
			if (firstLetter.toLowerCase().equals(firstLetter))
				ok = false;
			if (ok) {
				author.setSource(author.getSource()
						+ (author.getSource().length() > 0 ? " ; " : "")
						+ lastName);
				log.warn("Author [" + author.getXid() + "] [" + name
						+ "] Add src: [" + lastName + "] => ["
						+ author.getSource() + "]");
				getSession().update(author);
			}
		}
		getSession().flush();
	}

	@Override
	@Transactional
	public void updateStaticIndexes() {
		try {
			ScrollableResults taxons = getSession()
					.createCriteria(AkpTaxon.class).setFetchSize(1000)
					.setReadOnly(true).setLockMode(LockMode.NONE).scroll();
			Set<Pair<String, String>> taxonSet = new HashSet<Pair<String, String>>();
			while (taxons.next()) {
				AkpTaxon taxon = (AkpTaxon) taxons.get(0);
				taxonSet.add(new Pair<String, String>(taxon.getSortKey(), taxon
						.getHtmlName()));
			}
			outputStaticFileIndex("taxon", "PlantKelt Linnean Names Index",
					taxonSet, staticIndexLocation);
			taxonSet.clear();
			ScrollableResults vernas = getSession()
					.createCriteria(AkpVernacularName.class).setFetchSize(1000)
					.setFetchMode("bibs", FetchMode.JOIN)
					.setFetchMode("lexicalGroup", FetchMode.JOIN)
					.setReadOnly(true).setLockMode(LockMode.NONE).scroll();
			Set<Pair<String, String>> vernaSet = new HashSet<Pair<String, String>>();
			while (vernas.next()) {
				AkpVernacularName verna = (AkpVernacularName) vernas.get(0);
				vernaSet.add(new Pair<String, String>(verna.getName(), verna
						.getName()
						+ " ("
						+ verna.getLexicalGroup().getLang().getCode() + ")"));
			}
			outputStaticFileIndex("verna", "PlantKelt Vernacular Names Index",
					vernaSet, staticIndexLocation);
		} catch (IOException e) {
			throw new RuntimeException(
					"Error generating static search engine indexes", e);
		}
	}

	private void outputStaticFileIndex(String elementBase, String title,
			Set<Pair<String, String>> elementsSet, String location)
			throws IOException {
		final int MAX_PAGE = 100;
		List<Pair<String, String>> elementsList = new ArrayList<Pair<String, String>>(
				elementsSet);
		elementsSet.clear();
		Collections.sort(elementsList, new Comparator<Pair<String, String>>() {
			@Override
			public int compare(Pair<String, String> o1, Pair<String, String> o2) {
				return o1.getFirst().compareToIgnoreCase(o2.getSecond());
			}
		});
		int n = elementsList.size() / MAX_PAGE + 1;
		PrintStream indexOut = new PrintStream(new FileOutputStream(
				String.format("%s/%s.html", location, elementBase)));
		indexOut.println("<html><body>");
		indexOut.println(String.format("<h1>%s</h1>", title));
		indexOut.println("<ul>");
		for (int page = 0; page < MAX_PAGE; page++) {
			int ia = page * n;
			int ib = (page + 1) * n;
			if (ib > elementsList.size())
				ib = elementsList.size();
			indexOut.println(String
					.format("<li><a href='%s_%02d.html'><b>Page %02d<b/></a> <small>[ %s ... %s ]</small></li>",
							elementBase, page, page, elementsList.get(ia)
									.getSecond(), elementsList.get(ib - 1)
									.getSecond()));
			PrintStream pageOut = new PrintStream(new FileOutputStream(
					String.format("%s/%s_%02d.html", location, elementBase,
							page)));
			pageOut.println("<html><body>");
			pageOut.println(String.format("<h1>Plantkelt - page %d</h1>", page));
			pageOut.println("<ul>");
			for (int i = ia; i < ib; i++) {
				pageOut.println(String
						.format("<li>%s (<a href='http://www.plantkelt.net/'>→ PlantKelt</a>)</li>",
								elementsList.get(i).getSecond()));
			}
			pageOut.println("</ul>");
			pageOut.println("<p>Copyright &copy; 2001-2013 <b>Plantkelt</b>, <a href='http://www.plantkelt.net/'>www.plantkelt.net</a></p>");
			pageOut.println("</body></html>");
			pageOut.close();
		}
		indexOut.println("</ul>");
		indexOut.println("<p>Copyright &copy; 2001-2013 <b>Plantkelt</b>, <a href='http://www.plantkelt.net/'>www.plantkelt.net</a></p>");
		indexOut.println("</body></html>");
		indexOut.close();
		elementsList.clear();
	}

	public void setStaticIndexLocation(String staticIndexLocation) {
		this.staticIndexLocation = staticIndexLocation;
	}

	private Session getSession() {
		return sessionProvider.get();
	}

}
