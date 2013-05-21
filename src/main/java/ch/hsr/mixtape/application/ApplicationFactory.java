package ch.hsr.mixtape.application;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import ch.hsr.mixtape.application.service.PlaylistService;
import ch.hsr.mixtape.application.service.QueryService;
import ch.hsr.mixtape.application.service.SystemService;
import ch.hsr.mixtape.model.SystemSettings;

/**
 * @author Stefan Derungs
 */
public class ApplicationFactory {

	private static DatabaseManager databaseManager;

	private static PlaylistService playlistService;

	private static QueryService queryService;

	private static SystemService systemService;

	private static SystemSettings systemSettings;

	public static DatabaseManager getDatabaseManager() {
		if (databaseManager == null)
			databaseManager = new DatabaseManager();

		return databaseManager;
	}

	public static PlaylistService getPlaylistService() {
		if (playlistService == null)
			playlistService = new PlaylistService();

		return playlistService;
	}

	public static QueryService getQueryService() {
		if (queryService == null)
			queryService = new QueryService();

		return queryService;
	}

	public static SystemService getSystemService() {
		if (systemService == null)
			systemService = new SystemService();

		return systemService;
	}

	public static SystemSettings getSystemSettings() throws FirstRunException {
		if (systemSettings == null) {
			EntityManager em = getDatabaseManager().getEntityManager();
			TypedQuery<SystemSettings> query = em.createNamedQuery("getAllSystemSettings", SystemSettings.class);
			try {
				systemSettings = query.getSingleResult();
				if (systemSettings == null)
					throw new FirstRunException();
			} catch (NoResultException | NonUniqueResultException e) {
				throw new FirstRunException();
			}
		}

		return systemSettings;
	}
}
