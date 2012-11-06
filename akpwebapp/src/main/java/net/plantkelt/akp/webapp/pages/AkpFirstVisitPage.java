package net.plantkelt.akp.webapp.pages;

import net.plantkelt.akp.webapp.elements.AkpFirstVisitNavigator;

public class AkpFirstVisitPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	public AkpFirstVisitPage() {
		super();
		add(new AkpFirstVisitNavigator("navigator", this));
	}
}
