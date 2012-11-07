package net.plantkelt.akp.webapp.pages;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

public class AkpFirstVisitMainPage extends AkpFirstVisitPage {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpFirstVisitMainPage() {
		add(AkpHomePage.link("taxonSearch"));
		add(AkpAuthorHomePage.link("authorSearch"));
		// TODO
		add(AkpHomePage.link("subscribeLink"));

		RepeatingView exampleRepeat = new RepeatingView("exampleRepeat");
		add(exampleRepeat);
		for (Integer xid : akpTaxonService.getPublicPlantXids()) {
			AkpPlant plant = akpTaxonService.getPlant(xid);
			WebMarkupContainer item = new WebMarkupContainer(
					exampleRepeat.newChildId());
			exampleRepeat.add(item);
			item.add(AkpPlantPage.link("exampleLink", xid));
			item.add(new Label("exampleLabel", plant.getMainName()
					.getHtmlName()).setEscapeModelStrings(false));
		}

	}
}
