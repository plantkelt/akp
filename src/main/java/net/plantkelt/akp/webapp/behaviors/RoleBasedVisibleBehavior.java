package net.plantkelt.akp.webapp.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;

import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

public class RoleBasedVisibleBehavior extends Behavior {
	private static final long serialVersionUID = 1L;

	public String[] roles;

	public RoleBasedVisibleBehavior() {
		this.roles = null;
	}

	public RoleBasedVisibleBehavior(String... roles) {
		this.roles = roles;
	}

	@Override
	public void onConfigure(Component component) {
		if (roles == null || roles.length == 0) {
			/*
			 * No roles provided, or empty roles set: just check if user is
			 * logged-in
			 */
			if (!AkpWicketSession.get().isLoggedIn()) {
				component.setVisible(false);
			}
		} else if (!AkpWicketSession.get().hasRole(roles)) {
			/* Check if logged-in user has one role of the provided */
			component.setVisible(false);
		}
	}
}
