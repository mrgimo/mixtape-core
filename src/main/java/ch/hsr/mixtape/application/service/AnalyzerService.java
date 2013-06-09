package ch.hsr.mixtape.application.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import ch.hsr.mixtape.model.Distance;
import ch.hsr.mixtape.model.Song;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class AnalyzerService {

	ListeningExecutorService analyzingExecutor = MoreExecutors
			.listeningDecorator(Executors.newSingleThreadExecutor());

	public void analyze(final List<Song> songs) {

		ListenableFuture<List<Distance>> distances = analyzingExecutor
				.submit(new Callable<List<Distance>>() {

					@Override
					public List<Distance> call() throws Exception {
						return null; // mixTape.addSongs(songs);
					}
				});

		Futures.addCallback(distances,
				new FutureCallback<List<Distance>>() {

					@Override
					public void onSuccess(List<Distance> distances) {
						persist(distances, songs);
						// inform UI?
					}

					@Override
					public void onFailure(Throwable throwable) {
						// inform UI?
					}
				});
	}

	private void persist(List<Distance> distances, List<Song> songs) {
		EntityManager entityManager = ApplicationFactory.getDatabaseService().getNewEntityManager();
		entityManager.getTransaction().begin();

		for (Song song : songs)
			entityManager.persist(entityManager.merge(song));

		for (Distance distance : distances)
			entityManager.persist(entityManager.merge(distance));

		entityManager.getTransaction().commit();
		ApplicationFactory.getDatabaseService().closeEntityManager(entityManager);

	}
}
