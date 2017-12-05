package net.plantkelt.akp.webapp.guice;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import net.plantkelt.akp.service.guice.AkpServiceGuiceModule;
import net.plantkelt.akp.service.guice.ProvideHibernateSessionModule;

public class AkpGuiceApplicationConfig {

	private Map<String, String> initParams;

	public AkpGuiceApplicationConfig(Map<String, String> initParams) {
		this.initParams = initParams;
	}

	public Injector getInjector() {

		final String[] DB_PARAMS_NAMES = { "javax.persistence.jdbc.url",
				"javax.persistence.jdbc.user",
				"javax.persistence.jdbc.password" };

		// Deployment or development ?
		String releaseConfiguration = initParams
				.get("net.plantkelt.akp.configuration");
		System.setProperty("wicket.configuration", releaseConfiguration);
		boolean dev = "development".equals(releaseConfiguration);

		// Logging configuration
		Logger logger = null;
		String log4jFile = initParams.get("net.plantkelt.akp.logfile");
		if (log4jFile != null) {
			Properties properties = new Properties();
			try {
				properties.load(new FileInputStream(log4jFile));
				BasicConfigurator.resetConfiguration();
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
			String paramValue = initParams.get(paramName);
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
		akpServiceGuiceModule.setInitParameters(initParams);

		return Guice.createInjector(new AkpGuiceHibernateModule(),
				new AkpWebappGuiceServletModule(), akpServiceGuiceModule,
				new ProvideHibernateSessionModule(), jpaPersistModule);
	}
}
