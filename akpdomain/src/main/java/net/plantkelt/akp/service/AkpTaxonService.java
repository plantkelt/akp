package net.plantkelt.akp.service;

import java.util.List;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpLexicalGroup;
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
	
	public List<AkpPlant> searchPlantFromName(String name);

	public void updatePlant(AkpPlant plant);

	public void createNewTaxon(AkpPlant ownerPlant);

	public void updateTaxon(AkpTaxon taxon);

	public void deleteTaxon(AkpTaxon taxon);

	public void addRootVernacularName(AkpLexicalGroup lexicalGroup);

	public void addChildVernacularName(AkpVernacularName parentName);

	public void updateVernacularName(AkpVernacularName vernacularName);

	public boolean deleteVernacularName(AkpVernacularName vernacularName);

	public AkpBib getBib(String xid);
	
	public List<String> searchBibFromId(String id);
	
	public void testNode();

}
