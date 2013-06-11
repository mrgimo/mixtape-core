package ch.hsr.mixtape.application.service;

import static ch.hsr.mixtape.application.ApplicationFactory.getDatabaseService;
import static ch.hsr.mixtape.application.ApplicationFactory.getMixtape;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.model.Distance;
import ch.hsr.mixtape.model.Song;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class AnalyzerService {

	private static final Logger LOG = LoggerFactory
			.getLogger(AnalyzerService.class);

	ListeningExecutorService analyzingExecutor = MoreExecutors
			.listeningDecorator(Executors.newSingleThreadExecutor());

	public void analyze(final List<Song> songs) {
		LOG.info("Submitted "+songs.size()+" song(s) for analysis.");

		ListenableFuture<Collection<Distance>> distances = analyzingExecutor
				.submit(new Callable<Collection<Distance>>() {

					@Override
					public Collection<Distance> call() throws Exception {
						return getMixtape().addSongs(songs);
					}
				});

		Futures.addCallback(distances,
				new FutureCallback<Collection<Distance>>() {

					@Override
					public void onSuccess(Collection<Distance> distances) {
						LOG.info("Analysing songs successful.");
						persist(distances, songs);
					}

					@Override
					public void onFailure(Throwable throwable) {
						LOG.error("Error during analysing songs.", throwable);
					}
				});
	}

	private void persist(Collection<Distance> distances, List<Song> songs) {
		LOG.info("Persisting now...");
		EntityManager entityManager = getDatabaseService()
				.getNewEntityManager();
		entityManager.getTransaction().begin();

		for (Song song : songs) {
			song.setAnalyzeDate(new Date());
			entityManager.merge(song);
		}

		entityManager.flush();
		LOG.info("Flushed songs.");

		for (Distance distance : distances)
			entityManager.persist(distance);

		entityManager.getTransaction().commit();
		getDatabaseService().closeEntityManager(entityManager);

	}
}
