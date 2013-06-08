package ch.hsr.mixtape.application;

import java.io.File;

/**
 * This class splits up a given filename in components such as filepath,
 * filename (without path or extension) and extension.
 * 
 * @author Stefan Derungs
 */
public class FilepathExtractor {

	private final static char PATH_SEPARATOR = File.separatorChar;

	private final static char EXTENSION_SEPARATOR = '.';

	/**
	 * @return Returns the file extension without '.' or if there is no
	 *         extension at all, an empty string is returned.
	 */
	public static String getExtension(String filepath) {
		int dot = filepath.lastIndexOf(EXTENSION_SEPARATOR);
		if (dot < 0)
			return "";
		return filepath.substring(dot + 1);
	}

	/**
	 * @return Returns the filename without path and extension.
	 */
	public static String getBasename(String filepath) {
		int dot = filepath.lastIndexOf(EXTENSION_SEPARATOR);
		int sep = filepath.lastIndexOf(PATH_SEPARATOR);

		if (sep < 0) {
			if (dot < 0)
				return filepath;
			else
				return filepath.substring(0, dot);
		} else {
			if (dot < 0)
				return filepath.substring(sep + 1);
			else
				return filepath.substring(sep + 1, dot);
		}
	}

	/**
	 * @return Returns the files path.
	 */
	public static String getPath(String filepath) {
		int sep = filepath.lastIndexOf(PATH_SEPARATOR);

		if (sep < 0)
			return "";
		else
			return filepath.substring(0, sep);
	}

}