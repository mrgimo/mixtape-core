package ch.hsr.mixtape.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stefan Derungs
 */
public class DatabaseService {

	private static final Logger LOG = LoggerFactory
			.getLogger(DatabaseService.class);

	private EntityManagerFactory emFactory;

	private List<EntityManager> entityManagers = new ArrayList<EntityManager>();

	private EntityManager localEM;

	public DatabaseService() {
//		emFactory = Persistence.createEntityManagerFactory("mixtapePersistence",
//				getEMFactoryOverrides());

		emFactory = Persistence
				.createEntityManagerFactory("mixtapePersistence");

		localEM = getNewEntityManager();

		LOG.info("Initialized EntityManager...");
	}

	private Map<String, Object> getEMFactoryOverrides() {
		Map<String, String> systemEnv = System.getenv();
		String dbPath = systemEnv.get("mixtapeData").trim();
		if (dbPath.endsWith("/"))
			dbPath = dbPath.substring(0, dbPath.length() - 1);

		Map<String, Object> configOverrides = new HashMap<String, Object>();
		for (Entry<String, String> env : systemEnv.entrySet()) {
			if (env.getKey().contains("javax.persistence.jdbc.url")) {
				configOverrides.put("javax.persistence.jdbc.url", env
						.getValue().replace("mixtapeData", dbPath));
			} else if (env.getKey().contains("eclipselink.logging.file")) {
				configOverrides.put("eclipselink.logging.file", env.getValue()
						.replace("mixtapeData", dbPath));
			} else if (env.getKey()
					.contains("eclipselink.application-location")) {
				configOverrides.put("eclipselink.application-location", env
						.getValue().replace("mixtapeData", dbPath));
			}
		}
		return configOverrides;
	}

	public EntityManager getNewEntityManager() {
		synchronized (entityManagers) {
			EntityManager em = emFactory.createEntityManager();
			entityManagers.add(em);

			entityManagers.notifyAll();
			return em;
		}
	}

	public void closeEntityManager(EntityManager em) {
		synchronized (entityManagers) {
			entityManagers.remove(em);
			if (em.isOpen())
				em.close();

			entityManagers.notifyAll();
		}
	}

	/**
	 * Flushes the persistence context to the database and closes the database
	 * connection afterwards.
	 */
	public void shutdown() {
		LOG.info("Shutting down database connection...");

		synchronized (entityManagers) {
			for (EntityManager em : entityManagers) {
				LOG.info("Terminating entity manager resource: "+em);
				terminateEntityManager(em);
			}
			
			LOG.info("Closing entity manager factory now...");
			emFactory.close();
		}

		LOG.info("Database connection terminated...");
	}

	/**
	 * Terminates an entity manager rolling back all unsubmitted changes.
	 */
	private void terminateEntityManager(EntityManager em) {
		if (!em.isOpen())
			return;

		EntityTransaction transaction = em.getTransaction();
		if (transaction.isActive()) {
			transaction.rollback();
			transaction.commit();
		}

		em.close();
	}

	public <T> void persist(T entity, Class<T> entityClass) {
		synchronized (localEM) {
			localEM.getTransaction().begin();
			localEM.persist(localEM.merge(entity));
			localEM.getTransaction().commit();
			LOG.debug("Persisted entity of type " + entityClass + ".");
			localEM.notifyAll();
		}
	}

	/**
	 * This method is just for debugging purposes.
	 */
	public <T> void debugQuery(TypedQuery<T> query,
			Map<String, Object> parameters) {
		Session session = localEM.unwrap(JpaEntityManager.class)
				.getActiveSession();
		DatabaseQuery dbQuery = ((EJBQueryImpl<T>) query).getDatabaseQuery();

		dbQuery.prepareCall(session, new DatabaseRecord());
		System.err.println("Query without replaced parameters: ");
		System.err.println(dbQuery.getSQLString());

		if (parameters != null) {
			DatabaseRecord recordWithValues = new DatabaseRecord();
			for (Entry<String, Object> entry : parameters.entrySet())
				recordWithValues.add(new DatabaseField(entry.getKey()),
						entry.getValue());

			System.err.println("Query with replaced parameters: ");
			System.err.println(dbQuery.getTranslatedSQLString(session,
					recordWithValues));
		}
	}
}
