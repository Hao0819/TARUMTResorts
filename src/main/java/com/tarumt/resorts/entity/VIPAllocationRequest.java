package com.tarumt.resorts.entity;

import java.time.LocalDate;

/**
 * Represents one VIP/loyalty-tier guest's request for priority room
 * allocation.
 *
 * @author brian
 */
public class VIPAllocationRequest {
    private String requestId;
    private Guest guest;
    private String requestTime;
    private String requestedRoomType;
    private LocalDate requestedCheckInDate;
    private int stayDurationDays;
    private String status; // "WAITING", "ASSIGNED", "CANCELLED"

    public VIPAllocationRequest() {
    }

    public VIPAllocationRequest(
            String requestId,
            Guest guest,
            String requestTime,
            String requestedRoomType,
            LocalDate requestedCheckInDate,
            int stayDurationDays) {

        this.requestId = requestId;
        this.guest = guest;
        this.requestTime = requestTime;
        this.requestedRoomType = requestedRoomType;
        setRequestedCheckInDate(requestedCheckInDate);
        setStayDurationDays(stayDurationDays);
        this.status = "WAITING";
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
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

    public void setRequestedCheckInDate(LocalDate requestedCheckInDate) {
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

    /** Calculated from check-in date + stay duration. */
    public LocalDate getRequestedCheckOutDate() {
        if (requestedCheckInDate == null || stayDurationDays <= 0) {
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
        return String.format("%-10s %-20s %-10s %-10s %-16s %-12s %-12s %-9s",
                requestId,
                guest.getName(),
                guest.getMembershipTier(),
                requestedRoomType,
                requestTime,
                requestedCheckInDate,
                getRequestedCheckOutDate(),
                status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VIPAllocationRequest)) {
            return false;
        }
        VIPAllocationRequest other = (VIPAllocationRequest) obj;
        return requestId != null && requestId.equals(other.requestId);
    }

    @Override
    public int hashCode() {
        return requestId != null ? requestId.hashCode() : 0;
    }
}