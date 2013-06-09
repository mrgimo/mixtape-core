package ch.hsr.mixtape.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.exception.PlaylistChangedException;
import ch.hsr.mixtape.model.Playlist;
import ch.hsr.mixtape.model.PlaylistItem;
import ch.hsr.mixtape.model.PlaylistSettings;
import ch.hsr.mixtape.model.Song;

/**
 * This service is responsible for managing the playlist.
 * 
 * @author Stefan Derungs
 */
public class PlaylistService {

	private static final Logger LOG = LoggerFactory
			.getLogger(PlaylistService.class);

	private static final DatabaseService DB = ApplicationFactory
			.getDatabaseService();

	private ReentrantReadWriteLock playlistLock = new ReentrantReadWriteLock(
			true);

	private ReentrantLock subscriberLock = new ReentrantLock(true);

	private Playlist playlist = new Playlist();

	private ArrayList<PlaylistSubscriber> subscribers = new ArrayList<PlaylistSubscriber>();

	/**
	 * Add subscriber if not already subscribed.
	 */
	public void subscribeToPlaylist(PlaylistSubscriber subscriber) {
		try {
			subscriberLock.lock();
			LOG.debug("Acquired Write-Lock in `subscribeToPlaylist`.");

			if (!subscribers.contains(subscriber))
				subscribers.add(subscriber);
		} finally {
			subscriberLock.unlock();
			LOG.debug("Released Write-Lock in `subscribeToPlaylist`.");
		}
	}

	/**
	 * Remove subscriber if present.
	 */
	public void unsubscribeFromPlaylist(PlaylistSubscriber subscriber) {
		try {
			subscriberLock.lock();
			LOG.debug("Acquired Write-Lock in `unsubscribeFromPlaylist`.");

			subscribers.remove(subscriber);
		} finally {
			subscriberLock.unlock();
			LOG.debug("Released Write-Lock in `unsubscribeFromPlaylist`.");
		}
	}

	private void persistPlaylistAndNotifySubscribers() {
		DB.persist(playlist, Playlist.class);

		for (PlaylistSubscriber ps : subscribers)
			ps.notifyPlaylistChanged();
	}

	public boolean isPlaylistInitialized() {
		return playlist != null && playlist.isInitialized();
	}

	/**
	 * @throws InvalidPlaylistException
	 */
	private void ensurePlaylistIsInitialized() throws InvalidPlaylistException {
		if (!isPlaylistInitialized())
			throw new InvalidPlaylistException(
					"Uninitialized playlist. You have to create a playlist first.");
	}

	public void createPlaylist(PlaylistSettings settings)
			throws InvalidPlaylistException {
		try {
			playlistLock.writeLock().lock();
			LOG.debug("Acquired Write-Lock in `createNewPlaylist`.");

			playlist = new Playlist(settings);
			ApplicationFactory.getMixtapeService().initialMix(playlist);
			

			persistPlaylistAndNotifySubscribers();
		} finally {
			playlistLock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `createNewPlaylist`.");
		}
	}

	/**
	 * Returns a clone of the current playlist.
	 * 
	 * @throws InvalidPlaylistException
	 */
	public Playlist getPlaylist() throws InvalidPlaylistException {
		try {
			playlistLock.readLock().lock();
			LOG.debug("Acquired Read-Lock in `getPlaylist`.");

			ensurePlaylistIsInitialized();

			return playlist.clone();
		} finally {
			playlistLock.readLock().unlock();
			LOG.debug("Released Read-Lock in `getPlaylist`.");
		}
	}

	public PlaylistSettings getPlaylistSettings()
			throws InvalidPlaylistException {
		try {
			playlistLock.readLock().lock();
			LOG.debug("Acquired Read-Lock in `getPlaylistSettings`.");

			ensurePlaylistIsInitialized();

			return playlist.getSettings();
		} finally {
			playlistLock.readLock().unlock();
			LOG.debug("Released Read-Lock in `getPlaylistSettings`.");
		}
	}

	public void advance() throws InvalidPlaylistException {
		try {
			playlistLock.writeLock().lock();
			LOG.debug("Acquired Read-Lock in `advance`.");

			ensurePlaylistIsInitialized();

			playlist.advance();

			persistPlaylistAndNotifySubscribers();
		} finally {
			playlistLock.writeLock().unlock();
			LOG.debug("Released Read-Lock in `advance`.");
		}
	}

	/**
	 * 
	 * @param songId
	 *            The song to be sorted.
	 * @param oldPosition
	 *            The songs old position. This position is provided to double
	 *            check the playlist has not changed since the last request.
	 * @param newPosition
	 *            The songs new position.
	 * @return
	 * @return True if oldPosition equals newPosition and no sorting is needed
	 *         or if sorting successful.
	 * @throws InvalidPlaylistException
	 * @throws PlaylistChangedException
	 *             Is thrown if the given oldPosition does not match with the
	 *             songs current position.
	 */
	public void alterSorting(int songId, int oldPosition, int newPosition)
			throws InvalidPlaylistException, PlaylistChangedException {
		try {
			playlistLock.writeLock().lock();
			LOG.debug("Acquired Write-Lock in `alterSorting`.");

			ensurePlaylistIsInitialized();

			playlist.alterSorting(songId, oldPosition, newPosition);

			updatePlaylistItems();
			persistPlaylistAndNotifySubscribers();
			LOG.debug("Resorted song with id " + songId + ".");
		} finally {
			playlistLock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `alterSorting`.");
		}
	}

	private void updatePlaylistItems() {
		playlist.getItems();
	}

	/**
	 * Adds the given song to the current playlist. A path-finding is performed
	 * to find the best place to put the song.
	 * 
	 * @param songId
	 *            The song to be added.
	 * @return True if adding succeeds. False else.
	 * @throws InvalidPlaylistException
	 * @throws EntityNotFoundException
	 *             If song could not be found in database.
	 */
	public void addWish(int songId) throws InvalidPlaylistException,
			EntityNotFoundException {
		try {
			playlistLock.writeLock().lock();
			LOG.debug("Acquired Write-Lock in `addWish`.");

			ensurePlaylistIsInitialized();
			Song song = mapSong(songId);

			/*
			 * pathfinding:
			 * 
			 * new Playlist : mixtape.initialMix(playlist)
			 * 
			 * add one song to playlist: mixtape.mixAnotherSong(playlist,
			 * addedSong)
			 * 
			 * add multiple songs to playlist:
			 * mixtape.mixMultipleSongs(playlist, addedSongs)
			 */

			// TODO do path-finding and add song at appropriate place
			PlaylistItem lastItem;
			List<PlaylistItem> playlistItems = playlist.getItems();
			if (!playlistItems.isEmpty())
				lastItem = playlistItems.get(playlistItems.size() - 1);
			else
				lastItem = null;

			// ArrayList<PlaylistItem> items = Pathfinder.findPath(song,
			// lastItem,
			// playlist.getSettings());
			// playlist.addAllItems(items);
			//
			// DistanceUpdater.updatePlaylistDistances(playlist);

			updatePlaylistItems();
			persistPlaylistAndNotifySubscribers();
			LOG.debug("Added wish:" + song.getTitle() + ".");
		} finally {
			playlistLock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `addWish`.");
		}
	}

	/**
	 * @param songId
	 *            The song to be removed.
	 * @param expectedSongPosition
	 *            This position is provided to double check the playlist has not
	 *            changed since the last request.
	 * @return True if removing succeeds.
	 * @throws InvalidPlaylistException
	 * @throws PlaylistChangedException
	 *             Is thrown if either the given songPosition does not match
	 *             with the songId or the songPosition does not exist at all.
	 */
	public void removeSong(int songId, int expectedSongPosition)
			throws InvalidPlaylistException, PlaylistChangedException {
		try {
			playlistLock.writeLock().lock();
			LOG.debug("Acquired Write-Lock in `removeSong`.");

			ensurePlaylistIsInitialized();

			PlaylistItem removed;
			if ((removed = playlist.removeItem(songId)) != null) {
				updatePlaylistItems();
				persistPlaylistAndNotifySubscribers();
				LOG.debug("Removed song " + removed.getCurrent().getTitle()
						+ " from playlist.");
			}
		} catch (IndexOutOfBoundsException e) {
			PlaylistChangedException ex = new PlaylistChangedException(
					"Removing song from playlist failed due to changed playlist. "
							+ "Try again after updating your playlist view.");
			ex.addSuppressed(e);
			throw ex;
		} finally {
			playlistLock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `removeSong`.");
		}
	}

	/**
	 * @throws EntityNotFoundException
	 *             If song could not be found in database.
	 */
	private Song mapSong(int songId) throws EntityNotFoundException {
		LOG.debug("Looking for song id: " + songId);
		Song song = DB.findObjectById(songId, Song.class);
		if (song == null)
			throw new EntityNotFoundException("The song with id " + songId
					+ " was not found.");
		return song;
	}

}
