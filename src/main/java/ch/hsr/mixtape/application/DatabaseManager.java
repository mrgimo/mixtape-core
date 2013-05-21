package ch.hsr.mixtape.application;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.model.Song;

public class DatabaseManager {

	private static final Logger log = LoggerFactory
			.getLogger(DatabaseManager.class);

	private EntityManager entityManager;

	private ArrayList<Song> dummyDatabase;

	public DatabaseManager() {
		EntityManagerFactory factory = Persistence
				.createEntityManagerFactory("mixtapePersistence");
		entityManager = factory.createEntityManager();
		log.info("Initialized EntityManager...");

		dummyDatabase = DummyData.getDummyDatabase(); // TODO: implement db
	}

	/**
	 * TODO remove and implement
	 * 
	 * @return
	 */
	public ArrayList<Song> getDummyDatabase() {
		return dummyDatabase;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Flushes the persistence context to the database and closes the database
	 * connection afterwards.
	 */
	public void shutdown() {
		log.info("Shutting down database connection...");

		entityManager.close();
		log.info("Database connection terminated...");
	}

}
