package ch.hsr.mixtape.application;

import java.util.ArrayList;
import java.util.Random;

import ch.hsr.mixtape.model.PlaylistSettings;
import ch.hsr.mixtape.model.Song;
import ch.hsr.mixtape.model.SongSimilarity;

public class DummyData {

	private static ArrayList<Song> availableSongs;

	public static ArrayList<Song> getDummyDatabase() {
		availableSongs = new ArrayList<Song>();

		availableSongs.add(new Song("/lights out.mp3", "Lights out", "Scooter",
				"Who's got the last laugh now?", false));
		availableSongs.add(new Song("/elvenpath.mp3", "Elvenpath", "Nightwish",
				"Angels fall first", false));
		availableSongs.add(new Song("/bring me to life.mp3",
				"Bring me to Life", "Evanescence", "Fallen", false));
		availableSongs.add(new Song("/where will you go.mp3",
				"Where will You go?", "Evanescence", "Origin", false));
		availableSongs.add(new Song("/oblivion.mp3", "Oblivion",
				"30 Seconds to Mars", "30 Seconds to Mars", false));
		availableSongs.add(new Song("/beautiful lie.mp3", "Beautiful Lie",
				"30 Seconds to Mars", "A Beautiful Lie", false));
		availableSongs.add(new Song("/up in the air.mp3", "Up in the Air",
				"30 Seconds to Mars", "Love Lust Faith + Dreams", false));
		availableSongs.add(new Song("/until it breaks.mp3", "Until it breaks",
				"Linkin Park", "Living Things", false));
		availableSongs.add(new Song("/laura palmer.mp3", "Laura Palmer",
				"Bastille", "Bad Blood", false));
		availableSongs.add(new Song("/road trippin.mp3", "Road Trippin",
				"Red Hot Chili Peppers", "Californication", false));
		availableSongs.add(new Song("/marketplace.mp3", "Marketplace",
				"Snow Patrol", "Songs for Polarbears", false));
		availableSongs
				.add(new Song("/low.mp3", "Low", "Coldplay", "X&Y", false));
		availableSongs.add(new Song("/run boy run.mp3", "Run Boy Run",
				"Woodkid", "The Golden Age", false));
		availableSongs.add(new Song("/lucky now.mp3", "Lucky now",
				"Ryan Adams", "Ashes & Fire", false));
		availableSongs.add(new Song("/runaway train.mp3", "Runaway Train",
				"Soul Asylum", "Grave Dancers Union", false));

		return availableSongs;
	}

	public static PlaylistSettings getDummyPlaylistSettings() {
		return new PlaylistSettings(40, 120);
	}

	public static ArrayList<Song> getDummyPlaylist() {
		ArrayList<Song> songs = new ArrayList<Song>();

		if (availableSongs == null)
			getDummyDatabase();

		for (int i = 0; i < availableSongs.size(); i++) {
			if (i % 2 == 0)
				songs.add(availableSongs.get(i));
		}

		initializeSimilarities(songs);
		return songs;
	}

	private static void initializeSimilarities(ArrayList<Song> songs) {
		Random r = new Random();
		for (int i = 1; i < songs.size(); i++) {
			int tempo = (int) (r.nextDouble() * 100);
			int melody = (int) (r.nextDouble() * 100);
			int mfcc = (int) (r.nextDouble() * 100);
			int perception = (int) (r.nextDouble() * 100);
			int total = (tempo + melody + mfcc + perception) / 4;
			songs.get(i).setSongSimilarity(
					new SongSimilarity(songs.get(i), songs.get(i - 1), total,
							tempo, melody, mfcc, perception));
			songs.get(i).setUserWish(i % 3 == 2 ? true : false);
		}
	}
}
