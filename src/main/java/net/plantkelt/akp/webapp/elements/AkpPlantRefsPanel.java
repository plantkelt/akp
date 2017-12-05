package net.plantkelt.akp.webapp.elements;

import java.util.List;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpPlantRefsPanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	public AkpPlantRefsPanel(String id, final IModel<AkpPlant> plantModel) {
		super(id);

		final boolean isAdmin = AkpWicketSession.get()
				.hasRole(AkpUserRoles.ROLE_ADMIN);

		// Plant refs
		IModel<List<AkpPlant>> plantRefsModel = new PropertyModel<List<AkpPlant>>(
				plantModel, "plantRefs");
		ListView<AkpPlant> plantRefsList = new ListView<AkpPlant>(
				"plantRefsList", plantRefsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpPlant> item) {
				AkpPlantRefPanel plantRefPanel = new AkpPlantRefPanel(
						"plantRef", item.getModel(), new AkpPlantRefListener() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onPlantRefRemoved(
									AjaxRequestTarget target,
									AkpPlant targetPlant) {
								akpTaxonService.removePlantRefFromPlant(
										plantModel.getObject(), targetPlant);
								target.add(AkpPlantRefsPanel.this);
							}
						});
				item.add(plantRefPanel);
			}
		};
		add(plantRefsList);

		// Plant ref adder
		AkpPlantRefAdderPanel plantRefAdder = new AkpPlantRefAdderPanel(
				"plantRefAdder", new AkpPlantRefAdderListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void onPlantRefAdded(AjaxRequestTarget target,
							AkpPlant targetPlant) {
						akpTaxonService.addPlantRefToPlant(
								plantModel.getObject(), targetPlant);
						target.add(AkpPlantRefsPanel.this);
					}
				});
		add(plantRefAdder);
		plantRefAdder.setVisible(isAdmin);

		setOutputMarkupId(true);
	}
}
