package net.plantkelt.akp.webapp.elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import net.plantkelt.akp.domain.AkpLogEntry;

public class AkpLogTablePanel extends Panel {

	private static final long serialVersionUID = 1L;

	public AkpLogTablePanel(String id,
			final IModel<List<AkpLogEntry>> logListModel) {
		super(id);

		final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

		ListView<AkpLogEntry> logListView = new ListView<AkpLogEntry>(
				"logTable", logListModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpLogEntry> item) {
				AkpLogEntry logEntry = item.getModelObject();
				Label dateLabel = new Label("date",
						dateFormat.format(logEntry.getDate()));
				item.add(dateLabel);
				Label userLabel = new Label("user", logEntry.getLogin());
				item.add(userLabel);
				Label typeLabel = new Label("type",
						getString("log.type." + logEntry.getType()));
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
}
