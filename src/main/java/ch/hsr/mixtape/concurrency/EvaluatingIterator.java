package ch.hsr.mixtape.concurrency;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;

public class EvaluatingIterator<T> implements Iterator<T> {

	private final ListenableFuture<T> END_OF_QUEUE = new NoFuture<>();

	private final BlockingQueue<ListenableFuture<T>> futures = Queues.newLinkedBlockingQueue();

	private ListenableFuture<T> next;

	public EvaluatingIterator(ListenableFuture<T> initial) {
		next = initial;
	}

	public boolean hasNext() {
		return next != END_OF_QUEUE;
	}

	public T next() {
		try {
			ListenableFuture<T> current = next;
			next = futures.take();
			return current.get();
		} catch (InterruptedException | ExecutionException exception) {
			throw new RuntimeException(exception);
		}
	}

	public void remove() {
		try {
			futures.take();
		} catch (InterruptedException exception) {
			throw new RuntimeException(exception);
		}
	}

	public void put(ListenableFuture<T> future) {
		try {
			futures.put(future);
		} catch (InterruptedException exception) {
			throw new RuntimeException(exception);
		}
	}

	public void finish() {
		try {
			futures.put(END_OF_QUEUE);
		} catch (InterruptedException exception) {
			throw new RuntimeException(exception);
		}
	}

}