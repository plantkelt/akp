package net.plantkelt.akp.webapp.pages;

import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketApplication;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class AkpLoginPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLoginService akpLoginService;
	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpLoginPage() {
		this(null);
	}

	public AkpLoginPage(final PageParameters parameters) {
		SignInPanel signInPanel = new SignInPanel("signInPanel", false);
		signInPanel.setRememberMe(false);
		add(signInPanel);

		Form<Void> autologinForm = new Form<Void>("autologinForm");
		autologinForm
				.setVisible(AkpWicketApplication.get().getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT);
		add(autologinForm);
		Button autologinAdminButton = new Button("autologinAdminButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				AkpUser laurent = akpLoginService.getUser("laurent");
				if (laurent != null) {
					AkpWicketSession.get().autologin(laurent);
					setResponsePage(AkpHomePage.class);
				} else {
					throw new RuntimeException(
							"Laurent do not exists! Better start worrying.");
				}
			}
		};
		autologinForm.add(autologinAdminButton);
		Button autologinUserButton = new Button("autologinUserButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				AkpUser laurent = akpLoginService.getUser("laurent2");
				if (laurent != null) {
					AkpWicketSession.get().autologin(laurent);
					setResponsePage(AkpHomePage.class);
				} else {
					throw new RuntimeException(
							"Laurent2 do not exists! Better start worrying.");
				}
			}
		};
		autologinForm.add(autologinUserButton);
	}

}
