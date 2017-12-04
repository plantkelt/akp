package net.plantkelt.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;

import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpLogService.LoginGetter;
import net.plantkelt.akp.service.guice.AkpServiceGuiceModule;
import net.plantkelt.akp.service.guice.ProvideHibernateSessionModule;

public class AkpApplicationConfigurator {

	private Injector injector;
	private PersistService persistService;

	public AkpApplicationConfigurator(AkpCmdLineOpts params) {

		Properties jpaProperties = new Properties();
		jpaProperties.put("javax.persistence.jdbc.url", params.jdbcUrl);
		jpaProperties.put("javax.persistence.jdbc.user", params.jdbcUser);
		jpaProperties.put("javax.persistence.jdbc.password",
				params.jdbcPassword);
		JpaPersistModule jpaPersistModule = new JpaPersistModule("akpJpaUnit");
		jpaPersistModule.properties(jpaProperties);

		Map<String, String> initParams = new HashMap<>();
		initParams.put("net.plantkelt.akp.configuration",
				params.development ? "development" : "deployment");
		initParams.put("net.plantkelt.akp.logfile", params.logConfiguration);
		initParams.put("net.plantkelt.akp.static-index-location",
				params.staticIndexLocation);
		initParams.put("net.plantkelt.akp.smtp.host", params.smtpHost);
		initParams.put("net.plantkelt.akp.smtp.port", "" + params.smtpPort);
		initParams.put("net.plantkelt.akp.smtp.login", params.smtpLogin);
		initParams.put("net.plantkelt.akp.smtp.password", params.smtpPassword);
		initParams.put("net.plantkelt.akp.smtp.to", params.smtpTo);

		AkpServiceGuiceModule akpServiceGuiceModule = new AkpServiceGuiceModule(
				null, false);
		akpServiceGuiceModule.setInitParameters(initParams);

		injector = Guice.createInjector(new ProvideHibernateSessionModule(),
				jpaPersistModule, akpServiceGuiceModule);
		persistService = injector.getInstance(PersistService.class);
	}

	public void start() {
		persistService.start();
		AkpLogService akpLogService = injector.getInstance(AkpLogService.class);
		akpLogService.setLoginGetter(new LoginGetter() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getCurrentRemoteAddr() {
				return "localhost";
			}

			@Override
			public String getCurrentLogin() {
				return "r2d2";
			}
		});
	}

	public void stop() {
		persistService.stop();
	}

	public AkpApplicationService getApplicationService() {
		return injector.getInstance(AkpApplicationService.class);
	}

}
