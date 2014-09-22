package io.boxcar.publisher.model;

import io.boxcar.publisher.client.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the "content" to be published
 * @author jpcarlino
 *
 */
public class Alert {
	
	public class Aps {
		String badge;
		String sound;
		String alert;
	}
	
	Aps aps;
	List<String> tags;
	List<String> target_os;
	List<Integer> client_ids;
	String id;
	long expires;
	List<String> device_tokens;
	
	public Alert(String text) {
		this.aps = new Aps();
		this.aps.badge = "auto";
		this.aps.sound = "beep.wav";
		this.aps.alert = text;
		this.tags = new ArrayList<String>();
		this.tags.add("@all");
		this.id = null;
		this.target_os = null;
		this.client_ids = null;
		this.device_tokens = null;
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
		this.target_os = null;
		this.client_ids = null;
		this.device_tokens = null;
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

	public List<String> getTargetOS() {
		return target_os;
	}

	public void setTargetOS(List<String> targetOS) {
		this.target_os = targetOS;
	}
	
	public void setTargetOS(String targetOS) {
		this.target_os = new ArrayList<String>();
		this.target_os.add(targetOS);
	}

	public List<Integer> getClientIds() {
		return client_ids;
	}

	public void setClientIds(List<Integer> clientIds) {
		this.client_ids = clientIds;
	}

	public List<String> getDeviceTokens() {
		return device_tokens;
	}

	public void setDeviceTokens(List<String> deviceTokens) {
		this.device_tokens = deviceTokens;
	}
	
}
