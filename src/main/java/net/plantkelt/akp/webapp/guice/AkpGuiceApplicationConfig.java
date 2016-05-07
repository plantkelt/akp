package net.plantkelt.akp.webapp.guice;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.plantkelt.akp.service.guice.AkpServiceGuiceModule;
import net.plantkelt.akp.service.guice.ProvideHibernateSessionModule;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

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

		// Logging configuration
		Logger logger = null;
		String log4jFile = servletContext
				.getInitParameter("net.plantkelt.akp.logfile");
		if (log4jFile != null) {
			Properties properties = new Properties();
			try {
				properties.load(new FileInputStream(log4jFile));
				PropertyConfigurator.configure(log4jFile);
				logger = LogManager.getLogger(this.getClass().getName());
				logger.info("Configured logging using: " + log4jFile);
			} catch (IOException e) {
				System.out
						.println("Cannot configure log4j using provided file: "
								+ log4jFile + ":" + e.getLocalizedMessage());
			}
		}

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

		AkpServiceGuiceModule akpServiceGuiceModule = new AkpServiceGuiceModule(
				null, dev);
		// Init parameters
		akpServiceGuiceModule
				.setInitParameters(marshalInitParameters(servletContext));

		return Guice.createInjector(new AkpGuiceHibernateModule(),
				new AkpWebappGuiceServletModule(), akpServiceGuiceModule,
				new ProvideHibernateSessionModule(), jpaPersistModule);
	}

	private Map<String, String> marshalInitParameters(
			ServletContext servletContext) {
		Map<String, String> retval = new HashMap<>();
		@SuppressWarnings("unchecked")
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
