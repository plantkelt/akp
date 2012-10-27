package net.plantkelt.akp.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.plantkelt.akp.domain.AkpAuthor;
import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpSearchData;
import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpVernacularName;

public interface AkpTaxonService {

	public AkpClass getClass(Integer xid);

	public List<AkpClass> getFamilies();

	public void createNewClass(AkpClass parentClass);

	public void moveDownChildClass(AkpClass parentClass,
			int childIndexToMoveDown);

	public void updateClass(AkpClass akpClass);

	public boolean canDeleteClass(AkpClass akpClass);

	public boolean deleteClass(AkpClass akpClass);

	public AkpPlant createNewPlant(AkpClass owningClass);

	public AkpPlant getPlant(Integer xid);

	public Set<Integer> getPublicPlantXids();

	public List<AkpPlant> searchPlantFromName(String name);

	public void updatePlantComments(AkpPlant plant, String newComments);

	public void addPlantRefToPlant(AkpPlant plant, AkpPlant targetPlant);

	public void removePlantRefFromPlant(AkpPlant plant, AkpPlant targetPlant);

	public boolean canDeletePlant(AkpPlant plant);

	public boolean deletePlant(AkpPlant plant);

	public boolean createNewPlantTag(AkpPlant plant, int tagType);

	public void updatePlantTag(AkpPlantTag tag);

	public void deletePlantTag(AkpPlantTag tag);

	public void createNewTaxon(AkpPlant ownerPlant);

	public void updateTaxonName(AkpTaxon taxon, String newName);

	public void deleteTaxon(AkpTaxon taxon);

	public void addRootVernacularName(AkpLexicalGroup lexicalGroup);

	public void addChildVernacularName(AkpVernacularName parentName);

	public void updateVernacularNameName(AkpVernacularName vernacularName,
			String newName);

	public void updateVernacularNameComments(AkpVernacularName vernacularName,
			String newComments);

	public void addBibToVernacularName(AkpBib bib,
			AkpVernacularName vernacularName);

	public void removeBibFromVernacularName(AkpBib bib,
			AkpVernacularName vernacularName);

	public void addPlantRefToVernacularName(AkpPlant targetPlant,
			AkpVernacularName vernacularName);

	public void removePlantRefFromVernacularName(AkpPlant targetPlant,
			AkpVernacularName vernacularName);

	public boolean deleteVernacularName(AkpVernacularName vernacularName);

	public AkpBib createNewBib(String xid);

	public boolean deleteBib(AkpBib bib);

	public AkpBib getBib(String xid);

	public List<AkpBib> getBibs();

	public List<AkpBib> searchBib(int limit, String xid, String title,
			String author, String date, String isbn, String comments,
			String editor);

	public List<String> searchBibFromId(String id);

	public void updateBib(AkpBib bib);

	public AkpLang getLang(String xid);

	public List<AkpLang> getLangList();

	public boolean createNewLexicalGroup(AkpPlant plant, AkpLang lang,
			Integer correct);

	public AkpLexicalGroup getLexicalGroup(Integer xid);

	public boolean deleteLexicalGroup(AkpLexicalGroup lexicalGroup);

	public List<AkpPlant> getPlantBackRefs(AkpPlant plant);

	public List<AkpVernacularName> getVernacularNameBackRefs(AkpPlant plant);

	public List<AkpVernacularName> getVernacularNameRefsFromBib(AkpBib bib);

	public AkpAuthor createNewAuthor(String xid);

	public AkpAuthor getAuthor(String xid);

	public Map<String, AkpAuthor> getAuthors(Set<String> xids);

	public List<AkpAuthor> searchAuthor(int limit, String xid, String name,
			String dates, String source);

	public List<AkpTaxon> getTaxonsForAuthor(int limit, AkpAuthor author);

	public void updateAuthor(AkpAuthor author);

	public boolean deleteAuthor(AkpAuthor author);

	public AkpSearchResult search(AkpSearchData searchData);

}
