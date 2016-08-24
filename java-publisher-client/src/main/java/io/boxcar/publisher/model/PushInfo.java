package io.boxcar.publisher.model;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class PushInfo {
    Integer id;
    @SerializedName("created_at")
    Date createdAt;
    @SerializedName("send_later_at")
    Date sendLaterAt;    
    String state;
    Integer sent,errors,opened;

	public Integer getId() {
		return id;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public Date getSendLaterAt() {
		return sendLaterAt;
	}
	public String getState() {
		return state;
	}
	public Integer getSent() {
		return sent;
	}
	public Integer getErrors() {
		return errors;
	}
	public Integer getOpened() {
		return opened;
	}

	@Override
	public String toString() {
		return "Id: " + id + " - created at: " + createdAt + " - send later at: " + sendLaterAt;
	}
	
}
