package net.plantkelt.akp.service.impl;

import java.util.List;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.service.AkpLogService;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class AkpLogServiceImpl implements AkpLogService {

	@Inject
	private Provider<Session> sessionProvider;

	private Session getSession() {
		return sessionProvider.get();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AkpLogEntry> getPlantLogs(Integer plantId) {
		return (List<AkpLogEntry>) getSession()
				.createCriteria(AkpLogEntry.class)
				.add(Restrictions.eq("plantId", plantId))
				.addOrder(Order.desc("date")).list();
	}

}
