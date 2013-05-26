package ch.hsr.mixtape;

import java.io.File;
import java.io.FileFilter;

public class SongFileFilter implements FileFilter {

	private static final String[] ALLOWED_SUFFIXES = {
			".mp3"
	};

	public boolean accept(File file) {
		for (String allowedSuffix : ALLOWED_SUFFIXES)
			if (hasSuffix(file, allowedSuffix))
				return true;

		return false;
	}

	private boolean hasSuffix(File file, String allowedSuffix) {
		return file.getName().toLowerCase().endsWith(allowedSuffix.toLowerCase());
	}

}