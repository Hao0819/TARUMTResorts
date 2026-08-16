package com.tarumt.resorts.dao;

import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.LoyaltyTransaction.TransactionType;
import com.tarumt.resorts.entity.MembershipTier;
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
            account.setDeactivatedAt(LocalDateTime.now());
        }

        loyaltyAccounts.enqueue(account);
    }

    /**
     * Seeds real completed-booking EARN records and a few verified historical
     * imports. ADJUST is not normal earning: here it means audited points
     * migrated from a previous system. These imported earned points qualify
     * for tier because their historical earned time is known.
     */
    private void initializeLoyaltyTransactions() {

        LocalDateTime seedTime = LocalDateTime.now();

        // L002 must start at 4,800; booking 20260010 remains unprocessed.
        addImportedEarnedPoints("L002", 4800,
                seedTime.minusMonths(3));
        addImportedEarnedPoints("L003", 7500,
                seedTime.minusMonths(4));
        addImportedEarnedPoints("L004", 12000,
                seedTime.minusMonths(5));
        addImportedEarnedPoints("L006", 23000,
                seedTime.minusMonths(2));
        // These shared Guests also appear in the VIP history. Their verified
        // legacy earnings keep the Loyalty ledger and VIP priority tier in
        // agreement instead of overwriting the shared Guest tier with NONE.
        addImportedEarnedPoints("L005", 15000,
                seedTime.minusMonths(2).minusDays(3));
        addImportedEarnedPoints("L010", 12000,
                seedTime.minusMonths(4).minusDays(2));
        addImportedEarnedPoints("L011", 17500,
                seedTime.minusMonths(3).minusDays(4));
        addImportedEarnedPoints("L012", 23000,
                seedTime.minusMonths(2).minusDays(6));

        // Demo clocks: one expires soon and one is already expired.
        addBookingEarnTransaction("L005", "20260007",
                seedTime.minusYears(1).plusMinutes(4), seedTime);
        addBookingEarnTransaction("L007", "20260008",
                seedTime.minusYears(1).minusMinutes(1), seedTime);

        // Normal batches use actual checkout time and expire one year later.
        addBookingEarnTransaction("L014", "20260009", null, seedTime);
        addBookingEarnTransaction("L009", "20260011", null, seedTime);
        addBookingEarnTransaction("L013", "20260012", null, seedTime);
        addBookingEarnTransaction("L017", "20260013", null, seedTime);
        addBookingEarnTransaction("L020", "20260014", null, seedTime);

        deriveAccountsFromLedger(seedTime);
        validateLedgerBalances(seedTime);
    }

    private void addImportedEarnedPoints(
            String loyaltyId,
            int points,
            LocalDateTime verifiedEarnedTime) {

        if (findAccountByLoyaltyId(loyaltyId) == null || points <= 0) {
            return;
        }

        loyaltyTransactions.enqueue(new LoyaltyTransaction(
                generateInitialTransactionId(), loyaltyId, null,
                TransactionType.ADJUST, points, verifiedEarnedTime,
                verifiedEarnedTime.plusYears(1)));
    }

    /**
     * Adds one EARN batch for one real CHECKED_OUT and PAID booking. A demo
     * earned time changes only the clock; expiry remains earnedTime + 1 year.
     */
    private boolean addBookingEarnTransaction(
            String loyaltyId,
            String bookingId,
            LocalDateTime demoEarnedTime,
            LocalDateTime seedTime) {

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
        LocalDateTime expiryTime = earnedTime.plusYears(1);

        LoyaltyTransaction earnTransaction = new LoyaltyTransaction(
                generateInitialTransactionId(), loyaltyId,
                booking.getConfirmationNumber(), TransactionType.EARN,
                earnedPoints, earnedTime, expiryTime);
        loyaltyTransactions.enqueue(earnTransaction);

        // Close an already-expired demo batch while preserving full history.
        if (!seedTime.isBefore(expiryTime)) {
            int expiredPoints = earnTransaction.expireRemainingPoints();
            loyaltyTransactions.enqueue(new LoyaltyTransaction(
                    generateInitialTransactionId(), loyaltyId,
                    booking.getConfirmationNumber(), TransactionType.EXPIRE,
                    expiredPoints, seedTime, null));
        }

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

        int points = account.getTierQualifyingPoints();
        MembershipTier tier;

        if (points >= 20000) {
            tier = MembershipTier.ELITE;
        } else if (points >= 15000) {
            tier = MembershipTier.DIAMOND;
        } else if (points >= 10000) {
            tier = MembershipTier.PLATINUM;
        } else if (points >= 5000) {
            tier = MembershipTier.GOLD;
        } else if (points >= 2000) {
            tier = MembershipTier.SILVER;
        } else {
            tier = MembershipTier.NONE;
        }

        account.setMembershipTier(tier);
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
