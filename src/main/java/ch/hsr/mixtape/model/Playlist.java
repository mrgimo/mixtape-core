package ch.hsr.mixtape.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.exception.PlaylistChangedException;

/**
 * @author Stefan Derungs
 */
@Entity
public class Playlist implements Cloneable {

	private static final String UNINITIALIZED_PLAYLIST_MESSAGE = "Playlist has not been initialized. "
			+ "You have to initialize and define playlist settings first.";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private PlaylistSettings settings;
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<PlaylistItem> items;

	public Playlist() {
		items = new ArrayList<PlaylistItem>();
	}

	public boolean isInitialized() {
		return settings != null;
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public void resetPlaylist(PlaylistSettings settings) {
		this.settings = settings;
		items = new ArrayList<PlaylistItem>();
	}

	public Song getCurrentSong() {
		if (!isInitialized() || isEmpty())
			return null;

		return items.get(0).getCurrent();
	}

	public List<PlaylistItem> getItems() {
		return items;
	}

	/**
	 * @throws InvalidPlaylistException
	 */
	public void addItem(PlaylistItem item) throws InvalidPlaylistException {
		if (!isInitialized())
			throw new InvalidPlaylistException(UNINITIALIZED_PLAYLIST_MESSAGE);

		items.add(item);
	}

	/**
	 * @throws InvalidPlaylistException
	 */
	public void addAllItems(ArrayList<PlaylistItem> items)
			throws InvalidPlaylistException {
		if (!isInitialized())
			throw new InvalidPlaylistException(UNINITIALIZED_PLAYLIST_MESSAGE);

		items.addAll(items);
	}

	/**
	 * Clones the current playlist settings and returns the clone.
	 */
	public PlaylistSettings getSettings() {
		return settings.clone();
	}

	/**
	 * Searches the current playlist for a songId.
	 * 
	 * @return If no song is found -1 is returned.
	 */
	private int getSongIndexById(long songId) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getCurrent().getId() == songId)
				return i;
		}
		return -1;
	}

	/**
	 * @throws InvalidPlaylistException
	 */
	public PlaylistItem removeItem(long songId) throws InvalidPlaylistException {
		if (!isInitialized())
			throw new InvalidPlaylistException(UNINITIALIZED_PLAYLIST_MESSAGE);

		return items.remove(getSongIndexById(songId));
	}

	/**
	 * Alter order of a listed song.
	 * 
	 * @param songId
	 * @param oldPosition
	 *            The purpose of this parameter is to overcome the problem of
	 *            concurrent playlist changes when playlist viewed by different
	 *            users is out of sync (e.g. over webinterface).
	 * @param newPosition
	 * @throws PlaylistChangedException
	 */
	public void alterSorting(long songId, int oldPosition, int newPosition)
			throws PlaylistChangedException {
		if (oldPosition == newPosition)
			return;

		if (getSongIndexById(songId) != oldPosition)
			throw new PlaylistChangedException(
					"Song position did not match. Resorting song in playlist failed "
							+ "due to changed playlist. Try again after updating "
							+ "your playlist view.");

		PlaylistItem item = items.remove(oldPosition);
		items.add(newPosition, item);
		// TODO: update similarities
	}

	/**
	 * Advance playlist to the next song, only if there are any songs left in
	 * the list.
	 */
	public void advance() {
		if (isEmpty())
			return;

		items.remove(0);
	}

	@Override
	public Playlist clone() {
		Playlist p = new Playlist();
		p.id = -1;

		ArrayList<PlaylistItem> clonedItems = new ArrayList<PlaylistItem>(
				this.items.size());
		Collections.copy(clonedItems, this.items);
		p.items = clonedItems;
		p.settings = this.settings.clone();
		return p;
	}

	public PlaylistItem getLastItem() {
		return items.get(items.size() - 1);
	}

	public List<Song> getSongsInPlaylist() {

		ArrayList<Song> songsInPlaylist = new ArrayList<Song>();

		for (PlaylistItem playlistItem : items)
			songsInPlaylist.add(playlistItem.getCurrent());
		
		return songsInPlaylist;
	}

}
