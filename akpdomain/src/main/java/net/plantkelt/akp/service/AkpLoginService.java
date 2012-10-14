package net.plantkelt.akp.service;

import net.plantkelt.akp.domain.AkpUser;

public interface AkpLoginService {

	public abstract AkpUser login(String login, String password);
}
