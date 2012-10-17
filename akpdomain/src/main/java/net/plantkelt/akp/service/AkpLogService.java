package net.plantkelt.akp.service;

import java.util.List;

import net.plantkelt.akp.domain.AkpLogEntry;
import net.plantkelt.akp.domain.AkpPlant;

public interface AkpLogService {

	public interface LoginGetter {
		public String getCurrentLogin();
	}

	public abstract void setLoginGetter(LoginGetter loginGetter);

	public abstract List<AkpLogEntry> getPlantLogs(Integer plantId);

	public abstract void logPlantCreation(AkpPlant plant);

	public abstract void logPlantDeletion(AkpPlant plant);
}
