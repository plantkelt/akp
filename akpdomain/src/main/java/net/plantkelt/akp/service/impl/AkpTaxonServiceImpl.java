package net.plantkelt.akp.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.domain.AkpSearchResult.AkpSearchResultColumn;
import net.plantkelt.akp.domain.AkpSearchResult.AkpSearchResultRow;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpTaxonService;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class AkpTaxonServiceImpl implements AkpTaxonService, Serializable {
	private static final long serialVersionUID = 1L;

	private static final boolean DEBUG_TAXON_SORT = false;

	@Inject
	private Provider<Session> sessionProvider;
	@Inject
	private AkpLogService akpLogService;

	private static Set<Integer> PUBLIC_PLANT_XIDS;

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
		Collections.sort(families);
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
	}

	@Transactional
	@Override
	public void updateClass(AkpClass akpClass) {
		getSession().update(akpClass);
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
		if (taxonName.matches("\\w(\\(|\\[)"))
			retval.add("error.no.space.before.parenthesis");
		if (taxonName.matches("(\\)|\\])\\w"))
			retval.add("error.no.space.after.parenthesis");
		if (taxonName.matches("/\\w>\\w"))
			retval.add("error.no.space.before.opening.tag");
		if (taxonName.matches("\\w<\\w"))
			retval.add("error.no.space.after.closing.tag");
		if (taxonName.contains(" ."))
			retval.add("error.space.before.dot");
		if (taxonName.contains(" nec ") && !taxonName.contains(" non "))
			retval.add("error.nec.without.non");
		if (taxonName.matches("([^\\s\\w]|[^,]\\s)(non|nec)\\s"))
			retval.add("error.nec.non.missing.comma");
		Matcher synMatcher = Pattern.compile("<l>.*?</l>").matcher(taxonName);
		for (int i = 1; i <= synMatcher.groupCount(); i++) {
			String syn = synMatcher.group(i);
			if (!syn.matches("^<l><(i|b)>(<(x|\\+)>)??[A-Z][a-z,\\-]+? (<(x|\\+)> )??([a-z,\\-]+?)|(spp\\.)</(i|b)>")) {
				retval.add("error.invalid.taxon.structure");
				break;
			}
		}
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
		return bib;
	}

	@Transactional
	@Override
	public boolean deleteBib(AkpBib bib) {
		getSession().delete(bib);
		return true;
	}

	@Transactional
	@Override
	public AkpBib getBib(String xid) {
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
		getSession().update(bib);
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
	public List<AkpLang> getLangList() {
		List<AkpLang> langs = getSession().createCriteria(AkpLang.class).list();
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
			criteria.add(Restrictions.like("xid", "%" + xid + "%"));
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
	}

	@Transactional
	@Override
	public boolean deleteAuthor(AkpAuthor author) {
		getSession().delete(author);
		return true;
	}

	@Transactional
	@Override
	public AkpSearchResult search(AkpSearchData searchData) {
		switch (searchData.getSearchType()) {
		case TAXON:
			return searchTaxon(searchData);
		case VERNA:
			return searchVerna(searchData);
		default:
			return new AkpSearchResult();
		}
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
					result.addColumn(new AkpSearchResultColumn(error,
							"comment", true));
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
			retval.addRow(result);
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

	private Session getSession() {
		return sessionProvider.get();
	}

}
