package ch.hsr.mixtape.application;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import ch.hsr.mixtape.model.Song;

class SongAudioQueue {

	private ReentrantLock lock = new ReentrantLock(true);

	private HashMap<Song, Byte[]> queue = new HashMap<Song, Byte[]>();

	public void enqueue(Song song, Byte[] audioData) {
		try {
			lock.lock();
			if (!queue.containsKey(song))
				queue.put(song, audioData);
		} finally {
			lock.unlock();
		}
	}

	public Byte[] dequeue(Song song) {
		try {
			lock.lock();
			return queue.remove(song);
		} finally {
			lock.unlock();
		}
	}

	public Byte[] lookup(Song song) {
		try {
			lock.lock();
			return queue.get(song);
		} finally {
			lock.unlock();
		}
	}
}