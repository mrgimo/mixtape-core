package ch.hsr.mixtape.application.service;

public interface PlaylistSubscriber {

	/**
	 * This method can be used to notify playlist subscribers after a
	 * multithreaded/asynchronous call.
	 */
	public void notifyPlaylistReady();

}
