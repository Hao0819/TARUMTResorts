/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.entity;

import java.time.LocalDate;

/**
 * WalkInRegistration.java
 * Represents a single walk-in guest registration record within the
 * standard booking queue.
 *
 * @author Junhao
 */
public class WalkInRegistration {

    private String registrationId;
    private Guest guest;
    private String registrationTime;
    private String requestedRoomType;
    // Date on which the guest plans to begin the stay.
    private LocalDate requestedCheckInDate;
    // Number of nights requested by the guest.
    private int stayDurationDays;
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

    /**
     * Creates a registration with its requested stay schedule.
     */
    public WalkInRegistration(
            String registrationId,
            Guest guest,
            String registrationTime,
            String requestedRoomType,
            LocalDate requestedCheckInDate,
            int stayDurationDays) {

        this(
                registrationId,
                guest,
                registrationTime,
                requestedRoomType);

        setRequestedCheckInDate(requestedCheckInDate);
        setStayDurationDays(stayDurationDays);
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

    public LocalDate getRequestedCheckInDate() {
        return requestedCheckInDate;
    }

    public void setRequestedCheckInDate(
            LocalDate requestedCheckInDate) {

        if (requestedCheckInDate == null) {
            throw new IllegalArgumentException(
                    "Requested check-in date cannot be null.");
        }

        this.requestedCheckInDate = requestedCheckInDate;
    }

    public int getStayDurationDays() {
        return stayDurationDays;
    }

    public void setStayDurationDays(int stayDurationDays) {
        if (stayDurationDays <= 0) {
            throw new IllegalArgumentException(
                    "Stay duration must be greater than zero.");
        }

        this.stayDurationDays = stayDurationDays;
    }

    /**
     * Calculates the requested check-out date from the check-in date
     * and number of nights.
     */
    public LocalDate getRequestedCheckOutDate() {
        if (requestedCheckInDate == null
                || stayDurationDays <= 0) {

            return null;
        }

        return requestedCheckInDate.plusDays(stayDurationDays);
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
        if (this == obj)
            return true;
        if (!(obj instanceof WalkInRegistration))
            return false;
        WalkInRegistration other = (WalkInRegistration) obj;
        return registrationId != null && registrationId.equals(other.registrationId);
    }

    @Override
    public int hashCode() {
        return registrationId != null ? registrationId.hashCode() : 0;
    }
}
