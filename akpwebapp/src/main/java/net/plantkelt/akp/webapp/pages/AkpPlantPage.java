package net.plantkelt.akp.webapp.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpLangGroup;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpLexicalGroupPanel;
import net.plantkelt.akp.webapp.elements.AkpParentClassPathLabel;
import net.plantkelt.akp.webapp.elements.AkpPlantBackRefsPanel;
import net.plantkelt.akp.webapp.elements.AkpPlantControlPanel;
import net.plantkelt.akp.webapp.elements.AkpPlantHeaderPanel;
import net.plantkelt.akp.webapp.elements.AkpPlantRefsPanel;
import net.plantkelt.akp.webapp.elements.AkpPlantSynonymsPanel;
import net.plantkelt.akp.webapp.elements.AkpPlantTagsPanel;
import net.plantkelt.akp.webapp.models.BrEnFrStringModel;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class AkpPlantPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private Integer plantId;
	private IModel<AkpPlant> plantModel;
	private int lastLangGroupXid = -1;

	public AkpPlantPage(PageParameters parameters) {
		super(parameters);

		plantId = parameters.get("xid").toOptionalInteger();
		// Load data
		boolean isAdmin = AkpWicketSession.get().isAdmin();

		// Check for public access
		if (!AkpWicketSession.get().isLoggedIn()
				&& !akpTaxonService.getPublicPlantXids().contains(plantId))
			throw new UnauthorizedInstantiationException(AkpPlantPage.class);

		plantModel = new LoadableDetachableModel<AkpPlant>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpPlant load() {
				return akpTaxonService.getPlant(plantId);
			}
		};
		// Set page title
		setPageTitleModel(new PropertyModel<String>(plantModel,
				"mainName.textName"));

		// Parent classes
		AkpParentClassPathLabel parentPathLabel = new AkpParentClassPathLabel(
				"parentPath", new PropertyModel<AkpClass>(plantModel,
						"akpClass"));
		add(parentPathLabel);

		// Plant main name
		AkpPlantHeaderPanel headerPanel = new AkpPlantHeaderPanel(
				"headerPanel", plantModel);
		add(headerPanel);

		// Plant xref
		AkpPlantRefsPanel plantRefsPanel = new AkpPlantRefsPanel(
				"plantRefsPanel", plantModel);
		add(plantRefsPanel);

		// Synonyms
		AkpPlantSynonymsPanel synonymsPanel = new AkpPlantSynonymsPanel(
				"synonymsPanel", plantModel);
		add(synonymsPanel);

		// Tags
		AkpPlantTagsPanel tagsPanel = new AkpPlantTagsPanel("tagsPanel",
				plantModel);
		add(tagsPanel);

		// Links
		Link<AkpPlantLogsPage> viewHistoryLink = AkpPlantLogsPage.link(
				"viewHistoryLink", plantId);
		viewHistoryLink.setVisible(isAdmin);
		add(viewHistoryLink);

		// Lexical groups
		IModel<List<AkpLexicalGroup>> lexicalGroupsModel = new LoadableDetachableModel<List<AkpLexicalGroup>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpLexicalGroup> load() {
				List<AkpLexicalGroup> retval = new ArrayList<AkpLexicalGroup>(
						plantModel.getObject().getSortedLexicalGroups());
				// Only return lang with correct profile
				int profile = AkpUser.PROFILE_USER;
				if (AkpWicketSession.get().isLoggedIn())
					profile = AkpWicketSession.get().getAkpUser().getProfile();
				for (Iterator<AkpLexicalGroup> i = retval.iterator(); i
						.hasNext();) {
					AkpLexicalGroup lexicalGroup = i.next();
					if (lexicalGroup.getLang().getLevel() > profile)
						i.remove();
				}
				return retval;
			}
		};
		ListView<AkpLexicalGroup> lexicalGroupsListView = new ListView<AkpLexicalGroup>(
				"lexicalGroups", lexicalGroupsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpLexicalGroup> item) {
				if (item.getIndex() == 0)
					lastLangGroupXid = -1;
				AkpLexicalGroup lexicalGroup = item.getModelObject();
				AkpLangGroup langGroup = lexicalGroup.getLang().getLangGroup();
				// If needed lang group header
				Label langGroupLabel = new Label("langGroupName",
						new BrEnFrStringModel(langGroup.getName()));
				item.add(langGroupLabel);
				langGroupLabel.setVisible(lastLangGroupXid != langGroup
						.getXid());
				lastLangGroupXid = langGroup.getXid();
				// Create model to prevent reloading whole plant
				final Integer lexicalGroupXid = lexicalGroup.getXid();
				IModel<AkpLexicalGroup> lexicalGroupModel = new LoadableDetachableModel<AkpLexicalGroup>(
						lexicalGroup) {
					private static final long serialVersionUID = 1L;

					@Override
					protected AkpLexicalGroup load() {
						return akpTaxonService.getLexicalGroup(lexicalGroupXid);
					}
				};
				// Lexical group panel
				AkpLexicalGroupPanel lexicalGroupPanel = new AkpLexicalGroupPanel(
						"lexicalGroupPanel", lexicalGroupModel,
						AkpPlantPage.this);
				item.add(lexicalGroupPanel);
			}
		};
		add(lexicalGroupsListView);

		// Back references
		AkpPlantBackRefsPanel backRefsPanel = new AkpPlantBackRefsPanel(
				"backRefsPanel", plantModel);
		add(backRefsPanel);

		// Plant control panel (add lexical group, delete plant)
		AkpPlantControlPanel plantControlPanel = new AkpPlantControlPanel(
				"plantControlPanel", this, plantModel);
		add(plantControlPanel);

		this.setOutputMarkupId(true);
	}

	public static Link<AkpPlantPage> link(String id, Integer xid) {
		return new BookmarkablePageLink<AkpPlantPage>(id, AkpPlantPage.class,
				new PageParameters().add("xid", xid));
	}
}
