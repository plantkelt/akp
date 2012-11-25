package net.plantkelt.akp.webapp.wicket;

import java.lang.reflect.InvocationTargetException;

import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpLogService.LoginGetter;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.webapp.pages.AkpAuthorHomePage;
import net.plantkelt.akp.webapp.pages.AkpAuthorPage;
import net.plantkelt.akp.webapp.pages.AkpBibHomePage;
import net.plantkelt.akp.webapp.pages.AkpBibPage;
import net.plantkelt.akp.webapp.pages.AkpClassPage;
import net.plantkelt.akp.webapp.pages.AkpExceptionPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitClassificationPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitCorpusPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitMainPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitSourcesPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitToolsPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitWhoPage;
import net.plantkelt.akp.webapp.pages.AkpHomePage;
import net.plantkelt.akp.webapp.pages.AkpIndexPage;
import net.plantkelt.akp.webapp.pages.AkpLangHomePage;
import net.plantkelt.akp.webapp.pages.AkpLangPage;
import net.plantkelt.akp.webapp.pages.AkpLoginPage;
import net.plantkelt.akp.webapp.pages.AkpLogoutPage;
import net.plantkelt.akp.webapp.pages.AkpPlantLogsPage;
import net.plantkelt.akp.webapp.pages.AkpPlantPage;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.inject.Injector;

public class AkpWicketApplication extends AuthenticatedWebApplication {

	private AkpLoginService loginService;

	private transient Injector injector;

	public AkpWicketApplication(Injector injector) {
		this.injector = injector;
	}

	@Override
	protected void init() {
		super.init();
		// Guice
		getComponentInstantiationListeners().add(
				new GuiceComponentInjector(this, injector));
		loginService = injector.getInstance(AkpLoginService.class);
		injector.getInstance(AkpLogService.class).setLoginGetter(
				new LoginGetter() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getCurrentLogin() {
						return AkpWicketSession.get().getAkpUser().getLogin();
					}

					@Override
					public String getCurrentRemoteAddr() {
						return ((ServletWebRequest) RequestCycle.get()
								.getRequest()).getContainerRequest()
								.getRemoteAddr();
					}
				});

		// Mount pages
		// Common
		mountPage("/", AkpIndexPage.class);
		mountPage("/home", AkpHomePage.class);
		mountPage("/login", AkpLoginPage.class);
		mountPage("/logout", AkpLogoutPage.class);
		mountPage("/badluck", AkpExceptionPage.class);
		mountPage("/class/${xid}", AkpClassPage.class);
		mountPage("/plant/${xid}", AkpPlantPage.class);
		mountPage("/plant/history/${xid}", AkpPlantLogsPage.class);
		mountPage("/bibs", AkpBibHomePage.class);
		mountPage("/bib/${xid}", AkpBibPage.class);
		mountPage("/authors", AkpAuthorHomePage.class);
		mountPage("/author/${xid}", AkpAuthorPage.class);
		mountPage("/langs", AkpLangHomePage.class);
		mountPage("/lang/${xid}", AkpLangPage.class);
		mountPage("/about", AkpFirstVisitMainPage.class);
		mountPage("/about/corpus", AkpFirstVisitCorpusPage.class);
		mountPage("/about/classification",
				AkpFirstVisitClassificationPage.class);
		mountPage("/about/sources", AkpFirstVisitSourcesPage.class);
		mountPage("/about/who", AkpFirstVisitWhoPage.class);
		mountPage("/about/tools", AkpFirstVisitToolsPage.class);

		// Look for extended browser info from client. Needed as server is in
		// UTC.
		getRequestCycleSettings().setGatherExtendedBrowserInfo(true);

		// Install custom handler for exception.
		getRequestCycleListeners().add(new AbstractRequestCycleListener() {

			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				Throwable cause = ex;
				if (cause instanceof WicketRuntimeException) {
					cause = cause.getCause();
				}
				if (cause instanceof InvocationTargetException) {
					cause = cause.getCause();
				}
				if (cause instanceof RuntimeException) {
					return new RenderPageRequestHandler(new PageProvider(
							new AkpExceptionPage(cause)));
				}
				return null;
			}
		});
	}

	@Override
	public Class<? extends WebPage> getHomePage() {
		return AkpHomePage.class;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return AkpLoginPage.class;
	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
		return AkpWicketSession.class;
	}

	public AkpLoginService getLoginService() {
		return loginService;
	}

	public Injector getInjector() {
		return injector;
	}
}
