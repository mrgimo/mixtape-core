package ch.hsr.mixtape.concurrency;

import java.util.concurrent.BlockingQueue;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ForwardingBlockingQueue;

public class OfferBlockingQueue<E> extends ForwardingBlockingQueue<E> {

	private final BlockingQueue<E> delegate;

	public OfferBlockingQueue(int capacity) {
		delegate = Queues.newLinkedBlockingQueue(capacity);
	}

	protected BlockingQueue<E> delegate() {
		return delegate;
	}

	public boolean offer(E element) {
		try {
			put(element);
			return true;
		} catch (InterruptedException exception) {
			return false;
		}
	}

}