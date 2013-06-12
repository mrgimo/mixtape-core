package ch.hsr.mixtape.application.service;

import static ch.hsr.mixtape.application.ApplicationFactory.getDatabaseService;
import static ch.hsr.mixtape.application.ApplicationFactory.getMixtape;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.model.Distance;
import ch.hsr.mixtape.model.Song;

import com.google.common.util.concurrent.ListeningExecutorService;

public class AnalyzerService {

	private static final Logger LOG = LoggerFactory.getLogger(AnalyzerService.class);

	private final ListeningExecutorService analyzingExecutor = listeningDecorator(newSingleThreadExecutor());

	public void analyze(final List<Song> songs) {
		LOG.info("Submitted " + songs.size() + " song(s) for analysis.");

		analyzingExecutor.submit(new Runnable() {

			public void run() {
				for (Song song : songs)
					try {
						Collection<Distance> distances = getMixtape().addSong(song);

						LOG.info("Analysing songs successful.");
						persist(distances, song);
					} catch (IOException | InterruptedException | ExecutionException exception) {
						LOG.error("Error during analysing songs.", exception);
					}
			}

		});
	}

	private void persist(Collection<Distance> distances, Song song) {
		LOG.info("Persisting now...");
		EntityManager entityManager = getDatabaseService()
				.getNewEntityManager();
		entityManager.getTransaction().begin();

		song.setAnalyzeDate(new Date());
		entityManager.merge(song);

		entityManager.flush();
		LOG.info("Flushed songs.");

		for (Distance distance : distances)
			entityManager.persist(distance);

		entityManager.getTransaction().commit();
		getDatabaseService().closeEntityManager(entityManager);

	}
}
