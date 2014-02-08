package net.plantkelt.app;

import com.beust.jcommander.JCommander;

public class AkpApplication {

	public static void main(String[] args) {

		AkpCmdLineOpts cmdLineOpts = new AkpCmdLineOpts();
		new JCommander(cmdLineOpts, args);

		if (cmdLineOpts.help) {
			usage();
			return;
		}

		AkpApplicationConfigurator config = new AkpApplicationConfigurator();
		AkpApplicationService appSrv = config.getApplicationService();

		config.start();

		if (cmdLineOpts.taxonRegexp != null) {
			appSrv.taxonRegexp(cmdLineOpts.taxonRegexp.get(0),
					cmdLineOpts.taxonRegexp.get(1), cmdLineOpts.commit);
		}

		config.stop();
	}

	private static void usage() {
		System.out
				.println("AkpApp - Copyright (c) 2014 Plantkelt.net / Laurent Gregoire & Roland Le Moigne.");
		System.out.println("");
		System.out.println("Usage:");
		System.out
				.println(" --help                         Display this help screen");
		System.out
				.println(" --taxonRegexp search replace   Search and replace taxons");
		System.out
				.println(" --commit                       Save to database (make a dryrun w/o first!)");
		System.out.println("");
		System.out.println("");
	}
}
