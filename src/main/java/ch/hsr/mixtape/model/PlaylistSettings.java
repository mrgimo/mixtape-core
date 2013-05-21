package ch.hsr.mixtape.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class PlaylistSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Temporal(TemporalType.DATE)
	private Date creationDate;
	
	private int distanceAdvanceInPercent;
	
	private int averageBPM;
	
	public PlaylistSettings() {
		creationDate = new Date();
	}
	
	public PlaylistSettings(int distanceAdvanceInPercent, int averageBPM) {
		this();
		this.distanceAdvanceInPercent = distanceAdvanceInPercent;
		this.averageBPM = averageBPM;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public int getDistanceAdvanceInPercent() {
		return distanceAdvanceInPercent;
	}

	public void setDistanceAdvanceInPercent(int distanceAdvanceInPercent) {
		this.distanceAdvanceInPercent = distanceAdvanceInPercent;
	}

	public int getAverageBPM() {
		return averageBPM;
	}

	public void setAverageBPM(int averageBPM) {
		this.averageBPM = averageBPM;
	}
	
}
