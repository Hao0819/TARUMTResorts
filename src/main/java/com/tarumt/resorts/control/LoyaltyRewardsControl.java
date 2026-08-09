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
import com.tarumt.resorts.entity.LoyaltyTransaction.TransactionType;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.MembershipTier;
import java.time.LocalDate;
import java.util.Iterator;
import com.tarumt.resorts.entity.RedemptionRequest;

/**
 * Handles the business logic for the Loyalty and Rewards Service.
 *
 * Functions include account creation, member searching,
 * points earning and redemption, tier recalculation,
 * account activation/deactivation, filtering and report generation.
 */
public class LoyaltyRewardsControl {

    private ListQueueInterface<LoyaltyAccount> loyaltyAccounts;
    private ListQueueInterface<LoyaltyTransaction> loyaltyTransactions;
    private ListQueueInterface<Guest> guests;
    private ListQueueInterface<Booking> bookings;
    private ListQueueInterface<RedemptionRequest> redemptionRequests;
    
    private int nextRedemptionRequestNumber = 1;
    
    /**
    * Generates the next redemption request ID.
    *
    * @return request ID such as R001, R002, R003
    */
    private String generateRedemptionRequestId() {

        String requestId =
                String.format(
                        "R%03d",
                        nextRedemptionRequestNumber);

        nextRedemptionRequestNumber++;

        return requestId;
    }
    
    /**
    * Submits a new redemption request to the FIFO queue.
    *
    * @param loyaltyId loyalty account ID
    * @param points number of points requested
    * @return created request, or null if invalid
    */
    public RedemptionRequest submitRedemptionRequest(
            String loyaltyId,
            int points) {

        if (loyaltyId == null
                || loyaltyId.trim().isEmpty()) {

            return null;
        }

        if (points <= 0) {
            return null;
        }

        LoyaltyAccount account =
                findMemberByLoyaltyId(
                        loyaltyId.trim());

        if (account == null) {
            return null;
        }

        // Inactive accounts cannot submit redemption requests.
        if (!account.isActive()) {
            return null;
        }

        // Requested points cannot exceed the current balance.
        if (points > account.getPointsBalance()) {
            return null;
        }

        RedemptionRequest request =
                new RedemptionRequest(
                        generateRedemptionRequestId(),
                        account.getLoyaltyId(),
                        points,
                        LocalDate.now());

        boolean submitted =
                redemptionRequests.enqueue(request);

        if (!submitted) {
            return null;
        }

        return request;
    }
    
    /**
    * Returns the pending redemption requests in FIFO order.
    *
    * @return custom ADT containing pending redemption requests
    */
    public ListQueueInterface<RedemptionRequest>
            getPendingRedemptionRequests() {

        return redemptionRequests;
    }
            
    /**
    * Processes the next redemption request in FIFO order.
    *
    * The request at the front of the queue is checked using peek().
    * It is removed using dequeue() only after redemption succeeds.
    *
    * @return true if the next request was processed successfully
    */
    public boolean processNextRedemptionRequest() {

    if (redemptionRequests == null
            || redemptionRequests.isEmpty()) {

        return false;
    }

    // Look at the first request without removing it.
    RedemptionRequest nextRequest =
            redemptionRequests.peek();

    if (nextRequest == null) {
        return false;
    }

    // Perform the actual redemption.
    boolean redeemed =
            redeemPoints(
                    nextRequest.getLoyaltyId(),
                    nextRequest.getPoints());

    if (!redeemed) {
        /*
         * Do not dequeue when redemption fails.
         * The request remains pending until it is
         * successfully processed or cancelled.
         */
        return false;
    }

    // FIFO: remove the front request only after success.
    redemptionRequests.dequeue();

    return true;
}
    
    /**
    * Cancels a pending redemption request.
    *
    * The queue is rebuilt using dequeue and enqueue so that
    * the remaining requests keep their original FIFO order.
    *
    * @param requestId redemption request ID to cancel
    * @return true if the request was found and cancelled
    */
    public boolean cancelRedemptionRequest(
        String requestId) {

    if (requestId == null
            || requestId.trim().isEmpty()) {

        return false;
    }

    if (redemptionRequests == null
            || redemptionRequests.isEmpty()) {

        return false;
    }

    int originalSize =
            redemptionRequests.getNumberOfEntries();

    boolean cancelled = false;

    for (int i = 0; i < originalSize; i++) {

        RedemptionRequest currentRequest =
                redemptionRequests.dequeue();

        if (!cancelled
                && currentRequest.getRequestId()
                        .equalsIgnoreCase(
                                requestId.trim())) {

            // Do not enqueue this request again.
            cancelled = true;

        } else {

            // Put all other requests back into the queue.
            redemptionRequests.enqueue(
                    currentRequest);
        }
    }

    return cancelled;
}
    

    /**
     * Loyalty conversion rule.
     * Currently 1 RM spent = 1 loyalty point.
     *
     * Change this constant if the team later agrees on
     * a different conversion rate.
     */
    private static final double POINTS_PER_RM = 1.0;

    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------

    public LoyaltyRewardsControl() {

        loyaltyAccounts =
        new DoublyLinkedListQueue<>();

        loyaltyTransactions =
        new DoublyLinkedListQueue<>();

        guests =
        new DoublyLinkedListQueue<>();

        bookings =
        new DoublyLinkedListQueue<>();
        
        redemptionRequests =
        new DoublyLinkedListQueue<>();
    }

    /**
     * Allows existing loyalty-account and transaction collections
     * to be supplied to the control class.
     *
     * @param loyaltyAccounts custom ADT containing loyalty accounts
     * @param loyaltyTransactions custom ADT containing transactions
     */
    public LoyaltyRewardsControl(
        ListQueueInterface<LoyaltyAccount> loyaltyAccounts,
        ListQueueInterface<LoyaltyTransaction> loyaltyTransactions,
        ListQueueInterface<Guest> guests,
        ListQueueInterface<Booking> bookings) {

        if (loyaltyAccounts == null) {
            this.loyaltyAccounts =
            new DoublyLinkedListQueue<>();
        } else {
            this.loyaltyAccounts = loyaltyAccounts;
        }

        if (loyaltyTransactions == null) {
            this.loyaltyTransactions =
            new DoublyLinkedListQueue<>();
        } else {
            this.loyaltyTransactions = loyaltyTransactions;
        }

        if (guests == null) {
            this.guests =
            new DoublyLinkedListQueue<>();
        } else {
            this.guests = guests;
        }

        if (bookings == null) {
            this.bookings =
            new DoublyLinkedListQueue<>();
        } else {
            this.bookings = bookings;
        }
        
        redemptionRequests =
        new DoublyLinkedListQueue<>();

        recalculateAllTiers();
    }

    // -------------------------------------------------------------------------
    // COLLECTION ACCESS
    // -------------------------------------------------------------------------

    /**
     * Returns all loyalty accounts.
     *
     * @return the custom loyalty-account collection
     */
    public ListQueueInterface<LoyaltyAccount> getLoyaltyAccounts() {
        return loyaltyAccounts;
    }
    public ListQueueInterface<LoyaltyTransaction>
    getLoyaltyTransactions() {

        return loyaltyTransactions;
    }

    // -------------------------------------------------------------------------
    // MEMBER AND BOOKING SEARCH
    // -------------------------------------------------------------------------

    /**
     * Finds a loyalty member using the loyalty ID.
     *
     * Uses the shared ADT's key-based linear-search method.
     *
     * @param loyaltyId loyalty ID to search for
     * @return matching loyalty account, or null if not found
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
     * Finds a loyalty member using the guest ID.
     *
     * @param guestId guest ID to search for
     * @return matching loyalty account, or null if not found
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
     * Checks whether a booking has already received loyalty points.
     *
     * Only EARN transactions are checked because REDEEM and EXPIRE
     * transactions do not represent completed-stay rewards.
     *
     * @param bookingId booking ID to check
     * @return true if the booking has already received points
     */
    public boolean hasBookingReceivedPoints(String bookingId) {

        if (bookingId == null
            || bookingId.trim().isEmpty()) {

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

    // -------------------------------------------------------------------------
    // TIER MANAGEMENT
    // -------------------------------------------------------------------------

    /**
     * Recalculates and updates a member's tier based on
     * the current points balance.
     *
     * Tier thresholds:
     * NONE       : 0 - 1,999
     * SILVER     : 2,000 - 4,999
     * GOLD       : 5,000 - 9,999
     * PLATINUM   : 10,000 - 14,999
     * DIAMOND    : 15,000 - 19,999
     * ELITE      : 20,000 and above
     *
     * @param account loyalty account to update
     */
    public void recalculateTier(LoyaltyAccount account) {

        if (account == null) {
            return;
        }

        /*
        * Inactive loyalty accounts must not receive
        * membership/VIP priority.
        */
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
     * Recalculates the tier of every loyalty account when
     * the Loyalty module is initialized.
     */
    private void recalculateAllTiers() {

        Iterator<LoyaltyAccount> iterator =
        loyaltyAccounts.getIterator();

        while (iterator.hasNext()) {

            LoyaltyAccount account =
            iterator.next();

            recalculateTier(account);
        }
    }

    // -------------------------------------------------------------------------
    // TRANSACTION ID AND POINT EARNING
    // -------------------------------------------------------------------------

    /**
     * Generates the next loyalty transaction ID.
     *
     * Example:
     * T001, T002, T003
     *
     * @return newly generated transaction ID
     */
    private String generateTransactionId() {

        int nextNumber =
        loyaltyTransactions.getNumberOfEntries() + 1;

        return String.format("T%03d", nextNumber);
    }

    /**
     * Adds automatically calculated loyalty points from
     * an eligible completed booking.
     *
     * Validation:
     * - Loyalty account exists
     * - Loyalty account is active
     * - Booking exists
     * - Booking belongs to the same Guest
     * - Booking status is CHECKED_OUT
     * - Booking has not already received points
     * - Booking amount produces valid points
     *
     * @param loyaltyId loyalty account ID
     * @param bookingId booking confirmation number
     * @return true when points are successfully awarded
     */
    public boolean addPointsFromCompletedStay(
        String loyaltyId,
        String bookingId) {

        if (loyaltyId == null
            || loyaltyId.trim().isEmpty()) {
            return false;
        }

        if (bookingId == null
            || bookingId.trim().isEmpty()) {
            return false;
        }

        LoyaltyAccount account =
        findMemberByLoyaltyId(loyaltyId.trim());

        if (account == null) {
            return false;
        }

        // Inactive accounts cannot earn points
        if (!account.isActive()) {
            return false;
        }

        Booking booking =
    // -------------------------------------------------------------------------
    // BOOKING AND REWARD HELPERS
    // -------------------------------------------------------------------------

        findBookingByConfirmationNumber(
            bookingId.trim());

        // Booking must exist
        if (booking == null) {
            return false;
        }

        // Booking must contain a Guest
        if (booking.getGuest() == null
            || booking.getGuest().getGuestId() == null) {
            return false;
        }

        // Booking must belong to the same Guest
        if (account.getGuestId() == null
            || !account.getGuestId()
            .equalsIgnoreCase(
            booking.getGuest().getGuestId())) {
            return false;
        }

        // Only completed stays can receive loyalty points
        if (booking.getStatus() == null
            || !booking.getStatus()
            .equalsIgnoreCase("CHECKED_OUT")) {
            return false;
        }
        
        // Booking must be fully paid before points can be awarded
        if (booking.getPaymentStatus() == null
            || !booking.getPaymentStatus()
            .equalsIgnoreCase("PAID")) {

        return false;
    }

        // Prevent duplicate booking rewards
        if (hasBookingReceivedPoints(
            booking.getConfirmationNumber())) {
            return false;
        }

        // Calculate points automatically from booking amount
        int points =
        calculateRewardPoints(booking);

        if (points <= 0) {
            return false;
        }

        long newBalance =
        (long) account.getPointsBalance()
        + points;

        if (newBalance > Integer.MAX_VALUE) {
            return false;
        }

        LocalDate transactionDate =
        LocalDate.now();

        LocalDate expiryDate =
        transactionDate.plusYears(1);

        LoyaltyTransaction earnTransaction =
        new LoyaltyTransaction(
            generateTransactionId(),
            account.getLoyaltyId(),
            booking.getConfirmationNumber(),
            LoyaltyTransaction.TransactionType.EARN,
            points,
            transactionDate,
            expiryDate
        );

        boolean stored =
        loyaltyTransactions.enqueue(
            earnTransaction);

        if (!stored) {
            return false;
        }

        account.setPointsBalance(
            (int) newBalance);

        recalculateTier(account);

        return true;
    }

    // -------------------------------------------------------------------------
    // POINT REDEMPTION
    // -------------------------------------------------------------------------

    public boolean redeemPoints(
        String loyaltyId,
        int points) {

        if (loyaltyId == null
            || loyaltyId.trim().isEmpty()) {

            return false;
        }

        // Reject zero or negative points
        if (points <= 0) {
            return false;
        }

        LoyaltyAccount account =
        findMemberByLoyaltyId(loyaltyId.trim());

        // Member does not exist
        if (account == null) {
            return false;
        }

        // Inactive accounts cannot redeem points
        if (!account.isActive()) {
            return false;
        }

        // Check total account balance
        if (account.getPointsBalance() < points) {
            return false;
        }

        LocalDate currentDate = LocalDate.now();

        int availableEarnedPoints = 0;

        Iterator<LoyaltyTransaction> transactionIterator =
        loyaltyTransactions.getIterator();

        while (transactionIterator.hasNext()) {

            LoyaltyTransaction transaction =
            transactionIterator.next();

            boolean sameMember =
            transaction.getLoyaltyId() != null
            && transaction.getLoyaltyId()
            .equalsIgnoreCase(
                account.getLoyaltyId());

            boolean usablePointsTransaction =
            (transaction.getTransactionType()
                == TransactionType.EARN
                || transaction.getTransactionType()
                == TransactionType.ADJUST)
            && transaction.getRemainingPoints() > 0
            && (transaction.getTransactionType()
                == TransactionType.ADJUST
                || !transaction.isExpiredOn(currentDate));

            if (sameMember && usablePointsTransaction) {

                availableEarnedPoints +=
                transaction.getRemainingPoints();
            }
        }

        // Not enough usable, non-expired earned points
        if (availableEarnedPoints < points) {
            return false;
        }

        int remainingToRedeem = points;

        Iterator<LoyaltyTransaction> redeemIterator =
        loyaltyTransactions.getIterator();

        while (redeemIterator.hasNext()
            && remainingToRedeem > 0) {

            LoyaltyTransaction transaction =
            redeemIterator.next();

            boolean sameMember =
            transaction.getLoyaltyId() != null
            && transaction.getLoyaltyId()
            .equalsIgnoreCase(
                account.getLoyaltyId());

            boolean usablePointsTransaction =
            (transaction.getTransactionType()
                == TransactionType.EARN
                || transaction.getTransactionType()
                == TransactionType.ADJUST)
            && transaction.getRemainingPoints() > 0
            && (transaction.getTransactionType()
                == TransactionType.ADJUST
                || !transaction.isExpiredOn(currentDate));

            if (sameMember && usablePointsTransaction) {

                int deducted =
                transaction.deductRemainingPoints(
                    remainingToRedeem);

                remainingToRedeem -= deducted;
            }
        }

        // Deduct from the account's total balance
        account.setPointsBalance(
            account.getPointsBalance() - points);

        // Update tier after the balance decreases
        recalculateTier(account);

        // Record the redemption
        LoyaltyTransaction redeemTransaction =
        new LoyaltyTransaction(
            generateTransactionId(),
            account.getLoyaltyId(),
            null,
            TransactionType.REDEEM,
            points,
            currentDate,
            null
        );

        loyaltyTransactions.enqueue(redeemTransaction);

        return true;
    }

    // -------------------------------------------------------------------------
    // ACCOUNT CREATION
    // -------------------------------------------------------------------------

    /**
     * Creates and stores a new loyalty account.
     *
     * The method prevents duplicate loyalty IDs and prevents one guest
     * from having more than one loyalty account.
     *
     * @param newAccount loyalty account to be created
     * @return true if successfully created, false if the account is invalid
     *         or already exists
     */
    public boolean createLoyaltyAccount(LoyaltyAccount newAccount) {

        if (newAccount == null) {
            return false;
        }

        if (newAccount.getLoyaltyId() == null
            || newAccount.getLoyaltyId().trim().isEmpty()) {

            return false;
        }

        if (newAccount.getGuest() == null
            || newAccount.getGuestId() == null
            || newAccount.getGuestId().trim().isEmpty()) {

            return false;
        }

        // Prevent duplicate loyalty ID
        LoyaltyAccount existingLoyaltyAccount =
        findMemberByLoyaltyId(
            newAccount.getLoyaltyId());

        if (existingLoyaltyAccount != null) {
            return false;
        }

        // Prevent the same guest from creating another loyalty account
        LoyaltyAccount existingGuestAccount =
        findMemberByGuestId(
            newAccount.getGuestId());

        if (existingGuestAccount != null) {
            return false;
        }

        return loyaltyAccounts.enqueue(newAccount);
    }

    /**
     * Finds a guest from the shared Guest collection.
     *
     * @param guestId guest ID to search for
     * @return matching Guest, or null if not found
     */
    public Guest findGuestById(String guestId) {

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
     * Generates the next available loyalty ID.
     *
     * Example: L001, L002, L003
     *
     * @return next available loyalty ID
     */
    private String generateLoyaltyId() {

        int number = 1;
        String loyaltyId;

        do {
            loyaltyId = String.format(
                "L%03d",
                number
            );

            number++;

        } while (findMemberByLoyaltyId(loyaltyId) != null);

        return loyaltyId;
    }

    /**
     * Creates a loyalty account for an existing guest.
     *
     * The guest must exist in the shared Guest collection and
     * must not already have a loyalty account.
     *
     * @param guestId existing guest ID
     * @return created loyalty account, or null if unsuccessful
     */
    public LoyaltyAccount createAccountForGuest(
        String guestId) {

        if (guestId == null
            || guestId.trim().isEmpty()) {

            return null;
        }

        Guest guest =
        findGuestById(guestId.trim());

        // Guest must exist
        if (guest == null) {
            return null;
        }

        // One guest can only have one loyalty account
        if (findMemberByGuestId(guestId.trim()) != null) {
            return null;
        }

        /*
        * Prevent silently overwriting an existing tier.
        * A Guest without a LoyaltyAccount should normally
        * have MembershipTier.NONE.
        */
        if (guest.getMembershipTier()
            != MembershipTier.NONE) {

            return null;
        }

        LoyaltyAccount newAccount =
        new LoyaltyAccount(
            generateLoyaltyId(),
            guest,
            0,
            true
        );

        // A new account with zero points starts at NONE
        recalculateTier(newAccount);

        boolean created =
        createLoyaltyAccount(newAccount);

        if (!created) {
            return null;
        }

        return newAccount;
    }

    // -------------------------------------------------------------------------
    // ACCOUNT STATUS MANAGEMENT
    // -------------------------------------------------------------------------

    /**
     * Updates the active status of a loyalty account.
     *
     * Inactive accounts cannot earn or redeem points and
     * should not receive VIP priority.
     *
     * @param loyaltyId loyalty account ID
     * @param active true to activate, false to deactivate
     * @return true if the account status was updated
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

        /*
        * An inactive member should not be treated
        * as a priority/VIP member.
        */
        if (!active) {
            account.setMembershipTier(
                MembershipTier.NONE);
        } else {
            /*
            * When reactivated, restore the correct tier
            * based on the current points balance.
            */
            recalculateTier(account);
        }

        return true;
    }

    // -------------------------------------------------------------------------
    // TIER AND POINTS REPORT
    // -------------------------------------------------------------------------

    /**
     * Filters loyalty members according to tier, account status,
     * and minimum points.
     *
     * The filtered result is ordered by points from highest to lowest.
     *
     * @param tier selected tier, or null to include every tier
     * @param statusFilter 0 for all, 1 for active, 2 for inactive
     * @param minimumPoints minimum points required
     * @return custom ADT containing matching loyalty accounts
     */
    public ListQueueInterface<LoyaltyAccount> filterMembers(
        MembershipTier tier,
        int statusFilter,
        int minimumPoints) {

        ListQueueInterface<LoyaltyAccount> emptyResult =
        new DoublyLinkedListQueue<>();

        if (minimumPoints < 0) {
            return emptyResult;
        }

        if (statusFilter < 0 || statusFilter > 2) {
            return emptyResult;
        }

        // Use the shared ADT filtering behaviour.
        ListQueueInterface<LoyaltyAccount> filteredAccounts =
        loyaltyAccounts.filter(account -> {

                boolean matchesTier =
                tier == null
                || account.getMembershipTier() == tier;

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

        /*
        * Insert matching accounts into another custom ADT
        * according to points balance, highest to lowest.
        */
        ListQueueInterface<LoyaltyAccount> orderedAccounts =
        new DoublyLinkedListQueue<>();

        Iterator<LoyaltyAccount> filteredIterator =
        filteredAccounts.getIterator();

        while (filteredIterator.hasNext()) {

            LoyaltyAccount account =
            filteredIterator.next();

            orderedAccounts.priorityEnqueue(
                account,
                (firstAccount, secondAccount) ->
                Integer.compare(
                secondAccount.getPointsBalance(),
                firstAccount.getPointsBalance()
            )
            );
        }

        return orderedAccounts;
    }

    /**
     * Generates the Expiring Points Report.
     *
     * The report filters earned points by expiry-date window,
     * membership tier and minimum remaining points.
     * Results are ordered by expiry date ascending.
     *
     * @param startDate beginning of the expiry window
     * @param endDate end of the expiry window
     * @param tier selected tier, or null for all tiers
     * @param minimumExpiringPoints minimum remaining points
     * @return ordered custom ADT containing expiring transactions
     */
    public ListQueueInterface<LoyaltyTransaction>
    // -------------------------------------------------------------------------
    // EXPIRING POINTS REPORTS
    // -------------------------------------------------------------------------

    generateExpiringPointsReport(
        LocalDate startDate,
        LocalDate endDate,
        MembershipTier tier,
        int minimumExpiringPoints) {

        ListQueueInterface<LoyaltyTransaction> emptyResult =
        new DoublyLinkedListQueue<>();

        if (startDate == null
            || endDate == null
            || endDate.isBefore(startDate)
            || minimumExpiringPoints < 0) {

            return emptyResult;
        }

        /*
        * Filter using the shared custom ADT.
        */
        ListQueueInterface<LoyaltyTransaction> filteredTransactions =
        loyaltyTransactions.filter(transaction -> {

                if (transaction.getTransactionType()
                != TransactionType.EARN) {

                    return false;
            }

                if (!transaction.isExpiringBetween(
                startDate,
                endDate)) {

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
                || account.getMembershipTier() == tier;
        });

        /*
        * Order by expiry date ascending using priorityEnqueue().
        */
        ListQueueInterface<LoyaltyTransaction> orderedTransactions =
        new DoublyLinkedListQueue<>();

        Iterator<LoyaltyTransaction> expiringIterator =
        filteredTransactions.getIterator();

        while (expiringIterator.hasNext()) {

            LoyaltyTransaction transaction =
            expiringIterator.next();

            orderedTransactions.priorityEnqueue(
                transaction,
                (firstTransaction, secondTransaction) ->
                firstTransaction.getExpiryDate()
                .compareTo(
                secondTransaction
                .getExpiryDate())
            );
        }

        return orderedTransactions;
    }

    /**
     * Identifies earned points that will expire within a specified
     * number of days.
     *
     * @param currentDate date used to begin the checking period
     * @param daysAhead number of days to check ahead
     * @return custom ADT containing expiring points records
     */
    public ListQueueInterface<LoyaltyTransaction>
    generateExpiringPointsAlerts(
        LocalDate currentDate,
        int daysAhead) {

        if (currentDate == null || daysAhead < 0) {
            return new DoublyLinkedListQueue<>();
        }

        LocalDate endDate =
        currentDate.plusDays(daysAhead);

        return generateExpiringPointsReport(
            currentDate,
            endDate,
            null,
            1
        );
    }

    // -------------------------------------------------------------------------
    // EXPIRED POINTS PROCESSING
    // -------------------------------------------------------------------------

    /**
     * Processes all earned points that have reached their expiry date.
     *
     * @param currentDate date used to check point expiry
     * @return total number of points deducted from loyalty accounts
     */
    public int processExpiredPoints(LocalDate currentDate) {

        if (currentDate == null) {
            return 0;
        }

        int totalExpiredPoints = 0;

        /*
        * Create a separate filtered collection first.
        * This avoids modifying loyaltyTransactions while
        * traversing the same collection.
        */
        ListQueueInterface<LoyaltyTransaction> expiredTransactions =
        loyaltyTransactions.filter(
            transaction ->
            transaction.isExpiredOn(
            currentDate)
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
                currentDate,
                null
            );

            loyaltyTransactions.enqueue(
                expiryTransaction);

            totalExpiredPoints +=
            actualDeduction;
        }

        return totalExpiredPoints;
    }

    /**
     * Finds a booking using its confirmation number.
     *
     * @param confirmationNumber booking confirmation number
     * @return matching Booking, or null if not found
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

    /**
     * Calculates loyalty points from the booking amount.
     *
     * Current rule:
     * RM1 spent = 1 loyalty point.
     *
     * @param booking completed booking
     * @return calculated loyalty points
     */
    public int calculateRewardPoints(Booking booking) {

        if (booking == null
            || booking.getAmount() <= 0) {

            return 0;
        }

        return (int) Math.floor(
            booking.getAmount() * POINTS_PER_RM
        );
    }
}
