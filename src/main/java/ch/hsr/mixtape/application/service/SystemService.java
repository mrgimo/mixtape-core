package ch.hsr.mixtape.application.service;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.application.FilepathExtractor;
import ch.hsr.mixtape.application.SongTagExtractor;
import ch.hsr.mixtape.model.Song;
import ch.hsr.mixtape.model.SystemStatus;

/**
 * This service is responsible for handling system status queries.
 * 
 * @author Stefan Derungs
 */
public class SystemService {

	private static final Logger LOG = LoggerFactory
			.getLogger(SystemService.class);

	private static final ArrayList<String> ALLOWED_MUSIC_FILETYPES = new ArrayList<String>(
			Arrays.asList("MP3", "OGG", "M4A", "AAC", "WMV"));
	
	private EntityManager em = ApplicationFactory
			.getDatabaseService().getNewEntityManager();

	private DecimalFormat df;

	private AtomicBoolean scanningMusicDirectory = new AtomicBoolean(false);

	public SystemService() {
		df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(2);
	}

	public boolean isScanningMusicDirectory() {
		return scanningMusicDirectory.get();
	}

	public void scanMusicDirectory() {
		if (!scanningMusicDirectory.compareAndSet(false, true))
			return;

		Thread scanner = new Thread(new MusicDirectoryScanner());
		scanner.start();
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
		ss.setProgress(df.format(numberOfAnalyzedSongs * 100
				/ totalNumberOfSongs));
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

	/**
	 * This class needs access to SystemService#scanningMusicDirectory.
	 * 
	 * @author Stefan Derungs
	 */
	private class MusicDirectoryScanner implements Runnable {

		private SongTagExtractor extractor;

		private EntityManager em = ApplicationFactory.getDatabaseService()
				.getNewEntityManager();;

		private final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			public boolean accept(Path path) throws IOException {
				File file = path.toFile();
				String extension = FilepathExtractor.getExtension(
						file.getName()).toUpperCase();
				return file.isDirectory()
						|| ALLOWED_MUSIC_FILETYPES.contains(extension);
			}
		};

		@Override
		public void run() {
			try {
				String musicDir = System.getenv("mixtapeMusicDir");
				Path path = Paths.get(musicDir);

				em.getTransaction().begin();
				scanDirectory(path);
				em.getTransaction().commit();

			} catch (Exception e) {
				LOG.error("An error occurred during music directory scanning.",
						e);
			} finally {
				scanningMusicDirectory.compareAndSet(true, false);
			}
		}

		private void scanDirectory(Path path) throws IOException {
			DirectoryStream<Path> ds = Files.newDirectoryStream(path, filter);
			Iterator<Path> iterator = ds.iterator();

			while (iterator.hasNext()) {
				final Path nextPath = iterator.next();
				File nextFile = path.toFile();

				if (nextFile.isDirectory()) {
					scanDirectory(nextPath);
				} else {
					Song song = new Song(nextFile.getAbsolutePath(), new Date());
					extractor.extractTagsFromSong(song);
					em.persist(em.merge(song));
				}
			}
		}

	}

}
