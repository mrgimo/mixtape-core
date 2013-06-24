package ch.hsr.mixtape.application;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The filepath of the songs are relative paths. This class provides back and
 * forth resolution of these paths.
 * 
 * @author Stefan Derungs
 */
public class PathResolver {

	private static final String MIXTAPE_DATA_DIRECTORY = System.getenv("mixtapeData");
	
	public static final String MUSIC_DIRECTORY_FILEPATH = System
			.getenv("mixtapeMusicDir");

	private static Path MIXTAPE_MUSIC_DATA_PATH;

	public static Path getRelativeSongFilepath(Path absoluteSongFilepath) {
		// Make sure MIXTAPE_MUSIC_DATA_PATH is initialized!
		getMusicDataPath();

		if (!absoluteSongFilepath.isAbsolute())
			return absoluteSongFilepath;

		return MIXTAPE_MUSIC_DATA_PATH.relativize(absoluteSongFilepath);
	}

	public static Path getAbsoluteSongFilepath(String relativeSongFilepath) {
		// Make sure MIXTAPE_MUSIC_DATA_PATH is initialized!
		getMusicDataPath();

		final Path path = Paths.get(relativeSongFilepath);
		if (path.isAbsolute())
			return path;
		
		return MIXTAPE_MUSIC_DATA_PATH.resolve(path);
	}

	public static Path getMusicDataPath() {
		if (MIXTAPE_MUSIC_DATA_PATH == null) {
			try {
				MIXTAPE_MUSIC_DATA_PATH = Paths.get(MUSIC_DIRECTORY_FILEPATH);
			} catch (Exception e) {
				throw new RuntimeException(
						"MusicDataFilepath could not be retrieved.", e);
			}
		}

		return MIXTAPE_MUSIC_DATA_PATH;
	}
	
	public static String getMixtapteDataPathname() {
		String pathname = MIXTAPE_DATA_DIRECTORY;
		if (!pathname.isEmpty() && !pathname.endsWith("/"))
			pathname += "/";
		
		return pathname;
	}

}
