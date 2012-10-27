package net.plantkelt.akp.webapp.pages;

import java.util.Collections;
import java.util.List;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.AttributeModifier;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class AkpBibHomePage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private IModel<String> xidModel;
	private IModel<String> titleModel;
	private IModel<String> authorModel;
	private IModel<String> dateModel;
	private IModel<String> isbnModel;
	private IModel<String> commentsModel;
	private IModel<String> editorModel;

	private IModel<String> addXidModel;

	public AkpBibHomePage() {
		super();

		// Load data
		boolean isAdmin = AkpWicketSession.get().isAdmin();

		// Search
		xidModel = new Model<String>();
		titleModel = new Model<String>();
		authorModel = new Model<String>();
		dateModel = new Model<String>();
		isbnModel = new Model<String>();
		commentsModel = new Model<String>();
		editorModel = new Model<String>();
		addXidModel = new Model<String>();
		SearchForm searchForm = new SearchForm("searchForm");
		add(searchForm);

		// Results
		IModel<List<AkpBib>> resultsModel = new LoadableDetachableModel<List<AkpBib>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpBib> load() {
				if (somethingToSearchFor()) {
					int limit = 10;
					if (AkpWicketSession.get().isLoggedIn())
						limit = 100;
					if (AkpWicketSession.get().isAdmin())
						limit = 500;
					return akpTaxonService.searchBib(limit,
							xidModel.getObject(), titleModel.getObject(),
							authorModel.getObject(), dateModel.getObject(),
							isbnModel.getObject(), commentsModel.getObject(),
							editorModel.getObject());
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
		ListView<AkpBib> bibList = new ListView<AkpBib>("bibList", resultsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpBib> item) {
				AkpBib bib = item.getModelObject();
				Link<?> bibLink = AkpBibPage.link("bibLink", bib.getXid());
				item.add(bibLink);
				bibLink.add(new Label("xidLabel", bib.getXid()));
				item.add(new Label("titleLabel", bib.getTitle()));
				item.add(new Label("authorLabel", bib.getAuthor()));
				item.add(new Label("dateLabel", bib.getDate()));
				item.add(new Label("isbnLabel", bib.getIsbn()));
				item.add(new Label("editorLabel", bib.getEditor()));
				item.add(new AttributeModifier("class",
						item.getIndex() % 2 == 0 ? "even" : "odd"));
			}
		};
		searchResultsSection.add(bibList);

		// Add bib section
		WebMarkupContainer addSection = new WebMarkupContainer("addSection");
		add(addSection);
		addSection.setVisible(isAdmin);
		Form<Void> addForm = new Form<Void>("addBibForm") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				String addXid = addXidModel.getObject();
				AkpBib bib = akpTaxonService.getBib(addXid);
				if (bib != null) {
					error(getString("bib.xid.already.exists"));
				} else {
					akpTaxonService.createNewBib(addXid);
					setResponsePage(AkpBibPage.class,
							new PageParameters().add("xid", addXid));
				}
			}
		};
		addSection.add(addForm);
		addForm.add(new FeedbackPanel("feedback"));
		addForm.add(new TextField<String>("xid", addXidModel));

	}

	private boolean somethingToSearchFor() {
		return xidModel.getObject() != null || titleModel.getObject() != null
				|| authorModel.getObject() != null
				|| dateModel.getObject() != null
				|| isbnModel.getObject() != null
				|| commentsModel.getObject() != null
				|| editorModel.getObject() != null;
	}

	private class SearchForm extends Form<Void> {
		private static final long serialVersionUID = 1L;

		public SearchForm(String id) {
			super(id);
			add(new TextField<String>("xid", xidModel));
			add(new TextField<String>("title", titleModel));
			add(new TextField<String>("author", authorModel));
			add(new TextField<String>("date", dateModel));
			add(new TextField<String>("isbn", isbnModel));
			add(new TextField<String>("comments", commentsModel));
			add(new TextField<String>("editor", editorModel));
		}
	}

	public static Link<AkpBibHomePage> link(String id) {
		return new BookmarkablePageLink<AkpBibHomePage>(id,
				AkpBibHomePage.class);
	}
}
