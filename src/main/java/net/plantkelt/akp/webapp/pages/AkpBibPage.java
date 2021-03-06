package net.plantkelt.akp.webapp.pages;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpSearchData;
import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.behaviors.JavascriptConfirmationModifier;
import net.plantkelt.akp.webapp.components.CollapsibleButton;
import net.plantkelt.akp.webapp.components.EditorModel;
import net.plantkelt.akp.webapp.components.InPlaceEditor;
import net.plantkelt.akp.webapp.elements.AkpSearchResultsPanel;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

public class AkpBibPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private String bibId;
	private IModel<AkpBib> bibModel;

	public AkpBibPage(PageParameters parameters) {
		super(parameters);

		// Load data
		bibId = parameters.get("xid").toOptionalString();
		boolean isAdmin = AkpWicketSession.get()
				.hasRole(AkpUserRoles.ROLE_ADMIN);
		bibModel = new LoadableDetachableModel<AkpBib>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpBib load() {
				return akpTaxonService.getBib(bibId);
			}
		};
		if (bibModel.getObject() == null)
			throw new IllegalArgumentException("Invalid bib ID: " + bibId);

		// Set page title
		setPageTitleModel(new PropertyModel<String>(bibModel, "shortName"));

		// Bib id
		Label bibIdLabel = new Label("bibId", bibId);
		add(bibIdLabel);

		// Title
		final InPlaceEditor titleEditor = new InPlaceEditor("titleEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return bibModel.getObject().getTitle();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpBib bib = bibModel.getObject();
						bib.setTitle(value == null ? "" : value);
						akpTaxonService.updateBib(bib);
						target.add(AkpBibPage.this);
					}
				}, 2, 60);
		add(titleEditor);
		titleEditor.setReadOnly(!isAdmin);
		Label titleLabel = new Label("titleLabel",
				new PropertyModel<String>(bibModel, "title"));
		titleEditor.add(titleLabel);

		// Author
		final InPlaceEditor authorEditor = new InPlaceEditor("authorEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return bibModel.getObject().getAuthor();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpBib bib = bibModel.getObject();
						bib.setAuthor(value == null ? "" : value);
						akpTaxonService.updateBib(bib);
						target.add(AkpBibPage.this);
					}
				}, 1, 60);
		add(authorEditor);
		authorEditor.setReadOnly(!isAdmin);
		Label authorLabel = new Label("authorLabel",
				new PropertyModel<String>(bibModel, "author"));
		authorEditor.add(authorLabel);

		// Date
		final InPlaceEditor dateEditor = new InPlaceEditor("dateEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return bibModel.getObject().getDate();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpBib bib = bibModel.getObject();
						bib.setDate(value == null ? "" : value);
						akpTaxonService.updateBib(bib);
						target.add(AkpBibPage.this);
					}
				}, 1, 40);
		add(dateEditor);
		dateEditor.setReadOnly(!isAdmin);
		Label dateLabel = new Label("dateLabel",
				new PropertyModel<String>(bibModel, "date"));
		dateEditor.add(dateLabel);

		// ISBN
		final InPlaceEditor isbnEditor = new InPlaceEditor("isbnEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return bibModel.getObject().getIsbn();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpBib bib = bibModel.getObject();
						bib.setIsbn(value == null ? "" : value);
						akpTaxonService.updateBib(bib);
						target.add(AkpBibPage.this);
					}
				}, 1, 20);
		add(isbnEditor);
		isbnEditor.setReadOnly(!isAdmin);
		Label isbnLabel = new Label("isbnLabel",
				new PropertyModel<String>(bibModel, "isbn"));
		isbnEditor.add(isbnLabel);

		// Comments
		final InPlaceEditor commentsEditor = new InPlaceEditor("commentsEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return bibModel.getObject().getComments();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpBib bib = bibModel.getObject();
						bib.setComments(value == null ? "" : value);
						akpTaxonService.updateBib(bib);
						target.add(AkpBibPage.this);
					}
				}, 1, 60);
		add(commentsEditor);
		commentsEditor.setReadOnly(!isAdmin);
		Label commentsLabel = new Label("commentsLabel",
				new PropertyModel<String>(bibModel, "comments"));
		commentsEditor.add(commentsLabel);

		// Editor
		final InPlaceEditor editorEditor = new InPlaceEditor("editorEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return bibModel.getObject().getEditor();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpBib bib = bibModel.getObject();
						bib.setEditor(value == null ? "" : value);
						akpTaxonService.updateBib(bib);
						target.add(AkpBibPage.this);
					}
				}, 1, 40);
		add(editorEditor);
		editorEditor.setReadOnly(!isAdmin);
		Label editorLabel = new Label("editorLabel",
				new PropertyModel<String>(bibModel, "editor"));
		editorEditor.add(editorLabel);

		// Vernacular names
		final IModel<List<AkpVernacularName>> vernaRefsModel = new LoadableDetachableModel<List<AkpVernacularName>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpVernacularName> load() {
				return akpTaxonService
						.getVernacularNameRefsFromBib(bibModel.getObject());
			}
		};
		WebMarkupContainer vernaRefsSection = new WebMarkupContainer(
				"vernaRefsSection");
		vernaRefsSection.setVisible(isAdmin);
		add(vernaRefsSection);
		WebMarkupContainer collapseDiv = new WebMarkupContainer("collapseDiv");
		vernaRefsSection.add(collapseDiv);
		CollapsibleButton collapseButton = new CollapsibleButton(
				"collapseButton", collapseDiv, false);
		vernaRefsSection.add(collapseButton);

		IModel<AkpSearchResult> searchResultModel = new LoadableDetachableModel<AkpSearchResult>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpSearchResult load() {
				AkpSearchData akpSearchData = new AkpSearchData();
				akpSearchData.setBibRefXid(bibId);
				akpSearchData.setVernacularName("%");
				akpSearchData.setLimit(2000);
				return akpTaxonService.search(
						AkpWicketSession.get().getAkpUser(), akpSearchData);
			}
		};
		AkpSearchResultsPanel resultsPanel = new AkpSearchResultsPanel(
				"resultsSection", searchResultModel);
		collapseDiv.add(resultsPanel);

		// Delete bib
		Form<Void> deleteForm = new Form<Void>("deleteForm");
		collapseDiv.add(deleteForm);
		Button deleteButton = new Button("deleteButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (akpTaxonService.deleteBib(bibModel.getObject()))
					setResponsePage(AkpBibHomePage.class);
			}

			@Override
			public boolean isVisible() {
				return AkpWicketSession.get().hasRole(AkpUserRoles.ROLE_ADMIN)
						&& vernaRefsModel.getObject().size() == 0;
			}
		};
		deleteForm.add(deleteButton);
		deleteButton.add(new JavascriptConfirmationModifier("onClick",
				getString("confirm.action.message")));
	}

	public static Link<AkpBibPage> link(String id, String xid) {
		return new BookmarkablePageLink<AkpBibPage>(id, AkpBibPage.class,
				new PageParameters().add("xid", xid));
	}
}
