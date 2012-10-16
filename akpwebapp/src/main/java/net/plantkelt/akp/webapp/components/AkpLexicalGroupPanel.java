package net.plantkelt.akp.webapp.components;

import java.util.List;

import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

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

public class AkpLexicalGroupPanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	public AkpLexicalGroupPanel(String id,
			final IModel<AkpLexicalGroup> lexicalGroupModel) {
		super(id);

		boolean isAdmin = AkpWicketSession.get().isAdmin();
		AkpLexicalGroup lexicalGroup = lexicalGroupModel.getObject();

		// Lang ID / correct code
		Label langCode = new Label("langCode", lexicalGroup.getLang().getXid());
		add(langCode);
		Label correctCode = new Label("correctCode",
				lexicalGroup.getCorrectDisplayCode());
		correctCode.setVisible(lexicalGroup.getCorrect() != 0);
		add(correctCode);

		// List of root names
		IModel<List<AkpVernacularName>> vernaListModel = new PropertyModel<List<AkpVernacularName>>(
				lexicalGroupModel, "rootVernacularNames");
		ListView<AkpVernacularName> vernaListView = new ListView<AkpVernacularName>(
				"vernaListView", vernaListModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpVernacularName> item) {
				AkpVernacularNamePanel vernaNamePanel = new AkpVernacularNamePanel(
						"vernaPanel", item.getModel(),
						AkpLexicalGroupPanel.this);
				item.add(vernaNamePanel);
			}
		};
		add(vernaListView);

		// Add root name button
		Form<Void> form = new Form<Void>("form");
		form.add(new AjaxSubmitLink("addRootNameButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				akpTaxonService.addRootVernacularName(lexicalGroupModel
						.getObject());
				target.add(AkpLexicalGroupPanel.this);
			}
		});
		add(form);
		form.setVisible(isAdmin);

		setOutputMarkupId(true);
	}
}
