package net.plantkelt.akp.webapp.components;

import net.plantkelt.akp.domain.AkpUser;
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
		WebMarkupContainer userSection = new WebMarkupContainer("userSection");
		WebMarkupContainer adminSection = new WebMarkupContainer("adminSection");
		BookmarkablePageLink<WebPage> logoutLink = new BookmarkablePageLink<WebPage>(
				"logoutLink", AkpLogoutPage.class);
		adminSection.add(new WebMarkupContainer("adminLink"));
		userSection.add(new Label("currentUser", userName));
		if (session.isSignedIn()) {
			AkpUser user = session.getAkpUser();
			userName = user.getLogin();
			userSection.setVisible(true);
			adminSection.setVisible(user.isAdmin());
		} else {
			userSection.setVisible(false);
		}
		userSection.add(logoutLink);
		userSection.add(adminSection);
		add(userSection);
	}
}
