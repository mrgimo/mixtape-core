package ch.hsr.mixtape.exception;

/**
 * This exception should be thrown when the client's request parameters
 * expectations do not match with the current playlist. As there can be
 * concurrent request from different clients, the clients could lack the most
 * recent playlist information.
 * 
 * @author Stefan Derungs
 */
public class PlaylistChangedException extends Exception {

	private static final long serialVersionUID = 931758302840221221L;

	public PlaylistChangedException(String message) {
		super(message);
	}

}
