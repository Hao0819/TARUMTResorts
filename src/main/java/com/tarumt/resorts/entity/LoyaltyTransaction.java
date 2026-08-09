
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.entity;

import java.time.LocalDate;

/**
 * Represents a points transaction belonging to a loyalty account.
 *
 * <p>Supported transaction types:</p>
 * <ul>
 *     <li>EARN   - points earned from a completed booking</li>
 *     <li>REDEEM - points redeemed by the member</li>
 *     <li>EXPIRE - points removed after expiry</li>
 *     <li>ADJUST - opening-balance or audited adjustment points</li>
 * </ul>
 *
 * <p>EARN transactions store a booking ID and expiry date so that
 * duplicate stay rewards and expiring points can be checked.</p>
 */
public class LoyaltyTransaction {

    // -------------------------------------------------------------------------
    // TRANSACTION TYPE
    // -------------------------------------------------------------------------

    /**
     * Types of loyalty point transactions.
     */
    public enum TransactionType {
        EARN,
        REDEEM,
        EXPIRE,
        ADJUST
    }

    // -------------------------------------------------------------------------
    // DATA FIELDS
    // -------------------------------------------------------------------------

    private String transactionId;
    private String loyaltyId;
    private String bookingId;
    private TransactionType transactionType;
    private int points;
    private int remainingPoints;
    private LocalDate transactionDate;
    private LocalDate expiryDate;

    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------

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
     * @param bookingId booking ID for an earning transaction
     * @param transactionType EARN, REDEEM, EXPIRE, or ADJUST
     * @param points number of points involved
     * @param transactionDate transaction date
     * @param expiryDate expiry date for earned points
     */
    public LoyaltyTransaction(
            String transactionId,
            String loyaltyId,
            String bookingId,
            TransactionType transactionType,
            int points,
            LocalDate transactionDate,
            LocalDate expiryDate) {

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
                && expiryDate == null) {

            throw new IllegalArgumentException(
                    "Earned points require an expiry date.");
        }

        this.transactionId = transactionId;
        this.loyaltyId = loyaltyId;
        this.bookingId = bookingId;
        this.transactionType = transactionType;
        this.points = points;

        /*
         * EARN and ADJUST transactions provide usable points.
         *
         * ADJUST is used for an opening balance so that existing
         * loyalty points are represented in the transaction ledger.
         */
        if (transactionType == TransactionType.EARN
                || transactionType == TransactionType.ADJUST) {

            this.remainingPoints = points;

        } else {
            this.remainingPoints = 0;
        }

        this.transactionDate = transactionDate == null
                ? LocalDate.now()
                : transactionDate;

        this.expiryDate = expiryDate;
    }

    // -------------------------------------------------------------------------
    // GETTERS AND SETTERS
    // -------------------------------------------------------------------------

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

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    // -------------------------------------------------------------------------
    // TRANSACTION HELPERS
    // -------------------------------------------------------------------------

    /**
     * Checks whether this is an earned-points transaction.
     *
     * @return true if the transaction type is EARN
     */
    public boolean isEarnTransaction() {
        return transactionType == TransactionType.EARN;
    }

    /**
     * Deducts points from this transaction's remaining usable balance.
     *
     * @param amount number of points to deduct
     * @return actual number of points deducted
     */
    public int deductRemainingPoints(int amount) {

        if (amount <= 0 || remainingPoints <= 0) {
            return 0;
        }

        int deductedPoints;

        if (amount > remainingPoints) {
            deductedPoints = remainingPoints;
        } else {
            deductedPoints = amount;
        }

        remainingPoints -= deductedPoints;

        return deductedPoints;
    }

    /**
     * Removes all remaining points from this transaction.
     *
     * @return number of points removed
     */
    public int expireRemainingPoints() {

        int expiredPoints = remainingPoints;
        remainingPoints = 0;

        return expiredPoints;
    }

    // -------------------------------------------------------------------------
    // EXPIRY CHECKING
    // -------------------------------------------------------------------------

    /**
     * Checks whether the remaining earned points have expired.
     *
     * @param currentDate date used for the expiry check
     * @return true if the remaining earned points have expired
     */
    public boolean isExpiredOn(LocalDate currentDate) {

        if (!isEarnTransaction()
                || remainingPoints <= 0
                || expiryDate == null
                || currentDate == null) {

            return false;
        }

        return !expiryDate.isAfter(currentDate);
    }

    /**
     * Checks whether the earned points expire within a given date range.
     *
     * Both the start date and end date are included.
     *
     * @param startDate beginning of expiry window
     * @param endDate end of expiry window
     * @return true if the points expire within the range
     */
    public boolean isExpiringBetween(
            LocalDate startDate,
            LocalDate endDate) {

        if (!isEarnTransaction()
                || remainingPoints <= 0
                || expiryDate == null
                || startDate == null
                || endDate == null) {

            return false;
        }

        boolean onOrAfterStart =
                !expiryDate.isBefore(startDate);

        boolean onOrBeforeEnd =
                !expiryDate.isAfter(endDate);

        return onOrAfterStart && onOrBeforeEnd;
    }

    // -------------------------------------------------------------------------
    // OBJECT METHODS
    // -------------------------------------------------------------------------

    @Override
    public String toString() {

        String displayedBookingId =
                bookingId == null ? "-" : bookingId;

        String displayedExpiryDate =
                expiryDate == null
                ? "-"
                : expiryDate.toString();

        return String.format(
                "%-8s %-8s %-10s %-8s %8d %10d %-12s %-12s",
                transactionId,
                loyaltyId,
                displayedBookingId,
                transactionType,
                points,
                remainingPoints,
                transactionDate,
                displayedExpiryDate
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