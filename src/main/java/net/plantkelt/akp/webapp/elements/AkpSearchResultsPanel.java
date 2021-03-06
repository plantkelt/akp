package net.plantkelt.akp.webapp.elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.plantkelt.akp.domain.AkpSearchResult;
import net.plantkelt.akp.domain.AkpSearchResult.AkpSearchResultColumn;
import net.plantkelt.akp.domain.AkpSearchResult.AkpSearchResultRow;
import net.plantkelt.akp.webapp.pages.AkpPlantPage;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

public class AkpSearchResultsPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public AkpSearchResultsPanel(String id,
			final IModel<AkpSearchResult> searchResultModel) {
		super(id);

		ListView<String> resultsHeaderListView = new ListView<String>(
				"resultsHeaderListView", new PropertyModel<List<String>>(
						searchResultModel, "headerKeys")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("headerLabel",
						getString(item.getModelObject())));
			}
		};
		add(resultsHeaderListView);

		final Map<String, AttributeAppender> classAppenders = new HashMap<String, AttributeAppender>();

		ListView<AkpSearchResultRow> resultsTableListView = new ListView<AkpSearchResultRow>(
				"resultsTableListView",
				new PropertyModel<List<AkpSearchResultRow>>(searchResultModel,
						"rows")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpSearchResultRow> item) {
				RepeatingView colRepeat = new RepeatingView("colRepeat");
				AkpSearchResultRow row = item.getModelObject();
				for (AkpSearchResultColumn col : row.getColumns()) {
					final Integer plantXid = col.getPlantXid() != null
							? col.getPlantXid()
							: row.getPlantXid();
					boolean hasPlantLink = AkpWicketSession.get().isLoggedIn()
							&& plantXid != null;
					final String langXid = col.getPlantXid() != null ? null
							: row.getLangXid();
					final Integer correct = col.getPlantXid() != null ? null
							: row.getCorrect();
					WebMarkupContainer cell = new WebMarkupContainer(
							colRepeat.newChildId());
					colRepeat.add(cell);
					WebMarkupContainer cellLink;
					if (hasPlantLink) {
						cellLink = new Link<AkpPlantPage>("cellLink") {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick() {
								if (langXid != null && correct != null)
									AkpWicketSession.get().getSessionData()
											.setLexicalGroupDefaultOpen(langXid,
													correct, true);
								setResponsePage(AkpPlantPage.class,
										new PageParameters().add("xid",
												plantXid));
							}
						};
					} else {
						cellLink = new WebMarkupContainer("cellLink");
						cellLink.setVisible(false);
					}
					cell.add(cellLink);
					String colValue = col.isValueKey()
							? getString(col.getDisplayValue())
							: col.getDisplayValue();
					Label cellValue = new Label("cellValue", colValue);
					Label cellValue2 = new Label("cellValue2", colValue);
					String className = col.getClassName();
					if (className != null) {
						AttributeAppender classAttributeAppender = classAppenders
								.get(className);
						if (classAttributeAppender == null) {
							classAttributeAppender = new AttributeAppender(
									"class", new Model<String>(className), " ");
							classAppenders.put(className,
									classAttributeAppender);
						}
						cellValue.add(classAttributeAppender);
						cellValue2.add(classAttributeAppender);
					}
					cellLink.add(cellValue);
					cell.add(cellValue2);
					cellValue.setEscapeModelStrings(col.isEscape());
					cellValue2.setEscapeModelStrings(col.isEscape());
					cellValue2.setVisible(!hasPlantLink);
				}
				item.add(colRepeat);
			}
		};
		add(resultsTableListView);
		Label numberOfResults = new Label("numberOfResults",
				new StringResourceModel("search.number.of.results", this,
						searchResultModel));
		add(numberOfResults);

		Label resultsLimit = new Label("resultsLimit", new StringResourceModel(
				"search.results.limit", this, searchResultModel)) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return searchResultModel.getObject().getLimit() > 0;
			}

		};
		add(resultsLimit);
	}
}
