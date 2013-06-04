package ch.hsr.mixtape;

import static ch.hsr.mixtape.MathUtils.square;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.hsr.mixtape.domain.Song;
import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesExtractor;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesExtractor;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesExtractor;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesExtractor;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public class Mixtape {

	private static final String[] ALLOWED_SUFFIXES = {
			".mp3"
	};

	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	private static final ListeningExecutorService executor = listeningDecorator(newFixedThreadPool(AVAILABLE_PROCESSORS));

	private final List<Song> songs;

	private final double[][][] distances;

	private Mixtape(List<Song> songs, double[][][] distances) {
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
		double[][][] distances = calcDistances(songs, processors);

		executor.shutdown();

		return new Mixtape(songs, distances);
	}

	private static double[][][] calcDistances(List<Song> songs, List<FeatureProcessor<?, ?>> processors)
			throws IOException, InterruptedException, ExecutionException {
		double[][][] distances = new double[songs.size()][songs.size()][processors.size()];

		SamplePublisher publisher = new SamplePublisher(processors);
		for (int x = 0; x < songs.size(); x++) {
			Song songX = songs.get(x);
			String nameX = "'" + new File(songX.getFilePath()).getName() + "'";
			System.out.println("Processing song " + nameX + ".");

			publisher.publish(songX);

			System.out.println("Postprocessing song " + nameX + ".");
			for (FeatureProcessor<?, ?> processor : processors)
				processor.postprocess(songX);

			System.out.println("1. before gc " + Runtime.getRuntime().freeMemory());
			Runtime.getRuntime().gc();
			System.out.println("1. after gc " + Runtime.getRuntime().freeMemory());

			for (int y = 0; y < x; y++) {
				Song songY = songs.get(y);
				String nameY = "'" + new File(songY.getFilePath()).getName() + "'";

				System.out.println("Calculating distance between " + nameX + " and " + nameY + ".");
				List<ListenableFuture<Double>> tasks = Lists.newArrayList();
				for (int i = 0; i < processors.size(); i++)
					tasks.add(processors.get(i).distanceBetween(songX, songY));

				System.out.println("Awaiting distance result.");
				List<Double> results = Futures.allAsList(tasks).get();
				for (int i = 0; i < results.size(); i++)
					distances[x][y][i] = results.get(i);

				System.out.println("distances between " + nameX + " and " + nameY + " = "
						+ Arrays.toString(distances[x][y]));
			}

			System.out.println("2. before gc " + Runtime.getRuntime().freeMemory());
			Runtime.getRuntime().gc();
			System.out.println("2. after gc " + Runtime.getRuntime().freeMemory());

		}

		return distances;
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

	public double distanceBetween(int x, int y, double[] weighting) {
		if (x > y)
			return distance(distances[x][y], weighting);
		else if (x < y)
			return distance(distances[y][x], weighting);
		else
			return 0;
	}

	private double distance(double[] distanceVector, double[] weighting) {
		double distance = 0;
		for (int i = 0; i < distanceVector.length; i++)
			distance += square(distanceVector[i]) * weighting[i];

		return sqrt(distance);
	}

	public List<Song> getSongs() {
		return songs;
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		List<FeatureExtractor<?, ?>> featureExtractors = Arrays.asList(
				new HarmonicFeaturesExtractor(),
				new SpectralFeaturesExtractor(),
				new PerceptualFeaturesExtractor(),
				new TemporalFeaturesExtractor()
				);

		List<File> files = Arrays.asList(new File("songs"));

		long start = System.currentTimeMillis();

		System.out.println("Loading songs...");
		Mixtape mixtape = Mixtape.loadSongs(featureExtractors, files);
		System.out.println("Finished in " + (System.currentTimeMillis() - start) * 0.001 + " seconds.");
		System.out.println();

		List<Song> songs = mixtape.getSongs();
		for (int x = 0; x < songs.size(); x++) {
			Song songX = songs.get(x);
			for (int y = 0; y < x; y++) {
				Song songY = songs.get(y);

				System.out.println(new File(songX.getFilePath()).getName() + " to "
						+ new File(songY.getFilePath()).getName() + " = "
						+ mixtape.distanceBetween(x, y, new double[] { 1, 1, 1, 1 }));
			}
		}
	}
}