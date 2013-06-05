package ch.hsr.mixtape.application.service;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.application.ApplicationFactory;
import ch.hsr.mixtape.application.DummyData;
import ch.hsr.mixtape.application.service.PlaylistStreamService.Streamer;
import ch.hsr.mixtape.exception.PlaylistChangedException;
import ch.hsr.mixtape.exception.UninitializedPlaylistException;
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

	private ReentrantReadWriteLock lock;

	private boolean initialized = false;

	private PlaylistSettings settings;

	private Song currentlyPlaying;

	private ArrayList<Song> nextSongs;

	public PlaylistService() {
		lock = new ReentrantReadWriteLock(true);
		LOG.debug("Initialized PlaylistManager...");
	}

	public boolean createPlaylist(PlaylistSettings settings) {
		try {
			lock.writeLock().lock();
			LOG.debug("Acquired Write-Lock in `createNewPlaylist`.");

			this.settings = settings;
			boolean result = initializePlaylist();

			return result;
		} finally {
			lock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `createNewPlaylist`.");
		}
	}

	private boolean initializePlaylist() {
		try {
			lock.writeLock().lock();
			LOG.debug("Acquired Write-Lock in `initializePlaylist`.");

			nextSongs = DummyData.getDummyPlaylist();
			initialized = true;

			return initialized;
		} finally {
			lock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `initializePlaylist`.");
		}
	}

	/**
	 * 
	 * @param streamer
	 *            This parameter is only to make sure, that advance can be
	 *            called by the streamer.
	 * @return 
	 * @throws UninitializedPlaylistException
	 */
	public Song advance(Streamer streamer)
			throws UninitializedPlaylistException {
		try {
			lock.writeLock().lock();
			LOG.debug("Acquired Read-Lock in `getCurrentSong`.");

			if (!initialized)
				throw new UninitializedPlaylistException(
						"No playlist available. You have to create a playlist first.");

			if (nextSongs.isEmpty())
				throw new UninitializedPlaylistException(
						"No more songs in playlist. Please create a new playlist "
								+ "or add a wish to the current one.");

			currentlyPlaying = nextSongs.remove(0);

			// TODO: listening-subscriber-handling?

			return currentlyPlaying;
		} finally {
			lock.writeLock().unlock();
			LOG.debug("Released Read-Lock in `getCurrentSong`.");
		}
	}

	public Song getCurrentSong() throws UninitializedPlaylistException {
		try {
			lock.readLock().lock();
			LOG.debug("Acquired Read-Lock in `getCurrentSong`.");

			if (!initialized)
				throw new UninitializedPlaylistException(
						"No playlist available. You have to create a playlist first.");

			return currentlyPlaying;
		} finally {
			lock.readLock().unlock();
			LOG.debug("Released Read-Lock in `getCurrentSong`.");
		}
	}

	public ArrayList<Song> getNextSongs() throws UninitializedPlaylistException {
		try {
			lock.readLock().lock();
			LOG.debug("Acquired Read-Lock in `getNextSongs`.");

			if (!initialized)
				throw new UninitializedPlaylistException(
						"No playlist available. You have to create a playlist first.");

			return nextSongs;
		} finally {
			lock.readLock().unlock();
			LOG.debug("Released Read-Lock in `getNextSongs`.");
		}
	}

	public ArrayList<Song> getNextNSongs(int n)
			throws UninitializedPlaylistException {
		try {
			lock.readLock().lock();
			LOG.debug("Acquired Read-Lock in `getNextNSongs`.");

			if (!initialized)
				throw new UninitializedPlaylistException(
						"No playlist available. You have to create a playlist first.");

			ArrayList<Song> upcoming = new ArrayList<Song>(n);
			for (int i = 0; i < n; i++)
				upcoming.add(nextSongs.get(i));

			return upcoming;
		} finally {
			lock.readLock().unlock();
			LOG.debug("Released Read-Lock in `getNextNSongs`.");
		}
	}

	public PlaylistSettings getCurrentPlaylistSettings()
			throws UninitializedPlaylistException {
		try {
			lock.readLock().lock();
			LOG.debug("Acquired Read-Lock in `getCurrentPlaylist`.");

			if (!initialized)
				throw new UninitializedPlaylistException(
						"No playlist available. You have to create a playlist first.");

			return settings;
		} finally {
			lock.readLock().unlock();
			LOG.debug("Released Read-Lock in `getCurrentPlaylistSettings`.");
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
	 * @return True if oldPosition equals newPosition and no sorting is needed
	 *         or if sorting successful.
	 * @throws UninitializedPlaylistException
	 * @throws PlaylistChangedException
	 *             Is thrown if the given oldPosition does not match with the
	 *             songs current position.
	 */
	public boolean alterSorting(long songId, int oldPosition, int newPosition)
			throws UninitializedPlaylistException, PlaylistChangedException {
		try {
			// TODO: persist the playlist change also to database?
			lock.writeLock().lock();
			LOG.debug("Acquired Write-Lock in `switchSorting`.");

			if (!initialized)
				throw new UninitializedPlaylistException(
						"No playlist available. You have to create a playlist first.");

			if (oldPosition == newPosition)
				return true;

			if (findIndexOfSongById(songId) != oldPosition)
				throw new PlaylistChangedException(
						"Song position did not match. Resorting song in playlist failed "
								+ "due to changed playlist. Try again after updating "
								+ "your playlist view.");

			Song song = nextSongs.remove(oldPosition);
			nextSongs.add(newPosition, song);
			DummyData.initializeSimilarities(nextSongs);

			return true;
		} finally {
			lock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `switchSorting`.");
		}
	}

	/**
	 * Adds the given song to the current playlist. A path-finding is performed
	 * to find the best place to put the song.
	 * 
	 * @param songId
	 *            The song to be added.
	 * @return True if adding succeeds. False else.
	 * @throws UninitializedPlaylistException
	 */
	public boolean addWish(long songId) throws UninitializedPlaylistException {
		try {
			lock.writeLock().lock();
			LOG.debug("Acquired Write-Lock in `placeWish`.");

			if (!initialized)
				throw new UninitializedPlaylistException(
						"Uninitialized playlist. You have to create a playlist first.");

			LOG.debug("Looking for id: " + songId);

			// TODO do path-finding and add song at appropriate place

			ArrayList<Song> data = ApplicationFactory.getDatabaseManager()
					.getDummyDatabase();
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).getId() == songId) {
					if (i > 0)
						data.get(i).setSongSimilarity(
								DummyData.getDummySongSimilarity(data.get(i),
										data.get(i - 1)));
					data.get(i).setUserWish(true);
					nextSongs.add(data.get(i));
					LOG.debug("Added wish (songId " + songId + ").");
					return true;
				}
			}
			return false;
		} finally {
			lock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `placeWish`.");
		}
	}

	/**
	 * @param songId
	 *            The song to be removed.
	 * @param songPosition
	 *            This position is provided to double check the playlist has not
	 *            changed since the last request.
	 * @return True if removing succeeds.
	 * @throws UninitializedPlaylistException
	 * @throws PlaylistChangedException
	 *             Is thrown if either the given songPosition does not match
	 *             with the songId or the songPosition does not exist at all.
	 */
	public boolean removeSong(long songId, int songPosition)
			throws UninitializedPlaylistException, PlaylistChangedException {
		try {
			lock.writeLock().lock();
			LOG.debug("Acquired Write-Lock in `removeSong`.");

			if (!initialized)
				throw new UninitializedPlaylistException(
						"Uninitialized playlist. You have to create a playlist first.");

			if (nextSongs.get(songPosition).getId() == songId) {
				Song song = nextSongs.remove(songPosition);
				LOG.debug("Removed song " + song.getTitle() + " (songId "
						+ songId + ").");
				return true;
			} else {
				throw new PlaylistChangedException(
						"Song position did not match. Removing song from playlist failed "
								+ "due to changed playlist. Try again after updating "
								+ "your playlist view.");
			}
		} catch (IndexOutOfBoundsException e) {
			PlaylistChangedException ex = new PlaylistChangedException(
					"Removing song from playlist failed due to changed playlist. "
							+ "Try again after updating your playlist view.");
			ex.addSuppressed(e);
			throw ex;
		} finally {
			lock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `removeSong`.");
		}
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

		for (int i = 0; i < nextSongs.size(); i++) {
			if (nextSongs.get(i).getId() == songId)
				return i;
		}
		return -1;
	}

}
