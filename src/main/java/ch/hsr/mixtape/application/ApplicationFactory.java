package ch.hsr.mixtape.application;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.Mixtape;
import ch.hsr.mixtape.application.service.AnalyzerService;
import ch.hsr.mixtape.application.service.DatabaseService;
import ch.hsr.mixtape.application.service.PlaylistPlaybackService;
import ch.hsr.mixtape.application.service.PlaylistService;
import ch.hsr.mixtape.application.service.PlaylistStreamService;
import ch.hsr.mixtape.application.service.QueryService;
import ch.hsr.mixtape.application.service.ServerService;
import ch.hsr.mixtape.model.Distance;

/**
 * @author Stefan Derungs
 */
public class ApplicationFactory {

	private static final Logger LOG = LoggerFactory
			.getLogger(ApplicationFactory.class);

	private static DatabaseService databaseService;

	private static PlaylistService playlistService;

	private static PlaylistStreamService playlistStreamService;
	
	private static PlaylistPlaybackService playlistPlaybackService;
	
	private static QueryService queryService;

	private static ServerService serverService;

	private static AnalyzerService analyzerService;

	private static Mixtape mixtapeService;

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

	public static PlaylistPlaybackService getPlaylistPlaybackService() {
		if (playlistPlaybackService == null) {
			LOG.debug("Initializing PlaylistPlaybackService");
			playlistPlaybackService = new PlaylistPlaybackService();
		}

		return playlistPlaybackService;
	}

	public static QueryService getQueryService() {
		if (queryService == null) {
			LOG.debug("Initializing QueryService");
			queryService = new QueryService();
		}

		return queryService;
	}

	public static ServerService getServerService() {
		if (serverService == null) {
			LOG.debug("Initializing ServerService");
			serverService = new ServerService();
		}

		return serverService;
	}

	public static AnalyzerService getAnalyzerService() {
		if (analyzerService == null) {
			LOG.debug("Initializing AnalyzerService");
			analyzerService = new AnalyzerService();
		}

		return analyzerService;
	}

	public static Mixtape getMixtape() {
		if (mixtapeService == null) {
			LOG.debug("Initializing MixtapeService");
			EntityManager em = getDatabaseService().getNewEntityManager();
			TypedQuery<Distance> query = em.createNamedQuery("getAllDistances",
					Distance.class);
			mixtapeService = new Mixtape(query.getResultList());
		}

		return mixtapeService;
	}

}
