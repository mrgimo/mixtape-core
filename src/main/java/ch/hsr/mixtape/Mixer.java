package ch.hsr.mixtape;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.distancefunction.DistanceGenerator;
import ch.hsr.mixtape.extraction.AudioExtractor;
import ch.hsr.mixtape.library.LibraryController;

public class Mixer {

	private static final int THREAD_COUNT = Runtime.getRuntime()
			.availableProcessors() - 2;
	private static final String PATH = "songs/";

	private Logger logger;
	
	public Mixer() {
		initLogger();
		
	}

	
	private void initLogger() {
		logger = Logger.getLogger("\n\nTime Measurements\n");
		try {
			FileHandler fileHandler = new FileHandler("logs/timeMeasurements.log", false);
			logger.addHandler(fileHandler);
			logger.setLevel(Level.ALL);
			fileHandler.setFormatter(new LoggingFormatter());
			logger.log(Level.INFO, "Time Measurement\n\n");
			logger.log(Level.INFO, "Available Processors: " + THREAD_COUNT + "\n");
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}


	public void mixSound() {
		long timeStart = System.currentTimeMillis();

		ArrayList<Song> songs = extractAudioData();

		LibraryController libraryController = new LibraryController();
		libraryController.addSongsToLibrary(songs);

		long clusterStart = System.currentTimeMillis();

		libraryController.organizeLibrary();
		libraryController.printClusters();

		logger.log(Level.INFO, "\nClustering done after "
				+ (System.currentTimeMillis() - clusterStart) + " ms \n");
		logger.log(Level.INFO, "Task completed for " + songs.size()
				+ " songs after " + (System.currentTimeMillis() - timeStart)
				+ " ms \n\n");
	}

	public ArrayList<Song> extractAudioData() {

		long startTime = System.currentTimeMillis();
		
		File audioDirectory = new File(PATH);
		File[] audioFiles = audioDirectory.listFiles();

		ExecutorService executorService = Executors
				.newFixedThreadPool(THREAD_COUNT);
		ArrayList<Song> songs = new ArrayList<Song>();
		Collection<Future<?>> futures = new LinkedList<Future<?>>();

		for (int i = 0; i < audioFiles.length; i++) {
			if (isMusicFile(audioFiles[i])) {

				Song song = new Song(audioFiles[i]);
				songs.add(song);

				AudioExtractor audioExtractor = new AudioExtractor(song);

				futures.add(executorService.submit(audioExtractor));
			}
		}
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		futures.clear();

		long featureExtractionEndTime = System.currentTimeMillis();
		
		logger.log(Level.INFO, "Feature extraction and building of suffix array and lcp array done after: " + (featureExtractionEndTime - startTime + " ms"));
		System.out.println("Computing distances: \n");

		for (int i = 0; i < songs.size() - 1; i++) {

			DistanceGenerator distanceGenerator = new DistanceGenerator(
					songs.get(i), songs, i + 1);
			futures.add(executorService.submit(distanceGenerator));
		}

		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		logger.log(Level.INFO, "\nDistance generation done in "
						+ (System.currentTimeMillis() - featureExtractionEndTime) + " ms");
		executorService.shutdown();
		return songs;
	}

	private static boolean isMusicFile(File musicFile) {
		return !musicFile.isDirectory() && musicFile.getName().endsWith(".mp3");
	}
	
	
	// just for testing purpose
	public static class LoggingFormatter extends SimpleFormatter {
		
		@Override
		public String format(LogRecord record) {
			return record.getMessage() + System.getProperty("line.separator");
			
		}
	}

}
