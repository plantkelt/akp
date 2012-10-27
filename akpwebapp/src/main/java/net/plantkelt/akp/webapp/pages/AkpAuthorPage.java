package net.plantkelt.akp.webapp.pages;

import java.util.List;

import net.plantkelt.akp.domain.AkpAuthor;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.behaviors.JavascriptConfirmationModifier;
import net.plantkelt.akp.webapp.components.CollapsibleButton;
import net.plantkelt.akp.webapp.components.EditorModel;
import net.plantkelt.akp.webapp.components.InPlaceEditor;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class AkpAuthorPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private String authorId;
	private IModel<AkpAuthor> authorModel;

	public AkpAuthorPage(PageParameters parameters) {
		super(parameters);

		// Load data
		authorId = parameters.get("xid").toOptionalString();
		boolean isAdmin = AkpWicketSession.get().isAdmin();
		boolean isLoggedIn = AkpWicketSession.get().isLoggedIn();
		authorModel = new LoadableDetachableModel<AkpAuthor>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpAuthor load() {
				return akpTaxonService.getAuthor(authorId);
			}
		};

		// Author id
		Label authorIdLabel = new Label("authorId", authorId);
		add(authorIdLabel);

		// Name
		final InPlaceEditor nameEditor = new InPlaceEditor("nameEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return authorModel.getObject().getName();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpAuthor author = authorModel.getObject();
						author.setName(value == null ? "" : value);
						akpTaxonService.updateAuthor(author);
						target.add(AkpAuthorPage.this);
					}
				});
		add(nameEditor);
		nameEditor.setReadOnly(!isAdmin);
		Label nameLabel = new Label("nameLabel", new PropertyModel<String>(
				authorModel, "name"));
		nameLabel.setEscapeModelStrings(false);
		nameEditor.add(nameLabel);

		// Source
		final InPlaceEditor sourceEditor = new InPlaceEditor("sourceEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return authorModel.getObject().getSource();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpAuthor author = authorModel.getObject();
						author.setSource(value == null ? "" : value);
						akpTaxonService.updateAuthor(author);
						target.add(AkpAuthorPage.this);
					}
				});
		add(sourceEditor);
		sourceEditor.setReadOnly(!isAdmin);
		Label sourceLabel = new Label("sourceLabel", new PropertyModel<String>(
				authorModel, "source"));
		sourceEditor.add(sourceLabel);

		// Dates
		final InPlaceEditor datesEditor = new InPlaceEditor("datesEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return authorModel.getObject().getDates();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpAuthor author = authorModel.getObject();
						author.setDates(value == null ? "" : value);
						akpTaxonService.updateAuthor(author);
						target.add(AkpAuthorPage.this);
					}
				});
		add(datesEditor);
		datesEditor.setReadOnly(!isAdmin);
		Label datesLabel = new Label("datesLabel", new PropertyModel<String>(
				authorModel, "dates"));
		datesEditor.add(datesLabel);

		// Taxon used
		final IModel<List<AkpTaxon>> taxonRefsModel = new LoadableDetachableModel<List<AkpTaxon>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpTaxon> load() {
				int limit = 100;
				if (AkpWicketSession.get().isAdmin())
					limit = 1000;
				return akpTaxonService.getTaxonsForAuthor(limit,
						authorModel.getObject());
			}
		};
		WebMarkupContainer taxonRefsSection = new WebMarkupContainer(
				"taxonRefsSection");
		taxonRefsSection.setVisible(isLoggedIn);
		add(taxonRefsSection);
		WebMarkupContainer collapseDiv = new WebMarkupContainer("collapseDiv");
		taxonRefsSection.add(collapseDiv);
		CollapsibleButton collapseButton = new CollapsibleButton(
				"collapseButton", collapseDiv, false);
		taxonRefsSection.add(collapseButton);
		ListView<AkpTaxon> taxonRefsList = new ListView<AkpTaxon>(
				"taxonRefsList", taxonRefsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpTaxon> item) {
				AkpTaxon taxon = item.getModelObject();
				Label taxonLabel = new Label("taxonName", taxon.getHtmlName());
				taxonLabel.setEscapeModelStrings(false);
				item.add(taxonLabel);
				item.add(new AttributeModifier("class",
						item.getIndex() % 2 == 0 ? "even" : "odd"));
			}
		};
		collapseDiv.add(taxonRefsList);

		// Delete author
		Form<Void> deleteForm = new Form<Void>("deleteForm");
		collapseDiv.add(deleteForm);
		Button deleteButton = new Button("deleteButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (akpTaxonService.deleteAuthor(authorModel.getObject())) {
					setResponsePage(AkpAuthorHomePage.class);
				}
			}

			@Override
			public boolean isVisible() {
				return AkpWicketSession.get().isAdmin()
						&& taxonRefsModel.getObject().size() == 0;
			}
		};
		deleteForm.add(deleteButton);
		deleteButton.add(new JavascriptConfirmationModifier("onClick",
				getString("confirm.action.message")));
	}

	public static Link<AkpAuthorPage> link(String id, String xid) {
		return new BookmarkablePageLink<AkpAuthorPage>(id, AkpAuthorPage.class,
				new PageParameters().add("xid", xid));
	}
}
