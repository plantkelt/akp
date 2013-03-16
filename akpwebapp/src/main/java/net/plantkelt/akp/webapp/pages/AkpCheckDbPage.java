package net.plantkelt.akp.webapp.pages;

import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpSearchResultsPanel;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

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

	private class CheckLink extends Link<String> {
		private static final long serialVersionUID = 1L;

		public CheckLink(String id, String checkType) {
			super(id, new Model<String>(checkType));
		}

		@Override
		public void onClick() {
			checkType = this.getModelObject();
		}
	}

	public AkpCheckDbPage() {
		add(new CheckLink("vernaDuplicatesLink", "duplicatedVernacularNames"));
		add(new CheckLink("taxonDuplicatesLink", "duplicatedTaxonNames"));
		add(new CheckLink("taxonSyntaxLink", "taxonSyntax"));

		checkResultModel = new LoadableDetachableModel<AkpSearchResult>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult load() {
				if (checkType.equals("duplicatedVernacularNames")) {
					return akpTaxonService.getDuplicatedVernacularNames();
				} else if (checkType.equals("duplicatedTaxonNames")) {
					return akpTaxonService.getDuplicatedTaxonNames();
				} else if (checkType.equals("taxonSyntax")) {
					return akpTaxonService.getTaxonSyntaxErrors();
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

	public static Link<AkpCheckDbPage> link(String id) {
		return new BookmarkablePageLink<AkpCheckDbPage>(id,
				AkpCheckDbPage.class);
	}

}
