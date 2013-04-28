package ch.hsr.mixtape;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import ch.hsr.mixtape.data.Song;
import ch.hsr.mixtape.distancefunction.DistanceGenerator;
import ch.hsr.mixtape.extraction.AudioExtractor;
import ch.hsr.mixtape.library.LibraryController;

public class MixTape {

	public static void main(String[] args) {
		Mixer mixer = new Mixer();

		mixer.mixSound();
	}
}
