package net.plantkelt.app;

import java.util.Properties;

import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpLogService.LoginGetter;
import net.plantkelt.akp.service.guice.ProvideHibernateSessionModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;

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

		AkpAppGuiceModule akpAppGuiceModule = new AkpAppGuiceModule();

		injector = Guice.createInjector(new ProvideHibernateSessionModule(),
				akpAppGuiceModule, jpaPersistModule);
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
