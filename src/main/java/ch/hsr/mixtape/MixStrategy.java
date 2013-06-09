package ch.hsr.mixtape;

import java.util.List;

import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.model.Playlist;
import ch.hsr.mixtape.model.Song;

public interface MixStrategy {

	public abstract void initialMix(Playlist playList)
			throws InvalidPlaylistException;

	public abstract void mixMultipleSongs(Playlist playlist,
			List<Song> addedSongs) throws InvalidPlaylistException;

	public abstract void mixAnotherSong(Playlist playlist, Song addedSong)
			throws InvalidPlaylistException;

}