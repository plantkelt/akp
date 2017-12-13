package net.plantkelt.akp.webapp.wicket;

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.domain.AkpUserRoles;

public class AkpWicketSession extends AuthenticatedWebSession {
	private static final long serialVersionUID = 1L;

	private AkpUser akpUser;
	private AkpSessionData sessionData;
	private static Roles adminRoles;

	public AkpUser getAkpUser() {
		return akpUser;
	}

	public AkpSessionData getSessionData() {
		if (sessionData == null) {
			sessionData = new AkpSessionData();
			if (hasRole(AkpUserRoles.ROLE_ADMIN))
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
		Roles roles = new Roles();
		if (isSignedIn()) {
			if (akpUser.getProfile() == AkpUser.PROFILE_ADMIN) {
				// Profile ADMIN
				if (adminRoles == null) {
					adminRoles = new Roles();
					adminRoles.addAll(AkpUserRoles.allRoles());
					adminRoles.add(AkpUserRoles.ROLE_ADMIN);
				}
				roles = adminRoles;
			} else {
				// Profile USER
				for (String role : akpUser.getRoles()) {
					roles.add(role);
				}
			}
		}
		return roles;
	}

	public boolean hasRole(String role) {
		AkpUser akpUser = getAkpUser();
		if (akpUser == null)
			return false;
		return akpUser.hasRole(role);
	}

	/**
	 * @return true if somebody is logged-in, and has ONE OF the given roles.
	 */
	public boolean hasRole(String... roles) {
		AkpUser akpUser = getAkpUser();
		if (akpUser == null)
			return false;
		return akpUser.hasRole(roles);
	}

	/**
	 * @return true if somebody is logged-in, and has right for the lang, and
	 *         has ONE OF the given roles.
	 */
	public boolean hasRole(AkpLang lang, String... roles) {
		AkpUser akpUser = getAkpUser();
		if (akpUser == null)
			return false;
		return akpUser.hasRole(lang, roles);
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
			case AkpUser.LANG_EN:
				setLocale(new Locale("en"));
				break;
			case AkpUser.LANG_FR:
				setLocale(new Locale("fr"));
				break;
			case AkpUser.LANG_BR:
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
