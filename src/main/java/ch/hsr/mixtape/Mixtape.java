package ch.hsr.mixtape;

import static ch.hsr.mixtape.MathUtils.square;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.hsr.mixtape.features.FeatureExtractor;
import ch.hsr.mixtape.features.harmonic.HarmonicFeaturesExtractor;
import ch.hsr.mixtape.features.perceptual.PerceptualFeaturesExtractor;
import ch.hsr.mixtape.features.spectral.SpectralFeaturesExtractor;
import ch.hsr.mixtape.features.temporal.TemporalFeaturesExtractor;
import ch.hsr.mixtape.model.Song;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ForwardingBlockingQueue;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class Mixtape {

	private static final String[] ALLOWED_SUFFIXES = { ".mp3" };

	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime()
			.availableProcessors();

	private static final BlockingQueue<Runnable> TASK_QUEUE = Queues
			.newArrayBlockingQueue(AVAILABLE_PROCESSORS * 4);
	private static final ListeningExecutorService executor = listeningDecorator(MoreExecutors
			.getExitingExecutorService(new ThreadPoolExecutor(
					AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS * 2, 1,
					TimeUnit.MINUTES, new ForwardingBlockingQueue<Runnable>() {

						protected BlockingQueue<Runnable> delegate() {
							return TASK_QUEUE;
						}

						public boolean offer(Runnable runnable) {
							try {
								put(runnable);
								return true;
							} catch (InterruptedException exception) {
								return false;
							}
						}

					})));

	private final List<Song> songs;

	private final double[][][] distances;

	private Mixtape(List<Song> songs, double[][][] distances) {
		this.songs = songs;
		this.distances = distances;
	}

	private static Mixtape loadSongs(
			Collection<FeatureExtractor<?, ?>> featuresExtractors,
			Collection<File> pathsToSongs) throws InterruptedException,
			ExecutionException, IOException {
		List<File> songFiles = new FileFinder(pathsToSongs,
				createSongFileFilter()).find();
		System.out.println("Processing " + songFiles.size() + " songs.");
		List<Song> songs = initSongs(songFiles);

		List<FeatureProcessor<?, ?>> processors = initExtractors(
				featuresExtractors, songs.size());
		double[][][] distances = calcDistances(songs, processors);

		// executor.shutdown();

		return new Mixtape(songs, distances);
	}

	private static double[][][] calcDistances(List<Song> songs,
			List<FeatureProcessor<?, ?>> processors) throws IOException,
			InterruptedException, ExecutionException {
		double[][][] distances = new double[songs.size()][songs.size()][processors
				.size()];

		List<ListenableFuture<Double>> futures = Lists.newArrayList();

		SamplePublisher publisher = new SamplePublisher(processors);
		for (int x = 0; x < songs.size(); x++) {
			Song songX = songs.get(x);
			String nameX = "'" + new File(songX.getFilepath()).getName() + "'";
			System.out.println("Processing song " + nameX + ".");

			publisher.publish(songX);

			System.out.println("Postprocessing song " + nameX + ".");
			for (FeatureProcessor<?, ?> processor : processors)
				processor.postprocess(songX);

			for (int y = 0; y < x; y++) {
				Song songY = songs.get(y);
				String nameY = "'" + new File(songY.getFilepath()).getName()
						+ "'";

				System.out.println("Calculating distance between " + nameX
						+ " and " + nameY + ".");
				for (int i = 0; i < processors.size(); i++) {
					ListenableFuture<Double> distance = processors.get(i)
							.distanceBetween(songX, songY);
					Futures.addCallback(distance,
							createDistanceCallback(distances[x][y], i));
					futures.add(distance);
				}

			}
		}

		Futures.allAsList(futures).get();

		return distances;
	}

	private static FutureCallback<Double> createDistanceCallback(
			final double[] distanceVector, final int i) {
		return new FutureCallback<Double>() {

			public void onSuccess(Double distance) {
				distanceVector[i] = distance;
			}

			public void onFailure(Throwable throwable) {
			}

		};
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
		return file.getName().toLowerCase()
				.endsWith(allowedSuffix.toLowerCase());
	}

	private static List<Song> initSongs(List<File> songFiles) {
		List<Song> songs = Lists.newArrayListWithCapacity(songFiles.size());
		for (int id = 0; id < songFiles.size(); id++)
			songs.add(new Song(id, songFiles.get(id).getAbsolutePath()));
		// TODO: this constructor is not allowed here => deprecated ;-)
		// Use DB instead :-)

		return songs;
	}

	private static List<FeatureProcessor<?, ?>> initExtractors(
			Collection<FeatureExtractor<?, ?>> featuresExtractors,
			int numberOfSongs) {
		List<FeatureProcessor<?, ?>> extractors = Lists
				.newArrayListWithCapacity(featuresExtractors.size());
		for (FeatureExtractor<?, ?> featuresExtractor : featuresExtractors)
			extractors.add(new FeatureProcessor<>(featuresExtractor, executor));

		return extractors;
	}

	public double distanceBetween(long x, long y, double[] weighting) {
		int temp_x = (int) x; // TODO: no more ints here buddy ;-)
		int temp_y = (int) y; // TODO: no more ints here buddy ;-)
		if (temp_x > temp_y)
			return distance(distances[temp_x][temp_y], weighting);
		else if (temp_x < temp_y)
			return distance(distances[temp_y][temp_x], weighting);
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

	public static void main(String[] args) throws InterruptedException,
			ExecutionException, IOException {
		List<FeatureExtractor<?, ?>> featureExtractors = Arrays.asList(
				new HarmonicFeaturesExtractor(),
				new SpectralFeaturesExtractor(),
				new PerceptualFeaturesExtractor(),
				new TemporalFeaturesExtractor());

		List<File> files = Arrays.asList(new File("songs"));

		long start = System.currentTimeMillis();

		System.out.println("Loading songs...");
		Mixtape mixtape = Mixtape.loadSongs(featureExtractors, files);
		System.out.println("Finished in "
				+ (System.currentTimeMillis() - start) * 0.001 + " seconds.");
		System.out.println();
		System.out.println();

		List<Song> songs = mixtape.getSongs();
		for (int x = 0; x < songs.size(); x++) {
			Map<Song, Double> distances = Maps.newHashMap();
			for (int y = 0; y < songs.size(); y++)
				distances.put(songs.get(y), mixtape.distanceBetween(x, y,
						new double[] { 1, 1, 1, 1 }));

			Ordering<Song> ordering = Ordering.natural().onResultOf(
					Functions.forMap(distances));
			System.out.println("Distances to song "
					+ new File(songs.get(x).getFilepath()).getName() + ":");
			for (Song song : ordering.sortedCopy(distances.keySet()))
				System.out.println(new File(song.getFilepath()).getName()
						+ " = " + distances.get(song));

			System.out.println();
		}
	}
}