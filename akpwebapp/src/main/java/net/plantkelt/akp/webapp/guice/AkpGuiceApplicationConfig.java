package net.plantkelt.akp.webapp.guice;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.plantkelt.akp.service.guice.AkpServiceGuiceModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;

public class AkpGuiceApplicationConfig extends GuiceServletContextListener {

	private ServletContext servletContext;

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
	}

	@Override
	protected Injector getInjector() {

		final String[] DB_PARAMS_NAMES = { "javax.persistence.jdbc.url",
				"javax.persistence.jdbc.user",
				"javax.persistence.jdbc.password" };

		// Deployment or development ?
		String releaseConfiguration = servletContext
				.getInitParameter("net.plantkelt.akp.configuration");
		System.setProperty("wicket.configuration", releaseConfiguration);
		boolean dev = "development".equals(releaseConfiguration);

		// JPA configuration
		Properties jpaProperties = new Properties();
		for (String paramName : DB_PARAMS_NAMES) {
			String paramValue = servletContext.getInitParameter(paramName);
			if (paramValue == null)
				throw new IllegalArgumentException("Param '" + paramName
						+ "' must be configured as <context-param>!");
			jpaProperties.put(paramName, paramValue);
		}
		JpaPersistModule jpaPersistModule = new JpaPersistModule("akpJpaUnit");
		jpaPersistModule.properties(jpaProperties);

		return Guice.createInjector(new AkpGuiceHibernateModule(),
				new AkpWebappGuiceServletModule(), new AkpServiceGuiceModule(
						null, dev), new ProvideHibernateSessionModule(),
				jpaPersistModule);
	}
}
