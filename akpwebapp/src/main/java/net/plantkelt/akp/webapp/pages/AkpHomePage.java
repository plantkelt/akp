package net.plantkelt.akp.webapp.pages;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;

public class AkpHomePage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	public AkpHomePage() {
	}

	public static Link<AkpHomePage> link(String id) {
		return new BookmarkablePageLink<AkpHomePage>(id, AkpHomePage.class);
	}
}
