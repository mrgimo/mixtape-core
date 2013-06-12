package ch.hsr.mixtape.application.service;

import static ch.hsr.mixtape.application.ApplicationFactory.getAnalyzerService;
import static ch.hsr.mixtape.application.ApplicationFactory.getDatabaseService;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.application.ApplicationFactory;
import ch.hsr.mixtape.application.MusicDirectoryScanner;
import ch.hsr.mixtape.application.SongPathResolver;
import ch.hsr.mixtape.io.AudioChannel;
import ch.hsr.mixtape.model.Song;
import ch.hsr.mixtape.model.SystemStatus;

/**
 * This service is responsible for handling system status queries.
 * 
 * @author Stefan Derungs
 */
public class ServerService {

	public static final String[] ALLOWED_MUSIC_FILETYPES = { "mp3", "ogg",
			"m4a", "aac", "wmv" };

	private static final Logger LOG = LoggerFactory
			.getLogger(ServerService.class);

	private EntityManager em = getDatabaseService().getNewEntityManager();

	private MusicDirectoryScanner directoryScanner;

	private Thread directoryScannerThread;

	private DecimalFormat df;

	private boolean hasStartedUp;

	public ServerService() {
		df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(2);
		System.err.println("ServerService initialized");
	}

	public void startUp() {
		if (hasStartedUp)
			return;

		String musicDirPath = SongPathResolver.MUSIC_DIRECTORY_FILEPATH;
		if (musicDirPath == null || musicDirPath.isEmpty())
			throw new RuntimeException("No MusicData Filepath defined!");

		List<Song> pendingSongs = em.createNamedQuery("getPendingSongs",
				Song.class).getResultList();
		System.err.println("PENDING SONGS: " + pendingSongs.size());
		if (!pendingSongs.isEmpty())
			getAnalyzerService().analyze(pendingSongs);
	}

	public void shutdown() {
		LOG.info("Shutthing down mixtape...");
		LOG.info("Deleting temporary files...");
		Path tempExtractionFile = Paths.get(AudioChannel.TEMPORARY_FILE_NAME);
		try {
			Files.deleteIfExists(tempExtractionFile);
		} catch (IOException e) {
			LOG.error("Error during shutdown: Temporary extraction"
					+ " file could not be deleted.", e);
		}

		LOG.info("Shutting down database service...");
		getDatabaseService().shutdown();
		LOG.info("Database service shut down.");
	}

	public boolean isScanningMusicDirectory() {
		return directoryScanner != null
				&& directoryScanner.isScanningMusicDirectory();
	}

	/**
	 * @return True if scanner thread could be started. False else.
	 */
	public boolean scanMusicDirectory() {
		if (directoryScanner == null
				|| !directoryScanner.isScanningMusicDirectory())
			directoryScanner = new MusicDirectoryScanner();

		if (!directoryScanner.compareAndSetScanning(false, true))
			return false;

		directoryScannerThread = new Thread(directoryScanner);
		directoryScannerThread.start();
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
		ss.setCurrentSystemLoad(load < 0 ? "n/v" : df.format(load));
	}

	/**
	 * Get the size in megabytes of the current database.
	 * 
	 * @return Returns the database size. Depending on the size the unit MB
	 *         (Megabytes) or GB (Gigabytes) is added.
	 */
	private void setDatabaseSize(SystemStatus ss) {
		try {
			String pathname = System.getenv("mixtapeData");
			if (!pathname.endsWith("/"))
				pathname += "/";

			File file = new File(pathname + "mixtapeDB");
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
		ss.setPendingSongs(ApplicationFactory.getQueryService()
				.getPendingSongs());
	}

}
