package ch.hsr.mixtape;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.math3.util.FastMath;

import ch.hsr.mixtape.domain.Song;
import ch.hsr.mixtape.features.FeatureExtractor;

import com.google.common.collect.Lists;

public class Mixtape {

	private static final String[] ALLOWED_SUFFIXES = {
			".mp3"
	};

	private final Collection<Song> songs;

	private final double[][][] distanceMatrices;

	private Mixtape(Collection<Song> songs, double[][][] distanceMatrices) {
		this.songs = songs;
		this.distanceMatrices = distanceMatrices;
	}

	private static Mixtape loadSongs(Collection<FeatureExtractor<?>> features, Collection<File> pathsToSongs)
			throws InterruptedException, ExecutionException, IOException {
		List<File> songFiles = new FileFinder(pathsToSongs, createSongFileFilter()).find();
		List<Song> songs = initSongs(songFiles);

		List<FeatureProcessor<?>> extractors = initExtractors(features, songs.size());
		for (Song song : songs)
			new SamplePublisher(song, extractors).publish();

		return new Mixtape(songs, getDistanceMatrices(extractors));
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

	private static List<FeatureProcessor<?>> initExtractors(Collection<FeatureExtractor<?>> features, int numberOfSongs) {
		List<FeatureProcessor<?>> extractors = Lists.newArrayListWithCapacity(features.size());
		for (FeatureExtractor<?> feature : features)
			extractors.add(new FeatureProcessor<>(feature, numberOfSongs));

		return extractors;
	}

	private static double[][][] getDistanceMatrices(List<FeatureProcessor<?>> extractors) {
		double[][][] distanceMatrix = new double[extractors.size()][][];
		for (int i = 0; i < extractors.size(); i++)
			distanceMatrix[i] = getDistanceMatrix(extractors.get(i).getDistances());

		return distanceMatrix;
	}

	private static double[][] getDistanceMatrix(List<List<Future<Double>>> distances) {
		double[][] distanceMatrix = new double[distances.size()][];
		for (int x = 0; x < distances.size(); x++)
			distanceMatrix[x] = getDistanceVector(distances.get(x));

		return distanceMatrix;
	}

	private static double[] getDistanceVector(List<Future<Double>> distances) {
		double[] distanceVector = new double[distances.size()];
		for (int y = 0; y < distances.size(); y++)
			distanceVector[y] = tryGet(distances.get(y));

		return distanceVector;
	}

	private static Double tryGet(Future<Double> future) {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException exception) {
			return Double.NaN;
		}
	}

	public double distanceBetween(Song songX, Song songY, double[] weighting) {
		int x = songX.getId();
		int y = songY.getId();

		if (x > y)
			return distanceBetween(x, y, weighting);
		else if (x < y)
			return distanceBetween(y, x, weighting);
		else
			return 0;
	}

	private double distanceBetween(int x, int y, double[] weighting) {
		double distance = 0;
		for (int i = 0; i < distanceMatrices.length; i++)
			distance += distanceMatrices[i][x][y] * distanceMatrices[i][x][y] * weighting[i];

		return FastMath.sqrt(distance);
	}

	public Collection<Song> getSongs() {
		return songs;
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		List<FeatureExtractor<?>> features = Arrays.asList();
		List<File> files = Arrays.asList(new File("songs"));

		Mixtape mixtape = Mixtape.loadSongs(features, files);
		System.out.println(mixtape.getSongs());
	}

}