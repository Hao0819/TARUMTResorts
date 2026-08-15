/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.tarumt.resorts.entity;

/**
 * Defines the reward packages available for direct loyalty redemption.
 *
 * Each reward has a display name and the number of loyalty points required
 * to redeem it.
 */
public enum RewardPackage {

    BREAKFAST_SET("Breakfast Set", 500),
    LUNCH_SET("Lunch Set", 800),
    DINNER_SET("Dinner Set", 1200),
    BUFFET_VOUCHER("Buffet Voucher", 1500);

    private final String rewardName;
    private final int pointsRequired;

    RewardPackage(
            String rewardName,
            int pointsRequired) {

        this.rewardName = rewardName;
        this.pointsRequired = pointsRequired;
    }

    public String getRewardName() {
        return rewardName;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    @Override
    public String toString() {
        return String.format(
                "%s - %d points",
                rewardName,
                pointsRequired);
    }
}