package net.plantkelt.akp.webapp.elements;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.EditorModel;
import net.plantkelt.akp.webapp.components.InPlaceEditor;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpPlantHeaderPanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	public AkpPlantHeaderPanel(String id, final IModel<AkpPlant> plantModel) {
		super(id);

		final boolean isAdmin = AkpWicketSession.get().isAdmin();

		// Main name
		InPlaceEditor nameEditor = new InPlaceEditor("nameEditor",
				new EditorModel<String>() {
					@Override
					public String getObject() {
						return plantModel.getObject().getMainName().getName();
					}

					@Override
					public void saveObject(AjaxRequestTarget target, String name) {
						AkpTaxon mainTaxon = plantModel.getObject()
								.getMainName();
						if (name != null && name.length() > 0) {
							mainTaxon.setName(name);
							akpTaxonService.updateTaxon(mainTaxon);
						}
						target.add(AkpPlantHeaderPanel.this);
					}
				});
		add(nameEditor);
		nameEditor.setReadOnly(!isAdmin);
		IModel<String> nameModel = new PropertyModel<String>(plantModel,
				"mainName.htmlName");
		Label nameLabel = new Label("nameLabel", nameModel);
		nameEditor.add(nameLabel);
		nameLabel.setEscapeModelStrings(false);

		// Comments
		InPlaceEditor commentsEditor = new InPlaceEditor("commentsEditor",
				new EditorModel<String>() {
					@Override
					public String getObject() {
						return plantModel.getObject().getComments();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String comments) {
						AkpPlant plant = plantModel.getObject();
						plant.setComments(comments);
						akpTaxonService.updatePlant(plant);
						target.add(AkpPlantHeaderPanel.this);
					}
				});
		add(commentsEditor);
		commentsEditor.setReadOnly(!isAdmin);
		IModel<String> commentsModel = new PropertyModel<String>(plantModel,
				"comments");
		Label commentsLabel = new Label("commentsLabel", commentsModel);
		commentsEditor.add(commentsLabel);

		setOutputMarkupId(true);
	}
}
