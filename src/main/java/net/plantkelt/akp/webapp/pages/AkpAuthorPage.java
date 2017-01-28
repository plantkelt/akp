package net.plantkelt.akp.webapp.pages;

import java.text.MessageFormat;

import net.plantkelt.akp.domain.AkpAuthor;
import net.plantkelt.akp.domain.AkpSearchData;
import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.behaviors.JavascriptConfirmationModifier;
import net.plantkelt.akp.webapp.components.CollapsibleButton;
import net.plantkelt.akp.webapp.components.EditorModel;
import net.plantkelt.akp.webapp.components.InPlaceEditor;
import net.plantkelt.akp.webapp.elements.AkpSearchResultsPanel;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
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
		if (authorModel.getObject() == null)
			throw new IllegalArgumentException(
					"Invalid author ID: " + authorId);

		// Set page title
		AkpAuthor author = authorModel.getObject();
		setPageTitle(author.getXid() + " - " + author.getTextName());

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
				}, 1, 60);
		add(nameEditor);
		nameEditor.setReadOnly(!isAdmin);
		Label nameLabel = new Label("nameLabel",
				new PropertyModel<String>(authorModel, "name"));
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
				}, 1, 60);
		add(sourceEditor);
		sourceEditor.setReadOnly(!isAdmin);
		Label sourceLabel = new Label("sourceLabel",
				new PropertyModel<String>(authorModel, "source"));
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
				}, 1, 40);
		add(datesEditor);
		datesEditor.setReadOnly(!isAdmin);
		Label datesLabel = new Label("datesLabel",
				new PropertyModel<String>(authorModel, "dates"));
		datesEditor.add(datesLabel);

		// Rename XID section
		WebMarkupContainer renameXidSection = new WebMarkupContainer(
				"renameXidSection");
		renameXidSection.setVisible(isAdmin);
		add(renameXidSection);
		renameXidSection.add(new FeedbackPanel("renameFeedback"));
		WebMarkupContainer collapseDiv2 = new WebMarkupContainer("collapseDiv");
		renameXidSection.add(collapseDiv2);
		CollapsibleButton collapseButton2 = new CollapsibleButton(
				"collapseButton", collapseDiv2, false);
		renameXidSection.add(collapseButton2);
		final IModel<String> newXidModel = new Model<String>(authorId);
		Form<String> renameForm = new Form<String>("renameXidForm") {
			private static final long serialVersionUID = 1L;

			public void onSubmit() {
				String newXid = newXidModel.getObject();
				int nChanges = akpTaxonService
						.renameAuthorXid(authorModel.getObject(), newXid);
				info(MessageFormat.format(getString("xid.renamed.to"), newXid,
						nChanges));
				setResponsePage(AkpAuthorPage.class,
						new PageParameters().add("xid", newXid));
			}
		};
		collapseDiv2.add(renameForm);
		renameForm.add(new RequiredTextField<String>("newXid", newXidModel));

		// Taxon used

		WebMarkupContainer taxonRefsSection = new WebMarkupContainer(
				"taxonRefsSection");
		taxonRefsSection.setVisible(isLoggedIn);
		add(taxonRefsSection);
		WebMarkupContainer collapseDiv = new WebMarkupContainer("collapseDiv");
		taxonRefsSection.add(collapseDiv);
		CollapsibleButton collapseButton = new CollapsibleButton(
				"collapseButton", collapseDiv, false);
		taxonRefsSection.add(collapseButton);

		final IModel<AkpSearchResult> searchResultModel = new LoadableDetachableModel<AkpSearchResult>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult load() {
				AkpSearchData akpSearchData = new AkpSearchData();
				akpSearchData.setTaxonName("%<a>" + authorId + "</a>%");
				int limit = 25;
				if (AkpWicketSession.get().isLoggedIn())
					limit = 200;
				if (AkpWicketSession.get().isAdmin())
					limit = 1000;
				akpSearchData.setLimit(limit);
				return akpTaxonService.search(
						AkpWicketSession.get().getAkpUser(), akpSearchData);
			}
		};
		AkpSearchResultsPanel resultsPanel = new AkpSearchResultsPanel(
				"resultsSection", searchResultModel);
		collapseDiv.add(resultsPanel);

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
						&& searchResultModel.getObject().isEmpty();
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
