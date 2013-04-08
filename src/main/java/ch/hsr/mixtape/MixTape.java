package ch.hsr.mixtape;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.library.LibraryController;

public class MixTape {
	private static final int THREAD_COUNT = 4;
	private static final String PATH = "songs/";

	public static void main(String[] args) {
		ArrayList<Song> songs = extractAudioData();

		LibraryController libraryController = new LibraryController();
		libraryController.addSongsToLibrary(songs);
		
		libraryController.organizeLibrary();
		libraryController.printClusters();
	}

	private static ArrayList<Song> extractAudioData() {

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

	private static ArrayList<Song> getSongsFromTasks(
			ArrayList<Future<Song>> sampleLoadingTasks) {
		ArrayList<Song> songs = new ArrayList<Song>();
		
		for (Future<Song> sampleLoadingTask : sampleLoadingTasks) {
			try {
				songs.add(sampleLoadingTask.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return songs;
	}

	private static boolean isMusicFile(File musicFile) {
		return !musicFile.isDirectory() && musicFile.getName().endsWith(".mp3");
	}
}
