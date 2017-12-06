package net.plantkelt.akp.webapp.pages;

import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpLogTablePanel;

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

		// Model
		IModel<List<AkpLogEntry>> logListModel = new LoadableDetachableModel<List<AkpLogEntry>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpLogEntry> load() {
				return akpLogService.getPlantLogs(plantId);
			}
		};

		// Table
		AkpLogTablePanel logTable = new AkpLogTablePanel("logTable",
				logListModel);
		add(logTable);
	}

	public static Link<AkpPlantLogsPage> link(String id, Integer xid) {
		return new BookmarkablePageLink<AkpPlantLogsPage>(id,
				AkpPlantLogsPage.class, new PageParameters().add("xid", xid));
	}
}
