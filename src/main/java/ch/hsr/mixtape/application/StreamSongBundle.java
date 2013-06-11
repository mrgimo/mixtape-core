package ch.hsr.mixtape.application;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.hsr.mixtape.model.Song;

public class StreamSongBundle {

	private Song song;

	private AudioFormat format;

	private ReadableByteChannel audio;

	public StreamSongBundle(Song song) throws UnsupportedAudioFileException,
			IOException {
		this.song = song;
		File file = new File(song.getFilepath());
		AudioInputStream in = AudioSystem.getAudioInputStream(file);
		format = in.getFormat();
		audio = Channels.newChannel(in);
	}

	public Song getSong() {
		return song;
	}

	public AudioFormat getAudioFormat() {
		return format;
	}

	public ReadableByteChannel getAudio() {
		return audio;
	}

}