package ch.hsr.mixtape.application.service;

import static ch.hsr.mixtape.application.ApplicationFactory.getDatabaseService;
import static ch.hsr.mixtape.application.ApplicationFactory.getMixtape;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.DistanceCallback;
import ch.hsr.mixtape.model.Distance;
import ch.hsr.mixtape.model.Song;

public class AnalyzerService {

	private static final Logger LOG = LoggerFactory
			.getLogger(AnalyzerService.class);

	public void analyze(final List<Song> songs) {
		LOG.info("Submitted " + songs.size() + " song(s) for analysis.");
		try {
			getMixtape().addSongs(songs, new DistanceCallback() {

				public void distanceAdded(Song song, Collection<Distance> distances) {
					LOG.info("Analysing song successful.");
					LOG.info("Analysis for `" + song.getTitle() + "` took "
							+ song.getAnalysisDurationInSeconds()
							+ " seconds.");
					persist(distances, song);
				}

			});
		} catch (IOException | InterruptedException | ExecutionException exception) {
			LOG.error("Error during analysing songs.", exception);
		}
	}

	private void persist(Collection<Distance> distances, Song song) {
		LOG.info("Persisting song now...");
		EntityManager entityManager = getDatabaseService().getNewEntityManager();
		entityManager.getTransaction().begin();

		entityManager.merge(song);
		entityManager.flush();
		LOG.info("Flushed song.");

		for (Distance distance : distances)
			entityManager.persist(distance);

		entityManager.getTransaction().commit();
		getDatabaseService().closeEntityManager(entityManager);

	}
}