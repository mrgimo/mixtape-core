package ch.hsr.mixtape.model;

import java.io.File;

import ch.hsr.mixtape.io.AudioChannel;

public class SongStream {
	
	private Song song;

	public SongStream(Song song) {
		this.song = song;
		AudioChannel audioChannel = AudioChannel.load(new File(song.getFilepath()));
		
	}

}
