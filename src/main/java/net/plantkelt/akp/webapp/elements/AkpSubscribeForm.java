package net.plantkelt.akp.webapp.elements;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import net.plantkelt.akp.domain.AkpSubscriptionRequest;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.webapp.pages.AkpSubscribeOkPage;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.google.inject.Inject;

public class AkpSubscribeForm extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLoginService akpLoginService;

	private AkpSubscriptionRequest request;

	public AkpSubscribeForm(String id) {
		super(id);

		request = new AkpSubscriptionRequest();
		add(new FeedbackPanel("feedback"));
		Form<AkpSubscriptionRequest> form = new Form<AkpSubscriptionRequest>(
				"form", new CompoundPropertyModel<AkpSubscriptionRequest>(
						request));
		add(form);
		form.add(new RequiredTextField<String>("login"));
		DropDownChoice<String> langSelect = new DropDownChoice<String>("lang",
				Arrays.asList("fr", "en", "br"));
		langSelect.setRequired(true);
		form.add(langSelect);
		form.add(new RequiredTextField<String>("name"));
		form.add(new RequiredTextField<String>("email"));
		form.add(new TextField<String>("organization"));
		form.add(new TextField<String>("occupation"));
		form.add(new TextField<String>("city"));
		form.add(new RequiredTextField<String>("state"));

		Button continueButton = new Button("continueButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (akpLoginService.getUser(request.getLogin()) != null) {
					error(getString("subscribe.login.already.exists"));
				} else if (!akpLoginService.checkLogin(request.getLogin())) {
					error(getString("subscribe.login.wrong.format"));
				} else if (!request.getEmail().matches(
						"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$")) {
					error(getString("subscribe.email.wrong.format"));
				} else {
					WebRequest req = (WebRequest) RequestCycle.get()
							.getRequest();
					HttpServletRequest httpReq = (HttpServletRequest) req
							.getContainerRequest();
					request.setClientIp(httpReq.getRemoteAddr());
					akpLoginService.subscriptionRequested(request);
					setResponsePage(new AkpSubscribeOkPage(request.getLogin(),
							request.getEmail()));
				}
			}
		};
		form.add(continueButton);
	}
}
