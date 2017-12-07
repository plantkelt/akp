package net.plantkelt.akp.standalone;

import java.net.URL;
import java.util.EnumSet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.inject.servlet.GuiceFilter;

import net.plantkelt.akp.webapp.guice.AkpGuiceServletApplicationConfig;
import net.plantkelt.app.AkpCmdLineOpts;

public class EmbeddedServer {

	private static final Logger log = LoggerFactory
			.getLogger(EmbeddedServer.class);

	private AkpCmdLineOpts params;

	static {
		// Bridge Java.util.logging to SLF4J
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	public EmbeddedServer(AkpCmdLineOpts params) {
		this.params = params;
	}

	public void run() {

		log.info("Starting app server on port {} (HTTP) of interface {}",
				params.port, params.bindAddress);

		// Thread pool
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(params.minThreads);
		threadPool.setMaxThreads(params.maxThreads);
		threadPool.setIdleTimeout(60000);

		Server jettyServer = new Server(threadPool);

		// HTTP connector
		ServerConnector http = new ServerConnector(jettyServer);
		http.setPort(params.port);
		http.setHost(params.bindAddress);
		http.setIdleTimeout(30000);
		jettyServer.addConnector(http);

		ServletContextHandler context = new ServletContextHandler(jettyServer,
				"/akp", ServletContextHandler.SESSIONS);

		context.addEventListener(
				new AkpGuiceServletApplicationConfig(params.getInitParams()));
		context.addFilter(GuiceFilter.class, "/*",
				EnumSet.of(javax.servlet.DispatcherType.REQUEST,
						javax.servlet.DispatcherType.ASYNC));
		// Default session timeout to 30 sec.
		context.getSessionHandler().setMaxInactiveInterval(30 * 60);

		String someFile = "plantkelt_logo.png";
		URL resource = getClass().getClassLoader().getResource(someFile);
		if (resource == null) {
			log.error("Can't find web resources root!");
		} else {
			String webDir = resource.toExternalForm();
			webDir = webDir.substring(0, webDir.length() - someFile.length());
			log.info("Using webdir: " + webDir);
			context.setResourceBase(webDir);
		}

		context.addServlet(DefaultServlet.class, "/");

		try {
			jettyServer.start();
			jettyServer.join();
		} catch (Exception e) {
			log.error("Caught exception: " + e, e);
			try {
				jettyServer.join();
			} catch (InterruptedException e1) {
				// Ignore
			}
			jettyServer.destroy();
			throw new RuntimeException(e);
		} finally {
			jettyServer.destroy();
		}
	}
}
