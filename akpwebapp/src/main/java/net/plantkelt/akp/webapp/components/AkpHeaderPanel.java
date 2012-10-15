package net.plantkelt.akp.webapp.components;

import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.webapp.pages.AkpClassPage;
import net.plantkelt.akp.webapp.pages.AkpLoginPage;
import net.plantkelt.akp.webapp.pages.AkpLogoutPage;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class AkpHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public AkpHeaderPanel(String id) {
		super(id);

		add(new AttributeAppender("class", new Model<String>("header"), " "));
		AkpWicketSession session = (AkpWicketSession) getSession();
		AkpUser akpUser = AkpWicketSession.get().getAkpUser();
		String userName = akpUser != null ? akpUser.getLogin() : "";

		// Links
		BookmarkablePageLink<WebPage> classesLink = new BookmarkablePageLink<WebPage>(
				"classesLink", AkpClassPage.class);
		add(classesLink);

		// No-User section
		WebMarkupContainer noUserSection = new WebMarkupContainer(
				"noUserSection");
		add(noUserSection);
		BookmarkablePageLink<WebPage> loginLink = new BookmarkablePageLink<WebPage>(
				"loginLink", AkpLoginPage.class);
		noUserSection.add(loginLink);

		// User section
		WebMarkupContainer userSection = new WebMarkupContainer("userSection");
		add(userSection);
		BookmarkablePageLink<WebPage> logoutLink = new BookmarkablePageLink<WebPage>(
				"logoutLink", AkpLogoutPage.class);
		userSection.add(logoutLink);
		userSection.add(new Label("currentUser", userName));

		// Flip between the two
		if (session.isSignedIn()) {
			AkpUser user = session.getAkpUser();
			userName = user.getLogin();
			noUserSection.setVisible(false);
			userSection.setVisible(true);
		} else {
			noUserSection.setVisible(true);
			userSection.setVisible(false);
		}
	}
}
