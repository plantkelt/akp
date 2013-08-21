package net.plantkelt.akp.webapp.elements;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.plantkelt.akp.domain.AkpAuthor;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.CollapsibleButton;
import net.plantkelt.akp.webapp.components.EditorModel;
import net.plantkelt.akp.webapp.components.InPlaceEditor;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpPlantSynonymsPanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	public AkpPlantSynonymsPanel(String id, final IModel<AkpPlant> plantModel) {
		super(id);

		final boolean isAdmin = AkpWicketSession.get().isAdmin();

		WebMarkupContainer collapseDiv = new WebMarkupContainer("collapseDiv");
		add(collapseDiv);

		CollapsibleButton collapseButton = new CollapsibleButton(
				"collapseButton", collapseDiv, AkpWicketSession.get()
						.getSessionData().isSynonymsDefaultOpen()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onOpenClose(boolean opened) {
				AkpWicketSession.get().getSessionData()
						.setSynonymsDefaultOpen(opened);
			}
		};
		add(collapseButton);

		final IModel<List<AkpTaxon>> listModel = new PropertyModel<List<AkpTaxon>>(
				plantModel, "synonyms");
		final IModel<Map<String, AkpAuthor>> authorsModel = new LoadableDetachableModel<Map<String, AkpAuthor>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected Map<String, AkpAuthor> load() {
				Set<String> authorIds = new HashSet<String>();
				for (AkpTaxon taxon : listModel.getObject()) {
					authorIds.addAll(taxon.getReferencedAuthorIds());
				}
				return akpTaxonService.getAuthors(authorIds);
			}
		};
		ListView<AkpTaxon> synonymsList = new ListView<AkpTaxon>("synonyms",
				listModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpTaxon> item) {
				final IModel<AkpTaxon> taxonModel = item.getModel();
				InPlaceEditor editor = new InPlaceEditor("synonymEditor",
						new EditorModel<String>() {
							private static final long serialVersionUID = 1L;

							@Override
							public String getObject() {
								return taxonModel.getObject().getName();
							}

							@Override
							public void saveObject(AjaxRequestTarget target,
									String name) {
								AkpTaxon taxon = taxonModel.getObject();
								if (name == null || name.length() == 0) {
									akpTaxonService.deleteTaxon(taxon);
								} else {
									akpTaxonService
											.updateTaxonName(taxon, name);
									List<String> errorKeys = akpTaxonService
											.checkTaxon(taxon);
									if (!errorKeys.isEmpty()) {
										StringBuffer errorMsg = new StringBuffer();
										errorMsg.append(
												getString("taxon.name.error.dialog"))
												.append("\\n");
										for (String errorKey : errorKeys)
											errorMsg.append(getString(errorKey))
													.append("\\n");
										target.appendJavaScript(String.format(
												"alert('%s')",
												errorMsg.toString().replace(
														"'", "\\'")));
									}
								}
								authorsModel.detach();
								target.add(AkpPlantSynonymsPanel.this);
							}
						});
				item.add(editor);
				editor.setReadOnly(!isAdmin);
				if (taxonModel.getObject().getName().equals("<l><b></b></l>"))
					editor.open();
				AkpTaxonLabel synonymLabel = new AkpTaxonLabel("synonymName",
						taxonModel, authorsModel);
				editor.add(synonymLabel);
				synonymLabel.setEscapeModelStrings(false);
			}
		};
		collapseDiv.add(synonymsList);
		setOutputMarkupId(true);

		// Add synonym button
		Form<Void> form = new Form<Void>("form");
		form.add(new AjaxSubmitLink("addSynonymButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				akpTaxonService.createNewTaxon(plantModel.getObject());
				target.add(AkpPlantSynonymsPanel.this);
			}
		});
		collapseDiv.add(form);
		form.setVisible(isAdmin);
	}
}
