package net.plantkelt.akp.webapp.guice;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;

import com.google.inject.persist.PersistFilter;
import com.google.inject.servlet.ServletModule;

public class AkpWebappGuiceServletModule extends ServletModule {

	public AkpWebappGuiceServletModule() {
	}

	@Override
	protected void configureServlets() {
		// Transaction filter
		filter("/*").through(PersistFilter.class);

		// Wicket
		bind(WebApplication.class).toProvider(
				AkpWicketApplicationProvider.class);
		Map<String, String> params = new HashMap<String, String>();
		params.put(WicketFilter.FILTER_MAPPING_PARAM, "/web/*");
		filter("/web/*").through(WicketGuiceFilter.class, params);

	}
}
