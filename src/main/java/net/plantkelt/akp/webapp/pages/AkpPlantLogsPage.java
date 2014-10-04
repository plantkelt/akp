package net.plantkelt.akp.webapp.pages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
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
		final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

		// Header
		Label plantName = new Label("plantName",
				plant == null ? plantId.toString() : plant.getMainName()
						.getHtmlName());
		plantName.setEscapeModelStrings(false);
		add(plantName);

		Link<AkpPlantPage> viewPlantLink = AkpPlantPage.link("viewPlantLink",
				plantId);
		add(viewPlantLink);

		// Table
		IModel<List<AkpLogEntry>> logModel = new LoadableDetachableModel<List<AkpLogEntry>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpLogEntry> load() {
				return akpLogService.getPlantLogs(plantId);
			}
		};
		ListView<AkpLogEntry> logListView = new ListView<AkpLogEntry>(
				"logTable", logModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpLogEntry> item) {
				AkpLogEntry logEntry = item.getModelObject();
				Label dateLabel = new Label("date", dateFormat.format(logEntry
						.getDate()));
				item.add(dateLabel);
				Label userLabel = new Label("user", logEntry.getLogin());
				item.add(userLabel);
				Label typeLabel = new Label("type", getString("log.type."
						+ logEntry.getType()));
				item.add(typeLabel);
				Label newValueLabel = new Label("newValue",
						logEntry.getNewValue());
				item.add(newValueLabel);
				Label oldValueLabel = new Label("oldValue",
						logEntry.getOldValue());
				item.add(oldValueLabel);
				item.add(new AttributeModifier("class",
						item.getIndex() % 2 == 0 ? "even" : "odd"));
			}
		};
		add(logListView);

	}

	public static Link<AkpPlantLogsPage> link(String id, Integer xid) {
		return new BookmarkablePageLink<AkpPlantLogsPage>(id,
				AkpPlantLogsPage.class, new PageParameters().add("xid", xid));
	}
}
