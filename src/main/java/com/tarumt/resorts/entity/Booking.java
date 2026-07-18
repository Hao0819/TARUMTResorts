package com.tarumt.resorts.entity;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Booking.java
 * Links a Guest to a Room via a unique confirmation number. Created once
 * Room Allocation assigns a room (from either the Walk-In or VIP path),
 * and is the shared record that Front-Desk looks up and Loyalty updates
 * on checkout.
 *
 * Per ECB rules, this entity only references other entities (Guest, Room),
 * never a Control or Boundary class.
 *
 * @author junha
 */
public class Booking {
    private String confirmationNumber;
    private Guest guest;
    private Room room;
    private String checkInTime;
    private String status; // "ACTIVE", "CHECKED_OUT"

    public Booking() {
    }

    public Booking(String confirmationNumber, Guest guest, Room room, String checkInTime) {
        this.confirmationNumber = confirmationNumber;
        this.guest = guest;
        this.room = room;
        this.checkInTime = checkInTime;
        this.status = "ACTIVE";
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-20s %-8s %-16s %-12s",
                confirmationNumber, guest.getName(), room.getRoomNumber(), checkInTime, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Booking)) return false;
        Booking other = (Booking) obj;
        return confirmationNumber != null && confirmationNumber.equals(other.confirmationNumber);
    }

    @Override
    public int hashCode() {
        return confirmationNumber != null ? confirmationNumber.hashCode() : 0;
    }
}