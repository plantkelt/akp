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

	public AkpApplicationConfigurator() {

		Properties jpaProperties = new Properties();
		jpaProperties.put("javax.persistence.jdbc.url",
				"jdbc:postgresql://localhost:5432/akp");
		jpaProperties.put("javax.persistence.jdbc.user", "akp");
		jpaProperties.put("javax.persistence.jdbc.password", "");
		JpaPersistModule jpaPersistModule = new JpaPersistModule("akpJpaUnit");
		jpaPersistModule.properties(jpaProperties);

		// TODO Read this parameters from somewhere
		Map<String, String> initParams = new HashMap<>();
		initParams.put("net.plantkelt.akp.static-index-location",
				"/var/www/akp/static/");
		initParams.put("net.plantkelt.akp.smtp.host", "smtp.gmail.com");
		initParams.put("net.plantkelt.akp.smtp.port", "587");
		initParams.put("net.plantkelt.akp.smtp.login", "r2d2@plantkelt.net");
		initParams.put("net.plantkelt.akp.smtp.password", "xxxx");
		initParams.put("net.plantkelt.akp.smtp.to", "melestr@plantkelt.bzh");
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
