package net.plantkelt.akp.service.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.plantkelt.akp.domain.AkpSubscriptionRequest;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpLoginService;

import org.hibernate.Criteria;
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
	public AkpUser createUser(String login) {
		if (getUser(login) != null) {
			throw new IllegalArgumentException("User with login '" + login
					+ "' already exist!");
		}
		AkpUser newUser = new AkpUser();
		newUser.setLogin(login);
		newUser.setLang(AkpUser.LANG_EN);
		newUser.setName(login);
		newUser.setProfile(AkpUser.PROFILE_USER);
		newUser.setExpire(new Date());
		getSession().save(newUser);
		return newUser;
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

	@Transactional
	@Override
	public void deleteUser(AkpUser user) {
		getSession().delete(user);
	}

	@Transactional
	@Override
	public void updatePassword(AkpUser user, String newPassword) {
		user.setMd5(md5Hash(newPassword));
		updateUser(user);
	}

	@Transactional
	@Override
	public List<AkpUser> searchUser(int limit, String loginPattern,
			String namePattern, String emailPattern, Integer profile,
			Boolean onlyExpired) {
		Criteria criteria = getSession().createCriteria(AkpUser.class);
		criteria.setMaxResults(limit);
		if (loginPattern != null)
			criteria.add(Restrictions.like("login", "%" + loginPattern + "%"));
		if (namePattern != null)
			criteria.add(Restrictions.like("name", "%" + namePattern + "%"));
		if (emailPattern != null)
			criteria.add(Restrictions.like("email", "%" + emailPattern + "%"));
		if (profile != null)
			criteria.add(Restrictions.eq("profile", profile));
		if (onlyExpired != null && onlyExpired)
			criteria.add(Restrictions.lt("expire", new Date()));
		@SuppressWarnings("unchecked")
		List<AkpUser> retval = criteria.list();
		Collections.sort(retval);
		return retval;
	}

	@Override
	public boolean checkLogin(String login) {
		return login.matches("[0-9a-zA-Z_]{4,16}");
	}

	@Override
	public void subscriptionRequested(AkpSubscriptionRequest request) {

		DateFormat dateFormat = DateFormat.getDateTimeInstance(
				DateFormat.SHORT, DateFormat.MEDIUM);
		String emailText = String
				.format("Cher Roland,\n"
						+ "\n"
						+ "Je viens de recevoir à l'instant une nouvelle demande d'inscription a PLANTKELT V2!\n"
						+ "\n"
						+ "Date de la demande: %s\n"
						+ "Nom: %s\n"
						+ "Email: %s\n"
						+ "Login souhaité: %s\n"
						+ "Langue: %s\n"
						+ "Organisation: %s\n"
						+ "Activité: %s\n"
						+ "Ville: %s\n"
						+ "Pays: %s\n"
						+ "Adresse IP: %s (http://www.geoiptool.com/fr/?IP=%s)\n"
						+ "\n"
						+ "Merci de créer le compte, et d'envoyer un mail de confirmation.\n"
						+ "\n"
						+ "Votre fidèle et dévoué,\n"
						+ "\n"
						+ "                 --R2D2\n"
						+ "\n"
						+ "PS: Merci de ne pas me répondre directement, je ne comprendrai pas.\n"
						+ "Adressez-vous plutôt à mon mécanicien attitré (root@plantkelt.net).\n",
						dateFormat.format(new Date()), request.getName(),
						request.getEmail(), request.getLogin(),
						request.getLang(), request.getOrganization(),
						request.getOccupation(), request.getCity(),
						request.getState(), request.getClientIp(),
						request.getClientIp());
		String to = "melestr@plantkelt.net";
		// String to = "laurent.gregoire@gmail.com";
		String from = "r2d2@plantkelt.net";
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		javax.mail.Session session = javax.mail.Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("r2d2@plantkelt.net",
								"xxxxxx");
					}
				});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			message.setSubject(String.format(
					"Demande d'inscription a PlantKelt V2! (%s)",
					request.getName()));
			message.setText(emailText);
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getGoogleAnalyticsTrackerAccount() {
		return "UA-6000713-1";
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
