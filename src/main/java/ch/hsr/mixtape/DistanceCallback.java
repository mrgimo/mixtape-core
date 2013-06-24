package ch.hsr.mixtape;

import java.util.Collection;

import ch.hsr.mixtape.model.Distance;
import ch.hsr.mixtape.model.Song;

public interface DistanceCallback {

	void distanceAdded(Song song, Collection<Distance> distances);

}