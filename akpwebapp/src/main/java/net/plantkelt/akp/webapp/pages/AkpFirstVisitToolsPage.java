package net.plantkelt.akp.webapp.pages;

import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebApplication;

public class AkpFirstVisitToolsPage extends AkpFirstVisitPage {

	private static final long serialVersionUID = 1L;

	public AkpFirstVisitToolsPage() {

		String version = null;
		try {
			ServletContext application = WebApplication.get()
					.getServletContext();
			InputStream inputStream = application
					.getResourceAsStream("/WEB-INF/classes/akp.properties");
			if (inputStream != null) {
				Properties props = new Properties();
				props.load(inputStream);
				version = props.getProperty("build.time");
			} else {
				version = "[akp.properties not found]";
			}
		} catch (Exception e) {
			version = e.getMessage();
		}
		add(new Label("buildTime", version == null ? "?" : version));
	}
}
