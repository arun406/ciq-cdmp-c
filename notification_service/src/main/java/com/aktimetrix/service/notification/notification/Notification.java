package com.aktimetrix.service.notification.notification;

import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
public class Notification {
    private String type;
    private long sequenceNumber;
    private long timestamp;
    private String message;
    private String tenant;
    private Object data;
    private Address sender;
    private List<Address> recipients;

    public Notification(String type) {
        this.type = type;
        this.timestamp = (new java.util.Date()).getTime();
    }

    public Notification(String type, long sequenceNumber) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = (new java.util.Date()).getTime();
    }

    public Notification(String type, long sequenceNumber, String message) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = (new java.util.Date()).getTime();
        this.message = message;
    }

    public Notification(String type, long sequenceNumber, long timestamp) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
    }

    public Notification(String type, long sequenceNumber, String message, long timestamp) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
        this.message = message;
    }
}
