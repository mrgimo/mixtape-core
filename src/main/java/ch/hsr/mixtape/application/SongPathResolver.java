package ch.hsr.mixtape.application;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The filepath of the songs are relative paths. This class provides back and
 * forth resolution of these paths.
 * 
 * @author Stefan Derungs
 */
public class SongPathResolver {

	public static final String MIXTAPE_MUSIC_DATA_FILEPATH = System
			.getenv("mixtapeMusicDir");

	public static final Path MIXTAPE_MUSIC_DATA_PATH = Paths
			.get(MIXTAPE_MUSIC_DATA_FILEPATH);

	public static Path getRelativeSongFilepath(Path absoluteSongFilepath) {
		if (!absoluteSongFilepath.isAbsolute())
			return absoluteSongFilepath;

		return MIXTAPE_MUSIC_DATA_PATH.relativize(absoluteSongFilepath);
	}

	public static Path getAbsoluteSongFilepath(String relativeSongFilepath) {
		final Path path = Paths.get(relativeSongFilepath);
		if (path.isAbsolute())
			return path;

		return MIXTAPE_MUSIC_DATA_PATH.resolve(path);
	}

}
