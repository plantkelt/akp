package net.plantkelt.akp.service;

import net.plantkelt.akp.domain.AkpUser;

public interface AkpLoginService {

	public abstract AkpUser login(String login, String password);

	public abstract void logout();
	
	public abstract AkpUser getUser(String login);
	
	public abstract void updateUser(AkpUser user);
	
}
