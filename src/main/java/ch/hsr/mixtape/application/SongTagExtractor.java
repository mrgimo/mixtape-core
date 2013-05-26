package ch.hsr.mixtape.application;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;

import ch.hsr.mixtape.model.Song;

import com.google.common.io.Files;

/**
 * Extracts ID3 tags from songs. Where no ID3 tags are found default values are
 * taken.
 * 
 * @author Stefan Derungs
 */
public class SongTagExtractor {

	private static final String DEFAULT_UNKNOWN = "unknown";

	private static final int DEFAULT_SAMPLING_RATE_IN_HZ = 44100;

	private String filename;

	private Tag tag;

	/**
	 * Extracts ID3 tags for the provided song and saves them into the song.
	 */
	public void extractTagsFromSong(Song song) {
		try {
			filename = Files.getNameWithoutExtension(song.getFilepath());

			AudioFile file = AudioFileIO.read(new File(song.getFilepath()));
			tag = file.getTag();
			AudioHeader header = file.getAudioHeader();

			song.setLengthInSeconds(header.getTrackLength());
			song.setSampleRateInHz(header.getSampleRateAsNumber() > 0 ? header
					.getSampleRateAsNumber() : DEFAULT_SAMPLING_RATE_IN_HZ);

			song.setTitle(getFieldValue(FieldKey.TITLE));
			song.setArtist(getFieldValue(FieldKey.ARTIST));
			song.setAlbum(getFieldValue(FieldKey.ALBUM));

		} catch (Exception e) {
			song.setTitle(getDefault(FieldKey.TITLE));
			song.setArtist(getDefault(FieldKey.ARTIST));
			song.setAlbum(getDefault(FieldKey.ALBUM));
		}
	}

	private String getFieldValue(FieldKey key) {
		try {
			String field = tag.getFirst(key);
			return !field.isEmpty() ? field : getDefault(key);
		} catch (KeyNotFoundException e) {
			return getDefault(key);
		}
	}

	private String getDefault(FieldKey key) {
		if (key == FieldKey.TITLE)
			return filename;
		return DEFAULT_UNKNOWN;
	}

}
