package net.plantkelt.akp.service.impl;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.Node;
import net.plantkelt.akp.service.AkpTaxonService;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class AkpTaxonServiceImpl implements AkpTaxonService {

	@Inject
	private Provider<Session> sessionProvider;

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
		parentClass.getChildren().add(newClass);
		getSession().save(newClass);
		getSession().update(parentClass);
	}

	@Transactional
	@Override
	public void updateClass(AkpClass akpClass) {
		getSession().saveOrUpdate(akpClass);
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
		getSession().delete(akpClass);
		return true;
	}

	@Transactional
	@Override
	public AkpPlant getPlant(Integer xid) {
		return (AkpPlant) getSession().get(AkpPlant.class, xid);
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
