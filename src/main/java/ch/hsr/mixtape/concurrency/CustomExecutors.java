package ch.hsr.mixtape.concurrency;

import static com.google.common.util.concurrent.MoreExecutors.getExitingExecutorService;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.ListeningExecutorService;

public class CustomExecutors {

	public static ListeningExecutorService exitingFixedExecutorServiceWithBlockingTaskQueue(int numberOfThreads,
			String name) {
		return listeningDecorator(getExitingExecutorService(new ThreadPoolExecutor(
				numberOfThreads,
				numberOfThreads,
				0L, TimeUnit.MILLISECONDS,
				new OfferBlockingQueue<Runnable>(numberOfThreads), createNamedThreadFactory(name))));
	}

	public static ListeningExecutorService exitingFixedExecutorService(int numberOfThreads, String name) {
		return listeningDecorator(getExitingExecutorService(new ThreadPoolExecutor(
				numberOfThreads,
				numberOfThreads,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), createNamedThreadFactory(name))));
	}

	private static ThreadFactory createNamedThreadFactory(final String name) {
		return new ThreadFactory() {

			private ThreadFactory delegate = Executors.defaultThreadFactory();
			private AtomicInteger numberOfThreads = new AtomicInteger(0);

			public Thread newThread(Runnable runnable) {
				Thread thread = delegate.newThread(runnable);
				thread.setName(name + "-" + numberOfThreads.incrementAndGet());

				return thread;
			}

		};
	}

}