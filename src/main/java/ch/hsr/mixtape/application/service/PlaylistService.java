package ch.hsr.mixtape.application.service;

import static ch.hsr.mixtape.application.ApplicationFactory.getMixtape;
import static ch.hsr.mixtape.application.ApplicationFactory.getQueryService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.exception.PlaylistChangedException;
import ch.hsr.mixtape.model.Distance;
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

	private ReentrantReadWriteLock playlistLock = new ReentrantReadWriteLock(
			true);

	private Playlist playlist = new Playlist();

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

			if (settings.getStartSongs() == null)
				settings.setStartSongs(new ArrayList<Song>());

			playlist = new Playlist(settings);
			getMixtape().initialMix(playlist);
		} finally {
			playlistLock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `createNewPlaylist`.");
		}
	}

	/**
	 * Returns the current playlist.
	 * 
	 * @throws InvalidPlaylistException
	 */
	public Playlist getPlaylist() throws InvalidPlaylistException {
		try {
			playlistLock.readLock().lock();
			LOG.debug("Acquired Read-Lock in `getPlaylist`.");

			ensurePlaylistIsInitialized();

			return playlist;
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

			if (oldPosition == newPosition)
				return;

			if (playlist.getSongIndexById(songId) != oldPosition)
				throw new PlaylistChangedException(
						"Song position did not match. Resorting song in playlist failed "
								+ "due to changed playlist. Try again after updating "
								+ "your playlist view.");

			List<PlaylistItem> playlistItems = playlist.getItems();
			moveItem(oldPosition, newPosition, playlistItems);
			updateAntecessors(oldPosition, newPosition, playlistItems);

			LOG.debug("Resorted song with id " + songId + ".");
		} finally {
			playlistLock.writeLock().unlock();
			LOG.debug("Released Write-Lock in `alterSorting`.");
		}
	}

	private void moveItem(int oldPosition, int newPosition,
			List<PlaylistItem> playlistItems) {
		PlaylistItem item = playlistItems.remove(oldPosition);
		playlistItems.add(newPosition, item);
	}

	private void updateAntecessors(int oldPosition, int newPosition,
			List<PlaylistItem> playlistItems) {

		PlaylistItem oldSuccessor = playlistItems.get(oldPosition + 1);
		oldSuccessor.setAntecessor(playlistItems.get(oldPosition).getCurrent());
		updateSimilarity(oldSuccessor);

		PlaylistItem movedItem = playlistItems.get(newPosition);
		movedItem
				.setAntecessor(playlistItems.get(newPosition - 1).getCurrent());
		updateSimilarity(movedItem);

		PlaylistItem newSuccessor = playlistItems.get(newPosition + 1);
		newSuccessor.setAntecessor(movedItem.getCurrent());
		updateSimilarity(newSuccessor);
	}

	private void updateSimilarity(PlaylistItem playlistItem) {
		Distance distance = getMixtape().distanceBetween(
				playlistItem.getCurrent(), playlistItem.getAntecessor());

		PlaylistSettings playlistSettings = playlist.getSettings();

		int harmonicSimilarity = (int) (100 - distance.getHarmonicDistance()
				* playlistSettings.getHarmonicSimilarity());
		int perceptualSimilarity = (int) (100 - distance
				.getPerceptualDistance()
				* playlistSettings.getPerceptualSimilarity());
		int spectralSimilarity = (int) (100 - distance.getSpectralDistance()
				* playlistSettings.getSpectralSimilarity());
		int temporalSimilarity = (int) (100 - distance.getTemporalDistance()
				* playlistSettings.getTemporalSimilarity());

		playlistItem.setHarmonicSimilarity(harmonicSimilarity);
		playlistItem.setPerceptualSimilarity(perceptualSimilarity);
		playlistItem.setSpectralSimilarity(spectralSimilarity);
		playlistItem.setTemporalSimilarity(temporalSimilarity);

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

			getMixtape().mixAnotherSong(playlist, song);

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

			List<PlaylistItem> playlistItems = playlist.getItems();

			int songIndexById = playlist.getSongIndexById(songId);

			if (songIndexById != -1) {
				PlaylistItem removed = playlistItems.remove(songIndexById);
				LOG.debug("Removed song " + removed.getCurrent().getTitle()
						+ " from playlist.");

				if (playlistItems.size() > songIndexById) {
					playlistItems.get(songIndexById).setAntecessor(
							playlistItems.get(songIndexById - 1).getCurrent());
					updateSimilarity(playlistItems.get(songIndexById));
				}
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
		Song song = getQueryService().findObjectById(songId, Song.class);
		if (song == null)
			throw new EntityNotFoundException("The song with id " + songId
					+ " was not found.");
		return song;
	}

}
