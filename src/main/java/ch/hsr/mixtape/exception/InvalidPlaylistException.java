package ch.hsr.mixtape.exception;

/**
 * This exception should be thrown when playlist modifications are requested but
 * there is no playlist at all.
 * 
 * @author Stefan Derungs
 */
public class InvalidPlaylistException extends Exception {

	private static final long serialVersionUID = -1408878395329623413L;

	public InvalidPlaylistException(String s) {
		super(s);
	}

}
