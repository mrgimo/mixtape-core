package ch.hsr.mixtape.application.service;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.application.ApplicationFactory;
import ch.hsr.mixtape.application.DummyData;
import ch.hsr.mixtape.application.UninitializedPlaylistException;
import ch.hsr.mixtape.model.PlaylistSettings;
import ch.hsr.mixtape.model.Song;

/**
 * This service is responsible for managing the playlist.
 * 
 * @author Stefan Derungs
 */
public class PlaylistService {

	private static final Logger log = LoggerFactory
			.getLogger(PlaylistService.class);

	private ReentrantReadWriteLock lock;

	private boolean initialized = false;

	private PlaylistSettings settings;

	private ArrayList<Song> songs;

	public PlaylistService() {
		lock = new ReentrantReadWriteLock(true);
		log.debug("Initialized PlaylistManager...");
	}

	public boolean createPlaylist(PlaylistSettings settings) {
		lock.writeLock().lock();
		log.debug("Acquired Write-Lock in `createNewPlaylist`.");

		this.settings = settings;
		boolean result = initializePlaylist();

		lock.writeLock().unlock();
		log.debug("Released Write-Lock in `createNewPlaylist`.");

		return result;
	}

	private boolean initializePlaylist() {
		lock.writeLock().lock();
		log.debug("Acquired Write-Lock in `initializePlaylist`.");

		songs = DummyData.getDummyPlaylist();
		initialized = true;

		lock.writeLock().unlock();
		log.debug("Released Write-Lock in `initializePlaylist`.");

		return initialized;
	}

	public ArrayList<Song> getCurrentPlaylist()
			throws UninitializedPlaylistException {
		lock.readLock().lock();
		log.debug("Acquired Read-Lock in `getCurrentPlaylist`.");

		if (!initialized)
			throw new UninitializedPlaylistException(
					"No playlist available. You have to create a playlist first.");

		lock.readLock().unlock();
		log.debug("Released Read-Lock in `getCurrentPlaylist`.");

		return songs;
	}

	public PlaylistSettings getCurrentPlaylistSettings()
			throws UninitializedPlaylistException {
		lock.readLock().lock();
		log.debug("Acquired Read-Lock in `getCurrentPlaylist`.");

		if (!initialized)
			throw new UninitializedPlaylistException(
					"No playlist available. You have to create a playlist first.");

		lock.readLock().unlock();
		log.debug("Released Read-Lock in `getCurrentPlaylistSettings`.");

		return settings;
	}

	/**
	 * 
	 * @return True if oldPosition equals newPosition and no sorting is needed.
	 *         True if sorting successful. False if sorting failed (e.g. if the
	 *         song is not at the expected oldPosition).
	 * @throws UninitializedPlaylistException
	 */
	public boolean switchSorting(long songId, int oldPosition, int newPosition)
			throws UninitializedPlaylistException {
		// TODO: persist the playlist change also to database?
		lock.writeLock().lock();
		log.debug("Acquired Write-Lock in `switchSorting`.");

		if (!initialized)
			throw new UninitializedPlaylistException(
					"No playlist available. You have to create a playlist first.");

		if (oldPosition == newPosition)
			return true;

		int songIndex = findIndexOfSongById(songId);
		if (songIndex != oldPosition)
			return false;

		Song song = songs.remove(songIndex);

		if (oldPosition > newPosition)
			songs.add(newPosition - 1, song);
		else
			songs.add(newPosition, song);

		lock.writeLock().unlock();
		log.debug("Released Write-Lock in `switchSorting`.");

		return true;
	}

	public boolean addWish(long songId) throws UninitializedPlaylistException {
		lock.writeLock().lock();
		log.debug("Acquired Write-Lock in `placeWish`.");

		if (!initialized)
			throw new UninitializedPlaylistException(
					"Uninitialized playlist. You have to create a playlist first.");

		log.debug("Looking for id: " + songId);
		// TODO do path-finding and add song at appropriate place
		for (Song s : ApplicationFactory.getDatabaseManager()
				.getDummyDatabase()) {
			log.debug(s.getId() + "\t\t" + s.getTitle());
			if (s.getId() == songId) {
				s.setUserWish(true);
				songs.add(s);
				return true;
			}
		}

		lock.writeLock().unlock();
		log.debug("Released Write-Lock in `placeWish`.");

		return false;
	}

	/**
	 * Searches the current playlist for a songId.
	 * 
	 * @return If no song is found -1 is returned.
	 * @throws UninitializedPlaylistException
	 */
	private int findIndexOfSongById(long songId)
			throws UninitializedPlaylistException {
		if (!initialized)
			throw new UninitializedPlaylistException(
					"Playlist has not been initialized.");

		for (int i = 0; i < songs.size(); i++) {
			if (songs.get(i).getId() == songId)
				return i;
		}
		return -1;
	}

}
