package ch.hsr.mixtape.data.valuemapper;


public class SpectralCentroidValueMaper implements ValueMapper {

	/*
	 * spectral centroid: 25.635690380337493
	 * spectral centroid: 19.60209302317004
	 */
	@Override
	public int mapValueToInt(double value) {
		return (int) (value * 10 + 1);
	}

}
