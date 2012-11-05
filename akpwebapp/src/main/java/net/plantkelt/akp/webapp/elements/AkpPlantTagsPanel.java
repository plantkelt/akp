package net.plantkelt.akp.webapp.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.EditorModel;
import net.plantkelt.akp.webapp.components.InPlaceEditor;
import net.plantkelt.akp.webapp.components.InPlaceSelector;
import net.plantkelt.akp.webapp.components.SelectorModel;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpPlantTagsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpPlantTagsPanel(String id, final IModel<AkpPlant> plantModel) {
		super(id);

		final boolean isAdmin = AkpWicketSession.get().isAdmin();

		IModel<List<AkpPlantTag>> listModel = new AbstractReadOnlyModel<List<AkpPlantTag>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public List<AkpPlantTag> getObject() {
				return new ArrayList<AkpPlantTag>(plantModel.getObject()
						.getTags());
			}
		};
		ListView<AkpPlantTag> tagsList = new ListView<AkpPlantTag>("tags",
				listModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpPlantTag> item) {
				final IModel<AkpPlantTag> tagModel = item.getModel();
				AkpPlantTag tag = tagModel.getObject();
				IModel<String> valueModel = new PropertyModel<String>(tagModel,
						"value");
				Label valueLabel = new Label("tagValue", valueModel);
				valueLabel.setEscapeModelStrings(false);
				valueLabel.add(new AttributeAppender("class",
						new Model<String>("tag_"
								+ tagModel.getObject().getType()), " "));
				if (tag.isTypeString()) {
					InPlaceEditor editor = new InPlaceEditor("tagEditor",
							new EditorModel<String>() {
								private static final long serialVersionUID = 1L;

								@Override
								public String getObject() {
									return tagModel.getObject()
											.getStringValue();
								}

								@Override
								public void saveObject(
										AjaxRequestTarget target, String value) {
									AkpPlantTag tag = tagModel.getObject();
									if (value == null || value.length() == 0) {
										akpTaxonService.deletePlantTag(tag);
									} else {
										tag.setStringValue(value);
										akpTaxonService.updatePlantTag(tag);
									}
									target.add(AkpPlantTagsPanel.this);
								}
							});
					item.add(editor);
					editor.setReadOnly(!isAdmin);
					editor.add(valueLabel);
				} else {
					InPlaceSelector<Integer> selector = new InPlaceSelector<Integer>(
							"tagEditor", new SelectorModel<Integer>() {
								private static final long serialVersionUID = 1L;

								@Override
								public Integer getObject() {
									return tagModel.getObject().getIntValue();
								}

								@Override
								public void saveObject(
										AjaxRequestTarget target, Integer value) {
									AkpPlantTag tag = tagModel.getObject();
									if (value == null) {
										akpTaxonService.deletePlantTag(tag);
									} else {
										tag.setIntValue(value);
										akpTaxonService.updatePlantTag(tag);
									}
									target.add(AkpPlantTagsPanel.this);
								}

								@Override
								public String getDisplayValue(Integer value) {
									if (value == null)
										return getString("remove.tag");
									else
										return tagModel.getObject()
												.getIntValueAsString(value);
								}

								@Override
								public List<Integer> getValues() {
									List<Integer> retval = new ArrayList<Integer>(
											tagModel.getObject()
													.getAllIntPossibleValues());
									Collections.sort(retval);
									retval.add(0, null);
									return retval;
								}

								@Override
								public String getIdValue(Integer t) {
									return t.toString();
								}
							});
					item.add(selector);
					selector.setReadOnly(!isAdmin);
					selector.add(valueLabel);
				}
			}
		};
		add(tagsList);
		setOutputMarkupId(true);

		// Add tag form
		Form<Void> form = new Form<Void>("form");
		List<Integer> tagTypes = AkpPlantTag.getAvailableTypes();
		final IModel<Integer> tagTypeModel = new Model<Integer>();
		DropDownChoice<Integer> tagTypeSelect = new DropDownChoice<Integer>(
				"tagTypeSelect", tagTypeModel, tagTypes,
				new IChoiceRenderer<Integer>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(Integer type) {
						return getString("tag." + type);
					}

					@Override
					public String getIdValue(Integer type, int index) {
						return type.toString();
					}
				});
		form.add(tagTypeSelect);
		form.add(new AjaxSubmitLink("addTagButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Integer tagType = tagTypeModel.getObject();
				if (tagType != null)
					akpTaxonService.createNewPlantTag(plantModel.getObject(),
							tagType);
				target.add(AkpPlantTagsPanel.this);
			}
		});
		add(form);
		form.setVisible(isAdmin);
	}
}
