package net.plantkelt.akp.webapp.components;

import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.markup.html.WebMarkupContainer;

public class AdminMarkupContainer extends WebMarkupContainer {
	private static final long serialVersionUID = 1L;

	public AdminMarkupContainer(String id) {
		super(id);
	}

	@Override
	public boolean isVisible() {
		return AkpWicketSession.get().isAdmin();
	}
}
