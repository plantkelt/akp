package net.plantkelt.akp.service;

import java.util.List;

import net.plantkelt.akp.domain.AkpSubscriptionRequest;
import net.plantkelt.akp.domain.AkpUser;

public interface AkpLoginService {

	public abstract AkpUser login(String login, String password);

	public abstract void logout();

	public abstract AkpUser getUser(String login);

	public abstract void updateUser(AkpUser user);

	public abstract boolean checkLogin(String login);

	public abstract List<AkpUser> searchUser(int limit, String loginPattern,
			String namePattern, String emailPattern, Integer profile);

	public abstract void subscriptionRequested(AkpSubscriptionRequest request);

}
