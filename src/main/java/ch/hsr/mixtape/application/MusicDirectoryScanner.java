package ch.hsr.mixtape.application;

import static ch.hsr.mixtape.application.service.ApplicationFactory.getAnalyzerService;
import static ch.hsr.mixtape.application.service.ApplicationFactory.getDatabaseService;
import static ch.hsr.mixtape.application.service.ApplicationFactory.getServerService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

	@Override
	public void run() {
		try {
			File directory = Paths.get(
					ServerService.MIXTAPE_MUSIC_DATA_FILEPATH).toFile();

			try {
				scanDirectory(directory);
			} catch (Exception e) {
				LOG.error("Error during scan phase.", e);
				return;
			}

			getAnalyzerService().analyze(songsForAnalyzer);

		} catch (Exception e) {
			LOG.error("An error occurred during music directory scanning.", e);
		} finally {
			getServerService().compareAndSetScanningMusicDirectory(true, false, this);
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

			Path relativePath = getServerService().getRelativeSongFilepath(file.toPath());
			Song song = new Song(relativePath.toString(), new Date());
			extractor.extractTagsFromSong(song);

			try {
				// Doing persist on a per transaction base to handle exception
				// caused by filepath uniqueness constraint when song is already
				// in database. If not done on a per transaction base, the
				// rollback would undo all successful/unique writes.
				em.getTransaction().begin();
				em.persist(em.merge(song));
				em.getTransaction().commit();
			} catch (Exception e) {
				continue;
			}

			songsForAnalyzer.add(song);
		}
	}

}