package net.plantkelt.akp.webapp.components;

import java.util.List;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
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
							// TODO delete verna
						} else {
							vernaName.setName(name);
							vernaName.getLexicalGroup().refreshVernacularNamesTree();
							akpTaxonService.updateVernacularName(vernaName);
							target.add(refreshComponent);
						}
					}
				});
		add(vernaEditor);
		vernaEditor.setReadOnly(!isAdmin);

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
				// TODO bib editor
				AkpBib bib = item.getModelObject();
				Label bibLabel = new Label("bibEntry", bib.getXid());
				item.add(bibLabel);
			}
		};
		add(bibListView);

		// TODO Add children recursive list
		IModel<List<AkpVernacularName>> childrenNamesModel = new PropertyModel<List<AkpVernacularName>>(
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

		setOutputMarkupId(true);
	}
}
