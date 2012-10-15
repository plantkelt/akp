package net.plantkelt.akp.webapp.components;

import java.util.List;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpPlantSynonymsPanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	public AkpPlantSynonymsPanel(String id, IModel<AkpPlant> akpPlantModel) {
		super(id);

		AkpUser user = AkpWicketSession.get().getAkpUser();
		final boolean isAdmin = user != null && user.isAdmin();

		IModel<List<AkpTaxon>> listModel = new PropertyModel<List<AkpTaxon>>(
				akpPlantModel, "synonyms");
		ListView<AkpTaxon> synonymsList = new ListView<AkpTaxon>("synonyms",
				listModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpTaxon> item) {
				final IModel<AkpTaxon> taxonModel = item.getModel();
				InPlaceEditor editor = new InPlaceEditor("synonymEditor",
						new EditorModel<String>() {
							@Override
							public String getObject() {
								return taxonModel.getObject().getName();
							}

							@Override
							public void saveObject(AjaxRequestTarget target,
									String name) {
								AkpTaxon taxon = taxonModel.getObject();
								if (name == null || name.length() == 0) {
									taxon.getPlant().removeTaxon(taxon);
									akpTaxonService.deleteTaxon(taxon);
									akpTaxonService.updatePlant(taxon
											.getPlant());
								} else {
									taxon.setName(name);
									akpTaxonService.updateTaxon(taxon);
								}
								target.add(AkpPlantSynonymsPanel.this);
							}
						});
				item.add(editor);
				editor.setReadOnly(!isAdmin);
				IModel<String> nameModel = new PropertyModel<String>(
						taxonModel, "htmlName");
				Label synonymLabel = new Label("synonymName", nameModel);
				editor.add(synonymLabel);
				synonymLabel.setEscapeModelStrings(false);
			}
		};
		add(synonymsList);
		setOutputMarkupId(true);
	}
}
