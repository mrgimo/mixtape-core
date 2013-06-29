package ch.hsr.mixtape.application.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Thread.State;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sound.sampled.AudioInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.AudioConverter;
import ch.hsr.mixtape.application.ApplicationFactory;
import ch.hsr.mixtape.application.StreamSongBundle;
import ch.hsr.mixtape.application.StreamSubscriber;
import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.model.Song;

/**
 * This class doesn't contain a working example.
 */
public class PlaylistStreamService {

	static final Logger LOG = LoggerFactory
			.getLogger(PlaylistStreamService.class);

	private static final int THREAD_SLEEP_TIME_IN_MILLI = 10000;

	static final int MAXIMUM_SONGS_IN_QUEUE = 5;

	static final int MINIMUM_SECONDS_IN_QUEUE = 60;

	static final int SAMPLE_RATE_IN_HZ = 44100;

	final PlaylistService playlistService = ApplicationFactory
			.getPlaylistService();

	private Streamer streamer;

	private Thread streamerThread;

	private AudioConverter converter;

	public PlaylistStreamService() {
		streamer = new Streamer();
		streamerThread = new Thread(streamer);
		streamerThread.start();
		converter = new AudioConverter();
	}

	public void subscribe(StreamSubscriber subscriber) {
		LOG.debug("in PlaylistStreamService.subscribe");
		streamer.subscribe(subscriber);
	}

	public byte[] getStreamData() throws InterruptedException {
		if (streamer.isShutdown.get()
				|| streamerThread.getState() == State.TERMINATED)
			throw new InterruptedException(
					"The streaming service was terminated.");
		wait();
		// TODO: get data from streamer
		return null;
	}

	public void shutdown() {
		try {
			LOG.debug("Waiting for the streaming service to shut down. "
					+ "This can take some time...");
			streamer.shutdown();
			if (streamerThread.getState() != State.TERMINATED) {
				LOG.error("Timeout for graceful shutdown of streaming service. "
						+ "Interrupting now.");
				streamerThread.interrupt();
			}
			LOG.debug("Streaming service shut down complete.");
		} catch (InterruptedException e) {
			LOG.error("Errors occured during shutdown of streaming service.", e);
		}
	}

	public boolean isShutdown() {
		return streamerThread.getState() == State.TERMINATED;
	}

	/**
	 * This class streams the content to the web.
	 * 
	 * @author Stefan Derungs
	 */
	public class Streamer implements Runnable {

		private AtomicBoolean isTerminated = new AtomicBoolean(false);

		private AtomicBoolean isShutdown = new AtomicBoolean(false);

		private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

		private StreamSongBundle current;

		private StreamSongBundle next;

		private ArrayList<StreamSubscriber> subscribers;

		public Streamer() {
			subscribers = new ArrayList<StreamSubscriber>();
		}

		@Override
		public void run() {
			final int BUFFER_CAPACITY = 1400;
			final String song = "C:\\Users\\Stefan Derungs\\Music\\iTunes\\iTunes Music\\Within Temptation\\The swan song.mp3";
			final File inputFile = new File(song);
			final File streamFile = new File("TEMP_CanonSample.ogg");
			AudioInputStream ais = null;
			ReadableByteChannel channel = null;
			try {
				while (!isTerminated.get() && !isShutdown.get()) {
					lock.writeLock().lock();
					if (subscribers.isEmpty()) {
						LOG.debug("No stream subscribers at the moment. Going to sleep for "
								+ THREAD_SLEEP_TIME_IN_MILLI + "ms.");
						lock.writeLock().unlock();
						Thread.sleep(THREAD_SLEEP_TIME_IN_MILLI);
						continue;
					}
					LOG.debug("Subscribers available. Starting/Continuing streaming now.");

					LOG.debug("Preparing song for streaming.");
					// converter.transcode(inputFile, streamFile, "ogg", 2); //
					// TODO uncomment

					/**
					 * // ais = AudioSystem.getAudioInputStream(streamFile); //
					 * channel = Channels.newChannel(ais); // byte[]
					 * readAllBytes = Files.readAllBytes(streamFile //
					 * .toPath()); // ByteArrayOutputStream baos = new
					 * ByteArrayOutputStream(); // baos.write(readAllBytes);
					 */

					LOG.debug("Buffering song.");
					ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
					buffer.order(ByteOrder.BIG_ENDIAN);

					FileInputStream fis = new FileInputStream(inputFile);
					FileChannel fchannel = fis.getChannel();
					int totalCounter = 1;
					LOG.debug("Buffering song to subscribers.");
					int readBytes;
					while ((readBytes = fchannel.read(buffer)) != -1) {
						if (readBytes == 0)
							continue;
						
						buffer.flip();

						System.err.println("Pass " + (totalCounter++)
								+ "\t\tReadBytes: " + readBytes); // TODO remove output
						byte[] tmp = new byte[BUFFER_CAPACITY];
						int counter = 0;
						while (buffer.remaining() > 0)
							tmp[counter++] = buffer.get();

						for (StreamSubscriber subscriber : subscribers)
							subscriber.provideData(tmp);

						buffer.clear();
					}
					//
					// while (channel.read(buffer) != -1) {
					// buffer.flip();
					// baos.write(buffer.get());
					// buffer.compact();
					// }
					// byte[] base64Encoded = Base64.base64Encode(baos
					// .toByteArray());

					// LOG.debug("Submitting buffered song to subscribers.");
					// for (StreamSubscriber subscriber : subscribers) {
					// subscriber.provideData(readAllBytes);
					// // subscriber.provideData(baos.toByteArray());
					// // subscriber.provideData(base64Encoded);
					// }

					// TODO: channel-handling for mono-songs
					// TODO: sample-handling for songs != 44100Hz
					lock.writeLock().unlock();

					Thread.sleep(15000);
				}
				// } catch (UnsupportedAudioFileException | IOException
				// | UninitializedPlaylistException e) {
				// LOG.error("An error occurred in the streaming service.", e);
			} catch (InterruptedException e) {
				LOG.info("The streaming service thread was interrupted.", e);
				notifyEndOfStream();
			} catch (IOException e) {
				LOG.info("The streaming service had an I/O-Problem.", e);
				// } catch (UnsupportedAudioFileException e) {
				// e.printStackTrace();
			} catch (IllegalArgumentException e) {
				LOG.error("The target file type is not supported by "
						+ "the system.", e);
				// } catch (EncoderException e) {
				// e.printStackTrace();
			} finally {
				try {
					if (ais != null)
						ais.close();

					if (channel != null)
						channel.close();

					Files.delete(streamFile.toPath());
				} catch (IOException e) {
					LOG.error("Temporary converted file could not be deleted.");
				}
				if (lock.writeLock().isHeldByCurrentThread())
					lock.writeLock().unlock();
				isTerminated.set(true);
				notifyEndOfStream();
				if (Thread.holdsLock(this))
					notifyAll();
			}
		}

		private double getSample(ByteBuffer buffer, int sampleSizeInBits) {
			switch (sampleSizeInBits) {
			case 8:
				return buffer.get();
			case 32:
				return buffer.getInt();
			default:
				return buffer.getShort();
			}
		}

		public void subscribe(StreamSubscriber subscriber) {
			LOG.debug("Acquiring lock for `subscribe` in Streamer.");
			lock.writeLock().lock();
			LOG.debug("Acquired lock for `subscribe` in Streamer.");
			if (subscribers != null && !subscribers.contains(subscriber)) {
				LOG.debug("Subscriber not in list yet.");
				subscribers.add(subscriber);
			}
			LOG.debug("Releasing lock for `subscribe` in Streamer.");
			lock.writeLock().unlock();
			LOG.debug("Released lock for `subscribe` in Streamer.");
		}

		private void notifyEndOfStream() {
			if (subscribers != null && !subscribers.isEmpty()) {
				LOG.info("Trying to notify stream subscribers for end of streaming.");
				for (StreamSubscriber s : subscribers)
					s.notifyEndOfStream();
				subscribers = null;
			}
		}

		/**
		 * This method tries to ensure the threads termination leaves the system
		 * in a consistent state.
		 * 
		 * @throws InterruptedException
		 */
		public void shutdown() throws InterruptedException {
			isShutdown.set(true);
			wait(THREAD_SLEEP_TIME_IN_MILLI);
			if (!isTerminated.get())
				notifyEndOfStream();
		}

		private boolean nextSongIsSame() throws InvalidPlaylistException {
			ArrayList<Song> nextSongs = null; //playlistService.getItems();
			if (nextSongs.isEmpty()) {
				next = null;
				return true;
			}
			return next.getSong() == nextSongs.get(0);
		}

	}

}
