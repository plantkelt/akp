package net.plantkelt.akp.service.guice;

import java.util.Properties;

import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.service.impl.AkpLogServiceImpl;
import net.plantkelt.akp.service.impl.AkpLoginServiceImpl;
import net.plantkelt.akp.service.impl.AkpTaxonServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class AkpServiceGuiceModule extends AbstractModule {

	@SuppressWarnings("unused")
	private Properties properties;
	private boolean dev;
	private String staticIndexLocation = "";

	public AkpServiceGuiceModule(Properties properties, boolean dev) {
		this.properties = properties;
		this.dev = dev;
	}

	@Override
	protected void configure() {
		// Config
		bindConstant().annotatedWith(Names.named("dev")).to(dev);
		bind(AkpLoginService.class).to(AkpLoginServiceImpl.class).in(
				Scopes.SINGLETON);
		bind(AkpLogService.class).to(AkpLogServiceImpl.class).in(
				Scopes.SINGLETON);
		AkpTaxonServiceImpl akpTaxonServiceImpl = new AkpTaxonServiceImpl();
		akpTaxonServiceImpl.setStaticIndexLocation(staticIndexLocation);
		bind(AkpTaxonService.class).toInstance(akpTaxonServiceImpl);
	}

	public void setStaticIndexLocation(String staticIndexLocation) {
		this.staticIndexLocation = staticIndexLocation;
	}
}