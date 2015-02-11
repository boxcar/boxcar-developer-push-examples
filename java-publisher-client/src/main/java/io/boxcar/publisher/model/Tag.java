package io.boxcar.publisher.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by jpcarlino on 22/01/15.
 */
public class Tag {

    Integer id;
    String name;
    Integer devices;
    Boolean deprecated;
    @SerializedName("created_at")
    Date createdAt;

    public Tag(String name) {
        this.name = name;
        id = null;
        devices = null;
        deprecated = null;
        createdAt = null;
    }

    public int getId() {
        return id != null ? id.intValue() : -1;
    }

    public void setId(int id) {
        this.id = new Integer(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDevices() {
        return devices != null ? devices.intValue() : 0;
    }

    public void setDevices(int devices) {
        this.devices = new Integer(devices);
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
