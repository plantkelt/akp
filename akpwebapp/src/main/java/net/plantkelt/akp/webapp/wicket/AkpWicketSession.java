package net.plantkelt.akp.webapp.wicket;

import net.plantkelt.akp.domain.AkpUser;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

public class AkpWicketSession extends AuthenticatedWebSession {
	private static final long serialVersionUID = 1L;

	private AkpUser akpUser;

	public AkpUser getAkpUser() {
		return akpUser;
	}

	public void autologin(AkpUser utilisateur) {
		this.akpUser = utilisateur;
		this.signIn(true);
	}

	public static AkpWicketSession get() {
		return (AkpWicketSession) Session.get();
	}

	public AkpWicketSession(Request request) {
		super(request);
	}

	public Roles getRoles() {
		if (isSignedIn()) {
			// TODO make generic
			if (akpUser.getProfile() == 3)
				return new Roles(new String[] { "USER", "ADMIN" });
			else
				return new Roles("USER");
		}
		return new Roles();
	}

	public boolean isAdmin() {
		if (getAkpUser() == null)
			return false;
		return getAkpUser().isAdmin();
	}

	@Override
	public boolean authenticate(String username, String password) {
		akpUser = getAkpApplication().getLoginService().login(username,
				password);
		return akpUser != null;
	}

	public AkpWicketApplication getAkpApplication() {
		return (AkpWicketApplication) getApplication();
	}

}
