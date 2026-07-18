/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.entity;

/**
 * WalkInRegistration.java
 * Represents a single walk-in guest registration record within the
 * standard booking queue.
 *
 * @author junha
 */
public class WalkInRegistration {

    private String registrationId;
    private Guest guest;
    private String registrationTime;
    private String requestedRoomType;
    private String status; // "WAITING", "ASSIGNED"

    public WalkInRegistration() {
    }

    public WalkInRegistration(String registrationId, Guest guest, String registrationTime, String requestedRoomType) {
        this.registrationId = registrationId;
        this.guest = guest;
        this.registrationTime = registrationTime;
        this.requestedRoomType = requestedRoomType;
        this.status = "WAITING";
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public String getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(String registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getRequestedRoomType() {
        return requestedRoomType;
    }

    public void setRequestedRoomType(String requestedRoomType) {
        this.requestedRoomType = requestedRoomType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-20s %-12s %-10s %-10s",
                registrationId, guest.getName(), requestedRoomType, registrationTime, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WalkInRegistration)) return false;
        WalkInRegistration other = (WalkInRegistration) obj;
        return registrationId != null && registrationId.equals(other.registrationId);
    }

    @Override
    public int hashCode() {
        return registrationId != null ? registrationId.hashCode() : 0;
    }
}
