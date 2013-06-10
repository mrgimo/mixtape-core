package ch.hsr.mixtape.application.service;

import static ch.hsr.mixtape.application.service.ApplicationFactory.getDatabaseService;
import static ch.hsr.mixtape.application.service.ApplicationFactory.getMixtape;

import java.util.Collection;
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
						persist(distances, songs);
					}

					@Override
					public void onFailure(Throwable throwable) {
					}
				});
	}

	private void persist(Collection<Distance> distances, List<Song> songs) {
		EntityManager entityManager = ApplicationFactory.getDatabaseService().getNewEntityManager();
		entityManager.getTransaction().begin();

		for (Song song : songs)
			entityManager.persist(entityManager.merge(song));

		for (Distance distance : distances)
			entityManager.persist(entityManager.merge(distance));

		entityManager.getTransaction().commit();
		getDatabaseService().closeEntityManager(entityManager);

	}
}
