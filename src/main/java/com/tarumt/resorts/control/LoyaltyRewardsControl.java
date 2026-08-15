/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.control;

import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.LoyaltyTransaction.TransactionType;
import com.tarumt.resorts.entity.MembershipTier;
import com.tarumt.resorts.entity.RewardPackage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Handles the business logic for the Loyalty and Rewards Service.
 *
 * Functions include member searching, account creation, points earning,
 * direct reward redemption, tier progression, account status management,
 * promotions, reporting, and point expiry.
 */
public class LoyaltyRewardsControl {

    private static final double POINTS_PER_RM = 1.0;

    private ListQueueInterface<LoyaltyAccount> loyaltyAccounts;
    private ListQueueInterface<LoyaltyTransaction> loyaltyTransactions;
    private ListQueueInterface<Guest> guests;
    private ListQueueInterface<Booking> bookings;

    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------

    /**
     * Creates an empty Loyalty and Rewards control.
     */
    public LoyaltyRewardsControl() {
        loyaltyAccounts = new DoublyLinkedListQueue<>();
        loyaltyTransactions = new DoublyLinkedListQueue<>();
        guests = new DoublyLinkedListQueue<>();
        bookings = new DoublyLinkedListQueue<>();
    }

    /**
     * Creates the Loyalty control using the shared collections.
     *
     * @param loyaltyAccounts shared loyalty-account collection
     * @param loyaltyTransactions shared loyalty-transaction collection
     * @param guests shared guest collection
     * @param bookings shared booking collection
     */
    public LoyaltyRewardsControl(
            ListQueueInterface<LoyaltyAccount> loyaltyAccounts,
            ListQueueInterface<LoyaltyTransaction> loyaltyTransactions,
            ListQueueInterface<Guest> guests,
            ListQueueInterface<Booking> bookings) {

        this.loyaltyAccounts =
                loyaltyAccounts == null
                        ? new DoublyLinkedListQueue<>()
                        : loyaltyAccounts;

        this.loyaltyTransactions =
                loyaltyTransactions == null
                        ? new DoublyLinkedListQueue<>()
                        : loyaltyTransactions;

        this.guests =
                guests == null
                        ? new DoublyLinkedListQueue<>()
                        : guests;

        this.bookings =
                bookings == null
                        ? new DoublyLinkedListQueue<>()
                        : bookings;

        recalculateAllTiers();
    }

    // -------------------------------------------------------------------------
    // COLLECTION ACCESS
    // -------------------------------------------------------------------------

    /**
     * Returns all loyalty accounts.
     *
     * @return shared loyalty-account collection
     */
    public ListQueueInterface<LoyaltyAccount> getLoyaltyAccounts() {
        return loyaltyAccounts;
    }

    /**
     * Returns all loyalty transactions.
     *
     * Kept as a public accessor because another integration or report may
     * still need the shared transaction collection.
     *
     * @return shared loyalty-transaction collection
     */
    public ListQueueInterface<LoyaltyTransaction> getLoyaltyTransactions() {
        return loyaltyTransactions;
    }

    // -------------------------------------------------------------------------
    // MEMBER AND GUEST SEARCH
    // -------------------------------------------------------------------------

    /**
     * Finds a loyalty member by Loyalty ID.
     *
     * @param loyaltyId Loyalty ID to search
     * @return matching account, or null if not found
     */
    public LoyaltyAccount findMemberByLoyaltyId(String loyaltyId) {

        if (loyaltyId == null || loyaltyId.trim().isEmpty()) {
            return null;
        }

        return loyaltyAccounts.searchByKey(
                loyaltyId.trim(),
                account -> account.getLoyaltyId()
        );
    }

    /**
     * Finds a loyalty member by Guest ID.
     *
     * @param guestId Guest ID to search
     * @return matching account, or null if not found
     */
    public LoyaltyAccount findMemberByGuestId(String guestId) {

        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }

        return loyaltyAccounts.searchByKey(
                guestId.trim(),
                account -> account.getGuestId()
        );
    }

    /**
     * Finds a guest from the shared Guest collection.
     *
     * @param guestId Guest ID to search
     * @return matching guest, or null if not found
     */
    public Guest findGuestById(String guestId) {

        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }

        return guests.searchByKey(
                guestId.trim(),
                guest -> guest.getGuestId()
        );
    }

    /**
     * Finds a booking using its confirmation number.
     *
     * @param confirmationNumber booking confirmation number
     * @return matching booking, or null if not found
     */
    public Booking findBookingByConfirmationNumber(
            String confirmationNumber) {

        if (confirmationNumber == null
                || confirmationNumber.trim().isEmpty()) {

            return null;
        }

        return bookings.searchByKey(
                confirmationNumber.trim(),
                booking -> booking.getConfirmationNumber()
        );
    }

    // -------------------------------------------------------------------------
    // ACCOUNT CREATION
    // -------------------------------------------------------------------------

    /**
     * Creates and stores a new loyalty account.
     *
     * The method prevents duplicate Loyalty IDs and prevents the same Guest
     * from owning more than one loyalty account.
     *
     * @param newAccount account to create
     * @return true if the account is created successfully
     */
    public boolean createLoyaltyAccount(LoyaltyAccount newAccount) {

        if (newAccount == null
                || newAccount.getLoyaltyId() == null
                || newAccount.getLoyaltyId().trim().isEmpty()
                || newAccount.getGuest() == null
                || newAccount.getGuestId() == null
                || newAccount.getGuestId().trim().isEmpty()) {

            return false;
        }

        if (findMemberByLoyaltyId(
                newAccount.getLoyaltyId()) != null) {

            return false;
        }

        if (findMemberByGuestId(
                newAccount.getGuestId()) != null) {

            return false;
        }

        return loyaltyAccounts.enqueue(newAccount);
    }

    /**
     * Creates a loyalty account for an existing Guest.
     *
     * @param guestId existing Guest ID
     * @return created account, or null if creation fails
     */
    public LoyaltyAccount createAccountForGuest(String guestId) {

        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }

        String trimmedGuestId = guestId.trim();

        Guest guest = findGuestById(trimmedGuestId);

        if (guest == null) {
            return null;
        }

        if (findMemberByGuestId(trimmedGuestId) != null) {
            return null;
        }

        /*
         * A Guest without a LoyaltyAccount should normally have tier NONE.
         * This prevents an existing tier from being silently overwritten.
         */
        if (guest.getMembershipTier() != MembershipTier.NONE) {
            return null;
        }

        LoyaltyAccount newAccount =
                new LoyaltyAccount(
                        generateLoyaltyId(),
                        guest,
                        0,
                        true
                );

        recalculateTier(newAccount);

        if (!createLoyaltyAccount(newAccount)) {
            return null;
        }

        return newAccount;
    }

    /**
     * Generates the next available Loyalty ID.
     *
     * @return ID such as L001, L002, L003
     */
    private String generateLoyaltyId() {

        int number = 1;
        String loyaltyId;

        do {
            loyaltyId = String.format("L%03d", number);
            number++;
        } while (findMemberByLoyaltyId(loyaltyId) != null);

        return loyaltyId;
    }

    // -------------------------------------------------------------------------
    // COMPLETED-BOOKING INTEGRATION
    // -------------------------------------------------------------------------

    /**
     * Processes eligible completed bookings for Loyalty automatically.
     *
     * A CHECKED_OUT and PAID booking that has not received points will:
     * 1. create a Loyalty account for the Guest when needed, and
     * 2. award the completed-stay points.
     *
     * This method is exposed as the Loyalty-side integration hook for the
     * checkout/front-desk module.
     */
    public void processCompletedBookingsForLoyalty() {

        Iterator<Booking> bookingIterator =
                bookings.getIterator();

        while (bookingIterator.hasNext()) {

            Booking booking = bookingIterator.next();

            if (booking == null
                    || booking.getGuest() == null
                    || booking.getGuest().getGuestId() == null
                    || booking.getConfirmationNumber() == null) {

                continue;
            }

            if (!isCompletedAndPaid(booking)) {
                continue;
            }

            if (hasBookingReceivedPoints(
                    booking.getConfirmationNumber())) {

                continue;
            }

            String guestId =
                    booking.getGuest().getGuestId();

            LoyaltyAccount account =
                    findMemberByGuestId(guestId);

            if (account == null) {
                account = createAccountForGuest(guestId);
            }

            if (account == null || !account.isActive()) {
                continue;
            }

            addPointsFromCompletedStay(
                    account.getLoyaltyId(),
                    booking.getConfirmationNumber());
        }
    }

    /**
     * Checks whether a booking is completed and fully paid.
     *
     * @param booking booking to check
     * @return true for CHECKED_OUT and PAID bookings
     */
    private boolean isCompletedAndPaid(Booking booking) {

        return booking != null
                && booking.getStatus() != null
                && booking.getStatus()
                        .equalsIgnoreCase("CHECKED_OUT")
                && booking.getPaymentStatus() != null
                && booking.getPaymentStatus()
                        .equalsIgnoreCase("PAID");
    }

    // -------------------------------------------------------------------------
    // POINT EARNING
    // -------------------------------------------------------------------------

    /**
     * Checks whether a booking has already received Loyalty points.
     *
     * @param bookingId booking confirmation number
     * @return true if an EARN transaction already exists for the booking
     */
    public boolean hasBookingReceivedPoints(String bookingId) {

        if (bookingId == null || bookingId.trim().isEmpty()) {
            return false;
        }

        LoyaltyTransaction transaction =
                loyaltyTransactions.searchByKey(
                        bookingId.trim(),
                        currentTransaction -> {

                            if (currentTransaction.getTransactionType()
                                    == TransactionType.EARN) {

                                return currentTransaction.getBookingId();
                            }

                            return null;
                        }
                );

        return transaction != null;
    }

    /**
     * Calculates Loyalty points from the booking amount.
     *
     * Current conversion: RM1 spent = 1 Loyalty point.
     *
     * @param booking booking to calculate
     * @return calculated points
     */
    public int calculateRewardPoints(Booking booking) {

        if (booking == null || booking.getAmount() <= 0) {
            return 0;
        }

        return (int) Math.floor(
                booking.getAmount() * POINTS_PER_RM
        );
    }

    /**
     * Adds Loyalty points from an eligible completed stay.
     *
     * The booking must exist, belong to the same Guest, be CHECKED_OUT,
     * be PAID, and must not have received points before.
     *
     * EARN points expire four minutes after being awarded for demonstration.
     *
     * @param loyaltyId Loyalty account ID
     * @param bookingId booking confirmation number
     * @return true if points are awarded successfully
     */
    public boolean addPointsFromCompletedStay(
            String loyaltyId,
            String bookingId) {

        if (loyaltyId == null
                || loyaltyId.trim().isEmpty()
                || bookingId == null
                || bookingId.trim().isEmpty()) {

            return false;
        }

        LoyaltyAccount account =
                findMemberByLoyaltyId(loyaltyId.trim());

        if (account == null || !account.isActive()) {
            return false;
        }

        Booking booking =
                findBookingByConfirmationNumber(
                        bookingId.trim());

        if (booking == null
                || booking.getGuest() == null
                || booking.getGuest().getGuestId() == null) {

            return false;
        }

        if (account.getGuestId() == null
                || !account.getGuestId()
                        .equalsIgnoreCase(
                                booking.getGuest().getGuestId())) {

            return false;
        }

        if (!isCompletedAndPaid(booking)) {
            return false;
        }

        if (hasBookingReceivedPoints(
                booking.getConfirmationNumber())) {

            return false;
        }

        int points = calculateRewardPoints(booking);

        if (points <= 0) {
            return false;
        }

        long newBalance =
                (long) account.getPointsBalance() + points;

        if (newBalance > Integer.MAX_VALUE) {
            return false;
        }

        LocalDateTime earnedTime =
                LocalDateTime.now();

        LoyaltyTransaction earnTransaction =
                new LoyaltyTransaction(
                        generateTransactionId(),
                        account.getLoyaltyId(),
                        booking.getConfirmationNumber(),
                        TransactionType.EARN,
                        points,
                        earnedTime.toLocalDate(),
                        earnedTime.plusMinutes(4)
                );

        if (!loyaltyTransactions.enqueue(earnTransaction)) {
            return false;
        }

        account.setPointsBalance((int) newBalance);
        recalculateTier(account);

        return true;
    }

    // -------------------------------------------------------------------------
    // DIRECT REWARD REDEMPTION
    // -------------------------------------------------------------------------

    /**
     * Redeems a selected RewardPackage immediately.
     *
     * @param loyaltyId Loyalty account ID
     * @param rewardPackage selected reward
     * @return true if the reward is redeemed successfully
     */
    public boolean redeemReward(
            String loyaltyId,
            RewardPackage rewardPackage) {

        if (loyaltyId == null
                || loyaltyId.trim().isEmpty()
                || rewardPackage == null) {

            return false;
        }

        return redeemPoints(
                loyaltyId.trim(),
                rewardPackage.getPointsRequired());
    }

    /**
     * Performs the point deduction for a direct redemption.
     *
     * ADJUST points never expire. EARN points may only be used while they
     * still have remaining points and have not expired.
     */
    private boolean redeemPoints(
            String loyaltyId,
            int points) {

        if (loyaltyId == null
                || loyaltyId.trim().isEmpty()
                || points <= 0) {

            return false;
        }

        LoyaltyAccount account =
                findMemberByLoyaltyId(loyaltyId.trim());

        if (account == null || !account.isActive()) {
            return false;
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        /*
         * Keep the account balance synchronized before checking whether the
         * member can redeem. This is especially important for the 4-minute
         * demonstration expiry.
         */
        processExpiredPoints(currentTime);

        if (account.getPointsBalance() < points) {
            return false;
        }

        int availablePoints = 0;

        Iterator<LoyaltyTransaction> checkIterator =
                loyaltyTransactions.getIterator();

        while (checkIterator.hasNext()) {

            LoyaltyTransaction transaction =
                    checkIterator.next();

            if (isUsablePointsTransaction(
                    transaction,
                    account.getLoyaltyId(),
                    currentTime)) {

                availablePoints +=
                        transaction.getRemainingPoints();
            }
        }

        if (availablePoints < points) {
            return false;
        }

        int remainingToRedeem = points;

        Iterator<LoyaltyTransaction> redeemIterator =
                loyaltyTransactions.getIterator();

        while (redeemIterator.hasNext()
                && remainingToRedeem > 0) {

            LoyaltyTransaction transaction =
                    redeemIterator.next();

            if (isUsablePointsTransaction(
                    transaction,
                    account.getLoyaltyId(),
                    currentTime)) {

                int deducted =
                        transaction.deductRemainingPoints(
                                remainingToRedeem);

                remainingToRedeem -= deducted;
            }
        }

        account.setPointsBalance(
                account.getPointsBalance() - points);

        recalculateTier(account);

        LoyaltyTransaction redeemTransaction =
                new LoyaltyTransaction(
                        generateTransactionId(),
                        account.getLoyaltyId(),
                        null,
                        TransactionType.REDEEM,
                        points,
                        currentTime.toLocalDate(),
                        null
                );

        loyaltyTransactions.enqueue(redeemTransaction);

        return true;
    }

    /**
     * Checks whether a transaction contains points that may be redeemed.
     */
    private boolean isUsablePointsTransaction(
            LoyaltyTransaction transaction,
            String loyaltyId,
            LocalDateTime currentTime) {

        if (transaction == null
                || loyaltyId == null
                || currentTime == null
                || transaction.getLoyaltyId() == null
                || !transaction.getLoyaltyId()
                        .equalsIgnoreCase(loyaltyId)
                || transaction.getRemainingPoints() <= 0) {

            return false;
        }

        if (transaction.getTransactionType()
                == TransactionType.ADJUST) {

            return true;
        }

        return transaction.getTransactionType()
                == TransactionType.EARN
                && !transaction.isExpiredAt(currentTime);
    }

    // -------------------------------------------------------------------------
    // TRANSACTION ID
    // -------------------------------------------------------------------------

    /**
     * Generates the next transaction ID.
     *
     * @return ID such as T001, T002, T003
     */
    private String generateTransactionId() {

        int nextNumber =
                loyaltyTransactions.getNumberOfEntries() + 1;

        return String.format("T%03d", nextNumber);
    }

    // -------------------------------------------------------------------------
    // TIER MANAGEMENT AND PROMOTIONS
    // -------------------------------------------------------------------------

    /**
     * Recalculates a member's tier based on current points.
     *
     * NONE: 0-1,999
     * SILVER: 2,000-4,999
     * GOLD: 5,000-9,999
     * PLATINUM: 10,000-14,999
     * DIAMOND: 15,000-19,999
     * ELITE: 20,000+
     *
     * Inactive accounts always use tier NONE.
     *
     * @param account account to update
     */
    public void recalculateTier(LoyaltyAccount account) {

        if (account == null) {
            return;
        }

        if (!account.isActive()) {
            account.setMembershipTier(MembershipTier.NONE);
            return;
        }

        int points = account.getPointsBalance();

        MembershipTier newTier;

        if (points >= 20000) {
            newTier = MembershipTier.ELITE;
        } else if (points >= 15000) {
            newTier = MembershipTier.DIAMOND;
        } else if (points >= 10000) {
            newTier = MembershipTier.PLATINUM;
        } else if (points >= 5000) {
            newTier = MembershipTier.GOLD;
        } else if (points >= 2000) {
            newTier = MembershipTier.SILVER;
        } else {
            newTier = MembershipTier.NONE;
        }

        account.setMembershipTier(newTier);
    }

    /**
     * Recalculates all tiers when the Loyalty module is initialized.
     */
    private void recalculateAllTiers() {

        Iterator<LoyaltyAccount> iterator =
                loyaltyAccounts.getIterator();

        while (iterator.hasNext()) {
            recalculateTier(iterator.next());
        }
    }

    /**
     * Returns the personalized promotion for a tier.
     *
     * @param tier membership tier
     * @return promotion description
     */
    public String getPromotionForTier(MembershipTier tier) {

        if (tier == null) {
            return "No promotion";
        }

        return switch (tier) {
            case NONE -> "No special promotion";
            case SILVER -> "5% dining discount";
            case GOLD -> "10% dining discount";
            case PLATINUM -> "Free breakfast voucher";
            case DIAMOND -> "15% dining + room upgrade";
            case ELITE -> "20% dining + VIP benefits";
        };
    }

    // -------------------------------------------------------------------------
    // ACCOUNT STATUS MANAGEMENT
    // -------------------------------------------------------------------------

    /**
     * Activates or deactivates a Loyalty account.
     *
     * Inactive accounts cannot earn or redeem points and use tier NONE.
     * Reactivating restores the correct tier from the current point balance.
     *
     * @param loyaltyId Loyalty account ID
     * @param active new active status
     * @return true if the account is found and updated
     */
    public boolean updateAccountStatus(
            String loyaltyId,
            boolean active) {

        if (loyaltyId == null
                || loyaltyId.trim().isEmpty()) {

            return false;
        }

        LoyaltyAccount account =
                findMemberByLoyaltyId(
                        loyaltyId.trim());

        if (account == null) {
            return false;
        }

        account.setActive(active);

        if (active) {
            recalculateTier(account);
        } else {
            account.setMembershipTier(
                    MembershipTier.NONE);
        }

        return true;
    }

    // -------------------------------------------------------------------------
    // TIER AND POINTS REPORT
    // -------------------------------------------------------------------------

    /**
     * Filters Loyalty members by tier, status, and minimum points.
     *
     * Results are ordered from the highest point balance to the lowest.
     *
     * @param tier selected tier, or null for all tiers
     * @param statusFilter 0 = all, 1 = active, 2 = inactive
     * @param minimumPoints minimum point balance
     * @return matching accounts in the custom ADT
     */
    public ListQueueInterface<LoyaltyAccount> filterMembers(
            MembershipTier tier,
            int statusFilter,
            int minimumPoints) {

        ListQueueInterface<LoyaltyAccount> emptyResult =
                new DoublyLinkedListQueue<>();

        if (minimumPoints < 0
                || statusFilter < 0
                || statusFilter > 2) {

            return emptyResult;
        }

        ListQueueInterface<LoyaltyAccount> filteredAccounts =
                loyaltyAccounts.filter(account -> {

                    boolean matchesTier =
                            tier == null
                                    || account.getMembershipTier()
                                    == tier;

                    boolean matchesStatus;

                    if (statusFilter == 1) {
                        matchesStatus = account.isActive();
                    } else if (statusFilter == 2) {
                        matchesStatus = !account.isActive();
                    } else {
                        matchesStatus = true;
                    }

                    boolean matchesPoints =
                            account.getPointsBalance()
                                    >= minimumPoints;

                    return matchesTier
                            && matchesStatus
                            && matchesPoints;
                });

        ListQueueInterface<LoyaltyAccount> orderedAccounts =
                new DoublyLinkedListQueue<>();

        Iterator<LoyaltyAccount> iterator =
                filteredAccounts.getIterator();

        while (iterator.hasNext()) {

            LoyaltyAccount account =
                    iterator.next();

            orderedAccounts.priorityEnqueue(
                    account,
                    (firstAccount, secondAccount) ->
                            Integer.compare(
                                    secondAccount.getPointsBalance(),
                                    firstAccount.getPointsBalance())
            );
        }

        return orderedAccounts;
    }

    // -------------------------------------------------------------------------
    // EXPIRING POINTS REPORTS
    // -------------------------------------------------------------------------

    /**
     * Generates the expiring-points report.
     *
     * Only EARN transactions with remaining points are considered.
     * Results are ordered by the earliest expiry time first.
     *
     * @param startTime start of expiry window
     * @param endTime end of expiry window
     * @param tier selected tier, or null for all tiers
     * @param minimumExpiringPoints minimum remaining expiring points
     * @return matching expiring transactions
     */
    public ListQueueInterface<LoyaltyTransaction>
            generateExpiringPointsReport(
                    LocalDateTime startTime,
                    LocalDateTime endTime,
                    MembershipTier tier,
                    int minimumExpiringPoints) {

        ListQueueInterface<LoyaltyTransaction> emptyResult =
                new DoublyLinkedListQueue<>();

        if (startTime == null
                || endTime == null
                || endTime.isBefore(startTime)
                || minimumExpiringPoints < 0) {

            return emptyResult;
        }

        ListQueueInterface<LoyaltyTransaction> filteredTransactions =
                loyaltyTransactions.filter(transaction -> {

                    if (transaction.getTransactionType()
                            != TransactionType.EARN) {

                        return false;
                    }

                    if (!transaction.isExpiringBetween(
                            startTime,
                            endTime)) {

                        return false;
                    }

                    if (transaction.getRemainingPoints()
                            < minimumExpiringPoints) {

                        return false;
                    }

                    LoyaltyAccount account =
                            findMemberByLoyaltyId(
                                    transaction.getLoyaltyId());

                    if (account == null) {
                        return false;
                    }

                    return tier == null
                            || account.getMembershipTier()
                            == tier;
                });

        ListQueueInterface<LoyaltyTransaction> orderedTransactions =
                new DoublyLinkedListQueue<>();

        Iterator<LoyaltyTransaction> iterator =
                filteredTransactions.getIterator();

        while (iterator.hasNext()) {

            LoyaltyTransaction transaction =
                    iterator.next();

            orderedTransactions.priorityEnqueue(
                    transaction,
                    (first, second) ->
                            first.getExpiryTime()
                                    .compareTo(
                                            second.getExpiryTime())
            );
        }

        return orderedTransactions;
    }

    /**
     * Generates notifications for points expiring within a number of minutes.
     *
     * @param currentTime current date and time
     * @param minutesAhead notification window in minutes
     * @return matching expiring transactions
     */
    public ListQueueInterface<LoyaltyTransaction>
            generateExpiringPointsAlerts(
                    LocalDateTime currentTime,
                    int minutesAhead) {

        if (currentTime == null || minutesAhead < 0) {
            return new DoublyLinkedListQueue<>();
        }

        return generateExpiringPointsReport(
                currentTime,
                currentTime.plusMinutes(minutesAhead),
                null,
                1
        );
    }

    // -------------------------------------------------------------------------
    // EXPIRED POINTS PROCESSING
    // -------------------------------------------------------------------------

    /**
     * Processes EARN points whose expiry time has passed.
     *
     * Expired remaining points are removed from the member balance and an
     * EXPIRE transaction is added to the transaction history.
     *
     * @param currentTime time used for the expiry check
     * @return total points expired
     */
    public int processExpiredPoints(
            LocalDateTime currentTime) {

        if (currentTime == null) {
            return 0;
        }

        int totalExpiredPoints = 0;

        ListQueueInterface<LoyaltyTransaction> expiredTransactions =
                loyaltyTransactions.filter(
                        transaction ->
                                transaction.isExpiredAt(
                                        currentTime)
                );

        Iterator<LoyaltyTransaction> expiredIterator =
                expiredTransactions.getIterator();

        while (expiredIterator.hasNext()) {

            LoyaltyTransaction earnTransaction =
                    expiredIterator.next();

            LoyaltyAccount account =
                    findMemberByLoyaltyId(
                            earnTransaction.getLoyaltyId());

            if (account == null) {
                continue;
            }

            int expiredPoints =
                    earnTransaction.expireRemainingPoints();

            int actualDeduction =
                    Math.min(
                            expiredPoints,
                            account.getPointsBalance()
                    );

            if (actualDeduction <= 0) {
                continue;
            }

            account.setPointsBalance(
                    account.getPointsBalance()
                            - actualDeduction
            );

            recalculateTier(account);

            LoyaltyTransaction expiryTransaction =
                    new LoyaltyTransaction(
                            generateTransactionId(),
                            account.getLoyaltyId(),
                            null,
                            TransactionType.EXPIRE,
                            actualDeduction,
                            currentTime.toLocalDate(),
                            null
                    );

            loyaltyTransactions.enqueue(
                    expiryTransaction);

            totalExpiredPoints += actualDeduction;
        }

        return totalExpiredPoints;
    }
}