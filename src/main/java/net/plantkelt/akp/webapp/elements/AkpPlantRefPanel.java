package net.plantkelt.akp.webapp.elements;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AjaxConfirmLink;
import net.plantkelt.akp.webapp.pages.AkpPlantPage;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.google.inject.Inject;

public class AkpPlantRefPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpPlantRefPanel(String id, final IModel<AkpPlant> targetPlantModel,
			final AkpPlantRefListener plantRefListener) {
		super(id);

		boolean isAdmin = AkpWicketSession.get().isAdmin();

		// Link to referenced plant
		AkpPlant targetPlant = targetPlantModel.getObject();
		Link<AkpPlantPage> plantRefLink = AkpPlantPage.link("plantRefLink",
				targetPlant.getXid());
		add(plantRefLink);

		// Label to display
		Label plantRefLabel = new Label("plantRefLabel",
				"⇒ " + targetPlant.getMainName().getHtmlName());
		plantRefLabel.setEscapeModelStrings(false);
		plantRefLink.add(plantRefLabel);

		// Delete link
		AjaxConfirmLink<Void> deleteLink = new AjaxConfirmLink<Void>(
				"deleteLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				AkpPlant plantRef = targetPlantModel.getObject();
				plantRefListener.onPlantRefRemoved(target, plantRef);
			}
		};
		deleteLink.setVisible(isAdmin);
		add(deleteLink);
	}
}
