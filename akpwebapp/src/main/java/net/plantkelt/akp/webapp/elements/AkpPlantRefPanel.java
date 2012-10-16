package net.plantkelt.akp.webapp.elements;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AjaxConfirmLink;
import net.plantkelt.akp.webapp.pages.AkpPlantPage;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.Component;
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

	public AkpPlantRefPanel(String id, final IModel<AkpPlant> plantModel,
			final IModel<AkpVernacularName> vernaNameModel,
			final Component refreshComponent) {
		super(id);

		boolean isAdmin = AkpWicketSession.get().isAdmin();

		// <a wicket:id="plantRefLink"><span
		// wicket:id="plantRefLabel"></span></a>
		// <a class="delete-small" wicket:id="deleteLink">&nbsp;</a>

		// Link to referenced plant
		AkpPlant plant = plantModel.getObject();
		Link<AkpPlantPage> plantRefLink = AkpPlantPage.link("plantRefLink",
				plant.getXid());
		add(plantRefLink);

		// Label to display
		Label plantRefLabel = new Label("plantRefLabel", "⇒ "
				+ plant.getMainName().getHtmlName());
		plantRefLabel.setEscapeModelStrings(false);
		plantRefLink.add(plantRefLabel);

		// Delete link
		AjaxConfirmLink<Void> deleteLink = new AjaxConfirmLink<Void>(
				"deleteLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				AkpPlant plantRef = plantModel.getObject();
				AkpVernacularName vernaName = vernaNameModel.getObject();
				vernaName.getPlantRefs().remove(plantRef);
				akpTaxonService.updateVernacularName(vernaName);
				target.add(refreshComponent);
			}
		};
		deleteLink.setVisible(isAdmin);
		add(deleteLink);
	}
}
