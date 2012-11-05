package net.plantkelt.akp.webapp.wicket;

import java.util.Locale;

import net.plantkelt.akp.domain.AkpUser;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

public class AkpWicketSession extends AuthenticatedWebSession {
	private static final long serialVersionUID = 1L;

	private AkpUser akpUser;
	private AkpSessionData sessionData;

	public AkpUser getAkpUser() {
		return akpUser;
	}

	public AkpSessionData getSessionData() {
		if (sessionData == null) {
			sessionData = new AkpSessionData();
			if (isAdmin())
				sessionData.setSynonymsDefaultOpen(true);
		}
		return sessionData;
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
			if (akpUser.getProfile() == AkpUser.PROFILE_ADMIN)
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

	public boolean isLoggedIn() {
		return getAkpUser() != null;
	}

	@Override
	public boolean authenticate(String username, String password) {
		akpUser = getAkpApplication().getLoginService().login(username,
				password);
		if (akpUser != null) {
			switch (akpUser.getLang()) {
			case 0:
				setLocale(new Locale("en"));
				break;
			case 1:
				setLocale(new Locale("fr"));
				break;
			case 2:
				setLocale(new Locale("br"));
				break;
			}
			return true;
		} else {
			return false;
		}
	}

	public AkpWicketApplication getAkpApplication() {
		return (AkpWicketApplication) getApplication();
	}

}
