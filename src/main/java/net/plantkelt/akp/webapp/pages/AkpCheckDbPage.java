package net.plantkelt.akp.webapp.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpSearchResultsPanel;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.google.inject.Inject;

@AuthorizeInstantiation(AkpUserRoles.ROLE_ADMIN)
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

		protected abstract AkpSearchResult check(
				AkpTaxonService akpTaxonService);
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
		CHECKERS.add(new Checker("authorWithoutTags") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getAuthorWithoutTags();
			}
		});
		CHECKERS.add(new Checker("unknownAuthors") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getUnknownAuthors();
			}

		});
		CHECKERS.add(new Checker("impreciseVernaWithoutPlantRef") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getImpreciseVernaWithoutPlantRef();
			}
		});
		CHECKERS.add(new Checker("authorRefCount") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getAuthorRefCount();
			}
		});
		CHECKERS.add(new Checker("plantsWithoutVerna") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getPlantsWithoutVerna();
			}
		});
		CHECKERS.add(new Checker("plantsXRefs") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getPlantsXRefs();
			}
		});
		CHECKERS.add(new Checker("hybridParents") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getHybridParents();
			}

		});
		CHECKERS.add(new Checker("equalsSynonyms") {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult check(AkpTaxonService akpTaxonService) {
				return akpTaxonService.getEqualsSynonyms();
			}

		});
	}

	@SuppressWarnings("unused")
	@Inject
	private AkpLoginService akpLoginService;
	@Inject
	private AkpTaxonService akpTaxonService;

	private Checker currentChecker = null;

	private class CheckLink extends Link<Checker> {
		private static final long serialVersionUID = 1L;

		public CheckLink(String id, Checker checker) {
			super(id, new Model<Checker>(checker));
			add(new Label("checkLabel", new StringResourceModel(
					"check.type." + checker.getCheckTypeId(), null)));
		}

		@Override
		public void onClick() {
			currentChecker = getModelObject();
		}
	}

	public AkpCheckDbPage() {

		add(new FeedbackPanel("feedback"));

		Link<Void> updateStaticListLink = new Link<Void>(
				"updateStaticListLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				akpTaxonService.updateStaticIndexes();
				info(getString("check.db.updatelist") + " : OK");
			}
		};
		add(updateStaticListLink);

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
				return currentChecker == null ? null
						: currentChecker.check(akpTaxonService);
			}
		};

		WebMarkupContainer checkResults = new WebMarkupContainer(
				"checkResults") {
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
