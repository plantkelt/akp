package net.plantkelt.akp.webapp.wicket;

import java.lang.reflect.InvocationTargetException;

import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.webapp.pages.AkpExceptionPage;
import net.plantkelt.akp.webapp.pages.AkpHomePage;
import net.plantkelt.akp.webapp.pages.AkpIndexPage;
import net.plantkelt.akp.webapp.pages.AkpLoginPage;
import net.plantkelt.akp.webapp.pages.AkpLogoutPage;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;

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

		// Mount pages
		// Common
		mountPage("/", AkpIndexPage.class);
		mountPage("/home", AkpHomePage.class);
		mountPage("/login", AkpLoginPage.class);
		mountPage("/logout", AkpLogoutPage.class);

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

		// getResourceSettings().getStringResourceLoaders().add(0,
		// new MecatrappStringResourceLoader());
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
