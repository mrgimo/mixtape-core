package ch.hsr.mixtape.application.service;

import static ch.hsr.mixtape.application.ApplicationFactory.getPlaylistService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.mixtape.application.PathResolver;
import ch.hsr.mixtape.exception.InvalidPlaylistException;
import ch.hsr.mixtape.model.Song;
import ch.hsr.mixtape.util.FilepathExtractor;

import com.google.common.io.Files;

/**
 * This class is only for demo purposes.
 */
public class PlaylistPlaybackService {

	private static final Logger LOG = LoggerFactory
			.getLogger(PlaylistPlaybackService.class);

	public static String TEMP_DIR = "src/main/webapp/resources/temp/";

	private Song currentSong;

	private File tempCurrentSong;

	private void initCurrentSong() throws InvalidPlaylistException, IOException {
		LOG.debug("Initializing current Song...");
		currentSong = getPlaylistService().getPlaylist().getCurrentSong();

		Path currentSongPath = PathResolver.getAbsoluteSongFilepath(currentSong
				.getFilepath());
		tempCurrentSong = getTempSongFile(currentSong);
		Files.copy(currentSongPath.toFile(), tempCurrentSong);
		LOG.debug("Initializing current Song done...");
	}

	public void advanceToNextSong() throws InvalidPlaylistException,
			IOException {
		LOG.debug("Advancing to next song...");
		if (tempCurrentSong != null)
			tempCurrentSong.delete();

		getPlaylistService().advance();

		initCurrentSong();
		LOG.debug("Next song is set up...");
	}

	public String getCurrentSong() throws InvalidPlaylistException, IOException {
		LOG.debug("Getting current song...");
		if (currentSong == null)
			initCurrentSong();

		return getWebPath(currentSong);
	}

	private String getWebPath(Song song) {
		return "resources/temp/" + FilepathExtractor.getBasename(song.getFilepath())
				+ "." + FilepathExtractor.getExtension(song.getFilepath());
	}

	private File getTempSongFile(Song current) {
		if (!Paths.get(TEMP_DIR).toFile().isDirectory())
			(new File(TEMP_DIR)).mkdir();

		return new File(TEMP_DIR
				+ FilepathExtractor.getBasename(current.getFilepath()) + "."
				+ FilepathExtractor.getExtension(current.getFilepath()));
	}
}
