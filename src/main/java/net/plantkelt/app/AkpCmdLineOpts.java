package net.plantkelt.app;

import java.util.List;

import com.beust.jcommander.Parameter;

public class AkpCmdLineOpts {

	@Parameter(names = "--taxonRegexp", arity = 2, description = "Taxon search/replace regexps")
	protected List<String> taxonRegexp;

	@Parameter(names = "--commit", description = "Save to database")
	protected boolean commit = false;

	@Parameter(names = "--help", help = true)
	protected boolean help;

}
