package net.plantkelt.akp.service.guice;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Session;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * See: http://groups.google.com/group/google-guice/browse_thread/thread/
 * bbb9a48e4faee182
 */
public class ProvideHibernateSessionModule extends AbstractModule {

	@Override
	protected void configure() {
	}

	@Provides
	@Inject
	protected Session providesHibernateSession(EntityManager entityManager) {
		return (Session) entityManager.getDelegate();
	}
}