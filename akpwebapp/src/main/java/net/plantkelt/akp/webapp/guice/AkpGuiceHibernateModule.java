package net.plantkelt.akp.webapp.guice;

import com.google.inject.AbstractModule;

public class AkpGuiceHibernateModule extends AbstractModule {

	@Override
	protected void configure() {
		// Hibernate DAO factory
		// bind(GenericDaoFactory.class).in(Scopes.SINGLETON);
		// bind(new TypeLiteral<HibernateGenericDao<?>>() {
		// });
		// Factories
		// bind(ISiteFactory.class).to(HibernateSiteFactory.class).in(
		// Scopes.SINGLETON);
	}
}
