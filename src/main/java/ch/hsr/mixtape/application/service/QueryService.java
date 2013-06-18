package ch.hsr.mixtape.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import ch.hsr.mixtape.application.ApplicationFactory;
import ch.hsr.mixtape.model.PlaylistSettings;
import ch.hsr.mixtape.model.Song;

/**
 * This service is responsible for handling song queries.
 * 
 * @author Stefan Derungs
 */
public class QueryService {

	private static final int MAX_QUERY_RESULTS = 50;

	private EntityManager em = ApplicationFactory.getDatabaseService()
			.getNewEntityManager();

	public <T> T findObjectById(int entityId, Class<T> entityClass) {
		return em.find(entityClass, entityId);
	}

	public List<Song> findSongsByTerm(String term, int maxResults) {
		String queryString = setupQueryString(term);
		TypedQuery<Song> query = em.createQuery(queryString, Song.class);

		if (maxResults > 0 && maxResults <= MAX_QUERY_RESULTS)
			query.setMaxResults(maxResults);
		else
			query.setMaxResults(MAX_QUERY_RESULTS);

		return query.getResultList();
	}

	/**
	 * Workaround as JPA does not support a list of strings being passed to a
	 * LIKE-statement.
	 */
	private String setupQueryString(String term) {
		final String query_base = "SELECT s FROM Song s WHERE (###WHERE###) AND s.analyzeDate IS NOT NULL";
		final String title_or = "LOWER(s.title) LIKE ':term' OR ";
		final String artist_or = "LOWER(s.artist) LIKE ':term' OR ";
		final String album_or = "LOWER(s.album) LIKE ':term' OR ";

		List<String> terms = prepareTermForQuery(term);

		StringBuilder sb = new StringBuilder();
		for (String s : terms) {
			sb.append(title_or.replace(":term", s));
			sb.append(artist_or.replace(":term", s));
			sb.append(album_or.replace(":term", s));
		}

		String query = sb.toString();
		if (!query.isEmpty())
			return query_base.replace("###WHERE###",
					query.subSequence(0, query.length() - 4));
		return "";
	}

	private List<String> prepareTermForQuery(String term) {
		List<String> terms = new ArrayList<String>();
		if (!term.contains(" ")) {
			terms.add("%" + term.toLowerCase().trim() + "%");
			return terms;
		}

		String[] split = term.split(" ");
		for (String s : split) {
			if (!s.trim().isEmpty())
				terms.add("%" + s.toLowerCase().trim() + "%");
		}
		return terms;
	}

	public List<Song> getAllSongs() {
		return em.createNamedQuery("getAllSongs", Song.class).getResultList();
	}

	public List<Song> getPendingSongs(int limit) {
		if (limit > 0)
			return em.createNamedQuery("getPendingSongs", Song.class)
					.setMaxResults(limit).getResultList();
		else
			return em.createNamedQuery("getPendingSongs", Song.class)
					.getResultList();
	}

	public List<Song> getAnalysedSongs() {
		return em.createNamedQuery("getAnalysedSongs", Song.class)
				.getResultList();
	}

	/**
	 * This method can e.g. be used to start generating a playlist with no
	 * {@link PlaylistSettings#startSongs} set.
	 * 
	 * To preserve jpa standard compatibility and because JPA doesn't support
	 * RAND()/RANDOM() function in ORDER BY clause, the randomness is calculated
	 * in this method.
	 * 
	 * This method should be used with caution as it generates a lot of database
	 * queries.
	 * 
	 * @return Returns a random song.
	 */
	public Song getRandomSong() {
		Song maxIdSong = em.createNamedQuery("findMaxId", Song.class)
				.setMaxResults(1).getSingleResult();
		Random rand = new Random();

		Song found = null;
		while (found == null) {
			int checkId = rand.nextInt(maxIdSong.getId() + 1);
			found = em.find(Song.class, checkId);
		}

		return found;
	}

}
