package net.plantkelt.akp.service.guice;

import java.util.Map;
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
	private Map<String, String> initParameters;

	private static final String[] STRING_INIT_PARAMS = {
			"net.plantkelt.akp.static-index-location",
			"net.plantkelt.akp.smtp.host", "net.plantkelt.akp.smtp.login",
			"net.plantkelt.akp.smtp.password", "net.plantkelt.akp.smtp.to" };

	private static final String[] INTEGER_INIT_PARAMS = {
			"net.plantkelt.akp.smtp.port" };

	public AkpServiceGuiceModule(Properties properties, boolean dev) {
		this.properties = properties;
		this.dev = dev;
	}

	@Override
	protected void configure() {
		// Config
		bindConstant().annotatedWith(Names.named("dev")).to(dev);
		bindConstants(String.class, STRING_INIT_PARAMS, STRING_CONVERTER);
		bindConstants(Integer.class, INTEGER_INIT_PARAMS, INTEGER_CONVERTER);
		bind(AkpLoginService.class).to(AkpLoginServiceImpl.class)
				.in(Scopes.SINGLETON);
		bind(AkpLogService.class).to(AkpLogServiceImpl.class)
				.in(Scopes.SINGLETON);
		bind(AkpTaxonService.class).to(AkpTaxonServiceImpl.class)
				.in(Scopes.SINGLETON);
	}

	public void setInitParameters(Map<String, String> initParameters) {
		this.initParameters = initParameters;
	}

	private interface Converter<T> {
		T convert(String str);
	}

	private static Converter<String> STRING_CONVERTER = new Converter<String>() {
		@Override
		public String convert(String str) {
			return str;
		}
	};

	private static Converter<Integer> INTEGER_CONVERTER = new Converter<Integer>() {
		@Override
		public Integer convert(String str) {
			return str == null ? null : Integer.parseInt(str);
		}
	};

	private <T> void bindConstants(Class<T> clazz, String[] paramNames,
			Converter<T> converter) {
		for (String initParamName : paramNames) {
			if (!initParamName.startsWith("net.plantkelt.akp."))
				throw new IllegalArgumentException(
						"Bad init param name: " + initParamName);
			T paramValue = converter.convert(initParameters.get(initParamName));
			if (paramValue == null)
				throw new IllegalArgumentException(
						"Missing init parameter: " + initParamName);
			String bindName = initParamName.replace("net.plantkelt.akp.", "");
			bind(clazz).annotatedWith(Names.named(bindName))
					.toInstance(paramValue);
		}
	}
}