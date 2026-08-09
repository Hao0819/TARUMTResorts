/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.entity;

import java.time.LocalDate;

/**
 * Represents a pending loyalty-points redemption request.
 */
public class RedemptionRequest {

    private String requestId;
    private String loyaltyId;
    private int points;
    private LocalDate requestDate;

    public RedemptionRequest(
            String requestId,
            String loyaltyId,
            int points,
            LocalDate requestDate) {

        this.requestId = requestId;
        this.loyaltyId = loyaltyId;
        this.points = points;
        this.requestDate = requestDate;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getLoyaltyId() {
        return loyaltyId;
    }

    public int getPoints() {
        return points;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    @Override
    public String toString() {
        return String.format(
                "%-8s %-8s %8d %-12s",
                requestId,
                loyaltyId,
                points,
                requestDate);
    }
}
