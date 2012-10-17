package net.plantkelt.akp.service;

import java.util.List;

import net.plantkelt.akp.domain.AkpLogEntry;

public interface AkpLogService {

	public abstract List<AkpLogEntry> getPlantLogs(Integer plantId);
	
}
