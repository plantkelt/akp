package net.plantkelt.akp.service;

import java.util.List;

import net.plantkelt.akp.domain.AkpSubscriptionRequest;
import net.plantkelt.akp.domain.AkpUser;

public interface AkpLoginService {

	public abstract String getAkpVersion();
	
	public abstract String getAkpTimestamp();

	public abstract AkpUser login(String login, String password);

	public abstract void logout();

	public AkpUser createUser(String login);

	public abstract AkpUser getUser(String login);

	public abstract void incUserRequestCount(AkpUser user);

	public abstract void updateUser(AkpUser user);

	public abstract void deleteUser(AkpUser user);

	public abstract void updatePassword(AkpUser user, String newPassword);

	public abstract boolean checkLogin(String login);

	public abstract List<AkpUser> searchUser(int limit, String loginPattern,
			String namePattern, String emailPattern, Integer profile,
			Boolean onlyExpired);

	public abstract void subscriptionRequested(AkpSubscriptionRequest request);

	public abstract String getGoogleAnalyticsTrackerAccount();
}
