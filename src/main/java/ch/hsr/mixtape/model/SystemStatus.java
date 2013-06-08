package ch.hsr.mixtape.model;

import java.util.List;

/**
 * @author Stefan Derungs
 */
public class SystemStatus {

	private int numberOfCores;

	private String availableMemory;

	private String currentSystemLoad;

	private String databaseSize;

	private String totalNumberOfSongs;

	private String numberOfAnalyzedSongs;

	private String numberOfPendingSongs;
	
	private String progress;

	private List<Song> pendingSongs;

	/**
	 * Get the number of available cores for the JVM.
	 */
	public int getNumberOfCores() {
		return numberOfCores;
	}

	public void setNumberOfCores(int numberOfCores) {
		this.numberOfCores = numberOfCores;
	}

	/**
	 * Get the reserved memory for the system in megabytes.
	 * 
	 * @return Returns the available memory for the JVM. Depending on the size
	 *         the unit MB (Megabytes) or GB (Gigabytes) is added.
	 */
	public String getAvailableMemory() {
		return availableMemory;
	}

	public void setAvailableMemory(String availableMemory) {
		this.availableMemory = availableMemory;
	}

	/**
	 * Get the current system load in percent.
	 */
	public String getCurrentSystemLoad() {
		return currentSystemLoad;
	}

	public void setCurrentSystemLoad(String currentSystemLoad) {
		this.currentSystemLoad = currentSystemLoad;
	}

	/**
	 * Get the size in megabytes of the current database.
	 * 
	 * @return Returns the database size. Depending on the size the unit MB
	 *         (Megabytes) or GB (Gigabytes) is added.
	 */
	public String getDatabaseSize() {
		return databaseSize;
	}

	public void setDatabaseSize(String databaseSize) {
		this.databaseSize = databaseSize;
	}

	/**
	 * Get the number of songs in database.
	 */
	public String getTotalNumberOfSongs() {
		return totalNumberOfSongs;
	}

	public void setTotalNumberOfSongs(String totalNumberOfSongs) {
		this.totalNumberOfSongs = totalNumberOfSongs;
	}

	/**
	 * Get number of analyzed songs in database.
	 */
	public String getNumberOfAnalyzedSongs() {
		return numberOfAnalyzedSongs;
	}

	public void setNumberOfAnalyzedSongs(String numberOfAnalyzedSongs) {
		this.numberOfAnalyzedSongs = numberOfAnalyzedSongs;
	}

	/**
	 * Get number of songs in database waiting for analysis.
	 */
	public String getNumberOfPendingSongs() {
		return numberOfPendingSongs;
	}

	public void setNumberOfPendingSongs(String numberOfPendingSongs) {
		this.numberOfPendingSongs = numberOfPendingSongs;
	}
	
	public String getProgress() {
		return progress;
	}
	
	public void setProgress(String string) {
		this.progress = string;
	}

	public List<Song> getPendingSongs() {
		return pendingSongs;
	}

	public void setPendingSongs(List<Song> pendingSongs) {
		this.pendingSongs = pendingSongs;
	}

}
