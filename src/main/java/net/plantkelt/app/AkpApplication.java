package net.plantkelt.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

import net.plantkelt.akp.standalone.EmbeddedServer;

public class AkpApplication {

	private static final Logger log = LoggerFactory
			.getLogger(AkpApplication.class);

	public static void main(String[] args) {

		/* Print version */
		String applicationVersion = AkpApplication.class.getPackage()
				.getImplementationVersion();
		if (applicationVersion == null)
			applicationVersion = "dev";
		System.out.println("Akp version " + applicationVersion);
		System.out.println(
				"Copyright (c) 2014,2017 Plantkelt.bzh / Laurent Gregoire & Roland Le Moigne.");

		AkpCmdLineOpts params = new AkpCmdLineOpts();
		JCommander jcmd = new JCommander(params);
		jcmd.parse(args);

		if (params.help) {
			// What?
		} else if (params.server) {
			runServer(params);
		} else {
			runTools(params);
		}
	}

	private static void runServer(AkpCmdLineOpts params) {
		// Start server
		EmbeddedServer jettyServer = new EmbeddedServer(params);
		// Loop to restart server on uncaught exceptions
		while (true) {
			try {
				jettyServer.run();
				return;
			} catch (Throwable throwable) {
				log.error("An uncaught {} occurred. Restarting server.",
						throwable.getClass().getSimpleName(), throwable);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private static void runTools(AkpCmdLineOpts params) {

		AkpApplicationConfigurator config = new AkpApplicationConfigurator(
				params);
		AkpApplicationService appSrv = config.getApplicationService();
		config.start();
		if (params.taxonRegexp != null) {
			appSrv.taxonRegexp(params.taxonRegexp.get(0),
					params.taxonRegexp.get(1), params.commit);
		}
		config.stop();
	}
}
