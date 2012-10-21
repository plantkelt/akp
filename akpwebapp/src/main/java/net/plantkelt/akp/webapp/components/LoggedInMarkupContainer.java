package net.plantkelt.akp.webapp.components;

import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.markup.html.WebMarkupContainer;

public class LoggedInMarkupContainer extends WebMarkupContainer {
	private static final long serialVersionUID = 1L;

	public LoggedInMarkupContainer(String id) {
		super(id);
	}

	@Override
	public boolean isVisible() {
		return AkpWicketSession.get().isLoggedIn();
	}
}
