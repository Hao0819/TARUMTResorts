package com.tarumt.resorts.entity;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Room.java
 * Represents a physical room in the resort. Shared across Room Allocation,
 * Housekeeping, and Front-Desk modules — availability is toggled as guests
 * check in/out, and cleaning status is tracked separately by Housekeeping.
 *
 * @author junha
 */
public class Room {
    private String roomNumber;
    private String roomType;
    private boolean isAvailable;
    // Current housekeeping state, synchronized from the latest status log.
    private String cleaningStatus = "UNKNOWN";

    public Room() {
    }

    public Room(String roomNumber, String roomType, boolean isAvailable) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.isAvailable = isAvailable;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getCleaningStatus() {
        return cleaningStatus;
    }

    public void setCleaningStatus(String cleaningStatus) {
        this.cleaningStatus = cleaningStatus;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-12s %-10s",
                roomNumber, roomType, isAvailable ? "AVAILABLE" : "OCCUPIED");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Room))
            return false;
        Room other = (Room) obj;
        return roomNumber != null && roomNumber.equals(other.roomNumber);
    }

    @Override
    public int hashCode() {
        return roomNumber != null ? roomNumber.hashCode() : 0;
    }
}