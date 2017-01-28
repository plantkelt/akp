package net.plantkelt.akp.webapp.elements;

import java.util.List;

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
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return plantModel.getObject().getMainName().getName();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String name) {
						AkpTaxon mainTaxon = plantModel.getObject()
								.getMainName();
						if (name != null && name.length() > 0) {
							akpTaxonService.updateTaxonName(mainTaxon, name);
							List<String> errorKeys = akpTaxonService
									.checkTaxon(mainTaxon);
							if (!errorKeys.isEmpty()) {
								StringBuffer errorMsg = new StringBuffer();
								errorMsg.append(
										getString("taxon.name.error.dialog"))
										.append("\\n");
								for (String errorKey : errorKeys)
									errorMsg.append(getString(errorKey))
											.append("\\n");
								target.appendJavaScript(String.format(
										"alert('%s')", errorMsg.toString()
												.replace("'", "\\'")));
							}
						}
						target.add(AkpPlantHeaderPanel.this);
					}
				}, 4, 60);
		add(nameEditor);
		nameEditor.setReadOnly(!isAdmin);
		IModel<AkpTaxon> nameModel = new PropertyModel<AkpTaxon>(plantModel,
				"mainName");
		AkpTaxonLabel nameLabel = new AkpTaxonLabel("nameLabel", nameModel,
				null);
		nameEditor.add(nameLabel);
		nameLabel.setEscapeModelStrings(false);

		// Comments
		InPlaceEditor commentsEditor = new InPlaceEditor("commentsEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return plantModel.getObject().getComments();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String comments) {
						akpTaxonService.updatePlantComments(
								plantModel.getObject(), comments);
						target.add(AkpPlantHeaderPanel.this);
					}
				}, 4, 60);
		commentsEditor.setVisible(false);
		add(commentsEditor);
		commentsEditor.setReadOnly(!isAdmin);
		IModel<String> commentsModel = new PropertyModel<String>(plantModel,
				"comments");
		Label commentsLabel = new Label("commentsLabel", commentsModel);
		commentsEditor.add(commentsLabel);

		setOutputMarkupId(true);
	}
}
