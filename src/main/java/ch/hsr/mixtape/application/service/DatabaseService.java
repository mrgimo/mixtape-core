package ch.hsr.mixtape.application.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.model.Song;

class DatabaseService {

	private static final Logger LOG = LoggerFactory
			.getLogger(DatabaseService.class);

	private EntityManager em;

	public DatabaseService() {
		EntityManagerFactory factory = Persistence
				.createEntityManagerFactory("mixtapePersistence");
		em = factory.createEntityManager();
		LOG.info("Initialized EntityManager...");
	}

	public EntityManager getEntityManager() {
		return em;
	}

	/**
	 * Flushes the persistence context to the database and closes the database
	 * connection afterwards.
	 */
	public void shutdown() {
		LOG.info("Shutting down database connection...");

		em.close();
		LOG.info("Database connection terminated...");
	}

	public List<Song> getAllSongs() {
		return em.createNamedQuery("getAllSongs", Song.class).getResultList();
	}

	public List<Song> getNewSongs() {
		return em.createNamedQuery("getNewSongs", Song.class).getResultList();
	}

	public List<Song> getAnalysedSongs() {
		return em.createNamedQuery("getAnalysedSongs", Song.class)
				.getResultList();
	}

	public <T> T findObjectById(long entityId, Class<T> entityClass) {
		T entity = em.find(entityClass, entityId);
		LOG.debug("Found entity with Id " + entityId + " of type "
				+ entityClass + ": " + (entity != null ? "YES" : "NO"));
		return entity;
	}

	public <T> void persist(T entity, Class<T> entityClass) {
		em.persist(em.merge(entity));
		LOG.debug("Persisted entity of type " + entityClass + ".");
	}

}
