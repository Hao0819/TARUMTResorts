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

    NONE(0, false, 0.00),
    SILVER(1, false, 0.05),
    GOLD(2, false, 0.08),
    PLATINUM(3, true, 0.10),
    DIAMOND(4, true, 0.15),
    ELITE(5, true, 0.20);

    private final int priorityLevel;
    private final boolean priorityTier;
    // Room discount shared by Standard and VIP booking calculations.
    private final double roomDiscountRate;

    MembershipTier(
            int priorityLevel,
            boolean priorityTier,
            double roomDiscountRate) {

        this.priorityLevel = priorityLevel;
        this.priorityTier = priorityTier;
        this.roomDiscountRate = roomDiscountRate;
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
}