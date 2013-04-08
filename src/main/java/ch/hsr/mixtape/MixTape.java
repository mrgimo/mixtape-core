package ch.hsr.mixtape;

import java.io.File;
import java.util.ArrayList;

import ch.hsr.mixtape.audio.AudioChannel;
import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.features.controller.FeatureController;
import ch.hsr.mixtape.library.LibraryController;

public class MixTape {
	private static final String PATH = "songs/";

	public static void main(String[] args) {
		ArrayList<Song> songs = generateAudioData();

		FeatureController featureController = new FeatureController();
		featureController.extractFeatures(songs);

//		printFeatureVectors(songs);

		LibraryController libraryController = new LibraryController();
		libraryController.addSongsToLibrary(songs);
		
		libraryController.organizeLibrary();
		libraryController.printClusters();
	}

	private static ArrayList<Song> generateAudioData() {
		SampleLoader sampleLoader = new SampleLoader();

		ArrayList<Song> songs = new ArrayList<Song>();

		File audioDirectory = new File(PATH);
		File[] audioFiles = audioDirectory.listFiles();

		for (int i = 0; i < audioFiles.length; i++) {
			if (isMusicFile(audioFiles[i])) {
				System.out.println("extracting data from "
						+ audioFiles[i].getName() + "...");

				double[] audioData = sampleLoader.getSamples(AudioChannel
						.load(audioFiles[i]));

				Song song = new Song(audioFiles[i].getName(), audioData);
				songs.add(song);

				System.out.println("done !");
				System.out.println();
			}
		}
		return songs;
	}

	private static boolean isMusicFile(File musicFile) {
		return !musicFile.isDirectory() && musicFile.getName().endsWith(".mp3");
	}

	private static void printFeatureVectors(ArrayList<Song> songs) {
	
		for (Song song : songs) {
			System.out.println("RMS: " + song.getFeatureVector().RMS);
			System.out.println("ZC: " + song.getFeatureVector().ZC);
		}
		
		System.out.println();
		System.out.println();
	
	}

}
