package net.plantkelt.akp.webapp.pages;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.webapp.elements.AkpHeaderPanel;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

public class AkpPageTemplate extends WebPage {

	private static final long serialVersionUID = 1L;

	@Inject
	AkpLoginService akpLoginService;

	public AkpPageTemplate() {
		super();
		init();
	}

	public AkpPageTemplate(PageParameters parameters) {
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
		add(new AkpHeaderPanel("headerBar"));
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				AkpPageTemplate.class, "res/akp.css")));
		response.render(JavaScriptHeaderItem
				.forUrl("http://www.google-analytics.com/ga.js"));
		response.render(JavaScriptHeaderItem.forScript(
				String.format(
						"var pageTracker = _gat._getTracker('%s'); pageTracker._trackPageview();",
						akpLoginService.getGoogleAnalyticsTrackerAccount()),
				"googleAnalyticsTracker"));
	}

	public AkpUser getUtilisateur() {
		return AkpWicketSession.get().getAkpUser();
	}

	public AkpWicketSession getSession() {
		return AkpWicketSession.get();
	}

}
