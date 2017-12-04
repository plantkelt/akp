package net.plantkelt.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.validators.PositiveInteger;

public class AkpCmdLineOpts {

	private static final String DEFAULT_JDBC_URL = "jdbc:postgresql://localhost:5432/akp";
	private static final String DEFAULT_JDBC_USER = "akp";
	private static final String DEFAULT_JDBC_PASSWORD = "";
	private static final int DEFAULT_PORT = 8080;

	@Parameter(names = "--taxonRegexp", arity = 2, description = "Taxon search/replace regexps")
	protected List<String> taxonRegexp;

	@Parameter(names = "--commit", description = "Save to database")
	protected boolean commit = false;

	@Parameter(names = "--help", help = true, description = "Print this help and exit.")
	protected boolean help = false;

	@Parameter(names = "--server", description = "Run webapp in an embedded Jetty server.")
	protected boolean server = false;

	@Parameter(names = { "--jdbcUrl" }, description = "Database JDBC url.")
	public String jdbcUrl = DEFAULT_JDBC_URL;

	@Parameter(names = { "--jdbcUser" }, description = "Database JDBC user.")
	public String jdbcUser = DEFAULT_JDBC_USER;

	@Parameter(names = {
			"--jdbcPassword" }, description = "Database JDBC password.")
	public String jdbcPassword = DEFAULT_JDBC_PASSWORD;

	@Parameter(names = {
			"--bindAddress" }, description = "Specify which network interface to bind to. 0.0.0.0 means all interfaces.")
	public String bindAddress = "0.0.0.0";

	@Parameter(names = {
			"--port" }, validateWith = AvailablePort.class, description = "Server port.")
	public Integer port = DEFAULT_PORT;

	@Parameter(names = {
			"--minThreads" }, description = "Minimum number of Jetty threads in pool.")
	public Integer minThreads = Runtime.getRuntime().availableProcessors();

	@Parameter(names = {
			"--maxThreads" }, description = "Maximum number of Jetty threads in pool.")
	public Integer maxThreads = Runtime.getRuntime().availableProcessors() * 2;

	@Parameter(names = {
			"--logConfiguration" }, description = "Log (4j) properties configuration file to load.")
	public String logConfiguration = null;

	@Parameter(names = {
			"--development" }, description = "True to enable development mode.")
	public boolean development = false;

	@Parameter(names = {
			"--staticIndexLocation" }, description = "Location of static index files.")
	public String staticIndexLocation = "/var/www/akp/static/";

	@Parameter(names = { "--smtpHost" }, description = "SMTP host.")
	public String smtpHost = "smtp.gmail.com";

	@Parameter(names = { "--smtpPort" }, description = "SMTP port.")
	public int smtpPort = 587;

	@Parameter(names = { "--smtpLogin" }, description = "SMTP login.")
	public String smtpLogin = "r2d2@plantkelt.net";

	@Parameter(names = { "--smtpPassword" }, description = "SMTP password.")
	public String smtpPassword = ""; // Must be set, but allow to start w/o

	@Parameter(names = {
			"--smtpTo" }, description = "Email address to send admin emails to")
	public String smtpTo = "root@localhost";

	public static class AvailablePort implements IParameterValidator {

		@Override
		public void validate(String name, String value)
				throws ParameterException {
			new PositiveInteger().validate(name, value);
			int port = Integer.parseInt(value);
			this.validate(port);
		}

		public void validate(int port) throws ParameterException {
			ServerSocket socket = null;
			boolean portUnavailable = false;
			String reason = null;
			try {
				socket = new ServerSocket(port);
			} catch (IOException e) {
				portUnavailable = true;
				reason = e.getMessage();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// will not be thrown
					}
				}
			}
			if (portUnavailable) {
				String msg = String.format(": port %d is not available. %s.",
						port, reason);
				throw new ParameterException(msg);
			}
		}
	}

	public Map<String, String> getInitParams() {
		Map<String, String> initParams = new HashMap<>();
		initParams.put("javax.persistence.jdbc.url", this.jdbcUrl);
		initParams.put("javax.persistence.jdbc.user", this.jdbcUser);
		initParams.put("javax.persistence.jdbc.password", this.jdbcPassword);
		initParams.put("net.plantkelt.akp.configuration",
				this.development ? "development" : "deployment");
		initParams.put("net.plantkelt.akp.logfile", this.logConfiguration);
		initParams.put("net.plantkelt.akp.static-index-location",
				this.staticIndexLocation);
		initParams.put("net.plantkelt.akp.smtp.host", this.smtpHost);
		initParams.put("net.plantkelt.akp.smtp.port", "" + this.smtpPort);
		initParams.put("net.plantkelt.akp.smtp.login", this.smtpLogin);
		initParams.put("net.plantkelt.akp.smtp.password", this.smtpPassword);
		initParams.put("net.plantkelt.akp.smtp.to", this.smtpTo);
		return initParams;
	}
}
