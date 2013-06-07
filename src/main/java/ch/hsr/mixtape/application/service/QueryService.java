package ch.hsr.mixtape.application.service;

import java.util.List;

import javax.persistence.TypedQuery;

import ch.hsr.mixtape.model.Song;

/**
 * This service is responsible for handling song queries.
 * 
 * @author Stefan Derungs
 */
public class QueryService {

	private static final DatabaseService DB = ApplicationFactory
			.getDatabaseService();
	
	private static final int MAX_QUERY_RESULTS = 20;

	public List<Song> findSongsByTerm(String term) {
		String cleanTerm = "%" + term.toLowerCase().trim() + "%";
		TypedQuery<Song> query = DB.getEntityManager().createNamedQuery(
				"findSongsByTerm", Song.class);
		query.setParameter("term", cleanTerm);
		query.setMaxResults(MAX_QUERY_RESULTS);
		return query.getResultList();
	}

}
