package net.plantkelt.akp.webapp.pages;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;

public class AkpExceptionPage extends WebPage {

	private static final long serialVersionUID = 1L;

	public AkpExceptionPage(Throwable ex) {
		add(new Label("exceptionLabel", ex.getLocalizedMessage()));
		WebMarkupContainer stackTracePanel = new WebMarkupContainer(
				"exceptionStackTracePanel");
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		sw.flush();
		MultiLineLabel stackTraceLabel = new MultiLineLabel(
				"exceptionStackTraceContent", sw.toString());
		stackTracePanel.add(stackTraceLabel);
		stackTracePanel.setVisible(getApplication().usesDevelopmentConfig());
		add(stackTracePanel);
	}
}
