package ch.hsr.mixtape.application.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import ch.hsr.mixtape.FooDistances;
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

		
		ListenableFuture<List<FooDistances>> distances = analyzingExecutor
				.submit(new Callable<List<FooDistances>>() {

					@Override
					public List<FooDistances> call() throws Exception {
						return null; // mixTape.addSongs(songs);
					}
				});

		Futures.addCallback(distances,
				new FutureCallback<List<FooDistances>>() {

					@Override
					public void onSuccess(List<FooDistances> distances) {
						persist(distances, songs);
						// inform UI?
					}

					@Override
					public void onFailure(Throwable throwable) {
						// inform UI?
					}
				});
	}


	private void persist(List<FooDistances> distances, List<Song> songs) {
		// persist songs & distances in db
	}
}
