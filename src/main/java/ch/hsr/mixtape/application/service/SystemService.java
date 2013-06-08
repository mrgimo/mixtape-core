package ch.hsr.mixtape.application.service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;

import ch.hsr.mixtape.application.MusicDirectoryScanner;
import ch.hsr.mixtape.model.SystemStatus;

/**
 * This service is responsible for handling system status queries.
 * 
 * @author Stefan Derungs
 */
public class SystemService {

	public static final String[] ALLOWED_MUSIC_FILETYPES = { "mp3", "ogg",
			"m4a", "aac", "wmv" };

	public static final String MIXTAPE_MUSIC_DATA_FILEPATH = System
			.getenv("mixtapeMusicDir");

	private static final Path MIXTAPE_MUSIC_DATA_PATH = Paths
			.get(MIXTAPE_MUSIC_DATA_FILEPATH);

	private EntityManager em = ApplicationFactory.getDatabaseService()
			.getNewEntityManager();

	private DecimalFormat df;

	private AtomicBoolean scanningMusicDirectory = new AtomicBoolean(false);

	public SystemService() {
		df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(2);
	}

	public Path getRelativeSongFilepath(Path absoluteSongFilepath) {
		return MIXTAPE_MUSIC_DATA_PATH.relativize(absoluteSongFilepath);
	}

	public Path getAbsoluteSongFilepath(String relativeSongFilepath) {
		return MIXTAPE_MUSIC_DATA_PATH.resolve(Paths.get(relativeSongFilepath));
	}

	/**
	 * @param caller
	 *            To make this method only callable from MusicDirectoryScanner.
	 * @return See {@link AtomicBoolean#compareAndSet(boolean, boolean)}
	 */
	public boolean compareAndSetScanningMusicDirectory(boolean expect,
			boolean update, Object caller) {
		if (!(caller instanceof MusicDirectoryScanner))
			return false;

		return scanningMusicDirectory.compareAndSet(expect, update);
	}

	public boolean isScanningMusicDirectory() {
		return scanningMusicDirectory.get();
	}

	/**
	 * @return True if scanner thread could be started. False else.
	 */
	public boolean scanMusicDirectory() {
		if (!scanningMusicDirectory.compareAndSet(false, true))
			return false;

		Thread scanner = new Thread(new MusicDirectoryScanner());
		scanner.start();
		return true;
	}

	public SystemStatus getSystemStatus() {
		return getCurrentSystemStatus();
	}

	private SystemStatus getCurrentSystemStatus() {
		SystemStatus ss = new SystemStatus();

		setNumberOfCores(ss);
		setAvailableMemory(ss);
		setCurrentSystemLoad(ss);
		setDatabaseSize(ss);
		long totalNumberOfSongs = getTotalNumberOfSongs();
		ss.setTotalNumberOfSongs(df.format(totalNumberOfSongs));
		long numberOfAnalyzedSongs = getNumberOfAnalyzedSongs();
		ss.setNumberOfAnalyzedSongs(df.format(numberOfAnalyzedSongs));

		if (totalNumberOfSongs > 0)
			ss.setProgress(df.format(numberOfAnalyzedSongs * 100
					/ totalNumberOfSongs));
		else
			ss.setProgress("0");

		setNumberOfPendingSongs(ss);
		setPendingSongs(ss);

		return ss;
	}

	private void setNumberOfCores(SystemStatus ss) {
		ss.setNumberOfCores(Runtime.getRuntime().availableProcessors());
	}

	private void setAvailableMemory(SystemStatus ss) {
		long memory = Runtime.getRuntime().maxMemory();
		if (memory > 1073741824)
			ss.setAvailableMemory(df.format(memory / 1073741824F) + " GB");
		else
			ss.setAvailableMemory(df.format(memory / 1048576F) + " MB");
	}

	/**
	 * Get the current system load in percent.
	 */
	private void setCurrentSystemLoad(SystemStatus ss) {
		double load = ManagementFactory.getOperatingSystemMXBean()
				.getSystemLoadAverage();
		ss.setCurrentSystemLoad((String) (load < 0 ? "n/v" : load));
	}

	/**
	 * Get the size in megabytes of the current database.
	 * 
	 * @return Returns the database size. Depending on the size the unit MB
	 *         (Megabytes) or GB (Gigabytes) is added.
	 */
	private void setDatabaseSize(SystemStatus ss) {
		try {
			File file = new File(System.getenv("mixtapeData") + "mixtapeDB");
			long size = FileUtils.sizeOfDirectory(file);
			if (size > 1073741824)
				ss.setDatabaseSize(df.format(size / 1073741824F) + " GB");
			else
				ss.setDatabaseSize(df.format(size / 1048576F) + " MB");
		} catch (Exception e) {
			ss.setDatabaseSize("n/v");
		}
	}

	/**
	 * Get the number of songs in database.
	 * 
	 * @return
	 */
	private Long getTotalNumberOfSongs() {
		return (Long) em.createNamedQuery("countAllSongs").getSingleResult();
	}

	/**
	 * Get number of analyzed songs in database.
	 * 
	 * @return
	 */
	private Long getNumberOfAnalyzedSongs() {
		return (Long) em.createNamedQuery("countAnalyzedSongs")
				.getSingleResult();
	}

	/**
	 * Get number of songs in database waiting for analysis.
	 */
	private void setNumberOfPendingSongs(SystemStatus ss) {
		Long count = (Long) em.createNamedQuery("countPendingSongs")
				.getSingleResult();
		ss.setNumberOfPendingSongs(df.format(count));
	}

	private void setPendingSongs(SystemStatus ss) {
		ss.setPendingSongs(ApplicationFactory.getDatabaseService()
				.getPendingSongs());
	}

}
