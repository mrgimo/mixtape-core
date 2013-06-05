package ch.hsr.mixtape.application.service;

import ch.hsr.mixtape.model.SystemStatus;

/**
 * This service is responsible for handling system status queries.
 * 
 * @author Stefan Derungs
 */
public class SystemService {

	private SystemStatus systemStatus;
	
	public SystemService() {
		systemStatus = new SystemStatus();
	}

	public SystemStatus getSystemStatus() {
		return systemStatus;
	}
	
}
