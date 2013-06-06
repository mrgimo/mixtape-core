package ch.hsr.mixtape;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ListenableFuture;

public class NoFuture<T> implements ListenableFuture<T> {

	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new RuntimeException();
	}

	public boolean isCancelled() {
		throw new RuntimeException();
	}

	public boolean isDone() {
		throw new RuntimeException();
	}

	public T get() throws InterruptedException, ExecutionException {
		throw new RuntimeException();
	}

	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		throw new RuntimeException();
	}

	public void addListener(Runnable listener, Executor executor) {
		throw new RuntimeException();
	}

}