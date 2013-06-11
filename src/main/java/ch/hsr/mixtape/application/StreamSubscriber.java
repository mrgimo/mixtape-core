package ch.hsr.mixtape.application;

/**
 * @author Stefan Derungs
 */
public interface StreamSubscriber {

	/**
	 * @return True if the subscriber is still alive. If false, the streaming
	 *         service should remove the subscriber from the internal
	 *         notification list.
	 */
	public boolean subscriberIsAlive();

	/**
	 * Supply new data to the audio stream subscriber.
	 * 
	 * @param data
	 *            If this data is sent over the internet, the data needs to be
	 *            Base64-encoded. See RFC below for more information.
	 * @see http://www.ietf.org/rfc/rfc3534.txt
	 */
	public void provideData(byte[] data);

	/**
	 * Notifies a subscriber about the termination of the streaming service.
	 * This method should only be called if the stream service is terminated,
	 * but not when there is just no data for a subscriber. Upon calling this
	 * method, the subscriber should unsubscribe/stop waiting for data.
	 */
	public void notifyEndOfStream();

}
