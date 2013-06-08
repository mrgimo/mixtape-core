package ch.hsr.mixtape.application.service;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import ch.hsr.mixtape.FooDistances;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class AnalyzerService {

	ListeningExecutorService taskProcessor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

	public void analyze(List<File> audioFiles) {
		
			ListenableFuture<List<FooDistances>> distances = taskProcessor.submit(new Callable<List<FooDistances>>() {

				@Override
				public List<FooDistances> call() throws Exception {
					return null; //analyzer.analyze(audioFiles);
				}
			});
			
			Futures.addCallback(distances, new FutureCallback<List<FooDistances>>() {


				@Override
				public void onSuccess(List<FooDistances> distances) {
					persistDistances(distances);
				}


				@Override
				public void onFailure(Throwable arg0) {}
			});
	}
	
	private void persistDistances(List<FooDistances> distances) {
		//persist distances in db
	}
}
