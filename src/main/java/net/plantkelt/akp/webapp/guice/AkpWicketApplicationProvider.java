package net.plantkelt.akp.webapp.guice;

import javax.inject.Inject;

import net.plantkelt.akp.webapp.wicket.AkpWicketApplication;

import org.apache.wicket.protocol.http.WebApplication;

import com.google.inject.Injector;
import com.google.inject.Provider;

public class AkpWicketApplicationProvider implements Provider<WebApplication> {

	private final Injector injector;

	@Inject
	public AkpWicketApplicationProvider(Injector injector) {
		this.injector = injector;
	}

	@Override
	public WebApplication get() {
		return new AkpWicketApplication(injector);
	}
}
