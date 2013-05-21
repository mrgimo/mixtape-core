package ch.hsr.mixtape.application.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.application.ApplicationFactory;
import ch.hsr.mixtape.model.Song;

/**
 * This service is responsible for handling song queries.
 * 
 * @author Stefan Derungs
 */
public class QueryService {

	private static final Logger log = LoggerFactory
			.getLogger(QueryService.class);

	public ArrayList<Song> findSongsByTerm(String term) {
		// TODO: implement db query
		ArrayList<Song> availableSongs = ApplicationFactory
				.getDatabaseManager().getDummyDatabase();

		ArrayList<Song> foundSongs = new ArrayList<Song>();

		// TODO: clean string
		String cleanTerm = term.toLowerCase();

		if (!cleanTerm.isEmpty())
			for (Song s : availableSongs)
				if (s.getTitle().toLowerCase().contains(cleanTerm)
						|| s.getArtist().toLowerCase().contains(cleanTerm)
						|| s.getAlbum().toLowerCase().contains(cleanTerm))
					foundSongs.add(s);

		return foundSongs;
	}

}
