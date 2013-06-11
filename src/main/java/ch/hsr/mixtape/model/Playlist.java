package ch.hsr.mixtape.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import ch.hsr.mixtape.exception.InvalidPlaylistException;

/**
 * @author Stefan Derungs
 */
@Entity
@NamedQueries({ @NamedQuery(name = "deleteAllPlaylists", query = "DELETE FROM Playlist p") })
public class Playlist {

	private static final String UNINITIALIZED_PLAYLIST_MESSAGE = "Playlist has not been initialized. "
			+ "You have to initialize and define playlist settings first.";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private PlaylistSettings settings;

	@OneToMany(cascade = CascadeType.ALL)
	private List<PlaylistItem> items = new ArrayList<PlaylistItem>();

	public Playlist() {
	}

	public Playlist(PlaylistSettings settings) {
		this.settings = settings;
	}

	public boolean isInitialized() {
		return settings != null;
	}

	public boolean isEmpty() {
		return items.isEmpty();
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
	 * Get the current playlist settings.
	 */
	public PlaylistSettings getSettings() {
		return settings;
	}

	/**
	 * Searches the current playlist for a songId.
	 * 
	 * @return If no song is found -1 is returned.
	 */
	public int getSongIndexById(int songId) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getCurrent().getId() == songId)
				return i;
		}
		return -1;
	}

	/**
	 * @throws InvalidPlaylistException
	 */
	public PlaylistItem removeItem(int songId) throws InvalidPlaylistException {
		if (!isInitialized())
			throw new InvalidPlaylistException(UNINITIALIZED_PLAYLIST_MESSAGE);

		return items.remove(getSongIndexById(songId));
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
