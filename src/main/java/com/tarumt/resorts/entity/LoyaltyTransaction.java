/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents one loyalty-points transaction.
 *
 * Transaction types:
 * EARN   - points earned from a completed booking
 * REDEEM - points redeemed by a member
 * EXPIRE - points removed after expiry
 * ADJUST - opening-balance or audited adjustment points
 *
 * EARN and ADJUST transactions can contain remaining usable points.
 * Only EARN transactions have an expiry time.
 */
public class LoyaltyTransaction {

    /**
     * Types of loyalty point transactions.
     */
    public enum TransactionType {
        EARN,
        REDEEM,
        EXPIRE,
        ADJUST
    }

    private String transactionId;
    private String loyaltyId;
    private String bookingId;
    private TransactionType transactionType;
    private int points;
    private int remainingPoints;
    private LocalDate transactionDate;
    private LocalDateTime expiryTime;

    /**
     * Default constructor.
     */
    public LoyaltyTransaction() {
    }

    /**
     * Creates a loyalty transaction.
     *
     * @param transactionId unique transaction ID
     * @param loyaltyId loyalty account ID
     * @param bookingId booking confirmation number for EARN transactions
     * @param transactionType transaction type
     * @param points transaction point amount
     * @param transactionDate transaction date
     * @param expiryTime expiry date and time for EARN points
     */
    public LoyaltyTransaction(
            String transactionId,
            String loyaltyId,
            String bookingId,
            TransactionType transactionType,
            int points,
            LocalDate transactionDate,
            LocalDateTime expiryTime) {

        if (points <= 0) {
            throw new IllegalArgumentException(
                    "Transaction points must be greater than zero.");
        }

        if (transactionType == null) {
            throw new IllegalArgumentException(
                    "Transaction type cannot be null.");
        }

        if (transactionType == TransactionType.EARN
                && (bookingId == null
                || bookingId.trim().isEmpty())) {

            throw new IllegalArgumentException(
                    "An earning transaction requires a booking ID.");
        }

        if (transactionType == TransactionType.EARN
                && expiryTime == null) {

            throw new IllegalArgumentException(
                    "Earned points require an expiry time.");
        }

        this.transactionId = transactionId;
        this.loyaltyId = loyaltyId;
        this.bookingId = bookingId;
        this.transactionType = transactionType;
        this.points = points;

        /*
         * EARN and ADJUST transactions provide usable points.
         * REDEEM and EXPIRE only record points that were removed.
         */
        if (transactionType == TransactionType.EARN
                || transactionType == TransactionType.ADJUST) {

            this.remainingPoints = points;
        } else {
            this.remainingPoints = 0;
        }

        this.transactionDate =
                transactionDate == null
                        ? LocalDate.now()
                        : transactionDate;

        this.expiryTime = expiryTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getLoyaltyId() {
        return loyaltyId;
    }

    public void setLoyaltyId(String loyaltyId) {
        this.loyaltyId = loyaltyId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(
            TransactionType transactionType) {

        this.transactionType = transactionType;
    }

    public int getPoints() {
        return points;
    }

    public int getRemainingPoints() {
        return remainingPoints;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(
            LocalDate transactionDate) {

        this.transactionDate = transactionDate;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(
            LocalDateTime expiryTime) {

        this.expiryTime = expiryTime;
    }

    /**
     * Deducts usable points from this transaction.
     *
     * @param amount requested amount to deduct
     * @return actual number of points deducted
     */
    public int deductRemainingPoints(int amount) {

        if (amount <= 0 || remainingPoints <= 0) {
            return 0;
        }

        int deductedPoints =
                Math.min(amount, remainingPoints);

        remainingPoints -= deductedPoints;

        return deductedPoints;
    }

    /**
     * Removes all remaining usable points from this transaction.
     *
     * @return number of points expired
     */
    public int expireRemainingPoints() {

        int expiredPoints = remainingPoints;
        remainingPoints = 0;

        return expiredPoints;
    }

    /**
     * Checks whether the remaining EARN points have expired.
     *
     * @param currentTime current date and time
     * @return true when the expiry time has been reached or passed
     */
    public boolean isExpiredAt(
            LocalDateTime currentTime) {

        if (transactionType != TransactionType.EARN
                || expiryTime == null
                || remainingPoints <= 0
                || currentTime == null) {

            return false;
        }

        return !currentTime.isBefore(expiryTime);
    }

    /**
     * Checks whether remaining EARN points expire within an inclusive
     * date-time range.
     *
     * @param startTime beginning of the expiry window
     * @param endTime end of the expiry window
     * @return true if expiryTime is between startTime and endTime
     */
    public boolean isExpiringBetween(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (transactionType != TransactionType.EARN
                || expiryTime == null
                || remainingPoints <= 0
                || startTime == null
                || endTime == null
                || endTime.isBefore(startTime)) {

            return false;
        }

        return !expiryTime.isBefore(startTime)
                && !expiryTime.isAfter(endTime);
    }

    @Override
    public String toString() {

        String displayedBookingId =
                bookingId == null
                        ? "-"
                        : bookingId;

        String displayedExpiryTime =
                expiryTime == null
                        ? "-"
                        : expiryTime.toString();

        return String.format(
                "%-8s %-8s %-10s %-8s %8d %10d %-12s %-20s",
                transactionId,
                loyaltyId,
                displayedBookingId,
                transactionType,
                points,
                remainingPoints,
                transactionDate,
                displayedExpiryTime
        );
    }

    /**
     * Transactions are considered equal when their transaction IDs match.
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof LoyaltyTransaction)) {
            return false;
        }

        LoyaltyTransaction other =
                (LoyaltyTransaction) obj;

        return transactionId != null
                && other.transactionId != null
                && transactionId.equalsIgnoreCase(
                        other.transactionId);
    }

    @Override
    public int hashCode() {

        if (transactionId == null) {
            return 0;
        }

        return transactionId.toLowerCase().hashCode();
    }
}