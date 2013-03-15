package net.plantkelt.akp.webapp.pages;

import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpSearchResultsPanel;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
public class AkpCheckDbPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLoginService akpLoginService;
	@Inject
	private AkpTaxonService akpTaxonService;

	private String checkType = null;
	private IModel<AkpSearchResult> checkResultModel;

	public AkpCheckDbPage() {
		Link<Void> vernaDuplicatesLink = new Link<Void>("vernaDuplicatesLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				checkType = "duplicatedVernacularNames";
			}
		};
		add(vernaDuplicatesLink);

		checkResultModel = new LoadableDetachableModel<AkpSearchResult>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult load() {
				if (checkType.equals("duplicatedVernacularNames")) {
					return akpTaxonService.getDuplicatedVernacularNames();
				}
				return null;
			}
		};

		AkpSearchResultsPanel checkResults = new AkpSearchResultsPanel(
				"checkResults", checkResultModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return checkType != null;
			}
		};
		add(checkResults);
	}
}
