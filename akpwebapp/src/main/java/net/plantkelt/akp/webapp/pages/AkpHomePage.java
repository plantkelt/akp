package net.plantkelt.akp.webapp.pages;

import java.util.Map;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpSearchData;
import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpSearchForm;
import net.plantkelt.akp.webapp.elements.AkpSearchResultsPanel;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
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

		// Feedback
		add(new FeedbackPanel("feedback"));

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
				AkpSearchData akpSearchData = searchDataModel.getObject();
				AkpSearchResult akpSearchResult = akpTaxonService
						.search(akpSearchData);
				Map<String, String> renameMap = akpSearchResult
						.getAuthorRenameMap();
				if (renameMap != null) {
					StringBuffer sb = new StringBuffer(
							getString("search.warn.author.rename"));
					sb.append(" ");
					for (Map.Entry<String, String> kv : renameMap.entrySet()) {
						sb.append(kv.getKey() + " → " + kv.getValue() + ", ");
					}
					if (sb.length() > 2)
						sb.setLength(sb.length() - 2);
					warn(sb.toString());
				}
				return akpSearchResult;
			}
		};

		// Search form
		AkpSearchForm form = new AkpSearchForm("searchForm", searchDataModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return searchDataModel.getObject().getSearchType() == null
						|| searchResultModel.getObject().isEmpty();
			}
		};
		add(form);

		// Results table
		AkpSearchResultsPanel resultsPanel = new AkpSearchResultsPanel(
				"resultsSection", searchResultModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !(searchDataModel.getObject().getSearchType() == null || searchResultModel
						.getObject().isEmpty());
			}
		};
		add(resultsPanel);
	}

	public static Link<AkpHomePage> link(String id) {
		return new BookmarkablePageLink<AkpHomePage>(id, AkpHomePage.class);
	}

}
