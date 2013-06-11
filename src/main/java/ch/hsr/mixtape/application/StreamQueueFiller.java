package ch.hsr.mixtape.application;
//package ch.hsr.mixtape.application.service;
//
//import java.util.ArrayList;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
//import ch.hsr.mixtape.exception.UninitializedPlaylistException;
//import ch.hsr.mixtape.model.Song;
//
///**
// * This class is responsible for preparing audio for streaming. It fetches
// * the songs from the playlist service and prepares it for stream output.
// * 
// * @author Stefan Derungs
// */
//class StreamQueueFiller implements Runnable {
//
//	/**
//	 * 
//	 */
//	private final PlaylistStreamService playlistStreamService;
//
//	/**
//	 * @param playlistStreamService
//	 */
//	StreamQueueFiller(PlaylistStreamService playlistStreamService) {
//		this.playlistStreamService = playlistStreamService;
//	}
//
//	private static final int MINIMUM_SAMPLES_IN_QUEUE = PlaylistStreamService.MINIMUM_SECONDS_IN_QUEUE
//			* PlaylistStreamService.SAMPLE_RATE_IN_HZ;
//
//	@Override
//	public void run() {
//		try {
//			while (!this.playlistStreamService.isShutdown.get()) {
//				if (this.playlistStreamService.fifo.available() >= MINIMUM_SAMPLES_IN_QUEUE
//						|| this.playlistStreamService.songs.size() >= PlaylistStreamService.MAXIMUM_SONGS_IN_QUEUE) {
//					Thread.sleep(5000);
//					continue;
//				}
//
//				ArrayList<Song> upcoming = this.playlistStreamService.playlistService
//						.getNextNSongs(PlaylistStreamService.MAXIMUM_SONGS_IN_QUEUE);
//
//				for (int i = 0; i < this.playlistStreamService.songs.size(); i++) {
//					Song song = this.playlistStreamService.songs.get(i);
//					if (!upcoming.get(i).equals(song)
//							&& !song.isInStreamQueue()) {
//						this.playlistStreamService.audioQueue.dequeue(this.playlistStreamService.songs.remove(i));
//						this.playlistStreamService.songs.add(upcoming.get(i));
//						prepareSongForStreaming(song);
//					}
//				}
//			}
//		} catch (UninitializedPlaylistException e) {
//			PlaylistStreamService.LOG.error(UninitializedPlaylistException.class + " caught in "
//					+ StreamQueueFiller.class);
//		} catch (InterruptedException e) {
//			PlaylistStreamService.LOG.error(InterruptedException.class + " caught in "
//					+ StreamQueueFiller.class);
//			Thread.currentThread().interrupt();
//		} catch (ExecutionException e) {
//			e.printStackTrace(); // from prepareSongForStreaming
//		} finally {
//			this.playlistStreamService.fifo.close();
//			this.playlistStreamService.isShutdown.set(true);
//		}
//	}
//
//	private void prepareSongForStreaming(Song song)
//			throws InterruptedException, ExecutionException {
//		final AudioFormatConverter manager = new AudioFormatConverter();
//		ExecutorService pool = Executors.newFixedThreadPool(1);
//		Future<Byte[]> result = pool.submit(manager);
//		this.playlistStreamService.audioQueue.enqueue(song, result.get());
//	}
//
//}