package net.plantkelt.akp.webapp.pages;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.inject.Inject;

import net.plantkelt.akp.domain.AkpUserLogEntry;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.service.AkpLogService;

@AuthorizeInstantiation(AkpUserRoles.ROLE_ADMIN)
public class AkpLoginListPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpLogService akpLogService;

	private IModel<String> loginModel;
	private IModel<Date> dateModel;

	private DateFormat dateFormat;

	public AkpLoginListPage() {
		super();

		dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM);

		// Search
		loginModel = new Model<String>();
		dateModel = new Model<Date>(new Date());
		SearchForm searchForm = new SearchForm("searchForm");
		add(searchForm);

		// Results
		IModel<List<AkpUserLogEntry>> resultsModel = new LoadableDetachableModel<List<AkpUserLogEntry>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpUserLogEntry> load() {
				if (somethingToSearchFor()) {
					return akpLogService.getLoginLogs(dateModel.getObject(),
							loginModel.getObject());
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
		ListView<AkpUserLogEntry> logList = new ListView<AkpUserLogEntry>(
				"logList", resultsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpUserLogEntry> item) {
				AkpUserLogEntry userLog = item.getModelObject();
				item.add(new Label("loginLabel", userLog.getLogin()));
				item.add(new Label("dateLabel",
						dateFormat.format(userLog.getDate())));
				item.add(new Label("addrLabel", userLog.getRemoteAddr()));
				item.add(new ExternalLink("geoipLink",
						"http://www.geoiptool.com/fr/?IP="
								+ userLog.getRemoteAddr()));
				item.add(new Label("operationLabel", getString(
						"userlog.operation." + userLog.getOperation())));
				item.add(new Label("valueLabel", userLog.getValue()));
			}
		};
		searchResultsSection.add(logList);
	}

	private boolean somethingToSearchFor() {
		return loginModel.getObject() != null || dateModel.getObject() != null;
	}

	private class SearchForm extends Form<Void> {
		private static final long serialVersionUID = 1L;

		public SearchForm(String id) {
			super(id);
			add(new TextField<String>("login", loginModel));
			add(new DateTextField("date", dateModel));
			add(new Button("previous") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {
					changeDate(-1);
				}
			});
			add(new Button("next") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {
					changeDate(1);
				}
			});
		}

		private void changeDate(int delta) {
			if (dateModel.getObject() == null)
				dateModel.setObject(new Date());
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateModel.getObject());
			cal.add(Calendar.DATE, delta);
			dateModel.setObject(cal.getTime());
		}
	}

	public static Link<AkpLoginListPage> link(String id) {
		return new BookmarkablePageLink<AkpLoginListPage>(id,
				AkpLoginListPage.class);
	}
}
