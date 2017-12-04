package net.plantkelt.akp.webapp.guice;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class AkpGuiceServletApplicationConfig
		extends GuiceServletContextListener {

	private Map<String, String> initParams;

	/**
	 * To be used within a webapp container.
	 */
	public AkpGuiceServletApplicationConfig() {
		this.initParams = null;
	}

	/**
	 * To be used with an embedded webapp server.
	 */
	public AkpGuiceServletApplicationConfig(Map<String, String> initParams) {
		this.initParams = initParams;
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		if (this.initParams == null) {
			this.initParams = marshalInitParameters(
					servletContextEvent.getServletContext());
		}
		super.contextInitialized(servletContextEvent);
	}

	@Override
	protected Injector getInjector() {
		AkpGuiceApplicationConfig appConfig = new AkpGuiceApplicationConfig(
				initParams);
		return appConfig.getInjector();
	}

	private Map<String, String> marshalInitParameters(
			ServletContext servletContext) {
		Map<String, String> retval = new HashMap<>();
		Enumeration<String> e = servletContext.getInitParameterNames();
		while (e.hasMoreElements()) {
			String parameterName = e.nextElement();
			String parameterValue = servletContext
					.getInitParameter(parameterName);
			retval.put(parameterName, parameterValue);
		}
		return retval;
	}
}
