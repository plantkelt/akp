package net.plantkelt.akp.webapp.elements;

import java.util.List;

import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.CollapsibleButton;
import net.plantkelt.akp.webapp.pages.AkpPlantPage;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.inject.Inject;

public class AkpPlantBackRefsPanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	public AkpPlantBackRefsPanel(String id, final IModel<AkpPlant> plantModel) {
		super(id);

		// Collapsible header
		WebMarkupContainer collapseDiv = new WebMarkupContainer("collapseDiv");
		add(collapseDiv);
		CollapsibleButton collapseButton = new CollapsibleButton(
				"collapseButton", collapseDiv, AkpWicketSession.get()
						.getSessionData().isBackRefsDefaultOpen()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onOpenClose(boolean opened) {
				AkpWicketSession.get().getSessionData()
						.setBackRefsDefaultOpen(opened);
			}
		};
		add(collapseButton);

		// Plant back references
		IModel<List<AkpPlant>> plantBackRefModel = new LoadableDetachableModel<List<AkpPlant>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpPlant> load() {
				return akpTaxonService.getPlantBackRefs(plantModel.getObject());
			}
		};
		ListView<AkpPlant> plantBackRefList = new ListView<AkpPlant>(
				"plantBackRefList", plantBackRefModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpPlant> item) {
				AkpPlant plant = item.getModelObject();
				Link<AkpPlantPage> plantLink = AkpPlantPage.link("plantLink",
						plant.getXid());
				item.add(plantLink);
				Label plantLabel = new Label("plant",
						"⇐ " + plant.getMainName().getHtmlName());
				plantLabel.setEscapeModelStrings(false);
				plantLink.add(plantLabel);
			}
		};
		collapseDiv.add(plantBackRefList);

		// Vernacular name back references
		IModel<List<AkpVernacularName>> vernaBackRefModel = new LoadableDetachableModel<List<AkpVernacularName>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpVernacularName> load() {
				return akpTaxonService
						.getVernacularNameBackRefs(plantModel.getObject());
			}
		};
		ListView<AkpVernacularName> vernaBackRefList = new ListView<AkpVernacularName>(
				"vernaBackRefList", vernaBackRefModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpVernacularName> item) {
				AkpVernacularName vernaName = item.getModelObject();
				AkpLexicalGroup lexicalGroup = vernaName.getLexicalGroup();
				Label vernaNameLabel = new Label("vernaName",
						"⇐ " + vernaName.getName());
				item.add(vernaNameLabel);
				Label langLabel = new Label("lang", lexicalGroup.getLang()
						.getXid()
						+ (lexicalGroup.getCorrect() == 0 ? ""
								: lexicalGroup.getCorrectDisplayCode()));
				item.add(langLabel);
				Link<AkpPlantPage> plantLink = AkpPlantPage.link("plantLink",
						lexicalGroup.getPlant().getXid());
				item.add(plantLink);
				Label plantLabel = new Label("plant",
						vernaName.getLexicalGroup().getPlant().getMainName()
								.getHtmlName());
				plantLabel.setEscapeModelStrings(false);
				plantLink.add(plantLabel);
			}
		};
		collapseDiv.add(vernaBackRefList);
	}
}
