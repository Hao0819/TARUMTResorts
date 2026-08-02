/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.entity;

/**
 * LoyaltyAccount.java
 *
 * Represents a guest's loyalty account.
 * Stores the loyalty ID, guest information, points balance,
 * and account status.
 *
 * The membership tier is stored in the associated Guest object
 * to avoid duplicating tier information.
 *
 * @author YourName
 */
public class LoyaltyAccount {

    private String loyaltyId;
    private Guest guest;
    private int pointsBalance;
    private boolean active;

    /**
     * Default constructor.
     */
    public LoyaltyAccount() {
        this.pointsBalance = 0;
        this.active = true;
    }

    /**
     * Creates a loyalty account.
     *
     * @param loyaltyId loyalty account ID
     * @param guest guest associated with the account
     * @param pointsBalance starting points balance
     * @param active account status
     */
    public LoyaltyAccount(
            String loyaltyId,
            Guest guest,
            int pointsBalance,
            boolean active) {

        if (pointsBalance < 0) {
            throw new IllegalArgumentException(
                    "Points balance cannot be negative.");
        }

        this.loyaltyId = loyaltyId;
        this.guest = guest;
        this.pointsBalance = pointsBalance;
        this.active = active;
    }

    public String getLoyaltyId() {
        return loyaltyId;
    }

    public void setLoyaltyId(String loyaltyId) {
        this.loyaltyId = loyaltyId;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    /**
     * Updates the points balance.
     *
     * Validation for adding and redeeming points will be handled
     * by LoyaltyRewardsControl.
     *
     * @param pointsBalance new points balance
     */
    public void setPointsBalance(int pointsBalance) {

        if (pointsBalance < 0) {
            throw new IllegalArgumentException(
                    "Points balance cannot be negative.");
        }

        this.pointsBalance = pointsBalance;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the guest ID belonging to this loyalty account.
     *
     * @return guest ID, or null if no guest is assigned
     */
    public String getGuestId() {

        if (guest == null) {
            return null;
        }

        return guest.getGuestId();
    }

    /**
     * Returns the member's name.
     *
     * @return guest name, or null if no guest is assigned
     */
    public String getMemberName() {

        if (guest == null) {
            return null;
        }

        return guest.getName();
    }

    /**
     * Returns the membership tier stored in the Guest entity.
     *
     * @return current membership tier
     */
    public MembershipTier getMembershipTier() {

        if (guest == null) {
            return MembershipTier.NONE;
        }

        return guest.getMembershipTier();
    }

    /**
     * Updates the membership tier stored in the Guest entity.
     *
     * @param membershipTier new membership tier
     */
    public void setMembershipTier(
            MembershipTier membershipTier) {

        if (guest != null) {
            guest.setMembershipTier(membershipTier);
        }
    }

    @Override
    public String toString() {

        return String.format(
                "%-10s %-10s %-20s %-10d %-12s %-10s",
                loyaltyId,
                getGuestId(),
                getMemberName(),
                pointsBalance,
                getMembershipTier(),
                active ? "ACTIVE" : "INACTIVE"
        );
    }

    /**
     * Two loyalty accounts are considered equal when they have
     * the same loyalty ID.
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof LoyaltyAccount)) {
            return false;
        }

        LoyaltyAccount other = (LoyaltyAccount) obj;

        return loyaltyId != null
                && other.loyaltyId != null
                && loyaltyId.equalsIgnoreCase(other.loyaltyId);
    }

    @Override
    public int hashCode() {

        if (loyaltyId == null) {
            return 0;
        }

        return loyaltyId.toLowerCase().hashCode();
    }
}
