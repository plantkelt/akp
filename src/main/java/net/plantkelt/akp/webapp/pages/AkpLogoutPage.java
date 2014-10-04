package net.plantkelt.akp.webapp.pages;

import javax.inject.Inject;

import net.plantkelt.akp.service.AkpLoginService;

import org.apache.wicket.authroles.authentication.pages.SignOutPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class AkpLogoutPage extends SignOutPage {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLoginService akpLoginService;

	public AkpLogoutPage() {
		this(null);
	}

	public AkpLogoutPage(final PageParameters parameters) {
		getSession().invalidate();
		akpLoginService.logout();
		setResponsePage(AkpIndexPage.class);
	}

}
