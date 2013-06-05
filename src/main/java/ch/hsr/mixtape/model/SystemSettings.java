package ch.hsr.mixtape.model;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Stefan Derungs
 */
@NamedQuery(name = "getAllSystemSettings", query = "SELECT s FROM SystemSettings s")
@Entity
public class SystemSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Temporal(TemporalType.DATE)
	private Date lastModified;

	private short serverPort;

	private String musicDirectoryPath;

	private String passwordHash;

	@PrePersist
	private void updateModifiedDate() {
		lastModified = new Date();
	}

	/**
	 * Get the server port for the webapp.
	 */
	public short getPort() {
		return serverPort;
	}

	/**
	 * Set the server port for the webapp.
	 */
	public void setServerPort(short port) {
		this.serverPort = port;
	}

	public String getMusicDirectoryPath() {
		return musicDirectoryPath;
	}

	public void setMusicDirectoryPath(String musicDirectoryPath) {
		this.musicDirectoryPath = musicDirectoryPath;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * @deprecated
	 */
	public boolean changePassword(String password, String repeat) {
		if (password.isEmpty() || !password.equals(repeat))
			return false;

		try {
			passwordHash = hashPassword(password);
			return true;
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			return false;
		}
	}

	/**
	 * @deprecated
	 */
	private String hashPassword(String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(password.getBytes("UTF-8"));
		return new String(digest.digest());
	}
}
