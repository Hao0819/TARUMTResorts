/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.tarumt.resorts.entity;

/**
 *Shared loyalty-tier definition used by Registration,
 * VIP Allocation and Loyalty modules.
 * @author junha
 */
public enum MembershipTier {

    NONE(0, false),
    SILVER(1, false),
    GOLD(2, false),
    PLATINUM(3, true),
    DIAMOND(4, true),
    ELITE(5, true);

    private final int priorityLevel;
    private final boolean priorityTier;

    MembershipTier(
            int priorityLevel,
            boolean priorityTier) {

        this.priorityLevel = priorityLevel;
        this.priorityTier = priorityTier;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    public boolean isPriorityTier() {
        return priorityTier;
    }
}