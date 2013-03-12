package net.plantkelt.akp.service.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpSubscriptionRequest;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpLoginService;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class AkpLoginServiceImpl implements AkpLoginService {

	@Inject
	private Provider<Session> sessionProvider;

	@Inject
	private AkpLogService akpLogService;

	@Transactional
	@Override
	public AkpUser login(String login, String password) {
		AkpUser user = (AkpUser) getSession().createCriteria(AkpUser.class)
				.add(Restrictions.eq("login", login)).uniqueResult();
		if (user == null) {
			return null; // User not found
		}
		if (user.getExpire().compareTo(new Date()) < 0) {
			return null; // User account expired
		}
		if (!md5Hash(password).equals(user.getMd5())) {
			return null; // Invalid password
		}
		akpLogService.userLogLogin(login);
		return user;
	}

	@Transactional
	@Override
	public void logout() {
		akpLogService.userLogLogout();
	}

	@Transactional
	@Override
	public AkpUser getUser(String login) {
		return (AkpUser) getSession().get(AkpUser.class, login);
	}

	@Transactional
	@Override
	public void updateUser(AkpUser user) {
		getSession().update(user);
	}

	@Override
	public boolean checkLogin(String login) {
		return login.matches("[0-9a-zA-Z_]{4,16}");
	}

	@Override
	public void subscriptionRequested(AkpSubscriptionRequest request) {
		// TODO Send email
	}

	private Session getSession() {
		return sessionProvider.get();
	}

	private String hash(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data);
			byte[] raw = md.digest();
			BigInteger bi = new BigInteger(1, raw);
			return String.format("%0" + (raw.length << 1) + "x", bi);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private String md5Hash(String content) {
		try {
			return hash(content.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
