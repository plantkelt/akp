package net.plantkelt.akp.webapp.elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.inject.Inject;

import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.pages.AkpPlantPage;

public class AkpLogTablePanel extends Panel {
	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private boolean displayLogin;
	private boolean displayPlant;
	private IModel<Map<Integer, AkpPlant>> plantCacheModel;

	public AkpLogTablePanel(String id,
			final IModel<List<AkpLogEntry>> logListModel, boolean aDisplayLogin,
			boolean aDisplayPlant) {
		super(id);
		this.displayLogin = aDisplayLogin;
		this.displayPlant = aDisplayPlant;

		add(new WebMarkupContainer("userHeader").setVisible(displayLogin));
		add(new WebMarkupContainer("plantHeader").setVisible(displayPlant));

		final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

		plantCacheModel = new LoadableDetachableModel<Map<Integer, AkpPlant>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected Map<Integer, AkpPlant> load() {
				return new HashMap<>();
			}
		};

		ListView<AkpLogEntry> logListView = new ListView<AkpLogEntry>(
				"logTable", logListModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpLogEntry> item) {
				AkpLogEntry logEntry = item.getModelObject();

				Label dateLabel = new Label("date",
						dateFormat.format(logEntry.getDate()));
				item.add(dateLabel);

				WebMarkupContainer userCell = new WebMarkupContainer(
						"userCell");
				item.add(userCell);
				userCell.setVisible(displayLogin);
				Label userLabel = new Label("user", logEntry.getLogin());
				userCell.add(userLabel);

				WebMarkupContainer plantCell = new WebMarkupContainer(
						"plantCell");
				item.add(plantCell);
				plantCell.setVisible(displayPlant);
				if (displayPlant) {
					AkpPlant akpPlant = lazyLoadPlant(logEntry.getPlantId());
					Link<AkpPlantPage> plantLink = AkpPlantPage
							.link("plantLink", logEntry.getPlantId());
					plantCell.add(plantLink);
					Label plantLabel = new Label("plant",
							akpPlant == null ? "" + logEntry.getPlantId()
									: akpPlant.getMainName().getHtmlName());
					if (akpPlant != null) {
						plantLabel.setEscapeModelStrings(false);
					}
					plantLink.add(plantLabel);
				}

				Label typeLabel = new Label("type",
						getString("log.type." + logEntry.getType()));
				item.add(typeLabel);

				Label newValueLabel = new Label("newValue",
						logEntry.getNewValue());
				item.add(newValueLabel);
				Label oldValueLabel = new Label("oldValue",
						logEntry.getOldValue());
				item.add(oldValueLabel);

				// TODO Use css "even/odd" selectors for that
				item.add(new AttributeModifier("class",
						item.getIndex() % 2 == 0 ? "even" : "odd"));
			}
		};
		add(logListView);
	}

	private AkpPlant lazyLoadPlant(Integer plantId) {
		Map<Integer, AkpPlant> plantCache = plantCacheModel.getObject();
		if (plantCache.containsKey(plantId)) {
			return plantCache.get(plantId); // Can be null
		}
		AkpPlant plant = akpTaxonService.getPlant(plantId);
		plantCache.put(plantId, plant); // Can also be null
		return plant;
	}
}
