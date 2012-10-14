package net.plantkelt.akp.service.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpLoginService;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class AkpLoginServiceImpl implements AkpLoginService {

	@Inject
	private Provider<Session> sessionProvider;

	@Transactional
	@Override
	public AkpUser login(String login, String password) {
		AkpUser user = (AkpUser) getSession().createCriteria(AkpUser.class)
				.add(Restrictions.eq("login", login)).uniqueResult();
		if (user.getExpire().compareTo(new Date()) < 0) {
			// User account expired
			return null;
		}
		String passwordHash = hash(password);
		System.err.println("*** password:    [" + password + "]");
		System.err.println("*** computed hash:" + passwordHash);
		System.err.println("*** account hash :" + user.getMd5());
		if (!passwordHash.equals(user.getMd5())) {
			// Invalid password
			return null;
		}
		return user;
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
			e.printStackTrace();
			return "" + data.hashCode();
		}
	}

	private String hash(String content) {
		try {
			return hash(content.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return content; // Should not happen
		}
	}

}
