package net.plantkelt.akp.webapp.components;

import java.util.ArrayList;
import java.util.List;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpPlantTagsPanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	public AkpPlantTagsPanel(String id, final IModel<AkpPlant> plantModel) {
		super(id);

		AkpUser user = AkpWicketSession.get().getAkpUser();
		final boolean isAdmin = user != null && user.isAdmin();

		IModel<List<AkpPlantTag>> listModel = new LoadableDetachableModel<List<AkpPlantTag>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpPlantTag> load() {
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
				InPlaceEditor editor = new InPlaceEditor("tagEditor",
						new EditorModel<String>() {
							@Override
							public String getObject() {
								// TODO
								return tagModel.getObject().getStringValue();
							}

							@Override
							public void saveObject(AjaxRequestTarget target,
									String name) {
								// TODO
								target.add(AkpPlantTagsPanel.this);
							}
						});
				item.add(editor);
				editor.setReadOnly(!isAdmin);
				IModel<String> valueModel = new PropertyModel<String>(tagModel,
						"value");
				Label valueLabel = new Label("tagValue", valueModel);
				valueLabel.add(new AttributeAppender("class",
						new Model<String>("tag_"
								+ tagModel.getObject().getType()), " "));
				editor.add(valueLabel);
			}
		};
		add(tagsList);
		setOutputMarkupId(true);

		// Add tag button
		Form<Void> form = new Form<Void>("form");
		form.add(new AjaxSubmitLink("addTagButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				// TODO
				target.add(AkpPlantTagsPanel.this);
			}
		});
		add(form);
		form.setVisible(isAdmin);
	}
}
