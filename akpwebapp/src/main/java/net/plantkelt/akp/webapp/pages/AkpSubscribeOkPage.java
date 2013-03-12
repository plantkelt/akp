package net.plantkelt.akp.webapp.pages;

import org.apache.wicket.markup.html.basic.Label;

public class AkpSubscribeOkPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	public AkpSubscribeOkPage(String login, String email) {
		super();
		add(new Label("login", login));
		add(new Label("email", email));
	}
}
