package net.plantkelt.akp.webapp.pages;

import java.util.List;

import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AkpLexicalGroupPanel;
import net.plantkelt.akp.webapp.components.AkpParentClassPathLabel;
import net.plantkelt.akp.webapp.components.AkpPlantHeaderPanel;
import net.plantkelt.akp.webapp.components.AkpPlantSynonymsPanel;
import net.plantkelt.akp.webapp.components.AkpPlantTagsPanel;

import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
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

		if (plantId == 3000)
			throw new UnauthorizedInstantiationException(AkpPlantPage.class);

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
		IModel<List<AkpLexicalGroup>> lexicalGroupsModel = new LoadableDetachableModel<List<AkpLexicalGroup>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpLexicalGroup> load() {
				// TODO Sort based on group lang + lang order
				return plantModel.getObject().getLexicalGroups();
			}
		};
		ListView<AkpLexicalGroup> lexicalGroupsListView = new ListView<AkpLexicalGroup>(
				"lexicalGroups", lexicalGroupsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpLexicalGroup> item) {
				Label langGroupLabel = new Label("langGroupName", item
						.getModelObject().getLang().getLangGroup().getName());
				item.add(langGroupLabel);
				AkpLexicalGroupPanel lexicalGroupPanel = new AkpLexicalGroupPanel(
						"lexicalGroupPanel", item.getModel());
				item.add(lexicalGroupPanel);
			}
		};
		add(lexicalGroupsListView);
	}

	public static Link<AkpPlantPage> link(String id, Integer xid) {
		return new BookmarkablePageLink<AkpPlantPage>(id, AkpPlantPage.class,
				new PageParameters().add("xid", xid));
	}
}
