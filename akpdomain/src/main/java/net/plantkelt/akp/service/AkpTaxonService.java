package net.plantkelt.akp.service;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpPlant;

public interface AkpTaxonService {

	public AkpClass getClass(Integer xid);

	public void createNewClass(AkpClass parentClass);

	public void moveDownChildClass(AkpClass parentClass,
			int childIndexToMoveDown);

	public void updateClass(AkpClass akpClass);

	public AkpPlant getPlant(Integer xid);
	
	public void testNode();

}
