package net.plantkelt.akp.webapp.pages;

import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

public class AkpPopupTemplate extends WebPage {

	private static final long serialVersionUID = 1L;

	private IModel<String> pageTitleModel = new Model<String>("PlantKelt");

	public AkpPopupTemplate() {
		super();
		init();
	}

	public AkpPopupTemplate(PageParameters parameters) {
		super(parameters);
		init();
	}

	private void init() {
		if (getApplication().usesDevelopmentConfig()) {
			add(new DebugBar("debug"));
		} else {
			add(new WebMarkupContainer("debug").setVisible(false));
		}
		// Force getting client timezone info here. See comment on method.
		AkpWicketSession.get().getClientInfo().getProperties().getTimeZone();
		add(new Label("pageTitle", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return AkpPopupTemplate.this.pageTitleModel.getObject();
			}
		}));
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				AkpPopupTemplate.class, "res/akp.css")));
	}

	public void setPageTitle(String pageTitle) {
		pageTitleModel = new Model<String>(pageTitle);
	}

	public void setPageTitleModel(IModel<String> pageTitleModel) {
		this.pageTitleModel = pageTitleModel;
	}

	@Override
	public AkpWicketSession getSession() {
		return AkpWicketSession.get();
	}

}
