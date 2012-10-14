package net.plantkelt.akp.webapp.pages;

import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class AkpLoginPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	public AkpLoginPage() {
		this(null);
	}

	public AkpLoginPage(final PageParameters parameters) {
		SignInPanel signInPanel = new SignInPanel("signInPanel", false);
		signInPanel.setRememberMe(false);
		add(signInPanel);
	}

}
