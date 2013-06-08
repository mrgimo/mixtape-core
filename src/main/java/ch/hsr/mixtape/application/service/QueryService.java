package ch.hsr.mixtape.application.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.Session;

import ch.hsr.mixtape.model.Song;

/**
 * This service is responsible for handling song queries.
 * 
 * @author Stefan Derungs
 */
public class QueryService {

	private static final DatabaseService DB = ApplicationFactory
			.getDatabaseService();

	private static final int MAX_QUERY_RESULTS = 50;

	public List<Song> findSongsByTerm(String term, int maxResults) {
		String queryString = setupQueryString(term);
		TypedQuery<Song> query = DB.getEntityManager().createQuery(queryString,
				Song.class);

		if (maxResults > 0 && maxResults <= MAX_QUERY_RESULTS)
			query.setMaxResults(maxResults);
		else
			query.setMaxResults(MAX_QUERY_RESULTS);

		debugQuery(query, term);
		return query.getResultList();
	}

	/**
	 * This method is just for debugging purposes.
	 */
	private void debugQuery(TypedQuery<Song> query, String term) {
		Session session = DB.getEntityManager().unwrap(JpaEntityManager.class)
				.getActiveSession();
		DatabaseQuery dbQuery = ((EJBQueryImpl<Song>) query).getDatabaseQuery();

		dbQuery.prepareCall(session, new DatabaseRecord());
		System.err.println(dbQuery.getSQLString());

		DatabaseRecord recordWithValues = new DatabaseRecord();
		recordWithValues.add(new DatabaseField("term"), term);
		System.err.println(dbQuery.getTranslatedSQLString(session,
				recordWithValues));
	}

	/**
	 * Workaround as JPA does not support a list of strings being passed to a
	 * LIKE-statement.
	 */
	private String setupQueryString(String term) {
		final String query_base = "SELECT s FROM Song s WHERE ###WHERE###";
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

}
