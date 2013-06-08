package ch.hsr.mixtape.application;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.application.service.ApplicationFactory;
import ch.hsr.mixtape.application.service.SystemService;
import ch.hsr.mixtape.model.Song;

/**
 * @author Stefan Derungs
 */
public class MusicDirectoryScanner implements Runnable {

	static final Logger LOG = LoggerFactory
			.getLogger(MusicDirectoryScanner.class);

	static final ArrayList<String> ALLOWED_MUSIC_FILETYPES = new ArrayList<String>(
			Arrays.asList("MP3", "OGG", "M4A", "AAC", "WMV"));

	private static final SystemService SYSTEM_SERVICE = ApplicationFactory
			.getSystemService();

	private SongTagExtractor extractor;

	private EntityManager em = ApplicationFactory.getDatabaseService()
			.getNewEntityManager();

	private List<Song> songsForAnalyzer = new ArrayList<Song>();

	private final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
		public boolean accept(Path path) throws IOException {
			File file = path.toFile();
			String extension = FilepathExtractor.getExtension(file.getName())
					.toUpperCase();
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
			try {
				scanDirectory(path);
				em.getTransaction().commit();
			} catch (IOException e) {
				LOG.error("Error during scan phase.", e);
				return;
			} catch (IllegalStateException | RollbackException e) {
				LOG.error("Error during transaction commit phase.", e);
				return;
			}

			ApplicationFactory.getAnalyzerService().analyze(songsForAnalyzer);

		} catch (Exception e) {
			LOG.error("An error occurred during music directory scanning.", e);
		} finally {
			SYSTEM_SERVICE.compareAndSetScanningMusicDirectory(true, false,
					this);
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
				songsForAnalyzer.add(song);
			}
		}
	}

}