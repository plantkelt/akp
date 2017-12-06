package net.plantkelt.akp.webapp.pages;

import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpLogTablePanel;
import net.plantkelt.akp.webapp.elements.AkpPagedTableControlPanel;
import net.plantkelt.akp.webapp.models.PagedListModel;

@AuthorizeInstantiation(AkpUserRoles.ROLE_VIEW_PLANT_HIST)
public class AkpPlantLogsPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;
	@Inject
	private AkpLogService akpLogService;

	private Integer plantId;

	public AkpPlantLogsPage(PageParameters parameters) {

		// Load data
		plantId = parameters.get("xid").toOptionalInteger();
		AkpPlant plant = akpTaxonService.getPlant(plantId);

		// Header
		Label plantName = new Label("plantName",
				plant == null ? plantId.toString()
						: plant.getMainName().getHtmlName());
		plantName.setEscapeModelStrings(false);
		add(plantName);

		Link<AkpPlantPage> viewPlantLink = AkpPlantPage.link("viewPlantLink",
				plantId);
		add(viewPlantLink);

		// Paged model
		PagedListModel<AkpLogEntry> logListModel = new PagedListModel<AkpLogEntry>(
				akpLogService.getPlantLogsCount(plantId), 100) {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpLogEntry> fetchPage(int pageNumber,
					int pageSize) {
				return akpLogService.getPlantLogs(plantId, pageNumber,
						pageSize);
			}
		};

		// Table and page control
		AkpPagedTableControlPanel<AkpLogEntry> tableControl = new AkpPagedTableControlPanel<>(
				"tableControl", logListModel);
		add(tableControl);
		AkpLogTablePanel logTable = new AkpLogTablePanel("logTable",
				logListModel, true, false);
		add(logTable);
	}

	public static Link<AkpPlantLogsPage> link(String id, Integer xid) {
		return new BookmarkablePageLink<AkpPlantLogsPage>(id,
				AkpPlantLogsPage.class, new PageParameters().add("xid", xid));
	}
}
