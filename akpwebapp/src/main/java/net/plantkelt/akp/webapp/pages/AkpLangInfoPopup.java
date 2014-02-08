package net.plantkelt.akp.webapp.pages;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.models.BrEnFrStringModel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class AkpLangInfoPopup extends AkpPopupTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private String langId;
	private IModel<AkpLang> langModel;

	public AkpLangInfoPopup(PageParameters parameters) {
		super(parameters);

		// Load data
		langId = parameters.get("xid").toOptionalString();
		langModel = new LoadableDetachableModel<AkpLang>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpLang load() {
				return akpTaxonService.getLang(langId);
			}
		};
		if (langModel.getObject() == null)
			throw new IllegalArgumentException("Invalid lang ID: " + langId);

		// Set page title
		AkpLang lang = langModel.getObject();
		setPageTitle(lang.getCode() + " - " + lang.getName());

		// Infos
		add(new Label("langId", langId));
		add(new Label("langName", new BrEnFrStringModel(lang.getName())));
		Label langDescLabel = new Label("langDesc", new BrEnFrStringModel(
				lang.getDesc()));
		langDescLabel.setEscapeModelStrings(false);
		add(langDescLabel);
	}

	public static Link<AkpLangInfoPopup> link(String id, String xid) {
		return new BookmarkablePageLink<AkpLangInfoPopup>(id,
				AkpLangInfoPopup.class, new PageParameters().add("xid", xid));
	}
}
