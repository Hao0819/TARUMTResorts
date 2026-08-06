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
 * @author Junhao
 */
public class Room {
    private String roomNumber;
    private String roomType;
    // Fixed nightly price for this room in Malaysian Ringgit.
    private double dailyRate;
    private boolean isAvailable;
    // Current housekeeping state, synchronized from the latest status log.
    private String cleaningStatus = "UNKNOWN";

    public Room() {
    }

    /**
     * Compatibility constructor for existing code that does not provide a rate.
     */
    public Room(
            String roomNumber,
            String roomType,
            boolean isAvailable) {

        this(roomNumber, roomType, isAvailable, 0.0);
    }

    /**
     * Creates a room with its fixed nightly rate.
     */
    public Room(
            String roomNumber,
            String roomType,
            boolean isAvailable,
            double dailyRate) {

        if (dailyRate < 0) {
            throw new IllegalArgumentException(
                    "Daily rate cannot be negative.");
        }

        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.isAvailable = isAvailable;
        this.dailyRate = dailyRate;
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

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        if (dailyRate < 0) {
            throw new IllegalArgumentException(
                    "Daily rate cannot be negative.");
        }

        this.dailyRate = dailyRate;
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