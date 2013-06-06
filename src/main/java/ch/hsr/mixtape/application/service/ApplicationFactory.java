package ch.hsr.mixtape.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.model.SystemSettings;

/**
 * @author Stefan Derungs
 */
public class ApplicationFactory {

	private static final Logger LOG = LoggerFactory
			.getLogger(ApplicationFactory.class);

	private static DatabaseService databaseService;

	private static PlaylistService playlistService;

	private static PlaylistStreamService playlistStreamService;

	private static QueryService queryService;

	private static SystemService systemService;

	private static SystemSettings systemSettings;

	public static DatabaseService getDatabaseService() {
		if (databaseService == null) {
			databaseService = new DatabaseService();
		}

		return databaseService;
	}

	public static PlaylistService getPlaylistService() {
		if (playlistService == null) {
			LOG.debug("Initializing PlaylistService");
			playlistService = new PlaylistService();
		}

		return playlistService;
	}

	public static PlaylistStreamService getPlaylistStreamService() {
		if (playlistStreamService == null) {
			LOG.debug("Initializing PlaylistStreamService");
			playlistStreamService = new PlaylistStreamService();
		}

		return playlistStreamService;
	}

	public static QueryService getQueryService() {
		if (queryService == null) {
			LOG.debug("Initializing QueryService");
			queryService = new QueryService();
		}

		return queryService;
	}

	public static SystemService getSystemService() {
		if (systemService == null) {
			LOG.debug("Initializing SystemService");
			systemService = new SystemService();
		}

		return systemService;
	}

	public static SystemSettings getSystemSettings() /* throws FirstRunException */{
		// TODO: implement for FirstRun
		// if (systemSettings == null) {
		// EntityManager em = getDatabaseManager().getEntityManager();
		// TypedQuery<SystemSettings> query = em.createNamedQuery(
		// "getAllSystemSettings", SystemSettings.class);
		// try {
		// systemSettings = query.getSingleResult();
		// if (systemSettings == null)
		// throw new FirstRunException();
		// } catch (NoResultException | NonUniqueResultException e) {
		// throw new FirstRunException();
		// }
		// }
		return systemSettings;
	}
}
