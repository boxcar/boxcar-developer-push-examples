package io.boxcar.publisher.model;

import java.util.Date;

/**
 * Created by jpcarlino on 22/01/15.
 */
public class Tag {

    Integer id;
    String name;
    Integer devices;
    Boolean deprecated;
    Date creationDate;

    public Tag(String name) {
        this.name = name;
        id = null;
        devices = null;
        deprecated = null;
        creationDate = null;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

}
