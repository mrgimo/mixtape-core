package ch.hsr.mixtape.application.service;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.model.Song;

/**
 * This service is responsible for handling song queries.
 * 
 * @author Stefan Derungs
 */
public class QueryService {

	private static final Logger LOG = LoggerFactory
			.getLogger(QueryService.class);

	private static final DatabaseService DB = ApplicationFactory
			.getDatabaseService();

	public List<Song> findSongsByTerm(String term) {
		String cleanTerm = term.toLowerCase().trim();
		TypedQuery<Song> query = DB.getEntityManager().createNamedQuery("findSongsByTerm", Song.class);
		query.setParameter("term", cleanTerm);
		return query.getResultList();
	}

}
