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
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.LoyaltyTransaction.TransactionType;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provides hard-coded sample data for the Loyalty and Rewards Service.
 *
 * The DAO uses the team's custom ListQueue ADT and shares the same Guest and
 * Booking objects used by the other resort modules. It does not read from or
 * write to files or a database.
 */
public class LoyaltyDAO {

    private static final int DEMO_EXPIRY_MINUTES = 4;

    private static final LocalDate OPENING_BALANCE_DATE =
            LocalDate.of(2026, 1, 1);

    private static final LocalDate FALLBACK_EARN_DATE =
            LocalDate.of(2026, 7, 1);

    private final ListQueueInterface<LoyaltyAccount> loyaltyAccounts;
    private final ListQueueInterface<LoyaltyTransaction> loyaltyTransactions;
    private final ListQueueInterface<Guest> guests;
    private final ListQueueInterface<Booking> bookings;
    private int sampleExpirySequence;

    /**
     * Creates the Loyalty sample collections from the shared Guest and
     * Booking collections.
     *
     * @param guests shared Guest collection
     * @param bookings shared Booking collection
     */
    public LoyaltyDAO(
            ListQueueInterface<Guest> guests,
            ListQueueInterface<Booking> bookings) {

        this.guests =
                guests == null
                        ? new DoublyLinkedListQueue<>()
                        : guests;

        this.bookings =
                bookings == null
                        ? new DoublyLinkedListQueue<>()
                        : bookings;

        loyaltyAccounts =
                new DoublyLinkedListQueue<>();

        loyaltyTransactions =
                new DoublyLinkedListQueue<>();

        initializeLoyaltyAccounts();
        initializeLoyaltyTransactions();
    }

    // -------------------------------------------------------------------------
    // LOYALTY ACCOUNT SAMPLE DATA
    // -------------------------------------------------------------------------

    /**
     * Creates the initial Loyalty accounts using the shared Guest objects.
     */
    private void initializeLoyaltyAccounts() {

        addInitialAccount("L001", "G001", 1000, true);     // NONE
        addInitialAccount("L002", "G002", 3200, true);     // SILVER
        addInitialAccount("L003", "G003", 7500, true);     // GOLD
        addInitialAccount("L004", "G004", 12000, true);    // PLATINUM
        addInitialAccount("L005", "G005", 17500, true);    // DIAMOND
        addInitialAccount("L006", "G006", 23000, true);    // ELITE

        addInitialAccount("L007", "G007", 850, false);     // inactive
        addInitialAccount("L008", "G008", 3200, true);     // SILVER

        addInitialAccount("L009", "G009", 7500, true);     // GOLD
        addInitialAccount("L010", "G010", 12000, true);    // PLATINUM
        addInitialAccount("L011", "G011", 17500, true);    // DIAMOND
        addInitialAccount("L012", "G012", 23000, true);    // ELITE

        addInitialAccount("L013", "G013", 500, true);      // NONE
        addInitialAccount("L014", "G014", 3200, true);     // SILVER
        addInitialAccount("L015", "G015", 7500, true);     // GOLD
        addInitialAccount("L016", "G016", 900, true);      // NONE

        addInitialAccount("L017", "G017", 3200, true);     // SILVER
        addInitialAccount("L018", "G018", 7500, true);     // GOLD
        addInitialAccount("L019", "G019", 3200, true);     // SILVER
        addInitialAccount("L020", "G020", 7500, true);     // GOLD
    }

    /**
     * Adds one initial Loyalty account when the Guest exists.
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

        if (!active) {
            account.setDeactivatedAt(
                    LocalDateTime.now());
        }

        loyaltyAccounts.enqueue(account);
    }

    // -------------------------------------------------------------------------
    // SHARED DATA LOOKUP HELPERS
    // -------------------------------------------------------------------------

    /**
     * Finds a Guest from the shared Guest collection.
     */
    private Guest findGuestById(String guestId) {

        if (guestId == null
                || guestId.trim().isEmpty()) {

            return null;
        }

        return guests.searchByKey(
                guestId.trim(),
                guest -> guest.getGuestId()
        );
    }

    /**
     * Finds a Booking from the shared Booking collection.
     */
    private Booking findBookingById(String bookingId) {

        if (bookingId == null
                || bookingId.trim().isEmpty()) {

            return null;
        }

        return bookings.searchByKey(
                bookingId.trim(),
                booking -> booking.getConfirmationNumber()
        );
    }

    /**
     * Finds a Loyalty account by Loyalty ID.
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

    // -------------------------------------------------------------------------
    // TRANSACTION SAMPLE DATA
    // -------------------------------------------------------------------------

    /**
     * Creates the opening-balance and completed-stay sample transactions.
     *
     * Eligible sample bookings are used as real EARN records. If one of those
     * bookings is unavailable or no longer eligible, the member's existing
     * balance is preserved as an opening-balance ADJUST record instead.
     */
    private void initializeLoyaltyTransactions() {

        seedBookingOrOpeningBalance("L005", "20260007");
        seedBookingOrOpeningBalance("L007", "20260008");
        seedBookingOrOpeningBalance("L014", "20260009");
        seedBookingOrOpeningBalance("L009", "20260011");
        seedBookingOrOpeningBalance("L013", "20260012");
        seedBookingOrOpeningBalance("L017", "20260013");

        addFullOpeningBalance("L001");
        addFullOpeningBalance("L002");
        addFullOpeningBalance("L003");
        addFullOpeningBalance("L004");
        addFullOpeningBalance("L006");
        addFullOpeningBalance("L008");
        addFullOpeningBalance("L010");
        addFullOpeningBalance("L011");
        addFullOpeningBalance("L012");
        addFullOpeningBalance("L015");
        addFullOpeningBalance("L016");
        addFullOpeningBalance("L018");
        addFullOpeningBalance("L019");
        addFullOpeningBalance("L020");
    }

    /**
     * Attempts to seed a real EARN record from a completed booking.
     * If the booking is not eligible, the account balance is recorded as
     * an opening adjustment so the transaction ledger remains consistent.
     */
    private void seedBookingOrOpeningBalance(
            String loyaltyId,
            String bookingId) {

        if (!addBookingEarnTransaction(
                loyaltyId,
                bookingId)) {

            addFullOpeningBalance(loyaltyId);
        }
    }

    /**
     * Records the entire current balance of one account as an opening
     * adjustment.
     */
    private void addFullOpeningBalance(String loyaltyId) {

        LoyaltyAccount account =
                findAccountByLoyaltyId(loyaltyId);

        if (account == null) {
            return;
        }

        addOpeningBalance(
                loyaltyId,
                account.getPointsBalance());
    }

    /**
     * Adds an opening-balance transaction.
     *
     * Opening points are divided into separate demo batches so one member can
     * have points expiring at different times.
     */
    private void addOpeningBalance(
            String loyaltyId,
            int points) {

        if (points <= 0) {
            return;
        }

        int firstBatch = points / 2;
        int secondBatch = points - firstBatch;

        addOpeningBalanceBatch(
                loyaltyId,
                firstBatch);

        addOpeningBalanceBatch(
                loyaltyId,
                secondBatch);
    }

    /**
     * Adds one expiring opening-balance batch.
     */
    private void addOpeningBalanceBatch(
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
                        OPENING_BALANCE_DATE,
                        nextSampleExpiryTime()
                );

        loyaltyTransactions.enqueue(transaction);
    }

    /**
     * Gives each seeded point batch a slightly different four-minute expiry.
     * The stagger simulates points earned at different moments before startup.
     */
    private LocalDateTime nextSampleExpiryTime() {

        LocalDateTime simulatedActivityTime =
                LocalDateTime.now().minusSeconds(
                        sampleExpirySequence * 3L);

        sampleExpirySequence++;

        return simulatedActivityTime.plusMinutes(
                DEMO_EXPIRY_MINUTES);
    }

    /**
     * Adds an EARN transaction from a real CHECKED_OUT and PAID booking.
     *
     * The transaction ledger is reconciled with the account's configured
     * starting balance:
     * - older points are represented by ADJUST;
     * - booking-earned points are represented by EARN;
     * - any already-used portion is represented by REDEEM.
     *
     * @return true when the booking was valid and transaction data was seeded
     */
    private boolean addBookingEarnTransaction(
            String loyaltyId,
            String bookingId) {

        LoyaltyAccount account =
                findAccountByLoyaltyId(loyaltyId);

        Booking booking =
                findBookingById(bookingId);

        if (account == null || booking == null) {
            return false;
        }

        if (!isEligibleCompletedBooking(
                account,
                booking)) {

            return false;
        }

        int earnedPoints =
                (int) Math.floor(
                        booking.getAmount());

        if (earnedPoints <= 0) {
            return false;
        }

        LocalDate earnDate =
                booking.getScheduledCheckOutDate();

        if (earnDate == null) {
            earnDate = FALLBACK_EARN_DATE;
        }

        int targetBalance =
                account.getPointsBalance();

        /*
         * If the member already had points before this booking, store the
         * older portion as separate opening point batches.
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
                        nextSampleExpiryTime()
                );

        /*
         * If the booking earned more points than the member's configured
         * current balance, the difference represents points already redeemed
         * before the program starts.
         */
        if (earnedPoints > targetBalance) {

            int previouslyRedeemed =
                    earnedPoints - targetBalance;

            earnTransaction.deductRemainingPoints(
                    previouslyRedeemed);

            loyaltyTransactions.enqueue(
                    earnTransaction);

            LoyaltyTransaction redeemTransaction =
                    new LoyaltyTransaction(
                            generateInitialTransactionId(),
                            loyaltyId,
                            null,
                            TransactionType.REDEEM,
                            previouslyRedeemed,
                            earnDate.plusDays(1),
                            null
                    );

            loyaltyTransactions.enqueue(
                    redeemTransaction);

            return true;
        }

        loyaltyTransactions.enqueue(
                earnTransaction);

        return true;
    }

    /**
     * Checks whether a Booking can be used as a real Loyalty EARN record.
     */
    private boolean isEligibleCompletedBooking(
            LoyaltyAccount account,
            Booking booking) {

        if (account == null
                || booking == null
                || booking.getGuest() == null
                || booking.getGuest().getGuestId() == null
                || account.getGuestId() == null) {

            return false;
        }

        boolean checkedOut =
                booking.getStatus() != null
                && booking.getStatus()
                        .equalsIgnoreCase(
                                "CHECKED_OUT");

        boolean paid =
                booking.getPaymentStatus() != null
                && booking.getPaymentStatus()
                        .equalsIgnoreCase(
                                "PAID");

        boolean sameGuest =
                account.getGuestId()
                        .equalsIgnoreCase(
                                booking.getGuest()
                                        .getGuestId());

        return checkedOut && paid && sameGuest;
    }

    /**
     * Generates the next transaction ID for sample data.
     */
    private String generateInitialTransactionId() {

        int nextNumber =
                loyaltyTransactions.getNumberOfEntries() + 1;

        return String.format(
                "T%03d",
                nextNumber);
    }

    // -------------------------------------------------------------------------
    // COLLECTION ACCESS
    // -------------------------------------------------------------------------

    /**
     * Returns the initialized Loyalty-account collection.
     */
    public ListQueueInterface<LoyaltyAccount>
            getLoyaltyAccounts() {

        return loyaltyAccounts;
    }

    /**
     * Returns the initialized Loyalty-transaction collection.
     */
    public ListQueueInterface<LoyaltyTransaction>
            getLoyaltyTransactions() {

        return loyaltyTransactions;
    }
}
