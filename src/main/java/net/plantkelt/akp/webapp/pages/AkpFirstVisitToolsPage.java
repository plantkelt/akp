package net.plantkelt.akp.webapp.pages;

import javax.inject.Inject;

import net.plantkelt.akp.service.AkpLoginService;

import org.apache.wicket.markup.html.basic.Label;

public class AkpFirstVisitToolsPage extends AkpFirstVisitPage {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLoginService akpLoginService;

	public AkpFirstVisitToolsPage() {

		add(new Label("buildVersion", akpLoginService.getAkpVersion()));
		add(new Label("buildTime", akpLoginService.getAkpTimestamp()));
	}
}
