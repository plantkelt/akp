package net.plantkelt.akp.webapp.components;

import org.apache.wicket.markup.html.WebMarkupContainer;

import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

public class RoleVisibleMarkupContainer extends WebMarkupContainer {
	private static final long serialVersionUID = 1L;

	private String role;

	public RoleVisibleMarkupContainer(String id, String role) {
		super(id);
		this.role = role;
	}

	@Override
	public boolean isVisible() {
		return AkpWicketSession.get().hasRole(role);
	}
}
