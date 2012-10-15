package net.plantkelt.akp.webapp.pages;

import java.util.List;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AkpParentClassPathLabel;
import net.plantkelt.akp.webapp.components.AkpPlantHeaderPanel;
import net.plantkelt.akp.webapp.components.AkpPlantSynonymsPanel;
import net.plantkelt.akp.webapp.components.AkpPlantTagsPanel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class AkpPlantPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private Integer plantId;
	private IModel<AkpPlant> plantModel;

	public AkpPlantPage(PageParameters parameters) {

		// Load data
		plantId = parameters.get("xid").toOptionalInteger();
		plantModel = new LoadableDetachableModel<AkpPlant>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpPlant load() {
				return akpTaxonService.getPlant(plantId);
			}
		};
		AkpPlant plant = plantModel.getObject();

		// Parent classes
		AkpParentClassPathLabel parentPathLabel = new AkpParentClassPathLabel(
				"parentPath", plant.getAkpClass());
		add(parentPathLabel);

		// Plant main name
		AkpPlantHeaderPanel headerPanel = new AkpPlantHeaderPanel(
				"headerPanel", plantModel);
		add(headerPanel);

		// Synonyms
		AkpPlantSynonymsPanel synonymsPanel = new AkpPlantSynonymsPanel(
				"synonymsPanel", plantModel);
		add(synonymsPanel);

		// Tags
		AkpPlantTagsPanel tagsPanel = new AkpPlantTagsPanel("tagsPanel",
				plantModel);
		add(tagsPanel);

		// Lexical groups
		RepeatingView lexicalGroupsRepeat = new RepeatingView("lexicalGroups");
		add(lexicalGroupsRepeat);
		List<AkpLexicalGroup> lexicalGroups = plant.getLexicalGroups();
		for (AkpLexicalGroup lexicalGroup : lexicalGroups) {
			WebMarkupContainer item = new WebMarkupContainer(
					lexicalGroupsRepeat.newChildId());
			lexicalGroupsRepeat.add(item);
			String xxx = String.format("%d - %s - %s",
					lexicalGroup.getCorrect(), lexicalGroup.getLang().getXid(),
					lexicalGroup.getLang().getLangGroup().getName());
			Label label = new Label("lexicalGroupValue", xxx);
			item.add(label);
			// Names
			RepeatingView vernaNamesRepeat = new RepeatingView("vernaNames");
			item.add(vernaNamesRepeat);
			List<AkpVernacularName> vernaNames = lexicalGroup
					.getVernacularNames();
			for (AkpVernacularName vernaName : vernaNames) {
				WebMarkupContainer item2 = new WebMarkupContainer(
						vernaNamesRepeat.newChildId());
				vernaNamesRepeat.add(item2);
				StringBuffer bibsb = new StringBuffer();
				for (AkpBib bib : vernaName.getBibs()) {
					bibsb.append("[").append(bib.getXid()).append("]");
				}
				String xxx2 = String.format("%d -> %d : %s (%s) -> %s",
						vernaName.getXid(), vernaName.getParentId(),
						vernaName.getName(), vernaName.getComment(),
						bibsb.toString());
				Label label2 = new Label("vernaNameValue", xxx2);
				item2.add(label2);
			}
		}
	}

	public static Link<AkpPlantPage> link(String id, Integer xid) {
		return new BookmarkablePageLink<AkpPlantPage>(id, AkpPlantPage.class,
				new PageParameters().add("xid", xid));
	}
}
