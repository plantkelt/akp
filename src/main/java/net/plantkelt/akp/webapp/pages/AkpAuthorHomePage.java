package net.plantkelt.akp.webapp.pages;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

import net.plantkelt.akp.domain.AkpAuthor;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

public class AkpAuthorHomePage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private IModel<String> xidModel;
	private IModel<String> nameModel;
	private IModel<String> datesModel;
	private IModel<String> sourceModel;

	private IModel<String> addXidModel;
	private IModel<Integer> limitModel;

	public AkpAuthorHomePage() {
		super();

		// Load data
		boolean isAdmin = AkpWicketSession.get()
				.hasRole(AkpUserRoles.ROLE_ADMIN);

		// Search
		xidModel = new Model<>();
		nameModel = new Model<>();
		datesModel = new Model<>();
		sourceModel = new Model<>();
		addXidModel = new Model<>();
		SearchForm searchForm = new SearchForm("searchForm");
		add(searchForm);

		limitModel = new LoadableDetachableModel<Integer>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected Integer load() {
				int ret = 10;
				if (AkpWicketSession.get().isLoggedIn())
					ret = 100;
				if (AkpWicketSession.get().hasRole(AkpUserRoles.ROLE_ADMIN))
					ret = 500;
				return ret;
			}
		};

		// Results
		IModel<List<AkpAuthor>> resultsModel = new LoadableDetachableModel<List<AkpAuthor>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpAuthor> load() {
				if (somethingToSearchFor()) {
					return akpTaxonService.searchAuthor(limitModel.getObject(),
							xidModel.getObject(), nameModel.getObject(),
							datesModel.getObject(), sourceModel.getObject());
				} else {
					return Collections.emptyList();
				}
			}
		};
		WebMarkupContainer searchResultsSection = new WebMarkupContainer(
				"searchResultsSection") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return somethingToSearchFor();
			}
		};
		add(searchResultsSection);
		ListView<AkpAuthor> authorList = new ListView<AkpAuthor>("authorList",
				resultsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpAuthor> item) {
				AkpAuthor author = item.getModelObject();
				Link<?> authorLink = AkpAuthorPage.link("authorLink",
						author.getXid());
				item.add(authorLink);
				authorLink.add(new Label("xidLabel", author.getXid()));
				item.add(new Label("nameLabel", author.getName())
						.setEscapeModelStrings(false));
				item.add(new Label("datesLabel", author.getDates()));
				item.add(new Label("sourceLabel", author.getSource()));
			}
		};
		searchResultsSection.add(authorList);

		Label numberOfResults = new Label("numberOfResults",
				new StringResourceModel("author.search.number.of.results", this,
						resultsModel));
		searchResultsSection.add(numberOfResults);

		Label resultsLimit = new Label("resultsLimit",
				new StringResourceModel("author.search.results.limit", this,
						null).setParameters(limitModel)) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return limitModel.getObject() > 0;
			}

		};
		searchResultsSection.add(resultsLimit);

		// Add author section
		WebMarkupContainer addSection = new WebMarkupContainer("addSection");
		add(addSection);
		addSection.setVisible(isAdmin);
		Form<Void> addForm = new Form<Void>("addAuthorForm") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				String addXid = addXidModel.getObject();
				if (addXid == null) {
					return;
				}
				AkpAuthor author = akpTaxonService.getAuthor(addXid);
				if (author != null) {
					error(getString("author.xid.already.exists"));
				} else {
					akpTaxonService.createNewAuthor(addXid);
					setResponsePage(AkpAuthorPage.class,
							new PageParameters().add("xid", addXid));
				}
			}
		};
		addSection.add(addForm);
		addForm.add(new FeedbackPanel("feedback"));
		addForm.add(new TextField<String>("xid", addXidModel));

	}

	private boolean somethingToSearchFor() {
		return xidModel.getObject() != null || nameModel.getObject() != null
				|| datesModel.getObject() != null
				|| sourceModel.getObject() != null;
	}

	private class SearchForm extends Form<Void> {
		private static final long serialVersionUID = 1L;

		public SearchForm(String id) {
			super(id);
			boolean isAdmin = AkpWicketSession.get()
					.hasRole(AkpUserRoles.ROLE_ADMIN);
			add(new TextField<String>("xid", xidModel));
			add(new TextField<String>("name", nameModel));
			WebMarkupContainer datesRow = new WebMarkupContainer("datesRow");
			add(datesRow);
			datesRow.setVisible(isAdmin);
			datesRow.add(new TextField<String>("dates", datesModel));
			WebMarkupContainer sourceRow = new WebMarkupContainer("sourceRow");
			add(sourceRow);
			sourceRow.setVisible(isAdmin);
			sourceRow.add(new TextField<String>("source", sourceModel));
		}
	}

	public static Link<AkpAuthorHomePage> link(String id) {
		return new BookmarkablePageLink<AkpAuthorHomePage>(id,
				AkpAuthorHomePage.class);
	}
}
