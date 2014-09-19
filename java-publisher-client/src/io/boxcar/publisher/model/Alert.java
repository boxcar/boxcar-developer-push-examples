package io.boxcar.publisher.model;

import io.boxcar.publisher.client.util.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Alert {
	
	private static long counter;
	
	static {
		counter = 0;
	}
	
	public class Aps {
		String badge;
		String sound;
		String alert;
	}
	
	Aps aps;
	List<String> tags;
	String id;
	long expires;
	
	public Alert(String text) {
		this.aps = new Aps();
		this.aps.badge = "auto";
		this.aps.sound = "beep.wav";
		this.aps.alert = text;
		this.tags = new ArrayList<String>();
		this.tags.add("@all");
		String id = String.valueOf(Calendar.getInstance().getTimeInMillis()) + 
			    String.valueOf(counter++);
		this.id = id;
		setTimeToLive(30000);
	}
	
	public Alert(String text, String id) {
		this.aps = new Aps();
		this.aps.badge = "auto";
		this.aps.sound = "beep.wav";
		this.aps.alert = text;
		this.tags = new ArrayList<String>();
		this.tags.add("@all");
		this.id = id;
		setTimeToLive(30000);
	}

	public String getAlert() {
		return aps.alert;
	}

	public void setAlert(String alert) {
		aps.alert = alert;
	}
	
	public String getSound() {
		return aps.sound;
	}

	public void setSound(String sound) {
		aps.sound = sound;
	}

	public String getBadge() {
		return aps.badge;
	}

	public void setBadge(String badge) {
		aps.badge = badge;
	}
	
	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTimeToLive(long millis) {
		this.expires = TimeUtils.getExpiryDate(millis).getTime() / 1000;
	}
}
