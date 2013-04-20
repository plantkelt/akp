package net.plantkelt.akp.webapp.pages;

import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
public class AkpAdminDbPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLoginService akpLoginService;
	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpAdminDbPage() {

		add(new FeedbackPanel("feedback"));
		add(new Link<Void>("mergeLang") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				akpTaxonService.mergeLang("deu", "alzasi");
			}});
	}

}
