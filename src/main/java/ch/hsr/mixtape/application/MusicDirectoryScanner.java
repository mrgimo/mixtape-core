package ch.hsr.mixtape.application;

import static ch.hsr.mixtape.application.service.ApplicationFactory.getAnalyzerService;
import static ch.hsr.mixtape.application.service.ApplicationFactory.getDatabaseService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.application.service.ServerService;
import ch.hsr.mixtape.model.Song;

/**
 * @author Stefan Derungs
 */
public class MusicDirectoryScanner implements Runnable {

	private static final Logger LOG = LoggerFactory
			.getLogger(MusicDirectoryScanner.class);

	private SongTagExtractor extractor = new SongTagExtractor();

	private EntityManager em = getDatabaseService().getNewEntityManager();

	private List<Song> songsForAnalyzer = new ArrayList<Song>();

	private AtomicBoolean scanningMusicDirectory = new AtomicBoolean(false);

	public boolean isScanningMusicDirectory() {
		return scanningMusicDirectory.get();
	}

	/**
	 * @return See {@link AtomicBoolean#compareAndSet(boolean, boolean)}
	 */
	public boolean compareAndSetScanning(boolean expect,
			boolean update) {
		return scanningMusicDirectory.compareAndSet(expect, update);
	}

	@Override
	public void run() {
		try {
			File directory = Paths.get(
					SongPathResolver.MIXTAPE_MUSIC_DATA_FILEPATH).toFile();

			try {
				scanDirectory(directory);
			} catch (Exception e) {
				LOG.error("Error during scan phase.", e);
				return;
			}

			LOG.info("Finished scanning music directory. Found "
					+ songsForAnalyzer.size() + " new songs.");
			getAnalyzerService().analyze(songsForAnalyzer);

		} catch (Exception e) {
			LOG.error("An error occurred during music directory scanning.", e);
		} finally {
			scanningMusicDirectory.set(false);
		}
	}

	private void scanDirectory(File directory) throws Exception {
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(directory,
				ServerService.ALLOWED_MUSIC_FILETYPES, true);
		Iterator<File> iterator = files.iterator();

		while (iterator.hasNext()) {
			File file = iterator.next();

			if (!file.exists()) {
				LOG.warn("File does not exist: " + file.getAbsolutePath());
				continue;
			} else if (!Files.isReadable(file.toPath())) {
				LOG.warn("File is not readable: " + file.getAbsolutePath());
				continue;
			}

			Path relativePath = SongPathResolver.getRelativeSongFilepath(file
					.toPath());
			Song song = new Song(relativePath.toString(), new Date());
			extractor.extractTagsFromSong(song);

			try {
				// Doing persist on a per transaction base to handle exception
				// caused by filepath uniqueness constraint when song is already
				// in database. If not done on a per transaction base, the
				// rollback would undo all successful/unique writes.
				em.getTransaction().begin();
				em.persist(song);
				em.getTransaction().commit();
			} catch (Exception e) {
				continue;
			}

			songsForAnalyzer.add(song);
		}
	}

}