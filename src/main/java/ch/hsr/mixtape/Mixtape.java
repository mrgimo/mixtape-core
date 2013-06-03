package ch.hsr.mixtape;

import static ch.hsr.mixtape.MathUtils.square;
import static java.util.Arrays.fill;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.util.FastMath;

import ch.hsr.mixtape.domain.Song;
import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesExtractor;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesExtractor;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesExtractor;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesExtractor;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class Mixtape {

	private static final String[] ALLOWED_SUFFIXES = {
			".mp3"
	};

	private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime()
			.availableProcessors());

	private final List<Song> songs;

	private final List<Table<Song, Song, Double>> distances;

	private Mixtape(List<Song> songs, List<Table<Song, Song, Double>> distances) {
		this.songs = songs;
		this.distances = distances;
	}

	private static Mixtape loadSongs(Collection<FeatureExtractor<?, ?>> featuresExtractors,
			Collection<File> pathsToSongs)
			throws InterruptedException, ExecutionException, IOException {
		List<File> songFiles = new FileFinder(pathsToSongs, createSongFileFilter()).find();
		System.out.println("Processing " + songFiles.size() + " songs.");
		List<Song> songs = initSongs(songFiles);

		List<FeatureProcessor<?, ?>> processors = initExtractors(featuresExtractors, songs.size());
		for (Song song : songs) {
			System.out.println("Processing song '" + song.getFilePath() + "'.");

			new SamplePublisher(song, processors).publish();
			for (FeatureProcessor<?, ?> processor : processors)
				processor.postprocess(song);

			System.out.println("before gc " + Runtime.getRuntime().freeMemory());
			Runtime.getRuntime().gc();
			System.out.println("after gc " + Runtime.getRuntime().freeMemory());
		}

		return new Mixtape(songs, getDistances(processors, songs));
	}

	private static FileFilter createSongFileFilter() {
		return new FileFilter() {

			public boolean accept(File file) {
				for (String allowedSuffix : ALLOWED_SUFFIXES)
					if (hasSuffix(file, allowedSuffix))
						return true;

				return false;
			}

		};
	}

	private static boolean hasSuffix(File file, String allowedSuffix) {
		return file.getName().toLowerCase().endsWith(allowedSuffix.toLowerCase());
	}

	private static List<Song> initSongs(List<File> songFiles) {
		List<Song> songs = Lists.newArrayListWithCapacity(songFiles.size());
		for (int id = 0; id < songFiles.size(); id++)
			songs.add(new Song(id, songFiles.get(id).getAbsolutePath()));

		return songs;
	}

	private static List<FeatureProcessor<?, ?>> initExtractors(Collection<FeatureExtractor<?, ?>> featuresExtractors,
			int numberOfSongs) {
		List<FeatureProcessor<?, ?>> extractors = Lists.newArrayListWithCapacity(featuresExtractors.size());
		for (FeatureExtractor<?, ?> featuresExtractor : featuresExtractors)
			extractors.add(new FeatureProcessor<>(featuresExtractor, executor));

		return extractors;
	}

	private static List<Table<Song, Song, Double>> getDistances(List<FeatureProcessor<?, ?>> featureProcessors,
			Collection<Song> songs) {
		System.out.println("Calculating distances.");
		List<Table<Song, Song, Double>> distances = Lists.newArrayList();

		for (FeatureProcessor<?, ?> featureProcessor : featureProcessors) {
			distances.add(featureProcessor.getDistances(songs));
			Runtime.getRuntime().gc();
		}
		
		return distances;
	}

	public double distanceBetween(Song songX, Song songY) {
		double[] weighting = new double[distances.size()];
		fill(weighting, 1);

		return distanceBetween(songX, songY, weighting);
	}

	public double distanceBetween(Song songX, Song songY, double[] weighting) {
		double distance = 0;
		for (int i = 0; i < distances.size(); i++)
			distance += square(distances.get(i).get(songX, songY)) * weighting[i];

		return FastMath.sqrt(distance);
	}

	public List<Song> getSongs() {
		return songs;
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		List<FeatureExtractor<?, ?>> featureExtractors = Arrays.asList(
				new HarmonicFeaturesExtractor(),
				new SpectralFeaturesExtractor(),
				new PerceptualFeaturesExtractor(),
				new TemporalFeaturesExtractor());

		List<File> files = Arrays.asList(new File("songs"));

		long start = System.currentTimeMillis();

		System.out.println("Loading songs...");
		Mixtape mixtape = Mixtape.loadSongs(featureExtractors, files);
		System.out.println("Finished in " + (System.currentTimeMillis() - start) * 0.001 + " seconds.");
		System.out.println();

		for (Song songX : mixtape.getSongs()) {
			for (Song songY : mixtape.getSongs()) {
				System.out.println(songX.getFilePath() + " to " + songY.getFilePath() + " = "
						+ mixtape.distanceBetween(songX, songY));
			}
			System.out.println();
		}
	}
}