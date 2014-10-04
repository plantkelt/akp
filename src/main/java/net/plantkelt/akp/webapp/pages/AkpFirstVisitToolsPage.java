package net.plantkelt.akp.webapp.pages;

import javax.inject.Inject;

import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.utils.Pair;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebApplication;

public class AkpFirstVisitToolsPage extends AkpFirstVisitPage {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLoginService akpLoginService;

	public AkpFirstVisitToolsPage() {

		Pair<String, String> akpVersions = akpLoginService
				.getAkpVersions(WebApplication.get().getServletContext());
		add(new Label("buildVersion", akpVersions.getFirst()));
		add(new Label("buildTime", akpVersions.getSecond()));
	}
}
