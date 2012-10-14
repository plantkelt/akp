package net.plantkelt.akp.webapp.pages;

import org.apache.wicket.authroles.authentication.pages.SignOutPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class AkpLogoutPage extends SignOutPage {

	private static final long serialVersionUID = 1L;

	public AkpLogoutPage() {
		this(null);
	}

	public AkpLogoutPage(final PageParameters parameters) {
		getSession().invalidate();
		setResponsePage(AkpIndexPage.class);
	}

}
