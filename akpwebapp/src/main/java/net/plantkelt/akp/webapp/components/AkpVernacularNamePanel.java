package net.plantkelt.akp.webapp.components;

import java.util.List;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpVernacularNamePanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	public AkpVernacularNamePanel(String id,
			final IModel<AkpVernacularName> vernaNameModel,
			final Component refreshComponent) {
		super(id);

		AkpVernacularName vernaName = vernaNameModel.getObject();
		final boolean isAdmin = AkpWicketSession.get().isAdmin();

		// Vernacular name in-place editor
		InPlaceEditor vernaEditor = new InPlaceEditor("vernaEditor",
				new EditorModel<String>() {
					@Override
					public String getObject() {
						return vernaNameModel.getObject().getName();
					}

					@Override
					public void saveObject(AjaxRequestTarget target, String name) {
						AkpVernacularName vernaName = vernaNameModel
								.getObject();
						if (name == null || name.length() == 0) {
							akpTaxonService.deleteVernacularName(vernaName);
						} else {
							vernaName.setName(name);
							akpTaxonService.updateVernacularName(vernaName);
						}
						target.add(refreshComponent);
					}
				});
		add(vernaEditor);
		vernaEditor.setReadOnly(!isAdmin);
		vernaEditor.setVisible(isAdmin || !vernaName.getName().equals("#"));

		// Vernacular name label
		Label vernaNameLabel = new Label("vernaName",
				new PropertyModel<String>(vernaNameModel, "name"));
		vernaEditor.add(vernaNameLabel);

		// Bib list entry
		IModel<List<AkpBib>> bibListModel = new PropertyModel<List<AkpBib>>(
				vernaNameModel, "bibs");
		ListView<AkpBib> bibListView = new ListView<AkpBib>("bibList",
				bibListModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpBib> item) {
				AkpBibPanel bibPanel = new AkpBibPanel("bibEntry",
						item.getModel(), vernaNameModel,
						AkpVernacularNamePanel.this);
				item.add(bibPanel);
			}
		};
		add(bibListView);

		// Bib adder
		AkpBibAdderPanel bibAdder = new AkpBibAdderPanel("bibAdder",
				vernaNameModel, this);
		add(bibAdder);
		bibAdder.setVisible(isAdmin);

		// Plant ref adder
		AkpPlantRefAdderPanel plantRefAdder = new AkpPlantRefAdderPanel(
				"plantRefAdder", vernaNameModel, this);
		add(plantRefAdder);
		plantRefAdder.setVisible(isAdmin);

		// Children recursive list
		final IModel<List<AkpVernacularName>> childrenNamesModel = new PropertyModel<List<AkpVernacularName>>(
				vernaNameModel, "children");
		ListView<AkpVernacularName> childrenListView = new ListView<AkpVernacularName>(
				"childrenList", childrenNamesModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpVernacularName> item) {
				AkpVernacularNamePanel subPanel = new AkpVernacularNamePanel(
						"childrenPanel", item.getModel(),
						AkpVernacularNamePanel.this);
				item.add(subPanel);
			}
		};
		add(childrenListView);

		// Add child name button
		Form<Void> form = new Form<Void>("form");
		form.add(new AjaxSubmitLink("addChildButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				akpTaxonService.addChildVernacularName(vernaNameModel
						.getObject());
				target.add(AkpVernacularNamePanel.this);
			}
		});
		add(form);
		form.setVisible(isAdmin);

		// Comments editor
		InPlaceEditor commentsEditor = new InPlaceEditor("commentsEditor",
				new EditorModel<String>() {

					@Override
					public String getObject() {
						return vernaNameModel.getObject().getComments();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String comments) {
						AkpVernacularName vernaName = vernaNameModel
								.getObject();
						vernaName.setComments(comments);
						akpTaxonService.updateVernacularName(vernaName);
						target.add(AkpVernacularNamePanel.this);
					}
				});
		add(commentsEditor);
		commentsEditor.setReadOnly(!isAdmin);
		Label commentsLabel = new Label("commentsLabel",
				new PropertyModel<String>(vernaNameModel, "comments"));
		commentsLabel.setEscapeModelStrings(false);
		commentsEditor.add(commentsLabel);

		// Plant refs
		IModel<List<AkpPlant>> plantRefsModel = new PropertyModel<List<AkpPlant>>(
				vernaNameModel, "plantRefs");
		ListView<AkpPlant> plantRefsList = new ListView<AkpPlant>(
				"plantRefsList", plantRefsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpPlant> item) {
				AkpPlantRefPanel plantRefPanel = new AkpPlantRefPanel(
						"plantRef", item.getModel(), vernaNameModel,
						AkpVernacularNamePanel.this);
				item.add(plantRefPanel);
			}
		};
		add(plantRefsList);

		setOutputMarkupId(true);
	}
}