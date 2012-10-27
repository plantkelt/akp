package net.plantkelt.akp.webapp.pages;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpSearchData;
import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpSearchForm;
import net.plantkelt.akp.webapp.elements.AkpSearchResultsPanel;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

public class AkpHomePage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private IModel<AkpSearchData> searchDataModel;
	private IModel<AkpSearchResult> searchResultModel;

	public AkpHomePage() {

		// Load data
		AkpSearchData akpSearchData = new AkpSearchData();
		boolean isAdmin = AkpWicketSession.get().isAdmin();
		boolean isLoggedIn = AkpWicketSession.get().isLoggedIn();
		akpSearchData.setLimit(isAdmin ? 1000 : isLoggedIn ? 100 : 10);

		searchDataModel = new Model<AkpSearchData>(akpSearchData);
		searchResultModel = new LoadableDetachableModel<AkpSearchResult>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult load() {
				return akpTaxonService.search(searchDataModel.getObject());
			}
		};

		// Search form
		AkpSearchForm form = new AkpSearchForm("searchForm", searchDataModel);
		add(form);

		// Results table
		AkpSearchResultsPanel resultsPanel = new AkpSearchResultsPanel(
				"resultsSection", searchResultModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return searchDataModel.getObject().getSearchType() != null;
			}
		};
		add(resultsPanel);
	}

	public static Link<AkpHomePage> link(String id) {
		return new BookmarkablePageLink<AkpHomePage>(id, AkpHomePage.class);
	}

}
