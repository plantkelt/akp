package net.plantkelt.akp.webapp.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpSearchResultsPanel;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
public class AkpCheckDbPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	private static abstract class Checker implements Serializable {
		private static final long serialVersionUID = 1L;
		private String checkTypeId;

		private Checker(String checkTypeId) {
			this.checkTypeId = checkTypeId;
		}

		private String getCheckTypeId() {
			return checkTypeId;
		}

		protected abstract AkpSearchResult check(AkpTaxonService akpTaxonService);
	}

	private static List<Checker> CHECKERS;
	static {
		CHECKERS = new ArrayList<AkpCheckDbPage.Checker>();
		CHECKERS.add(new Checker("duplicatedVernacularNames") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getDuplicatedVernacularNames();
			}
		});
		CHECKERS.add(new Checker("duplicatedTaxonNames") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getDuplicatedTaxonNames();
			}
		});
		CHECKERS.add(new Checker("taxonSyntaxErrors") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getTaxonSyntaxErrors();
			}
		});
	}

	@Inject
	private AkpLoginService akpLoginService;
	@Inject
	private AkpTaxonService akpTaxonService;

	private Checker currentChecker = null;

	private class CheckLink extends Link<Checker> {
		private static final long serialVersionUID = 1L;

		public CheckLink(String id, Checker checker) {
			super(id, new Model<Checker>(checker));
			add(new Label("checkLabel", getString("check.type."
					+ checker.getCheckTypeId())));
		}

		@Override
		public void onClick() {
			currentChecker = getModelObject();
		}
	}

	public AkpCheckDbPage() {

		RepeatingView checkLinkRepeat = new RepeatingView("checkLinkRepeat");
		add(checkLinkRepeat);

		for (Checker checker : CHECKERS) {
			WebMarkupContainer item = new WebMarkupContainer(
					checkLinkRepeat.newChildId());
			checkLinkRepeat.add(item);
			item.add(new CheckLink("checkLink", checker));
		}

		IModel<AkpSearchResult> checkResultModel = new LoadableDetachableModel<AkpSearchResult>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult load() {
				return currentChecker == null ? null : currentChecker
						.check(akpTaxonService);
			}
		};

		WebMarkupContainer checkResults = new WebMarkupContainer("checkResults") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return currentChecker != null;
			}
		};
		add(checkResults);
		checkResults.add(new Label("checkResultsHeader",
				new LoadableDetachableModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					protected String load() {
						return currentChecker == null ? ""
								: getString("check.type."
										+ currentChecker.getCheckTypeId());
					}
				}));
		AkpSearchResultsPanel checkResultsPanel = new AkpSearchResultsPanel(
				"checkResultsPanel", checkResultModel);
		checkResults.add(checkResultsPanel);
	}

	public static Link<AkpCheckDbPage> link(String id) {
		return new BookmarkablePageLink<AkpCheckDbPage>(id,
				AkpCheckDbPage.class);
	}

}