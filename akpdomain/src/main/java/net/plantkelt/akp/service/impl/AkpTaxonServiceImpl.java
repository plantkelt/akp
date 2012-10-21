package net.plantkelt.akp.service.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpSearchData;
import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.domain.AkpSearchResult.AkpSearchResultColumn;
import net.plantkelt.akp.domain.AkpSearchResult.AkpSearchResultRow;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.domain.Node;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpTaxonService;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class AkpTaxonServiceImpl implements AkpTaxonService {

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
				&& plant.getPlantRefs().size() == 0;
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
	public void addRootVernacularName(AkpLexicalGroup lexicalGroup) {
		AkpVernacularName name = new AkpVernacularName();
		name.setLexicalGroup(lexicalGroup);
		name.setName("");
		name.setComments("");
		name.setParentId(0);
		lexicalGroup.getVernacularNames().add(name);
		lexicalGroup.refreshVernacularNamesTree();
		getSession().save(name);
		akpLogService.logVernacularNameCreation(name);
	}

	@Transactional
	@Override
	public void addChildVernacularName(AkpVernacularName parentName) {
		AkpLexicalGroup lexicalGroup = parentName.getLexicalGroup();
		AkpVernacularName name = new AkpVernacularName();
		name.setLexicalGroup(lexicalGroup);
		name.setName("");
		name.setComments("");
		name.setParentId(parentName.getXid());
		lexicalGroup.getVernacularNames().add(name);
		lexicalGroup.refreshVernacularNamesTree();
		getSession().save(name);
		akpLogService.logVernacularNameCreation(name);
	}

	@Transactional
	@Override
	public void updateVernacularNameName(AkpVernacularName vernacularName,
			String newName) {
		String oldName = vernacularName.getName();
		vernacularName.setName(newName);
		vernacularName.getLexicalGroup().refreshVernacularNamesTree();
		getSession().update(vernacularName);
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
		akpLogService.logVernacularNameDeletion(vernacularName);
		getSession().delete(vernacularName);
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

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<String> searchBibFromId(String id) {
		return getSession().createCriteria(AkpBib.class)
				.add(Restrictions.like("xid", "%" + id + "%"))
				.setProjection(Projections.property("xid")).list();
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

	private Session getSession() {
		return sessionProvider.get();
	}

	@Transactional
	@Override
	public void testNode() {
		Node parent = (Node) getSession().get(Node.class, 1);
		// Node child0 = parent.getChildren().remove(0);
		// parent.getChildren().add(1, child0);
		Collections.reverse(parent.getChildren());
		Node x = new Node();
		x.setParent(parent);
		parent.getChildren().add(x);
		getSession().save(x);
		getSession().update(parent);
	}

}
