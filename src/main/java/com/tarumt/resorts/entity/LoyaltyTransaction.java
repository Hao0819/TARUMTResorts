/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.entity;

import com.tarumt.resorts.util.LoyaltyClock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents one loyalty-points transaction.
 *@author Gary Khor Wei Qi
 * Transaction types:
 * EARN   - points earned from a completed booking
 * REDEEM - points redeemed by a member
 * EXPIRE - points removed after expiry
 * ADJUST - opening-balance or audited adjustment points
 *
 * EARN and ADJUST transactions represent separate usable point batches.
 * Each batch may have its own expiry time and remaining-point balance.
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
    private int historyRemainingPoints;
    private RewardPackage rewardPackage;
    private LocalDate transactionDate;
    private LocalDateTime transactionTime;
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
     * @param expiryTime expiry time for this usable point batch
     */
    public LoyaltyTransaction(
            String transactionId,
            String loyaltyId,
            String bookingId,
            TransactionType transactionType,
            int points,
            LocalDate transactionDate,
            LocalDateTime expiryTime) {

        this(
                transactionId,
                loyaltyId,
                bookingId,
                transactionType,
                points,
                transactionDate == null
                        ? LoyaltyClock.now()
                        : transactionDate.atStartOfDay(),
                expiryTime);
    }

    /**
     * Creates a transaction with its exact event time. This overload is
     * additive so existing module calls using LocalDate remain compatible.
     */
    public LoyaltyTransaction(
            String transactionId,
            String loyaltyId,
            String bookingId,
            TransactionType transactionType,
            int points,
            LocalDateTime transactionTime,
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
            this.historyRemainingPoints = points;
        } else {
            this.remainingPoints = 0;
            this.historyRemainingPoints = 0;
        }

        this.transactionTime =
                transactionTime == null
                        ? LoyaltyClock.now()
                        : transactionTime;

        this.transactionDate =
                this.transactionTime.toLocalDate();

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

    /**
     * Returns the remaining value recorded for transaction-history display.
     * Unlike a point batch's live remainingPoints, this value is not changed
     * when a later redemption consumes that batch.
     */
    public int getHistoryRemainingPoints() {
        return historyRemainingPoints;
    }

    public RewardPackage getRewardPackage() {
        return rewardPackage;
    }

    /** Associates a redeemed reward with its ledger transaction. */
    public void setRewardPackage(RewardPackage rewardPackage) {

        if (transactionType != TransactionType.REDEEM
                && rewardPackage != null) {
            throw new IllegalStateException(
                    "Only REDEEM transactions can contain a reward.");
        }

        this.rewardPackage = rewardPackage;
    }

    /**
     * Records the account balance after a REDEEM or EXPIRE transaction and
     * the expiry time of the point batch that was removed.
     */
    public void recordRemovalResult(
            int balanceAfterTransaction,
            LocalDateTime sourceBatchExpiryTime) {

        if (transactionType != TransactionType.REDEEM
                && transactionType != TransactionType.EXPIRE) {
            throw new IllegalStateException(
                    "Only REDEEM or EXPIRE can record a removal result.");
        }

        if (balanceAfterTransaction < 0) {
            throw new IllegalArgumentException(
                    "Balance after transaction cannot be negative.");
        }

        historyRemainingPoints = balanceAfterTransaction;
        expiryTime = sourceBatchExpiryTime;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(
            LocalDate transactionDate) {

        this.transactionDate = transactionDate;

        if (transactionDate != null) {
            transactionTime = transactionDate.atStartOfDay();
        }
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(
            LocalDateTime transactionTime) {

        this.transactionTime = transactionTime;

        if (transactionTime != null) {
            transactionDate = transactionTime.toLocalDate();
        }
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
     * Removes all unused points from this expired batch.
     *
     * @return number of points expired
     */
    public int expireRemainingPoints() {

        int expiredPoints = remainingPoints;
        remainingPoints = 0;

        return expiredPoints;
    }

    /**
     * Checks whether this usable point batch has expired.
     */
    public boolean isExpiredAt(
            LocalDateTime currentTime) {

        if (!isUsablePointBatch()
                || expiryTime == null
                || remainingPoints <= 0
                || currentTime == null) {

            return false;
        }

        return !currentTime.isBefore(expiryTime);
    }

    /**
     * Checks whether this usable point batch expires inside an inclusive
     * date-time window.
     */
    public boolean isExpiringBetween(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (!isUsablePointBatch()
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

    private boolean isUsablePointBatch() {

        return transactionType == TransactionType.EARN
                || transactionType == TransactionType.ADJUST;
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
                "%-8s %-8s %-10s %-8s %8d %10d %-20s %-20s",
                transactionId,
                loyaltyId,
                displayedBookingId,
                transactionType,
                points,
                remainingPoints,
                transactionTime,
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
