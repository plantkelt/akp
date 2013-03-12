package net.plantkelt.akp.webapp.pages;

import net.plantkelt.akp.webapp.elements.AkpSubscribeForm;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;

public class AkpSubscribePage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	public AkpSubscribePage() {
		super();

		boolean isLoggedIn = AkpWicketSession.get().isLoggedIn();
		WebMarkupContainer alreadyLoggedInDiv = new WebMarkupContainer(
				"alreadyLoggedIn");
		alreadyLoggedInDiv.setVisible(isLoggedIn);
		add(alreadyLoggedInDiv);

		AkpSubscribeForm subscribeForm = new AkpSubscribeForm("subscribeForm");
		subscribeForm.setVisible(!isLoggedIn);
		add(subscribeForm);
	}

	public static Link<AkpSubscribePage> link(String id) {
		return new BookmarkablePageLink<AkpSubscribePage>(id,
				AkpSubscribePage.class);
	}
}
