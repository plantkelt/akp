package net.plantkelt.akp.service;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpVernacularName;

public interface AkpTaxonService {

	public AkpClass getClass(Integer xid);

	public void createNewClass(AkpClass parentClass);

	public void moveDownChildClass(AkpClass parentClass,
			int childIndexToMoveDown);

	public void updateClass(AkpClass akpClass);

	public boolean canDeleteClass(AkpClass akpClass);

	public boolean deleteClass(AkpClass akpClass);

	public AkpPlant getPlant(Integer xid);

	public void updatePlant(AkpPlant plant);

	public void createNewTaxon(AkpPlant ownerPlant);

	public void updateTaxon(AkpTaxon taxon);

	public void deleteTaxon(AkpTaxon taxon);

	public void updateVernacularName(AkpVernacularName vernacularName);

	public void testNode();

}
