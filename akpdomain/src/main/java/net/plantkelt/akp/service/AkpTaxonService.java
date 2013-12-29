package net.plantkelt.akp.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.plantkelt.akp.domain.AkpAuthor;
import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpLangGroup;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpSearchData;
import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.domain.AkpVernacularName;

public interface AkpTaxonService {

	public abstract AkpClass getClass(Integer xid);

	public abstract List<AkpClass> getFamilies();

	public abstract List<AkpClass> searchClass(String searchText);

	public abstract void createNewClass(AkpClass parentClass);

	public abstract void moveDownChildClass(AkpClass parentClass,
			int childIndexToMoveDown);

	public abstract void updateClass(AkpClass akpClass);

	public abstract boolean canDeleteClass(AkpClass akpClass);

	public abstract boolean deleteClass(AkpClass akpClass);

	public abstract AkpPlant createNewPlant(AkpClass owningClass);

	public abstract AkpPlant getPlant(Integer xid);

	public abstract Set<Integer> getPublicPlantXids();

	public abstract List<AkpPlant> searchPlantFromName(String name);

	public abstract void updatePlantComments(AkpPlant plant, String newComments);

	public abstract void addPlantRefToPlant(AkpPlant plant, AkpPlant targetPlant);

	public abstract void removePlantRefFromPlant(AkpPlant plant,
			AkpPlant targetPlant);

	public abstract boolean canDeletePlant(AkpPlant plant);

	public abstract boolean deletePlant(AkpPlant plant);

	public abstract void movePlant(AkpPlant plant, AkpClass newClass);

	public abstract boolean createNewPlantTag(AkpPlant plant, int tagType);

	public abstract void updatePlantTag(AkpPlantTag tag);

	public abstract void deletePlantTag(AkpPlantTag tag);

	public abstract void createNewTaxon(AkpPlant ownerPlant);

	public abstract void updateTaxonName(AkpTaxon taxon, String newName);

	public abstract List<String> checkTaxon(AkpTaxon taxon);

	public abstract void deleteTaxon(AkpTaxon taxon);

	public abstract void addRootVernacularName(AkpLexicalGroup lexicalGroup,
			String defaultBib);

	public abstract void addChildVernacularName(AkpVernacularName parentName,
			String defaultBib);

	public abstract void updateVernacularNameName(
			AkpVernacularName vernacularName, String newName);

	public abstract void updateVernacularNameComments(
			AkpVernacularName vernacularName, String newComments);

	public abstract void addBibToVernacularName(AkpBib bib,
			AkpVernacularName vernacularName);

	public abstract void removeBibFromVernacularName(AkpBib bib,
			AkpVernacularName vernacularName);

	public abstract void addPlantRefToVernacularName(AkpPlant targetPlant,
			AkpVernacularName vernacularName);

	public abstract void removePlantRefFromVernacularName(AkpPlant targetPlant,
			AkpVernacularName vernacularName);

	public abstract boolean deleteVernacularName(
			AkpVernacularName vernacularName);

	public abstract AkpBib createNewBib(String xid);

	public abstract boolean deleteBib(AkpBib bib);

	public abstract AkpBib getBib(String xid);

	public abstract List<AkpBib> getBibs();

	public abstract List<AkpBib> searchBib(int limit, String xid, String title,
			String author, String date, String isbn, String comments,
			String editor);

	public abstract List<String> searchBibFromId(String id);

	public abstract void updateBib(AkpBib bib);

	public abstract List<AkpLangGroup> getLangGroupList();

	public abstract boolean createNewLang(String xid);

	public abstract List<AkpLang> getLangList(int level);

	public abstract AkpLang getLang(String xid);

	public abstract void updateLang(AkpLang lang);

	public abstract boolean canDeleteLang(AkpLang lang);

	public abstract void deleteLang(AkpLang lang);

	public abstract boolean createNewLexicalGroup(AkpPlant plant, AkpLang lang,
			Integer correct);

	public abstract AkpLexicalGroup getLexicalGroup(Integer xid);

	public abstract boolean deleteLexicalGroup(AkpLexicalGroup lexicalGroup);

	public abstract List<AkpPlant> getPlantBackRefs(AkpPlant plant);

	public abstract List<AkpVernacularName> getVernacularNameBackRefs(
			AkpPlant plant);

	public abstract List<AkpVernacularName> getVernacularNameRefsFromBib(
			AkpBib bib);

	public abstract AkpAuthor createNewAuthor(String xid);

	public abstract AkpAuthor getAuthor(String xid);

	public abstract Map<String, AkpAuthor> getAuthors(Set<String> xids);

	public abstract Map<String, Set<AkpAuthor>> getAuthorFromSources(
			Set<String> oldXids);

	public abstract List<AkpAuthor> getAuthors();

	public abstract List<AkpAuthor> searchAuthor(int limit, String xid,
			String name, String dates, String source);

	public abstract List<AkpTaxon> getTaxonsForAuthor(int limit,
			AkpAuthor author);

	public abstract void updateAuthor(AkpAuthor author);

	public abstract int renameAuthorXid(AkpAuthor author, String newXid);

	public abstract boolean deleteAuthor(AkpAuthor author);

	public abstract AkpSearchResult search(AkpUser user,
			AkpSearchData searchData);

	public abstract Date getLastUpdate();

	public abstract Map<String, Long> getObjectCount();

	public abstract Map<AkpLang, Long> getVernacularNameCountPerLanguage();

	public abstract AkpSearchResult getDuplicatedVernacularNames();

	public abstract AkpSearchResult getDuplicatedTaxonNames();

	public abstract AkpSearchResult getTaxonSyntaxErrors();

	public abstract AkpSearchResult getAuthorWithoutTags();

	public abstract AkpSearchResult getImpreciseVernaWithoutPlantRef();

	public abstract AkpSearchResult getAuthorRefCount();

	public abstract AkpSearchResult getPlantsWithoutVerna();

	public abstract AkpSearchResult getPlantsXRefs();

	public abstract AkpSearchResult getHybridParents();

	public abstract AkpSearchResult getEqualsSynonyms();

	public abstract void mergeLang(String lang1, String lang2);

	public abstract void addAuthNameAsSource();

	public abstract void updateStaticIndexes();
}
