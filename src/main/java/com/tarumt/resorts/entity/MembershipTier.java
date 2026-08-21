/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.tarumt.resorts.entity;

/**
 * Shared loyalty-tier definition used by Registration,
 * VIP Allocation and Loyalty modules.
 * 
 * @author LimJunHao
 */
public enum MembershipTier {

    NONE(0, false, 0.00, 0),
    SILVER(1, false, 0.05, 500),
    GOLD(2, false, 0.08, 1500),
    PLATINUM(3, true, 0.10, 3000),
    DIAMOND(4, true, 0.15, 5000),
    ELITE(5, true, 0.20, 7000);

    private final int priorityLevel;
    private final boolean priorityTier;
    // Room discount shared by Standard and VIP booking calculations.
    private final double roomDiscountRate;
    private final int minimumTierPoints;

    MembershipTier(
            int priorityLevel,
            boolean priorityTier,
            double roomDiscountRate,
            int minimumTierPoints) {

        this.priorityLevel = priorityLevel;
        this.priorityTier = priorityTier;
        this.roomDiscountRate = roomDiscountRate;
        this.minimumTierPoints = minimumTierPoints;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    public boolean isPriorityTier() {
        return priorityTier;
    }

    /**
     * Returns the room discount rate as a decimal value.
     * For example, 0.10 represents a 10% discount.
     */
    public double getRoomDiscountRate() {
        return roomDiscountRate;
    }

    public int getMinimumTierPoints() {
        return minimumTierPoints;
    }

    /** Returns the highest tier reached by the qualifying-point total. */
    public static MembershipTier fromTierQualifyingPoints(int points) {
        MembershipTier matchedTier = NONE;

        for (MembershipTier tier : values()) {
            if (points >= tier.minimumTierPoints) {
                matchedTier = tier;
            }
        }

        return matchedTier;
    }
}
