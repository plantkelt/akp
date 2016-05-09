package net.plantkelt.akp.webapp.pages;

import java.util.List;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
public class AkpLangHomePage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private IModel<String> addXidModel;

	public AkpLangHomePage() {
		super();

		// Results
		IModel<List<AkpLang>> langListModel = new LoadableDetachableModel<List<AkpLang>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpLang> load() {
				return akpTaxonService.getLangList(AkpUser.PROFILE_ADMIN);
			}
		};
		ListView<AkpLang> langList = new ListView<AkpLang>("langList",
				langListModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpLang> item) {
				AkpLang lang = item.getModelObject();
				Link<AkpLangPage> langLink = AkpLangPage.link("langLink",
						lang.getXid());
				item.add(langLink);
				langLink.add(new Label("xidLabel", lang.getXid()));
				item.add(new Label("codeLabel", lang.getCode()));
				item.add(new Label("nameLabel", lang.getName()));
				item.add(new Label("grplngLabel", lang.getLangGroup().getName()));
				item.add(new Label("levelLabel", "" + lang.getLevel()));
				item.add(new AttributeModifier("class",
						item.getIndex() % 2 == 0 ? "even" : "odd"));
			}
		};
		add(langList);

		// Add bib section
		addXidModel = new Model<String>();
		WebMarkupContainer addSection = new WebMarkupContainer("addSection");
		add(addSection);
		Form<Void> addForm = new Form<Void>("addLangForm") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				String addXid = addXidModel.getObject();
				if (addXid == null) {
					return;
				}
				AkpLang lang = akpTaxonService.getLang(addXid);
				if (lang != null) {
					error(getString("lang.xid.already.exists"));
				} else {
					akpTaxonService.createNewLang(addXid);
					setResponsePage(AkpLangPage.class,
							new PageParameters().add("xid", addXid));
				}
			}
		};
		addSection.add(addForm);
		addForm.add(new FeedbackPanel("feedback"));
		addForm.add(new TextField<String>("xid", addXidModel));

	}

	public static Link<AkpLangHomePage> link(String id) {
		return new BookmarkablePageLink<AkpLangHomePage>(id,
				AkpLangHomePage.class);
	}
}
