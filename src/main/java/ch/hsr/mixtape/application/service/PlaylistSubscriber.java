package ch.hsr.mixtape.application.service;

public interface PlaylistSubscriber {

	public void notifyPlaylistChanged() throws RuntimeException;

}
