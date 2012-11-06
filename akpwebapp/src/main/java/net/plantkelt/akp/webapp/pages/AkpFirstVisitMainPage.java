package net.plantkelt.akp.webapp.pages;

public class AkpFirstVisitMainPage extends AkpFirstVisitPage {

	private static final long serialVersionUID = 1L;

	public AkpFirstVisitMainPage() {
		add(AkpHomePage.link("taxonSearch"));
		add(AkpAuthorHomePage.link("authorSearch"));
	}
}
