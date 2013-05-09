package net.plantkelt.akp.webapp.elements;

import java.util.List;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.CollapsibleButton;
import net.plantkelt.akp.webapp.models.BrEnFrStringModel;
import net.plantkelt.akp.webapp.wicket.AkpSessionData;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
			final IModel<AkpLexicalGroup> lexicalGroupModel,
			final Component refreshMasterComponent) {
		super(id);

		boolean isAdmin = AkpWicketSession.get().isAdmin();
		AkpLexicalGroup lexicalGroup = lexicalGroupModel.getObject();

		// Collapsible stuff
		WebMarkupContainer collapseDiv = new WebMarkupContainer("collapseDiv");
		add(collapseDiv);
		CollapsibleButton collapseButton = new CollapsibleButton(
				"collapseButton", collapseDiv, AkpWicketSession
						.get()
						.getSessionData()
						.isLexicalGroupDefaultOpen(
								lexicalGroup.getLang().getXid(),
								lexicalGroup.getCorrect())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onOpenClose(boolean open) {
				AkpLexicalGroup lexicalGroup = lexicalGroupModel.getObject();
				AkpSessionData sessionData = AkpWicketSession.get()
						.getSessionData();
				String langXid = lexicalGroup.getLang().getXid();
				sessionData.setLexicalGroupDefaultOpen(langXid,
						lexicalGroup.getCorrect(), open);
				sessionData.setDefautLangXid(langXid);
			}
		};
		add(collapseButton);

		// Lang ID / correct code
		AkpLang lang = lexicalGroup.getLang();
		WebMarkupContainer langCodeLink = new WebMarkupContainer("langCodeLink");
		add(langCodeLink);
		Label langCodeLabel = new Label("langCode", lang.getCode());
		langCodeLink.add(langCodeLabel);
		Label langTitleLabel = new Label("langTitle",
				new BrEnFrStringModel(lang.getName()));
		langCodeLink.add(langTitleLabel);
		Label langDescLabel = new Label("langDesc", new BrEnFrStringModel(
				lang.getDesc()));
		langDescLabel.setEscapeModelStrings(false);
		langCodeLink.add(langDescLabel);
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
		collapseDiv.add(vernaListView);

		// Add root name button
		Form<Void> form = new Form<Void>("form");
		collapseDiv.add(form);
		form.setVisible(isAdmin);
		form.add(new AjaxSubmitLink("addRootNameButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				AkpUser user = AkpWicketSession.get().getAkpUser();
				akpTaxonService.addRootVernacularName(lexicalGroupModel
						.getObject(), user == null ? null : user.getLastbib());
				target.add(AkpLexicalGroupPanel.this);
			}
		});
		form.add(new AjaxSubmitLink("deleteLexicalGroupButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				akpTaxonService.deleteLexicalGroup(lexicalGroupModel
						.getObject());
				target.add(refreshMasterComponent);
			}

			@Override
			public boolean isVisible() {
				return (lexicalGroupModel.getObject().getVernacularNames()
						.size() == 0);
			}
		});

		setOutputMarkupId(true);
	}
}
