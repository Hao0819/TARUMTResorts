package com.tarumt.resorts.dao;

import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.LoyaltyTransaction.TransactionType;
import com.tarumt.resorts.entity.MembershipTier;
import com.tarumt.resorts.entity.RewardPackage;
import com.tarumt.resorts.util.LoyaltyClock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;

/**
 * Builds Loyalty demo data from the shared Guest and Booking objects.
 * Accounts start at zero and the transaction ledger becomes the source of
 * their balances. Storage and traversal use only the team's custom ADT.
 */
public class LoyaltyDAO {

    private static final DateTimeFormatter BOOKING_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ListQueueInterface<LoyaltyAccount> loyaltyAccounts;
    private final ListQueueInterface<LoyaltyTransaction> loyaltyTransactions;
    private final ListQueueInterface<Guest> guests;
    private final ListQueueInterface<Booking> bookings;

    public LoyaltyDAO(
            ListQueueInterface<Guest> guests,
            ListQueueInterface<Booking> bookings) {

        this.guests = guests == null
                ? new DoublyLinkedListQueue<>() : guests;
        this.bookings = bookings == null
                ? new DoublyLinkedListQueue<>() : bookings;
        loyaltyAccounts = new DoublyLinkedListQueue<>();
        loyaltyTransactions = new DoublyLinkedListQueue<>();

        initializeLoyaltyAccounts();
        initializeLoyaltyTransactions();
    }

    /** Creates member profiles with zero points before ledger seeding. */
    private void initializeLoyaltyAccounts() {
        addInitialAccount("L001", "G001", true);
        addInitialAccount("L002", "G002", true);
        addInitialAccount("L003", "G003", true);
        addInitialAccount("L004", "G004", true);
        addInitialAccount("L005", "G005", true);
        addInitialAccount("L006", "G006", true);
        addInitialAccount("L007", "G007", false);
        addInitialAccount("L008", "G008", true);
        addInitialAccount("L009", "G009", true);
        addInitialAccount("L010", "G010", true);
        addInitialAccount("L011", "G011", true);
        addInitialAccount("L012", "G012", true);
        addInitialAccount("L013", "G013", true);
        addInitialAccount("L014", "G014", true);
        addInitialAccount("L015", "G015", true);
        addInitialAccount("L016", "G016", true);
        addInitialAccount("L017", "G017", true);
        addInitialAccount("L018", "G018", true);
        addInitialAccount("L019", "G019", true);
        addInitialAccount("L020", "G020", true);
    }

    private void addInitialAccount(
            String loyaltyId,
            String guestId,
            boolean active) {

        Guest guest = findGuestById(guestId);

        if (guest == null) {
            System.out.println("Warning: Guest " + guestId
                    + " was not found. Loyalty account " + loyaltyId
                    + " was not loaded.");
            return;
        }

        LoyaltyAccount account =
                new LoyaltyAccount(loyaltyId, guest, 0, active);

        if (!active) {
            account.setDeactivatedAt(LoyaltyClock.now());
        }

        loyaltyAccounts.enqueue(account);
    }

    /** Seeds every opening point batch from a real completed paid Booking. */
    private void initializeLoyaltyTransactions() {

        LocalDateTime seedTime = LoyaltyClock.now();

        // IDs 20261001 onward are historical Loyalty stays in the same shared
        // Booking collection. Booking 20260010 intentionally remains absent
        // from the ledger so automatic processing still awards L002 200 points.
        addHistoricalBookingEarnTransactions();

        // Completed bookings use their actual checkout times.
        addBookingEarnTransaction("L005", "20260007", null);
        addBookingEarnTransaction("L007", "20260008", null);
        addBookingEarnTransaction("L014", "20260009", null);
        addBookingEarnTransaction("L009", "20260011", null);
        addBookingEarnTransaction("L013", "20260012", null);
        addBookingEarnTransaction("L017", "20260013", null);
        addBookingEarnTransaction("L020", "20260014", null);

        /*
         * Completed demo redemptions give every tier meaningful performance
         * data and cover every available reward. Each redemption consumes
         * its member's actual point batches, so the ledger remains balanced.
         */
        LocalDateTime redemptionTime = seedTime.minusMinutes(30);
        addHistoricalRedemption("L001", RewardPackage.BREAKFAST_SET,
                redemptionTime.minusMinutes(4));
        addHistoricalRedemption("L016", RewardPackage.LUNCH_SET,
                redemptionTime.minusMinutes(2));
        addHistoricalRedemption("L013", RewardPackage.BREAKFAST_SET,
                redemptionTime);
        addHistoricalRedemption("L008", RewardPackage.LUNCH_SET,
                redemptionTime.plusMinutes(2));
        addHistoricalRedemption("L003", RewardPackage.DINNER_SET,
                redemptionTime.plusMinutes(4));
        addHistoricalRedemption("L004", RewardPackage.BUFFET_VOUCHER,
                redemptionTime.plusMinutes(6));
        addHistoricalRedemption("L011", RewardPackage.BREAKFAST_SET,
                redemptionTime.plusMinutes(8));
        addHistoricalRedemption("L006", RewardPackage.BUFFET_VOUCHER,
                redemptionTime.plusMinutes(10));
        addHistoricalRedemption("L006", RewardPackage.BUFFET_VOUCHER,
                redemptionTime.plusMinutes(12));

        deriveAccountsFromLedger(seedTime);
        initializeAccountActivity(seedTime);
        validateLedgerBalances(seedTime);
    }

    /** Derives inactivity activity and the next independent batch expiry. */
    private void initializeAccountActivity(LocalDateTime seedTime) {

        Iterator<LoyaltyAccount> accountIterator =
                loyaltyAccounts.getIterator();

        while (accountIterator.hasNext()) {
            LoyaltyAccount account = accountIterator.next();
            LocalDateTime lastActivity = null;
            LocalDateTime nextExpiry = null;
            Iterator<LoyaltyTransaction> transactionIterator =
                    loyaltyTransactions.getIterator();

            while (transactionIterator.hasNext()) {
                LoyaltyTransaction transaction = transactionIterator.next();

                if (!account.getLoyaltyId().equalsIgnoreCase(
                        transaction.getLoyaltyId())) {
                    continue;
                }

                TransactionType type = transaction.getTransactionType();

                if ((type == TransactionType.EARN
                        || type == TransactionType.ADJUST
                        || type == TransactionType.REDEEM)
                        && (lastActivity == null
                        || transaction.getTransactionTime()
                                .isAfter(lastActivity))) {
                    lastActivity = transaction.getTransactionTime();
                }

                if ((type == TransactionType.EARN
                        || type == TransactionType.ADJUST)
                        && transaction.getRemainingPoints() > 0
                        && transaction.getExpiryTime() != null
                        && seedTime.isBefore(transaction.getExpiryTime())
                        && (nextExpiry == null
                        || transaction.getExpiryTime()
                                .isBefore(nextExpiry))) {
                    nextExpiry = transaction.getExpiryTime();
                }
            }

            account.setLastPointsActivityTime(
                    lastActivity == null ? seedTime : lastActivity);
            account.setPointsExpiryTime(nextExpiry);
        }
    }

    /** Adds one completed historical redemption using FEFO point batches. */
    private void addHistoricalRedemption(
            String loyaltyId,
            RewardPackage rewardPackage,
            LocalDateTime redemptionTime) {

        if (findAccountByLoyaltyId(loyaltyId) == null
                || rewardPackage == null
                || redemptionTime == null) {
            return;
        }

        ListQueueInterface<LoyaltyTransaction> usableBatches =
                new DoublyLinkedListQueue<>();
        int availablePoints = 0;
        Iterator<LoyaltyTransaction> iterator =
                loyaltyTransactions.getIterator();

        while (iterator.hasNext()) {
            LoyaltyTransaction transaction = iterator.next();
            TransactionType type = transaction.getTransactionType();

            if (!transaction.getLoyaltyId().equalsIgnoreCase(loyaltyId)
                    || (type != TransactionType.EARN
                    && type != TransactionType.ADJUST)
                    || transaction.getRemainingPoints() <= 0
                    || transaction.getTransactionTime().isAfter(
                            redemptionTime)
                    || transaction.getExpiryTime() == null
                    || !redemptionTime.isBefore(
                            transaction.getExpiryTime())) {
                continue;
            }

            usableBatches.priorityEnqueue(
                    transaction,
                    (first, second) -> first.getExpiryTime()
                            .compareTo(second.getExpiryTime()));
            availablePoints = Math.addExact(
                    availablePoints,
                    transaction.getRemainingPoints());
        }

        int points = rewardPackage.getPointsRequired();

        if (availablePoints < points) {
            throw new IllegalStateException(
                    "Insufficient demo points for " + loyaltyId
                    + " to redeem " + rewardPackage.getRewardName() + ".");
        }

        int remainingToRedeem = points;
        LocalDateTime firstBatchExpiry = null;
        iterator = usableBatches.getIterator();

        while (iterator.hasNext() && remainingToRedeem > 0) {
            LoyaltyTransaction batch = iterator.next();
            int deducted = batch.deductRemainingPoints(remainingToRedeem);

            if (deducted > 0 && firstBatchExpiry == null) {
                firstBatchExpiry = batch.getExpiryTime();
            }

            remainingToRedeem -= deducted;
        }

        LoyaltyTransaction redemption = new LoyaltyTransaction(
                generateInitialTransactionId(),
                loyaltyId,
                null,
                TransactionType.REDEEM,
                points,
                redemptionTime,
                null);
        redemption.setRewardPackage(rewardPackage);
        redemption.recordRemovalResult(
                availablePoints - points,
                firstBatchExpiry);
        loyaltyTransactions.enqueue(redemption);
    }

    /** Converts every shared historical Loyalty Booking into one EARN batch. */
    private void addHistoricalBookingEarnTransactions() {

        Iterator<Booking> iterator = bookings.getIterator();

        while (iterator.hasNext()) {
            Booking booking = iterator.next();

            if (booking == null
                    || booking.getConfirmationNumber() == null
                    || !booking.getConfirmationNumber()
                            .matches("^20261\\d{3}$")
                    || booking.getGuest() == null) {
                continue;
            }

            LoyaltyAccount account = loyaltyAccounts.searchByKey(
                    booking.getGuest().getGuestId(),
                    loyaltyAccount -> loyaltyAccount.getGuestId());

            if (account == null
                    || !addBookingEarnTransaction(
                            account.getLoyaltyId(),
                            booking.getConfirmationNumber(),
                            null)) {
                throw new IllegalStateException(
                        "Unable to seed Loyalty EARN from Booking "
                        + booking.getConfirmationNumber() + ".");
            }
        }
    }

    /**
     * Adds one EARN batch for one real CHECKED_OUT and PAID booking. The
     * transaction expiry is three months after this batch's earned time.
     */
    private boolean addBookingEarnTransaction(
            String loyaltyId,
            String bookingId,
            LocalDateTime demoEarnedTime) {

        LoyaltyAccount account = findAccountByLoyaltyId(loyaltyId);
        Booking booking = findBookingById(bookingId);

        if (account == null || booking == null
                || !isEligibleCompletedBooking(account, booking)
                || hasSeededBooking(bookingId)) {
            return false;
        }

        int earnedPoints = (int) Math.floor(booking.getAmount());

        if (earnedPoints <= 0) {
            return false;
        }

        LocalDateTime earnedTime = demoEarnedTime == null
                ? readBookingEarnedTime(booking) : demoEarnedTime;
        LocalDateTime expiryTime = earnedTime.plusMonths(3);

        LoyaltyTransaction earnTransaction = new LoyaltyTransaction(
                generateInitialTransactionId(), loyaltyId,
                booking.getConfirmationNumber(), TransactionType.EARN,
                earnedPoints, earnedTime, expiryTime);
        // ADT method called: enqueue()
        loyaltyTransactions.enqueue(earnTransaction);

        return true;
    }

    private LocalDateTime readBookingEarnedTime(Booking booking) {

        String checkoutTime = booking.getCheckOutTime();

        if (checkoutTime != null && !checkoutTime.trim().isEmpty()) {
            try {
                return LocalDateTime.parse(
                        checkoutTime.trim(), BOOKING_TIME_FORMAT);
            } catch (DateTimeParseException exception) {
                // Use the structured scheduled date when legacy text is bad.
            }
        }

        if (booking.getScheduledCheckOutDate() != null) {
            return booking.getScheduledCheckOutDate().atTime(12, 0);
        }

        throw new IllegalStateException(
                "Completed booking " + booking.getConfirmationNumber()
                + " has no usable checkout time.");
    }

    private boolean hasSeededBooking(String bookingId) {

        Iterator<LoyaltyTransaction> iterator =
                loyaltyTransactions.getIterator();

        while (iterator.hasNext()) {
            LoyaltyTransaction transaction = iterator.next();

            if (transaction.getTransactionType() == TransactionType.EARN
                    && transaction.getBookingId() != null
                    && transaction.getBookingId().equalsIgnoreCase(bookingId)) {
                return true;
            }
        }

        return false;
    }

    /** Derives redeemable balance and lifetime qualifying points from ledger. */
    private void deriveAccountsFromLedger(LocalDateTime currentTime) {

        Iterator<LoyaltyAccount> accountIterator =
                loyaltyAccounts.getIterator();

        while (accountIterator.hasNext()) {
            LoyaltyAccount account = accountIterator.next();
            int balance = 0;
            int qualifyingPoints = 0;
            Iterator<LoyaltyTransaction> transactionIterator =
                    loyaltyTransactions.getIterator();

            while (transactionIterator.hasNext()) {
                LoyaltyTransaction transaction = transactionIterator.next();

                if (!account.getLoyaltyId().equalsIgnoreCase(
                        transaction.getLoyaltyId())) {
                    continue;
                }

                if (transaction.getTransactionType() == TransactionType.EARN
                        || transaction.getTransactionType()
                                == TransactionType.ADJUST) {
                    qualifyingPoints = Math.addExact(
                            qualifyingPoints, transaction.getPoints());

                    if (isUsableAt(transaction, currentTime)) {
                        balance = Math.addExact(
                                balance, transaction.getRemainingPoints());
                    }
                }
            }

            account.setPointsBalance(balance);
            account.setTierQualifyingPoints(qualifyingPoints);
            applyTier(account);
        }
    }

    private void validateLedgerBalances(LocalDateTime currentTime) {

        Iterator<LoyaltyAccount> iterator = loyaltyAccounts.getIterator();

        while (iterator.hasNext()) {
            LoyaltyAccount account = iterator.next();
            int ledgerBalance = calculateUsableLedgerBalance(
                    account.getLoyaltyId(), currentTime);

            if (account.getPointsBalance() != ledgerBalance) {
                throw new IllegalStateException(
                        "Loyalty ledger mismatch for "
                        + account.getLoyaltyId() + ": account="
                        + account.getPointsBalance() + ", ledger="
                        + ledgerBalance);
            }
        }
    }

    private int calculateUsableLedgerBalance(
            String loyaltyId,
            LocalDateTime currentTime) {

        int total = 0;
        Iterator<LoyaltyTransaction> iterator =
                loyaltyTransactions.getIterator();

        while (iterator.hasNext()) {
            LoyaltyTransaction transaction = iterator.next();
            TransactionType type = transaction.getTransactionType();

            if (transaction.getLoyaltyId().equalsIgnoreCase(loyaltyId)
                    && (type == TransactionType.EARN
                            || type == TransactionType.ADJUST)
                    && isUsableAt(transaction, currentTime)) {
                total = Math.addExact(total,
                        transaction.getRemainingPoints());
            }
        }

        return total;
    }

    private boolean isUsableAt(
            LoyaltyTransaction transaction,
            LocalDateTime currentTime) {

        return transaction.getRemainingPoints() > 0
                && transaction.getExpiryTime() != null
                && currentTime.isBefore(transaction.getExpiryTime());
    }

    private void applyTier(LoyaltyAccount account) {
        account.setMembershipTier(
                MembershipTier.fromTierQualifyingPoints(
                        account.getTierQualifyingPoints()));
    }

    private boolean isEligibleCompletedBooking(
            LoyaltyAccount account,
            Booking booking) {

        return booking.getGuest() != null
                && booking.getGuest().getGuestId() != null
                && account.getGuestId() != null
                && account.getGuestId().equalsIgnoreCase(
                        booking.getGuest().getGuestId())
                && booking.getStatus() != null
                && booking.getStatus().equalsIgnoreCase("CHECKED_OUT")
                && booking.getPaymentStatus() != null
                && booking.getPaymentStatus().equalsIgnoreCase("PAID");
    }

    private Guest findGuestById(String guestId) {
        return guests.searchByKey(guestId, guest -> guest.getGuestId());
    }

    private Booking findBookingById(String bookingId) {
        return bookings.searchByKey(
                bookingId, booking -> booking.getConfirmationNumber());
    }

    private LoyaltyAccount findAccountByLoyaltyId(String loyaltyId) {
        return loyaltyAccounts.searchByKey(
                loyaltyId, account -> account.getLoyaltyId());
    }

    private String generateInitialTransactionId() {
        return String.format("T%03d",
                loyaltyTransactions.getNumberOfEntries() + 1);
    }

    public ListQueueInterface<LoyaltyAccount> getLoyaltyAccounts() {
        return loyaltyAccounts;
    }

    public ListQueueInterface<LoyaltyTransaction> getLoyaltyTransactions() {
        return loyaltyTransactions;
    }
}
