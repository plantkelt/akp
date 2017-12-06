package net.plantkelt.akp.webapp.pages;

import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpLogTablePanel;
import net.plantkelt.akp.webapp.elements.AkpPagedTableControlPanel;
import net.plantkelt.akp.webapp.models.PagedListModel;

@AuthorizeInstantiation(AkpUserRoles.ROLE_ADMIN)
public class AkpUserLogsPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	@Inject
	private AkpTaxonService akpTaxonService;
	@Inject
	private AkpLoginService akpLoginService;
	@Inject
	private AkpLogService akpLogService;

	private String login;

	public AkpUserLogsPage(PageParameters parameters) {

		// Load data
		login = parameters.get("login").toOptionalString();
		AkpUser user = akpLoginService.getUser(login);

		// Header
		Label loginLbl = new Label("loginName", user == null ? login
				: user.getLogin() + " (" + user.getName() + ")");
		add(loginLbl);

		Link<AkpUserPage> editUserLink = AkpUserPage.link("editUserLink",
				login);
		add(editUserLink);

		// Paged model
		PagedListModel<AkpLogEntry> logListModel = new PagedListModel<AkpLogEntry>(
				akpLogService.getUserLogsCount(login), 100) {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpLogEntry> fetchPage(int pageNumber,
					int pageSize) {
				return akpLogService.getUserLogs(login, pageNumber, pageSize);
			}
		};

		// Table and page control
		AkpPagedTableControlPanel<AkpLogEntry> tableControl = new AkpPagedTableControlPanel<>(
				"tableControl", logListModel);
		add(tableControl);
		AkpLogTablePanel logTable = new AkpLogTablePanel("logTable",
				logListModel);
		add(logTable);
	}

	public static Link<AkpUserLogsPage> link(String id, String login) {
		return new BookmarkablePageLink<AkpUserLogsPage>(id,
				AkpUserLogsPage.class,
				new PageParameters().add("login", login));
	}
}
