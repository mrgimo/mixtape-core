package ch.hsr.mixtape.application.service;

import java.lang.management.ManagementFactory;

/**
 * This service is responsible for handling system status queries.
 * 
 * @author Stefan Derungs
 */
public class SystemService {

	/**
	 * Get the number of available cores in the system.
	 */
	public int getNumberOfCores() {
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * Get the reserved memory for the system in megabytes.
	 */
	public long getAvailableMemory() {
		return Runtime.getRuntime().totalMemory() / 1048576;
	}

	/**
	 * Get the current system load in percent.
	 */
	public double getCurrentSystemLoad() {
		// TODO
		return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
	}

	/**
	 * Get the size in megabytes of the current database.
	 */
	public float getDatabaseSize() {
		return 0; // TODO
	}

	/**
	 * Get the number of songs in database.
	 */
	public int getTotalNumberOfSongs() {
		return 0;
	}

	/**
	 * Get number of analyzed songs in database.
	 */
	public int getNumberOfAnalyzedSongs() {
		return 0;
	}

	/**
	 * Get number of songs in database waiting for analysis.
	 */
	public int getNumberOfPendingSongs() {
		return 0;
	}

}