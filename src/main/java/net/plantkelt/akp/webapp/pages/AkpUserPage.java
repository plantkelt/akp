package net.plantkelt.akp.webapp.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.behaviors.JavascriptConfirmationModifier;
import net.plantkelt.akp.webapp.renderers.SimpleToStringIChoiceRenderer;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

@AuthorizeInstantiation(AkpUserRoles.ROLE_ADMIN)
public class AkpUserPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLoginService akpLoginService;
	@Inject
	private AkpTaxonService akpTaxonService;

	private String userLogin;
	private IModel<AkpUser> userModel;

	public AkpUserPage(PageParameters parameters) {
		super(parameters);

		// Load data
		userLogin = parameters.get("login").toOptionalString();
		userModel = new LoadableDetachableModel<AkpUser>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpUser load() {
				return akpLoginService.getUser(userLogin);
			}
		};
		if (userModel.getObject() == null)
			throw new IllegalArgumentException(
					"Invalid user login: " + userLogin);

		// User login (immutable)
		add(new Label("loginLabel",
				new PropertyModel<String>(userModel, "login")));
		// Form
		add(new AkpUserForm("form", userModel));
	}

	public static Link<AkpUserPage> link(String id, String login) {
		return new BookmarkablePageLink<AkpUserPage>(id, AkpUserPage.class,
				new PageParameters().add("login", login));
	}

	private class AkpUserForm extends Form<AkpUser> {
		private static final long serialVersionUID = 1L;

		private IModel<String> passwordModel;

		private AkpUserForm(String id, IModel<AkpUser> userModel) {
			super(id, new CompoundPropertyModel<AkpUser>(userModel));
			// Feedback
			add(new FeedbackPanel("feedback"));
			if (userModel.getObject().getMd5() == null)
				warn(getString("no.password.defined.yet"));
			// Fields
			add(new RequiredTextField<String>("name"));
			add(new RequiredTextField<String>("email"));
			DateTextField expireField = new DateTextField("expire",
					"dd/MM/yyyy");
			expireField.setRequired(true);
			add(expireField);
			DropDownChoice<Integer> langSelect = new DropDownChoice<Integer>(
					"lang", new PropertyModel<Integer>(userModel, "lang"),
					Arrays.asList(AkpUser.LANG_EN, AkpUser.LANG_FR,
							AkpUser.LANG_BR),
					new IChoiceRenderer<Integer>() {
						private static final long serialVersionUID = 1L;

						@Override
						public Object getDisplayValue(Integer lang) {
							switch (lang) {
							case AkpUser.LANG_EN:
								return getString("lang.en");
							case AkpUser.LANG_FR:
								return getString("lang.fr");
							case AkpUser.LANG_BR:
								return getString("lang.br");
							}
							return null;
						}

						@Override
						public String getIdValue(Integer lang, int index) {
							return "" + lang;
						}
					});
			langSelect.setRequired(true);
			add(langSelect);
			DropDownChoice<Integer> profileSelect = new DropDownChoice<Integer>(
					"profile",
					Arrays.asList(AkpUser.PROFILE_USER, AkpUser.PROFILE_ADMIN),
					new IChoiceRenderer<Integer>() {
						private static final long serialVersionUID = 1L;

						@Override
						public Object getDisplayValue(Integer profile) {
							return getString("profile." + profile);
						}

						@Override
						public String getIdValue(Integer profile, int index) {
							return "" + profile;
						}
					});
			profileSelect.setRequired(true);
			add(profileSelect);

			// Roles
			List<AkpLang> allLangs = akpTaxonService
					.getLangList(AkpUser.PROFILE_ADMIN);
			List<String> langXids = new ArrayList<>(allLangs.size());
			for (AkpLang lang : allLangs) {
				langXids.add(lang.getXid());
			}
			Collections.sort(langXids);
			List<String> allRoles = AkpUserRoles.allRoles(langXids);
			final Palette<String> rolesPalette = new Palette<String>("roles",
					new IModel<List<String>>() {
						private static final long serialVersionUID = 1L;

						@Override
						public void detach() {
						}

						@Override
						public List<String> getObject() {
							List<String> roles = new ArrayList<String>(
									AkpUserForm.this.getModelObject()
											.getRoles());
							Collections.sort(roles);
							return roles;
						}

						@Override
						public void setObject(List<String> object) {
							AkpUserForm.this.getModelObject()
									.setRoles(new HashSet<String>(object));
						}
					}, new ListModel<String>(allRoles),
					new SimpleToStringIChoiceRenderer<String>(), 8, false) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled() {
					return AkpUserForm.this.getModelObject()
							.getProfile() != AkpUser.PROFILE_ADMIN;
				}

			};
			rolesPalette.setOutputMarkupId(true);
			add(rolesPalette);

			profileSelect.add(new AjaxFormComponentUpdatingBehavior("change") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.add(rolesPalette);
				}
			});

			// Password
			passwordModel = new Model<String>();
			TextField<String> passwordField = new TextField<String>("password",
					passwordModel);
			add(passwordField);

			// Delete button
			Button deleteButton = new Button("deleteButton") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {
					AkpUser user = AkpUserForm.this.getModelObject();
					if (user.equals(AkpWicketSession.get().getAkpUser())) {
						error(getString("cant.delete.self"));
					} else {
						akpLoginService.deleteUser(user);
						setResponsePage(AkpUserManagementPage.class);
					}
				}
			};
			deleteButton.add(new JavascriptConfirmationModifier("onClick",
					new StringResourceModel("confirm.action.message",
							AkpUserPage.this, null)));
			deleteButton.setDefaultFormProcessing(false);
			add(deleteButton);
			// Cancel button
			Button cancelButton = new Button("cancelButton") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {
					setResponsePage(AkpUserManagementPage.class);
				}
			};
			cancelButton.setDefaultFormProcessing(false);
			add(cancelButton);
		}

		@Override
		public void onSubmit() {
			AkpUser user = getModelObject();
			akpLoginService.updateUser(user);
			if (passwordModel.getObject() != null) {
				akpLoginService.updatePassword(user, passwordModel.getObject());
			}
			setResponsePage(AkpUserManagementPage.class);
		}
	}
}
