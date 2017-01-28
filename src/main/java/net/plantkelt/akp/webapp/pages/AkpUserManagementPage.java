package net.plantkelt.akp.webapp.pages;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpLoginService;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
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

@AuthorizeInstantiation("ADMIN")
public class AkpUserManagementPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLoginService akpLoginService;

	private IModel<String> loginModel;
	private IModel<String> nameModel;
	private IModel<String> emailModel;
	private IModel<Integer> profileModel;

	private IModel<Boolean> onlyExpiredModel;

	private DateFormat expireFormat;

	public AkpUserManagementPage() {
		super();

		expireFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

		// Search
		loginModel = new Model<String>();
		nameModel = new Model<String>();
		emailModel = new Model<String>();
		profileModel = new Model<Integer>();
		onlyExpiredModel = new Model<Boolean>();
		SearchForm searchForm = new SearchForm("searchForm");
		add(searchForm);

		// Results
		IModel<List<AkpUser>> resultsModel = new LoadableDetachableModel<List<AkpUser>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpUser> load() {
				if (somethingToSearchFor()) {
					int limit = 1000;
					return akpLoginService.searchUser(limit,
							loginModel.getObject(), nameModel.getObject(),
							emailModel.getObject(), profileModel.getObject(),
							onlyExpiredModel.getObject());
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
		ListView<AkpUser> userList = new ListView<AkpUser>("userList",
				resultsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpUser> item) {
				AkpUser user = item.getModelObject();
				Link<?> userLink = AkpUserPage.link("userLink",
						user.getLogin());
				item.add(userLink);
				userLink.add(new Label("loginLabel", user.getLogin()));
				item.add(new Label("nameLabel", user.getName()));
				item.add(new Label("emailLabel", user.getEmail()));
				item.add(new Label("profileLabel",
						getString("profile." + user.getProfile())));
				boolean expired = user.getExpire().compareTo(new Date()) < 0;
				item.add(new Label("expireLabel",
						expireFormat.format(user.getExpire()) + (expired
								? " (" + getString("user.expired") + ")"
								: "")));
				item.add(new Label("requestCountLabel",
						user.getRequestCount() == null ? "-"
								: "" + user.getRequestCount()));
				item.add(new AttributeModifier("class",
						item.getIndex() % 2 == 0 ? "even" : "odd"));
			}
		};
		searchResultsSection.add(userList);

		// Add user section
		WebMarkupContainer addSection = new WebMarkupContainer("addSection");
		add(addSection);
		Form<String> addForm = new Form<String>("addUserForm",
				new Model<String>()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				String addLogin = getModelObject();
				if (addLogin == null) {
					return;
				}
				AkpUser user = akpLoginService.getUser(addLogin);
				if (user != null) {
					error(getString("user.login.already.exists"));
				} else {
					akpLoginService.createUser(addLogin);
					setResponsePage(AkpUserPage.class,
							new PageParameters().add("login", addLogin));
				}
			}
		};
		addSection.add(addForm);
		addForm.add(new FeedbackPanel("feedback"));
		addForm.add(new TextField<String>("login", addForm.getModel()));

		// Link to user login history
		add(AkpLoginListPage.link("loginLogLink"));
	}

	private boolean somethingToSearchFor() {
		return loginModel.getObject() != null || nameModel.getObject() != null
				|| emailModel.getObject() != null
				|| profileModel.getObject() != null
				|| onlyExpiredModel.getObject() != null
						&& onlyExpiredModel.getObject();
	}

	private class SearchForm extends Form<Void> {
		private static final long serialVersionUID = 1L;

		public SearchForm(String id) {
			super(id);
			add(new TextField<String>("login", loginModel));
			add(new TextField<String>("name", nameModel));
			add(new TextField<String>("email", emailModel));
			DropDownChoice<Integer> profileChoice = new DropDownChoice<Integer>(
					"profile", profileModel,
					Arrays.asList(AkpUser.PROFILE_USER, AkpUser.PROFILE_ADMIN),
					new IChoiceRenderer<Integer>() {
						private static final long serialVersionUID = 1L;

						@Override
						public Object getDisplayValue(Integer object) {
							return getString("profile." + object);
						}

						@Override
						public String getIdValue(Integer object, int index) {
							return "" + object;
						}
					});
			// add(new CheckBox("onlyExpired", onlyExpiredModel));
			profileChoice.setNullValid(true);
			add(profileChoice);
		}
	}

	public static Link<AkpUserManagementPage> link(String id) {
		return new BookmarkablePageLink<AkpUserManagementPage>(id,
				AkpUserManagementPage.class);
	}
}
