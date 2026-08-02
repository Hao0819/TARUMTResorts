/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.control;

import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.dao.LoyaltyInitializerData;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.LoyaltyTransaction.TransactionType;
import com.tarumt.resorts.entity.MembershipTier;
import java.time.LocalDate;
import com.tarumt.resorts.entity.Guest;

/**
 * Handles the business logic for the Loyalty and Rewards Service.
 *
 * Functions include account creation, member searching, points updates,
 * tier recalculation, filtering, deletion and report generation.
 *
 * @author YourName
 */
    public class LoyaltyRewardsControl {

    private ListQueueInterface<LoyaltyAccount> loyaltyAccounts;
    private ListQueueInterface<LoyaltyTransaction> loyaltyTransactions;
    private ListQueueInterface<Guest> guests;

    /**
 * Creates an empty Loyalty control.
 * The main application normally uses the constructor
 * that receives the shared collections.
 */
    public LoyaltyRewardsControl() {

    loyaltyAccounts =
            new DoublyLinkedListQueue<>();

    loyaltyTransactions =
            new DoublyLinkedListQueue<>();

    guests =
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
        ListQueueInterface<Guest> guests) {

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
        recalculateAllTiers();
    }

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

    for (int i = 0;
            i < loyaltyAccounts.getNumberOfEntries();
            i++) {

        LoyaltyAccount account =
                loyaltyAccounts.getEntry(i);

        recalculateTier(account);
    }
}
    
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
 * Adds loyalty points from an eligible completed stay.
 *
 * The method checks:
 * 1. Loyalty ID is valid
 * 2. Booking ID is valid
 * 3. Points are positive
 * 4. Member exists
 * 5. Account is active
 * 6. Booking has not already received points
 *
 * A successful earning transaction expires one year
 * after the points are awarded.
 *
 * @param loyaltyId member's loyalty ID
 * @param bookingId completed booking ID
 * @param points points earned from the stay
 * @return true if points were successfully added
 */
    
    public boolean addPointsFromCompletedStay(
        String loyaltyId,
        String bookingId,
        int points) {

            if (loyaltyId == null
            || loyaltyId.trim().isEmpty()) {

            return false;
        }

        if (bookingId == null
            || bookingId.trim().isEmpty()) {

        return false;
        }

        // Prevent zero or negative points
        if (points <= 0) {
        return false;
        }

        LoyaltyAccount account =
            findMemberByLoyaltyId(loyaltyId.trim());

        // Member does not exist
        if (account == null) {
        return false;
        }

        // Inactive members cannot receive points
        if (!account.isActive()) {
            return false;
        }

        // Prevent duplicate completed-stay rewards
        if (hasBookingReceivedPoints(bookingId.trim())) {
            return false;
        }

    /*
    * Use long temporarily to ensure that adding points
    * does not exceed the maximum integer value.
    2
        2*/
    long calculatedBalance =
            (long) account.getPointsBalance() + points;

    if (calculatedBalance > Integer.MAX_VALUE) {
        return false;
    }

    LocalDate transactionDate = LocalDate.now();
    LocalDate expiryDate =
            transactionDate.plusYears(1);

    LoyaltyTransaction earnTransaction =
            new LoyaltyTransaction(
                    generateTransactionId(),
                    account.getLoyaltyId(),
                    bookingId.trim(),
                    TransactionType.EARN,
                    points,
                    transactionDate,
                    expiryDate
            );

    boolean transactionStored =
            loyaltyTransactions.enqueue(earnTransaction);

    if (!transactionStored) {
        return false;
    }

    account.setPointsBalance(
            (int) calculatedBalance);

    recalculateTier(account);

    return true;
    }
    
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

    /*
     * First traversal:
     * Calculate how many valid, non-expired earned points exist.
     */
    int availableEarnedPoints = 0;

    for (int i = 0;
            i < loyaltyTransactions.getNumberOfEntries();
            i++) {

        LoyaltyTransaction transaction =
                loyaltyTransactions.getEntry(i);

        boolean sameMember =
                transaction.getLoyaltyId() != null
                && transaction.getLoyaltyId()
                        .equalsIgnoreCase(
                                account.getLoyaltyId());

        boolean validEarnTransaction =
                transaction.getTransactionType()
                        == TransactionType.EARN
                && transaction.getRemainingPoints() > 0
                && !transaction.isExpiredOn(currentDate);

        if (sameMember && validEarnTransaction) {
            availableEarnedPoints +=
                    transaction.getRemainingPoints();
        }
    }

    // Not enough usable, non-expired earned points
    if (availableEarnedPoints < points) {
        return false;
    }

    /*
     * Second traversal:
     * Deduct the oldest earned points first.
     */
    int remainingToRedeem = points;

    for (int i = 0;
            i < loyaltyTransactions.getNumberOfEntries()
            && remainingToRedeem > 0;
            i++) {

        LoyaltyTransaction transaction =
                loyaltyTransactions.getEntry(i);

        boolean sameMember =
                transaction.getLoyaltyId() != null
                && transaction.getLoyaltyId()
                        .equalsIgnoreCase(
                                account.getLoyaltyId());

        boolean validEarnTransaction =
                transaction.getTransactionType()
                        == TransactionType.EARN
                && transaction.getRemainingPoints() > 0
                && !transaction.isExpiredOn(currentDate);

        if (sameMember && validEarnTransaction) {

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
    
    /**
 * Updates a loyalty member's points balance directly.
 *
 * This method may be used by an administrator to correct
 * an incorrect points balance.
 *
 * @param loyaltyId loyalty account ID
 * @param newPointsBalance new points balance
 * @return true if the balance was successfully updated
 */
    public boolean updatePointsBalance(
        String loyaltyId,
        int newPointsBalance) {

    if (loyaltyId == null
            || loyaltyId.trim().isEmpty()) {

        return false;
    }

    // A points balance cannot be negative
    if (newPointsBalance < 0) {
        return false;
    }

    LoyaltyAccount account =
            findMemberByLoyaltyId(
                    loyaltyId.trim());

    if (account == null) {
        return false;
    }

    account.setPointsBalance(newPointsBalance);

    // Update the tier based on the new balance
    recalculateTier(account);

    return true;
    }

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

    for (int i = 0;
            i < filteredAccounts.getNumberOfEntries();
            i++) {

        LoyaltyAccount account =
                filteredAccounts.getEntry(i);

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
 * Deletes a loyalty account from the shared custom ADT.
 *
 * The method uses dequeue and enqueue to rebuild the same
 * collection without the selected account. Past transaction
 * records are retained for audit purposes.
 *
 * @param loyaltyId loyalty account ID to delete
 * @return true if the account was deleted
 */
    public boolean deleteLoyaltyMember(String loyaltyId) {

    if (loyaltyId == null
            || loyaltyId.trim().isEmpty()) {

        return false;
    }

    LoyaltyAccount accountToDelete =
            findMemberByLoyaltyId(loyaltyId.trim());

    if (accountToDelete == null) {
        return false;
    }

    int originalSize =
            loyaltyAccounts.getNumberOfEntries();

    boolean deleted = false;

    /*
     * Process every original entry once.
     * Matching account is not inserted again.
     */
    for (int i = 0; i < originalSize; i++) {

        LoyaltyAccount currentAccount =
                loyaltyAccounts.dequeue();

        if (!deleted
                && currentAccount.getLoyaltyId()
                        .equalsIgnoreCase(
                                loyaltyId.trim())) {

            deleted = true;

        } else {
            loyaltyAccounts.enqueue(currentAccount);
        }
    }

    return deleted;
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

    for (int i = 0;
            i < filteredTransactions.getNumberOfEntries();
            i++) {

        LoyaltyTransaction transaction =
                filteredTransactions.getEntry(i);

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
     * Store the original size because new EXPIRE transactions
     * will be inserted during processing.
     */
    int originalTransactionCount =
            loyaltyTransactions.getNumberOfEntries();

    for (int i = 0;
            i < originalTransactionCount;
            i++) {

        LoyaltyTransaction earnTransaction =
                loyaltyTransactions.getEntry(i);

        if (!earnTransaction.isExpiredOn(currentDate)) {
            continue;
        }

        LoyaltyAccount account =
                findMemberByLoyaltyId(
                        earnTransaction.getLoyaltyId());

        if (account == null) {
            continue;
        }

        int expiredPoints =
                earnTransaction.expireRemainingPoints();

        /*
         * Prevent the account balance from becoming negative
         * if an administrator previously corrected the balance.
         */
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
                expiryTransaction
        );

        totalExpiredPoints += actualDeduction;
    }

    return totalExpiredPoints;
} 
    
}
