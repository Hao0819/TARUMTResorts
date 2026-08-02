package com.tarumt.resorts.entity;

/**
 * VIPAllocationRequest.java
 * Represents a single VIP/loyalty-tier guest's request for priority
 * room allocation. Mirrors WalkInRegistration's structure, but requests
 * are ordered by membership tier priority instead of strict arrival time.
 *
 * @author brian
 */
public class VIPAllocationRequest {

    private String requestId;
    private Guest guest;
    private String requestTime;
    private String requestedRoomType;
    private String status; // "WAITING", "ASSIGNED", "CANCELLED"

    public VIPAllocationRequest() {
    }

    public VIPAllocationRequest(
            String requestId,
            Guest guest,
            String requestTime,
            String requestedRoomType) {

        this.requestId = requestId;
        this.guest = guest;
        this.requestTime = requestTime;
        this.requestedRoomType = requestedRoomType;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-20s %-10s %-10s %-16s %-9s",
                requestId,
                guest.getName(),
                guest.getMembershipTier(),
                requestedRoomType,
                requestTime,
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
