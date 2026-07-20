package com.tarumt.resorts.entity;

/**
 * RoomStatusLog.java
 * Represents a single cleaning-status entry for a room, capturing the
 * status at a point in time. A sequence of these (oldest to newest) forms
 * a room's cleaning history, allowing the most recent entry to be rolled
 * back if logged incorrectly.
 *
 * @author KohJun
 */
public class RoomStatusLog {
    private String roomNumber;
    private String status;      // "DIRTY", "CLEANING", "INSPECTED", "READY"
    private String timestamp;

    public RoomStatusLog() {
    }

    public RoomStatusLog(String roomNumber, String status, String timestamp) {
        this.roomNumber = roomNumber;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-15s %-16s", roomNumber, status, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RoomStatusLog)) return false;
        RoomStatusLog other = (RoomStatusLog) obj;
        return roomNumber != null && roomNumber.equals(other.roomNumber)
                && status != null && status.equals(other.status)
                && timestamp != null && timestamp.equals(other.timestamp);
    }

    @Override
    public int hashCode() {
        int result = roomNumber != null ? roomNumber.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }
}