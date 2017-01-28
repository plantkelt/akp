package net.plantkelt.app;

import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.service.impl.AkpLogServiceImpl;
import net.plantkelt.akp.service.impl.AkpLoginServiceImpl;
import net.plantkelt.akp.service.impl.AkpTaxonServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class AkpAppGuiceModule extends AbstractModule {

	public AkpAppGuiceModule() {
	}

	@Override
	protected void configure() {

		AkpTaxonServiceImpl akpTaxonServiceImpl = new AkpTaxonServiceImpl();
		bind(AkpLoginService.class).to(AkpLoginServiceImpl.class)
				.in(Scopes.SINGLETON);
		bind(AkpLogService.class).to(AkpLogServiceImpl.class)
				.in(Scopes.SINGLETON);
		bind(AkpTaxonService.class).toInstance(akpTaxonServiceImpl);
		bind(AkpApplicationService.class).in(Scopes.SINGLETON);
	}
}