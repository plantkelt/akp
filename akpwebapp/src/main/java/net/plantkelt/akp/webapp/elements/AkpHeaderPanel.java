package net.plantkelt.akp.webapp.elements;

import java.util.Locale;

import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.webapp.pages.AkpAuthorHomePage;
import net.plantkelt.akp.webapp.pages.AkpBibHomePage;
import net.plantkelt.akp.webapp.pages.AkpClassPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitMainPage;
import net.plantkelt.akp.webapp.pages.AkpHomePage;
import net.plantkelt.akp.webapp.pages.AkpLangHomePage;
import net.plantkelt.akp.webapp.pages.AkpLoginPage;
import net.plantkelt.akp.webapp.pages.AkpLogoutPage;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class AkpHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private static final String[] LANGS = { "br", "en", "fr" };

	public AkpHeaderPanel(String id) {
		super(id);

		add(new AttributeAppender("class", new Model<String>("header"), " "));
		AkpWicketSession session = (AkpWicketSession) getSession();
		AkpUser akpUser = AkpWicketSession.get().getAkpUser();
		String userName = akpUser != null ? akpUser.getLogin() : "";

		// Links
		Link<AkpHomePage> searchLink = AkpHomePage.link("searchLink");
		add(searchLink);
		Link<AkpClassPage> classesLink = AkpClassPage.link("classesLink");
		add(classesLink);
		Link<AkpAuthorHomePage> authorsLink = AkpAuthorHomePage
				.link("authorsLink");
		add(authorsLink);

		// Logged-in links
		WebMarkupContainer loggedInLinks = new WebMarkupContainer(
				"loggedInLinks");
		loggedInLinks.setVisible(AkpWicketSession.get().isLoggedIn());
		add(loggedInLinks);
		Link<AkpBibHomePage> bibsLink = AkpBibHomePage.link("bibsLink");
		loggedInLinks.add(bibsLink);

		// Admin links
		WebMarkupContainer adminLinks = new WebMarkupContainer("adminLinks");
		adminLinks.setVisible(AkpWicketSession.get().isAdmin());
		add(adminLinks);
		Link<AkpLangHomePage> langsLink = AkpLangHomePage.link("langsLink");
		adminLinks.add(langsLink);

		// No-User section
		WebMarkupContainer noUserSection = new WebMarkupContainer(
				"noUserSection");
		add(noUserSection);
		BookmarkablePageLink<WebPage> loginLink = new BookmarkablePageLink<WebPage>(
				"loginLink", AkpLoginPage.class);
		noUserSection.add(loginLink);
		BookmarkablePageLink<WebPage> firstVisitLink = new BookmarkablePageLink<WebPage>(
				"firstVisitLink", AkpFirstVisitMainPage.class);
		noUserSection.add(firstVisitLink);

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

		// Lang section
		WebMarkupContainer langSection = new WebMarkupContainer("langSection");
		add(langSection);
		for (final String lang : LANGS) {
			Link<Void> langLink = new Link<Void>(lang + "Link") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
					AkpWicketSession.get().setLocale(new Locale(lang));
				}
			};
			langSection.add(langLink);
		}
	}
}
