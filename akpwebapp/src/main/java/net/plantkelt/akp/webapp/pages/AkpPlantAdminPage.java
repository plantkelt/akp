package net.plantkelt.akp.webapp.pages;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpClassSelectPanel;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
public class AkpPlantAdminPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private Integer plantId;

	public AkpPlantAdminPage(PageParameters parameters) {

		// Load data
		plantId = parameters.get("xid").toOptionalInteger();
		final IModel<AkpPlant> plantModel = new LoadableDetachableModel<AkpPlant>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpPlant load() {
				return akpTaxonService.getPlant(plantId);
			}
		};
		AkpPlant plant = plantModel.getObject();

		// Header
		Label plantName = new Label("plantName",
				plant == null ? plantId.toString() : plant.getMainName()
						.getHtmlName());
		plantName.setEscapeModelStrings(false);
		add(plantName);

		// Links
		Link<AkpPlantPage> viewPlantLink = AkpPlantPage.link("viewPlantLink",
				plantId);
		add(viewPlantLink);

		// Feedback
		add(new FeedbackPanel("feedback"));

		// Move class selector
		AkpClassSelectPanel classSelector = new AkpClassSelectPanel(
				"classSelector") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onClassSelected(AkpClass clazz) {
				akpTaxonService.movePlant(plantModel.getObject(), clazz);
				setResponsePage(AkpPlantPage.class,
						new PageParameters().set("xid", plantId));
			}
		};
		classSelector.setConfirmClick(true);
		add(classSelector);
	}

	public static Link<AkpPlantAdminPage> link(String id, Integer xid) {
		return new BookmarkablePageLink<AkpPlantAdminPage>(id,
				AkpPlantAdminPage.class, new PageParameters().add("xid", xid));
	}
}
