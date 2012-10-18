package net.plantkelt.akp.service.impl;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.domain.Node;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpTaxonService;

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
		return (AkpPlant) getSession().get(AkpPlant.class, xid);
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

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<String> searchBibFromId(String id) {
		return (List<String>) getSession().createCriteria(AkpBib.class)
				.add(Restrictions.like("xid", "%" + id + "%"))
				.setProjection(Projections.property("xid")).list();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AkpLang> getLangList() {
		return (List<AkpLang>) getSession().createCriteria(AkpLang.class)
				.list();
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
