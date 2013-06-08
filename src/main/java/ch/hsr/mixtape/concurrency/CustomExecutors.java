package ch.hsr.mixtape.concurrency;

import static com.google.common.util.concurrent.MoreExecutors.getExitingExecutorService;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListeningExecutorService;

public class CustomExecutors {

	public static ListeningExecutorService exitingFixedExecutorServiceWithBlockingTaskQueue(int numberOfThreads) {
		return listeningDecorator(getExitingExecutorService(createFixedThreadPool(
				numberOfThreads,
				new OfferBlockingQueue<Runnable>(2 * numberOfThreads))));
	}

	public static ListeningExecutorService exitingFixedExecutorService(int numberOfThreads) {
		return listeningDecorator(getExitingExecutorService(createFixedThreadPool(
				numberOfThreads,
				new LinkedBlockingQueue<Runnable>())));
	}

	private static ThreadPoolExecutor createFixedThreadPool(int numberOfThreads, BlockingQueue<Runnable> taskQueue) {
		return new ThreadPoolExecutor(
				numberOfThreads,
				numberOfThreads,
				0L, TimeUnit.MILLISECONDS,
				taskQueue);
	}

}
