package net.plantkelt.akp.service.impl;

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
			rootClass.setChildrens(rootClasses);
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
		List<AkpClass> children = parentClass.getChildrens();
		AkpClass toMove = children.remove(childIndexToMoveDown);
		int where = childIndexToMoveDown >= children.size() ? 0
				: childIndexToMoveDown + 1;
		children.add(where, toMove);
		parentClass.setChildrens(children);
		toMove.setParent(parentClass);
		getSession().update(parentClass);
		// for (AkpClass child: children) {
		// getSession().update(child);
		// }
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
		parentClass.getChildrens().add(newClass);
		getSession().save(newClass);
		getSession().update(parentClass);
	}

	@Transactional
	@Override
	public void updateClass(AkpClass akpClass) {
		getSession().saveOrUpdate(akpClass);
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
		Node child0 = parent.getChildren().remove(0);
		parent.getChildren().add(1, child0);
		getSession().update(parent);
	}

}
