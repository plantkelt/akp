package net.plantkelt.akp.webapp.pages;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

@AuthorizeInstantiation("USER")
public class AkpHomePage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	public AkpHomePage() {
	}
}
