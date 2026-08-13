/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.dao;

import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.LoyaltyTransaction.TransactionType;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.MembershipTier;

import java.time.LocalDate;

/**
 * LoyaltyInitializerData.java
 *
 * Creates and initializes sample loyalty-account data using
 * the team's shared generic ListQueue ADT.
 *
 * No Java Collection Framework classes are used.
 *
 * @author YourName
 */
public class LoyaltyInitializerData {

    private ListQueueInterface<LoyaltyAccount> loyaltyAccounts;
    private ListQueueInterface<LoyaltyTransaction> loyaltyTransactions;
    private ListQueueInterface<Guest> guests;
    private ListQueueInterface<Booking> bookings;

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------

    /**
     * Creates the custom ADT collection and loads sample data.
     */
    public LoyaltyInitializerData(
        ListQueueInterface<Guest> guests,
        ListQueueInterface<Booking> bookings) {

        this.guests = guests;
        this.bookings = bookings;

        loyaltyAccounts =
        new DoublyLinkedListQueue<>();

        loyaltyTransactions =
        new DoublyLinkedListQueue<>();

    // -------------------------------------------------------------------------
    // LOYALTY ACCOUNT SAMPLE DATA
    // -------------------------------------------------------------------------

        initializeLoyaltyAccounts();
    // -------------------------------------------------------------------------
    // LOYALTY TRANSACTION SAMPLE DATA
    // -------------------------------------------------------------------------

        initializeLoyaltyTransactions();
    }

    // -------------------------------------------------------------------------
    // SHARED DATA LOOKUP HELPERS
    // -------------------------------------------------------------------------

    /**
     * Finds a Guest from the shared Guest collection.
     *
     * @param guestId guest ID to search
     * @return matching Guest, or null if not found
     */
    private Guest findGuestById(String guestId) {

        if (guests == null
            || guestId == null
            || guestId.trim().isEmpty()) {

            return null;
        }

        return guests.searchByKey(
            guestId.trim(),
            guest -> guest.getGuestId()
        );
    }

    /**
     * Creates loyalty accounts using the same Guest objects
     * shared by Registration, Booking and Front-Desk modules.
     */
    private void initializeLoyaltyAccounts() {

        addInitialAccount("L001", "G001", 1000, true);    // NONE
        addInitialAccount("L002", "G002", 3200, true);    // SILVER
        addInitialAccount("L003", "G003", 7500, true);    // GOLD
        addInitialAccount("L004", "G004", 12000, true);   // PLATINUM
        addInitialAccount("L005", "G005", 17500, true);   // DIAMOND
        addInitialAccount("L006", "G006", 23000, true);   // ELITE

        addInitialAccount("L007", "G007", 850, false);     // inactive -> NONE
        addInitialAccount("L008", "G008", 3200, true);    // SILVER

        addInitialAccount("L009", "G009", 7500, true);    // GOLD
        addInitialAccount("L010", "G010", 12000, true);   // PLATINUM
        addInitialAccount("L011", "G011", 17500, true);   // DIAMOND
        addInitialAccount("L012", "G012", 23000, true);   // ELITE

        addInitialAccount("L013", "G013", 500, true);      // NONE
        addInitialAccount("L014", "G014", 3200, true);    // SILVER
        addInitialAccount("L015", "G015", 7500, true);    // GOLD
        addInitialAccount("L016", "G016", 900, true);      // NONE

        addInitialAccount("L017", "G017", 3200, true);    // SILVER
        addInitialAccount("L018", "G018", 7500, true);    // GOLD
        addInitialAccount("L019", "G019", 3200, true);    // SILVER
        addInitialAccount("L020", "G020", 7500, true);    // GOLD
    }

    /**
     * Adds one initial loyalty account when the Guest exists.
     */
    private void addInitialAccount(
        String loyaltyId,
        String guestId,
        int points,
        boolean active) {

        Guest guest = findGuestById(guestId);

        if (guest == null) {
            System.out.println(
                "Warning: Guest "
                + guestId
                + " was not found. Loyalty account "
                + loyaltyId
                + " was not loaded.");

            return;
        }

        LoyaltyAccount account =
        new LoyaltyAccount(
            loyaltyId,
            guest,
            points,
            active
        );

        loyaltyAccounts.enqueue(account);
    }

    private Booking findBookingById(String bookingId) {

        if (bookings == null
            || bookingId == null
            || bookingId.trim().isEmpty()) {

            return null;
        }

        return bookings.searchByKey(
            bookingId.trim(),
            booking -> booking.getConfirmationNumber()
        );
    }

    // -------------------------------------------------------------------------
    // LOYALTY LOOKUP AND ID HELPERS
    // -------------------------------------------------------------------------

    /**
     * Finds a loyalty account by loyalty ID.
     */
    private LoyaltyAccount findAccountByLoyaltyId(
        String loyaltyId) {

        if (loyaltyId == null
            || loyaltyId.trim().isEmpty()) {

            return null;
        }

        return loyaltyAccounts.searchByKey(
            loyaltyId.trim(),
            account -> account.getLoyaltyId()
        );
    }

    /**
     * Generates the next transaction ID for initial sample data.
     */
    private String generateInitialTransactionId() {

        int nextNumber =
        loyaltyTransactions.getNumberOfEntries() + 1;

        return String.format(
            "T%03d",
            nextNumber);
    }

    // -------------------------------------------------------------------------
    // TRANSACTION BUILDING HELPERS
    // -------------------------------------------------------------------------

    /**
     * Adds an opening-balance adjustment to the transaction ledger.
     *
     * This records existing points without using a fake Booking ID.
     */
    private void addOpeningBalance(
        String loyaltyId,
        int points) {

        if (points <= 0) {
            return;
        }

        LoyaltyTransaction transaction =
        new LoyaltyTransaction(
            generateInitialTransactionId(),
            loyaltyId,
            null,
            TransactionType.ADJUST,
            points,
            LocalDate.of(2026, 1, 1),
            null
        );

        loyaltyTransactions.enqueue(transaction);
    }

    /**
     * Adds points earned from a real CHECKED_OUT booking and
     * reconciles the transaction ledger with the account's
     * current starting balance.
     */
    private void addBookingEarnTransaction(
        String loyaltyId,
        String bookingId) {

        LoyaltyAccount account =
        findAccountByLoyaltyId(loyaltyId);

        Booking booking =
        findBookingById(bookingId);

        if (account == null || booking == null) {
            return;
        }

        // Only real completed stays may generate EARN records.
        if (booking.getStatus() == null
            || !booking.getStatus()
            .equalsIgnoreCase("CHECKED_OUT")) {

            return;
        }
        
        // Only paid bookings may generate EARN records.
        if (booking.getPaymentStatus() == null
            || !booking.getPaymentStatus()
                .equalsIgnoreCase("PAID")) {

            return;
        }

        // Booking must belong to the same Guest.
        if (booking.getGuest() == null
            || account.getGuestId() == null
            || !account.getGuestId()
            .equalsIgnoreCase(
            booking.getGuest().getGuestId())) {

            return;
        }

        int earnedPoints =
        (int) Math.floor(
            booking.getAmount());

        if (earnedPoints <= 0) {
            return;
        }

        LocalDate earnDate =
        booking.getScheduledCheckOutDate();

        if (earnDate == null) {
            earnDate = LocalDate.of(2026, 7, 1);
        }

        int targetBalance =
        account.getPointsBalance();

        /*
        * If the member already had points before this booking,
        * record those older points as an opening adjustment.
        */
        if (targetBalance > earnedPoints) {

            addOpeningBalance(
                loyaltyId,
                targetBalance - earnedPoints);
        }

        LoyaltyTransaction earnTransaction =
        new LoyaltyTransaction(
            generateInitialTransactionId(),
            loyaltyId,
            booking.getConfirmationNumber(),
            TransactionType.EARN,
            earnedPoints,
            earnDate,
            earnDate.plusYears(1)
        );

        /*
        * If the booking earned more points than the member's
        * current balance, the difference must already have
        * been redeemed.
        */
        if (earnedPoints > targetBalance) {

            int redeemedPoints =
            earnedPoints - targetBalance;

            earnTransaction.deductRemainingPoints(
                redeemedPoints);

            loyaltyTransactions.enqueue(
                earnTransaction);

            LoyaltyTransaction redeemTransaction =
            new LoyaltyTransaction(
                generateInitialTransactionId(),
                loyaltyId,
                null,
                TransactionType.REDEEM,
                redeemedPoints,
                earnDate.plusDays(1),
                null
            );

            loyaltyTransactions.enqueue(
                redeemTransaction);

            return;
        }

        loyaltyTransactions.enqueue(
            earnTransaction);
    }

    private void initializeLoyaltyTransactions() {

        addBookingEarnTransaction("L005", "20260007");
        addBookingEarnTransaction("L007", "20260008");
        addBookingEarnTransaction("L014", "20260009");
        addBookingEarnTransaction("L009", "20260011");
        addBookingEarnTransaction("L013", "20260012");
        addBookingEarnTransaction("L017", "20260013");

    // Opening balances for members without a valid seed EARN record
    addOpeningBalance("L001", 1000);
    addOpeningBalance("L002", 3200);
    addOpeningBalance("L003", 7500);
    addOpeningBalance("L004", 12000);
    addOpeningBalance("L006", 23000);
    addOpeningBalance("L008", 3200);
    addOpeningBalance("L010", 12000);
    addOpeningBalance("L011", 17500);
    addOpeningBalance("L012", 23000);
    addOpeningBalance("L015", 7500);
    addOpeningBalance("L016", 900);
    addOpeningBalance("L018", 7500);
    addOpeningBalance("L019", 3200);
    addOpeningBalance("L020", 7500);

    }

    // -------------------------------------------------------------------------
    // COLLECTION ACCESS
    // -------------------------------------------------------------------------

    /**
     * Returns the initialized loyalty-account collection.
     *
     * @return custom ADT containing loyalty accounts
     */
    public ListQueueInterface<LoyaltyAccount> getLoyaltyAccounts() {
        return loyaltyAccounts;
    }

    /**
     * Returns the initialized loyalty-transaction collection.
     *
     * @return custom ADT containing loyalty transactions
     */
    public ListQueueInterface<LoyaltyTransaction>
    getLoyaltyTransactions() {

        return loyaltyTransactions;
    }
}