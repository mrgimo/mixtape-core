package ch.hsr.mixtape.distancefunction;

import ch.hsr.mixtape.data.Song;

public interface DistanceFunction {
	
	public double distance(Song songX, Song songY);
}
