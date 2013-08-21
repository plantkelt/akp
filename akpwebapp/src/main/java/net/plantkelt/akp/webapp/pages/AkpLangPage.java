package net.plantkelt.akp.webapp.pages;

import java.util.Arrays;
import java.util.List;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpLangGroup;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.behaviors.JavascriptConfirmationModifier;
import net.plantkelt.akp.webapp.components.EditorModel;
import net.plantkelt.akp.webapp.components.InPlaceEditor;
import net.plantkelt.akp.webapp.components.InPlaceSelector;
import net.plantkelt.akp.webapp.components.SelectorModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
public class AkpLangPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private String langId;
	private IModel<AkpLang> langModel;

	public AkpLangPage(PageParameters parameters) {
		super(parameters);

		// Load data
		langId = parameters.get("xid").toOptionalString();
		langModel = new LoadableDetachableModel<AkpLang>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpLang load() {
				return akpTaxonService.getLang(langId);
			}
		};
		if (langModel.getObject() == null)
			throw new IllegalArgumentException("Invalid lang ID: " + langId);

		// Set page title
		AkpLang lang = langModel.getObject();
		setPageTitle(lang.getCode() + " - " + lang.getName());

		// Lang id
		Label langIdLabel = new Label("langId", langId);
		add(langIdLabel);

		// Code
		final InPlaceEditor codeEditor = new InPlaceEditor("codeEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return langModel.getObject().getCode();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpLang lang = langModel.getObject();
						lang.setCode(value == null ? lang.getXid() : value);
						akpTaxonService.updateLang(lang);
						target.add(AkpLangPage.this);
					}
				}, 1, 20);
		add(codeEditor);
		Label codeLabel = new Label("codeLabel", new PropertyModel<String>(
				langModel, "code"));
		codeEditor.add(codeLabel);

		// Name
		final InPlaceEditor nameEditor = new InPlaceEditor("nameEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return langModel.getObject().getName();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpLang lang = langModel.getObject();
						lang.setName(value == null ? "" : value);
						akpTaxonService.updateLang(lang);
						target.add(AkpLangPage.this);
					}
				}, 1, 40);
		add(nameEditor);
		Label nameLabel = new Label("nameLabel", new PropertyModel<String>(
				langModel, "name"));
		nameEditor.add(nameLabel);

		// Lang group
		final InPlaceSelector<AkpLangGroup> groupSelector = new InPlaceSelector<AkpLangGroup>(
				"groupSelector", new SelectorModel<AkpLangGroup>() {
					private static final long serialVersionUID = 1L;

					@Override
					public void saveObject(AjaxRequestTarget target,
							AkpLangGroup langGroup) {
						AkpLang lang = langModel.getObject();
						lang.setLangGroup(langGroup);
						akpTaxonService.updateLang(lang);
						target.add(AkpLangPage.this);
					}

					@Override
					public String getDisplayValue(AkpLangGroup langGroup) {
						return langGroup.getName();
					}

					@Override
					public List<AkpLangGroup> getValues() {
						return akpTaxonService.getLangGroupList();
					}

					@Override
					public AkpLangGroup getObject() {
						return langModel.getObject().getLangGroup();
					}

					@Override
					public String getIdValue(AkpLangGroup langGroup) {
						return "" + langGroup.getXid();
					}
				});
		add(groupSelector);
		Label groupLabel = new Label("groupLabel", new PropertyModel<String>(
				langModel, "langGroup.name"));
		groupSelector.add(groupLabel);

		// Level
		final InPlaceSelector<Integer> levelSelector = new InPlaceSelector<Integer>(
				"levelSelector", new SelectorModel<Integer>() {
					private static final long serialVersionUID = 1L;

					@Override
					public void saveObject(AjaxRequestTarget target,
							Integer level) {
						AkpLang lang = langModel.getObject();
						lang.setLevel(level);
						akpTaxonService.updateLang(lang);
						target.add(AkpLangPage.this);
					}

					@Override
					public String getDisplayValue(Integer level) {
						return getString("profile." + level);
					}

					@Override
					public List<Integer> getValues() {
						return Arrays.asList(AkpUser.PROFILE_USER,
								AkpUser.PROFILE_ADMIN);
					}

					@Override
					public Integer getObject() {
						return langModel.getObject().getLevel();
					}

					@Override
					public String getIdValue(Integer t) {
						return t.toString();
					}
				});
		add(levelSelector);
		Label levelLabel = new Label("levelLabel", new StringResourceModel(
				"profile.${level}", this, langModel));
		levelSelector.add(levelLabel);

		// Description
		final InPlaceEditor descEditor = new InPlaceEditor("descEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return langModel.getObject().getDesc();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String value) {
						AkpLang lang = langModel.getObject();
						lang.setDesc(value == null ? "" : value);
						akpTaxonService.updateLang(lang);
						target.add(AkpLangPage.this);
					}
				}, 6, 80);
		add(descEditor);
		Label descLabel = new Label("descLabel", new PropertyModel<String>(
				langModel, "desc"));
		descLabel.setEscapeModelStrings(false);
		descEditor.add(descLabel);

		// Delete lang
		Form<Void> deleteForm = new Form<Void>("deleteForm");
		add(deleteForm);
		Button deleteButton = new Button("deleteButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				akpTaxonService.deleteLang(langModel.getObject());
				setResponsePage(AkpLangHomePage.class);
			}

			@Override
			public boolean isVisible() {
				return akpTaxonService.canDeleteLang(langModel.getObject());
			}
		};
		deleteForm.add(deleteButton);
		deleteButton.add(new JavascriptConfirmationModifier("onClick",
				getString("confirm.action.message")));
	}

	public static Link<AkpLangPage> link(String id, String xid) {
		return new BookmarkablePageLink<AkpLangPage>(id, AkpLangPage.class,
				new PageParameters().add("xid", xid));
	}
}
