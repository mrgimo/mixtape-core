package ch.hsr.mixtape;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cern.colt.Arrays;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.data.SpectralCentroidFeature;
import ch.hsr.mixtape.distancefunction.KolmogorovDistance;
import ch.hsr.mixtape.distancefunction.skew.LCP;
import ch.hsr.mixtape.distancefunction.skew.SkewInteger;
import ch.hsr.mixtape.extraction.AudioExtractor;
import ch.hsr.mixtape.library.LibraryController;

public class MixTape {
	private static final int THREAD_COUNT = 2;
	private static final String PATH = "songs/";

	public static void main(String[] args) {
		ArrayList<Song> songs = extractAudioData();
		
		
		

		LibraryController libraryController = new LibraryController();
		libraryController.addSongsToLibrary(songs);
		
		libraryController.organizeLibrary();
		libraryController.printClusters();
	}

	public static ArrayList<Song> extractAudioData() {
		System.out.println("Available Processors: " + THREAD_COUNT);

		File audioDirectory = new File(PATH);
		File[] audioFiles = audioDirectory.listFiles();
		
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
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
		for (Future<?> future:futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		executorService.shutdown();
		return songs;
	}

	private static boolean isMusicFile(File musicFile) {
		return !musicFile.isDirectory() && musicFile.getName().endsWith(".mp3");
	}
}
