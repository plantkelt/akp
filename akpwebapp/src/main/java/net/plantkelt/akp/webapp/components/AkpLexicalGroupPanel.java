package net.plantkelt.akp.webapp.components;

import java.util.List;

import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.markup.html.basic.Label;
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

		AkpLexicalGroup lexicalGroup = lexicalGroupModel.getObject();

		Label langCode = new Label("langCode", lexicalGroup.getLang().getXid());
		add(langCode);

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

		setOutputMarkupId(true);
	}
}
