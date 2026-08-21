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
import com.tarumt.resorts.entity.RedemptionRequest;
import com.tarumt.resorts.util.LoyaltyClock;

/**
 * Handles the business logic for the Loyalty and Rewards Service.
 *
 * Functions include member searching, account creation, points earning,
 * direct reward redemption, tier progression, account status management,
 * promotions, reporting, and point expiry.
 * @author Gary Khor Wei Qi 
 */
public class LoyaltyRewardsControl {

    private static final double POINTS_PER_RM = 1.0;
    private static final int POINTS_EXPIRY_MONTHS = 3;
    private static final int EXPIRY_NOTIFICATION_DAYS = 7;
    private static final int ACCOUNT_INACTIVITY_YEARS = 1;

    private ListQueueInterface<LoyaltyAccount> loyaltyAccounts; // ADT collection declaration
    private ListQueueInterface<LoyaltyTransaction> loyaltyTransactions; // ADT collection declaration
    private ListQueueInterface<Guest> guests; // ADT collection declaration
    private ListQueueInterface<Booking> bookings; // ADT collection declaration
    private ListQueueInterface<RedemptionRequest> redemptionRequests; // ADT collection declaration
    private int nextRedemptionRequestNumber = 1;

    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------

    /** Creates an empty Loyalty service. */
    public LoyaltyRewardsControl() {
        loyaltyAccounts = new DoublyLinkedListQueue<>(); // ADT implementation creation
        loyaltyTransactions = new DoublyLinkedListQueue<>(); // ADT implementation creation
        guests = new DoublyLinkedListQueue<>(); // ADT implementation creation
        bookings = new DoublyLinkedListQueue<>(); // ADT implementation creation
        redemptionRequests = new DoublyLinkedListQueue<>(); // ADT implementation creation
    }

    /** Uses the shared data collections provided by Main. */
    public LoyaltyRewardsControl(
            ListQueueInterface<LoyaltyAccount> loyaltyAccounts, // ADT collection declaration
            ListQueueInterface<LoyaltyTransaction> loyaltyTransactions, // ADT collection declaration
            ListQueueInterface<Guest> guests, // ADT collection declaration
            ListQueueInterface<Booking> bookings) { // ADT collection declaration

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

        
        redemptionRequests = new DoublyLinkedListQueue<>(); // ADT implementation creation
        recalculateAllTiers();
    }

    // -------------------------------------------------------------------------
    // COLLECTION ACCESS
    // -------------------------------------------------------------------------

    /** Returns the shared member collection. */
    public ListQueueInterface<LoyaltyAccount> getLoyaltyAccounts() {
        return loyaltyAccounts;
    }

    /** Returns the shared point ledger. */
    public ListQueueInterface<LoyaltyTransaction> getLoyaltyTransactions() {
        return loyaltyTransactions;
    }

    /** Returns how many days before expiry to show a warning. */
    public int getExpiryNotificationDays() {
        return EXPIRY_NOTIFICATION_DAYS;
    }

    // Creates the next reward request ID.
    private String generateRedemptionRequestId() {

    String requestId = String.format(
            "R%03d",
            nextRedemptionRequestNumber);

    nextRedemptionRequestNumber++;

    return requestId;
}
    
    // Adds one valid reward request to the pending queue.
    public RedemptionRequest submitRedemptionRequest(
        String loyaltyId,
        RewardPackage rewardPackage) {

    if (loyaltyId == null
            || loyaltyId.trim().isEmpty()
            || rewardPackage == null) {

        return null;
    }

    LoyaltyAccount account =
            findMemberByLoyaltyId(loyaltyId.trim());

    if (account == null || !account.isActive()) {
        return null;
    }

    // Do not reserve rewards against a balance that has already expired.
    processExpiredPoints(LoyaltyClock.now());

    int points =
            rewardPackage.getPointsRequired();

    long pendingPoints = 0;

    Iterator<RedemptionRequest> requestIterator =
            redemptionRequests.getIterator(); // ADT method call: getIterator()

    while (requestIterator.hasNext()) {

        RedemptionRequest pendingRequest =
                requestIterator.next();

        if (pendingRequest.getLoyaltyId()
                .equalsIgnoreCase(
                        account.getLoyaltyId())) {

            pendingPoints +=
                    pendingRequest.getPoints();
        }
    }

    long availablePoints =
            (long) account.getPointsBalance()
            - pendingPoints;

    if (points > availablePoints) {
        return null;
    }

    RedemptionRequest request =
            new RedemptionRequest(
                    generateRedemptionRequestId(),
                    account.getLoyaltyId(),
                    rewardPackage,
                    points,
                    LoyaltyClock.today());

    if (!redemptionRequests.enqueue(request)) { // ADT method call: enqueue()
        return null;
    }

    return request;
}
    
    // -------------------------------------------------------------------------
    // MEMBER AND GUEST SEARCH
    // -------------------------------------------------------------------------

    /** Finds a member by Loyalty ID. */
    public LoyaltyAccount findMemberByLoyaltyId(String loyaltyId) {

        if (loyaltyId == null || loyaltyId.trim().isEmpty()) {
            return null;
        }

        return loyaltyAccounts.searchByKey( // ADT method call: searchByKey()
                loyaltyId.trim(),
                account -> account.getLoyaltyId()
        );
    }

    /** Finds a member by Guest ID. */
    public LoyaltyAccount findMemberByGuestId(String guestId) {

        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }

        return loyaltyAccounts.searchByKey( // ADT method call: searchByKey()
                guestId.trim(),
                account -> account.getGuestId()
        );
    }

    /** Finds a guest in the shared collection. */
    public Guest findGuestById(String guestId) {

        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }

        return guests.searchByKey( // ADT method call: searchByKey()
                guestId.trim(),
                guest -> guest.getGuestId()
        );
    }

    /** Finds a booking by confirmation number. */
    public Booking findBookingByConfirmationNumber(
            String confirmationNumber) {

        if (confirmationNumber == null
                || confirmationNumber.trim().isEmpty()) {

            return null;
        }

        return bookings.searchByKey( // ADT method call: searchByKey()
                confirmationNumber.trim(),
                booking -> booking.getConfirmationNumber()
        );
    }

    /** Returns completed stays that staff can check. */
    public ListQueueInterface<Booking> getCompletedStayBookingsForDisplay() {

        return bookings.filter(booking -> // ADT method call: filter()
                booking != null
                && booking.getStatus() != null
                && booking.getStatus().equalsIgnoreCase("CHECKED_OUT")
                && booking.getConfirmationNumber() != null
                && !booking.getConfirmationNumber()
                        .matches("^20261\\d{3}$"));
    }

    // -------------------------------------------------------------------------
    // ACCOUNT CREATION
    // -------------------------------------------------------------------------

    /** Adds a valid new account without creating duplicates. */
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

        if (!newAccount.isActive()
                && newAccount.getDeactivatedAt() == null) {

            newAccount.setDeactivatedAt(
                    LoyaltyClock.now());
        }

        return loyaltyAccounts.enqueue(newAccount); // ADT method call: enqueue()
    }

    /** Creates a Loyalty account for an existing guest. */
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

    /** Creates the next available Loyalty ID. */
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

    /** Finds new completed stays and awards their Loyalty points. */
    public AutomaticProcessingResult processCompletedBookingsForLoyalty() {

        int accountsCreated = 0;
        int bookingsProcessed = 0;
        int pointsAwarded = 0;
        ListQueueInterface<ProcessedBookingResult> processedItems = // ADT collection declaration
                new DoublyLinkedListQueue<>(); // ADT implementation creation

        Iterator<Booking> bookingIterator =
                bookings.getIterator(); // ADT method call: getIterator()

        while (bookingIterator.hasNext()) {

            Booking booking = bookingIterator.next();

            if (booking == null
                    || booking.getGuest() == null
                    || booking.getGuest().getGuestId() == null
                    || booking.getGuest().getGuestId().trim().isEmpty()
                    || booking.getConfirmationNumber() == null
                    || booking.getConfirmationNumber().trim().isEmpty()) {

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

                if (account != null) {
                    accountsCreated++;
                }
            }

            if (account == null || !account.isActive()) {
                continue;
            }

            int bookingPoints = calculateRewardPoints(booking);
            MembershipTier previousTier = account.getMembershipTier();

            if (addPointsFromCompletedStay(
                    account.getLoyaltyId(),
                    booking.getConfirmationNumber())) {

                bookingsProcessed++;
                pointsAwarded = Math.addExact(
                        pointsAwarded, bookingPoints);

                LoyaltyTransaction earnBatch =
                        findEarnTransactionByBookingId(
                                booking.getConfirmationNumber());

                if (earnBatch != null) {
                    int newPoints = account.getPointsBalance();

                    processedItems.enqueue(new ProcessedBookingResult( // ADT method call: enqueue()
                            account.getLoyaltyId(),
                            booking.getConfirmationNumber(),
                            newPoints - bookingPoints,
                            bookingPoints,
                            newPoints,
                            previousTier,
                            account.getMembershipTier(),
                            earnBatch.getTransactionId(),
                            earnBatch.getTransactionTime(),
                            earnBatch.getExpiryTime()));
                }
            }
        }

        return new AutomaticProcessingResult(
                accountsCreated,
                bookingsProcessed,
                pointsAwarded,
                processedItems);
    }

    /** Immutable counters for one scan of the shared Booking collection. */
    public static final class AutomaticProcessingResult {

        private final int accountsCreated;
        private final int bookingsProcessed;
        private final int pointsAwarded;
        private final ListQueueInterface<ProcessedBookingResult> // ADT collection declaration
                processedItems;

        private AutomaticProcessingResult(
                int accountsCreated,
                int bookingsProcessed,
                int pointsAwarded,
                ListQueueInterface<ProcessedBookingResult> processedItems) { // ADT collection declaration

            this.accountsCreated = accountsCreated;
            this.bookingsProcessed = bookingsProcessed;
            this.pointsAwarded = pointsAwarded;
            this.processedItems = processedItems;
        }

        // Returns how many accounts were created.
        public int getAccountsCreated() {
            return accountsCreated;
        }

        // Returns how many bookings received points.
        public int getBookingsProcessed() {
            return bookingsProcessed;
        }

        // Returns the total points awarded.
        public int getPointsAwarded() {
            return pointsAwarded;
        }

        public ListQueueInterface<ProcessedBookingResult>
                getProcessedItems() {

            return processedItems;
        }

        // Checks whether the automatic scan changed anything.
        public boolean hasActivity() {
            return accountsCreated > 0
                    || bookingsProcessed > 0
                    || pointsAwarded > 0;
        }
    }

    /** Read-only details for one booking processed during an automatic scan. */
    public static final class ProcessedBookingResult {

        private final String loyaltyId;
        private final String bookingId;
        private final int previousPoints;
        private final int pointsEarned;
        private final int newPoints;
        private final MembershipTier previousTier;
        private final MembershipTier newTier;
        private final String earnBatchId;
        private final LocalDateTime transactionTime;
        private final LocalDateTime expiryTime;

        private ProcessedBookingResult(
                String loyaltyId,
                String bookingId,
                int previousPoints,
                int pointsEarned,
                int newPoints,
                MembershipTier previousTier,
                MembershipTier newTier,
                String earnBatchId,
                LocalDateTime transactionTime,
                LocalDateTime expiryTime) {

            this.loyaltyId = loyaltyId;
            this.bookingId = bookingId;
            this.previousPoints = previousPoints;
            this.pointsEarned = pointsEarned;
            this.newPoints = newPoints;
            this.previousTier = previousTier;
            this.newTier = newTier;
            this.earnBatchId = earnBatchId;
            this.transactionTime = transactionTime;
            this.expiryTime = expiryTime;
        }

        // Returns the processed Loyalty ID.
        public String getLoyaltyId() {
            return loyaltyId;
        }

        // Returns the processed booking number.
        public String getBookingId() {
            return bookingId;
        }

        // Returns the balance before this Earn.
        public int getPreviousPoints() {
            return previousPoints;
        }

        // Returns the points earned from this booking.
        public int getPointsEarned() {
            return pointsEarned;
        }

        // Returns the balance after this Earn.
        public int getNewPoints() {
            return newPoints;
        }

        // Returns the tier before this Earn.
        public MembershipTier getPreviousTier() {
            return previousTier;
        }

        // Returns the tier after this Earn.
        public MembershipTier getNewTier() {
            return newTier;
        }

        // Returns the new EARN transaction ID.
        public String getEarnBatchId() {
            return earnBatchId;
        }

        // Returns when the points were earned.
        public LocalDateTime getTransactionTime() {
            return transactionTime;
        }

        // Returns when this point batch expires.
        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }

    /** Checks whether a booking is paid and checked out. */
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

    /** Checks whether a booking already received points. */
    public boolean hasBookingReceivedPoints(String bookingId) {

        if (bookingId == null || bookingId.trim().isEmpty()) {
            return false;
        }

        LoyaltyTransaction transaction =
                loyaltyTransactions.searchByKey( // ADT method call: searchByKey()
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

    /** Finds the EARN batch linked to a booking. */
    public LoyaltyTransaction findEarnTransactionByBookingId(
            String bookingId) {

        if (bookingId == null || bookingId.trim().isEmpty()) {
            return null;
        }

        return loyaltyTransactions.searchByKey( // ADT method call: searchByKey()
                bookingId.trim(), transaction ->
                        transaction.getTransactionType()
                                == TransactionType.EARN
                                ? transaction.getBookingId() : null);
    }

/** Calculates points from the original booking amount. */
public int calculateRewardPoints(Booking booking) {

    if (booking == null || booking.getAmount() <= 0) {
        return 0;
    }

    double originalBookingAmount =
            booking.getAmount();

    return (int) Math.floor(
            originalBookingAmount * POINTS_PER_RM);
}

/** Returns the room amount after the member discount. */
public double getActualPaymentAmount(Booking booking) {

    return calculatePayableAmount(booking);
}

/** Returns the room discount as a display percentage. */
public double getRoomDiscountPercentage(MembershipTier tier) {

    return getRoomDiscountRate(tier) * 100.0;
}

    /** Awards one EARN batch for a completed paid stay. */
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

        LocalDateTime earnedTime =
                LoyaltyClock.now();

        // Remove an already-expired account balance before adding new points.
        processExpiredPoints(earnedTime);

        long newBalance =
                (long) account.getPointsBalance() + points;

        long newTierQualifyingPoints =
                (long) account.getTierQualifyingPoints()
                + points;

        if (newBalance > Integer.MAX_VALUE
                || newTierQualifyingPoints > Integer.MAX_VALUE) {
            return false;
        }

        LocalDateTime batchExpiryTime =
                earnedTime.plusMonths(POINTS_EXPIRY_MONTHS);

        LoyaltyTransaction earnTransaction =
                new LoyaltyTransaction(
                        generateTransactionId(),
                        account.getLoyaltyId(),
                        booking.getConfirmationNumber(),
                        TransactionType.EARN,
                        points,
                        earnedTime,
                        batchExpiryTime
                );

        if (!loyaltyTransactions.enqueue(earnTransaction)) { // ADT method call: enqueue()
            return false;
        }

        account.setPointsBalance((int) newBalance);
        account.setTierQualifyingPoints(
                (int) newTierQualifyingPoints);
        recordPointsActivity(account, earnedTime);
        updateNextPointsExpiry(account, earnedTime);
        recalculateTier(account);

        return true;
    }

    // -------------------------------------------------------------------------
    // DIRECT REWARD REDEMPTION
    // -------------------------------------------------------------------------

    /** Redeems the selected reward for a member. */
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
                rewardPackage.getPointsRequired(),
                rewardPackage);
    }

    /** Redeems the earliest expiring point batches first. */
    private boolean redeemPoints(
            String loyaltyId,
            int points) {

        return redeemPoints(loyaltyId, points, null);
    }

    /** Redeems points and saves the reward in the ledger. */
    private boolean redeemPoints(
            String loyaltyId,
            int points,
            RewardPackage rewardPackage) {

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
                LoyaltyClock.now();

        /*
         * Expire any due batches before checking the redeemable balance.
         */
        processExpiredPoints(currentTime);

        if (account.getPointsBalance() < points) {
            return false;
        }

        ListQueueInterface<LoyaltyTransaction> usableBatches = // ADT collection declaration
                new DoublyLinkedListQueue<>(); // ADT implementation creation

        long availablePoints = 0;

        Iterator<LoyaltyTransaction> transactionIterator =
                loyaltyTransactions.getIterator(); // ADT method call: getIterator()

        while (transactionIterator.hasNext()) {

            LoyaltyTransaction transaction =
                    transactionIterator.next();

            if (!isUsablePointsTransaction(
                    transaction,
                    account.getLoyaltyId(),
                    currentTime)) {

                continue;
            }

            usableBatches.priorityEnqueue( // ADT method call: priorityEnqueue()
                    transaction,
                    this::comparePointBatchExpiry);

            availablePoints +=
                    transaction.getRemainingPoints();
        }

        if (availablePoints < points) {
            return false;
        }

        LoyaltyTransaction redeemTransaction =
                new LoyaltyTransaction(
                        generateTransactionId(),
                        account.getLoyaltyId(),
                        null,
                        TransactionType.REDEEM,
                        points,
                        currentTime,
                        null
                );
        redeemTransaction.setRewardPackage(rewardPackage);

        if (!loyaltyTransactions.enqueue(redeemTransaction)) { // ADT method call: enqueue()
            return false;
        }

        int remainingToRedeem = points;
        Iterator<LoyaltyTransaction> batchIterator =
                usableBatches.getIterator(); // ADT method call: getIterator()

        while (batchIterator.hasNext()
                && remainingToRedeem > 0) {

            LoyaltyTransaction pointBatch =
                    batchIterator.next();

            int deductedPoints =
                    pointBatch.deductRemainingPoints(
                            remainingToRedeem);

            remainingToRedeem -= deductedPoints;
        }

        int balanceAfterRedemption =
                account.getPointsBalance() - points;

        account.setPointsBalance(balanceAfterRedemption);
        recordPointsActivity(account, currentTime);
        updateNextPointsExpiry(account, currentTime);

        redeemTransaction.recordRemovalResult(
                balanceAfterRedemption,
                account.getPointsExpiryTime());

        return true;
    }

    /** Checks whether a point batch can still be redeemed. */
    private boolean isUsablePointsTransaction(
            LoyaltyTransaction transaction,
            String loyaltyId,
            LocalDateTime currentTime) {

        if (transaction == null
                || loyaltyId == null
                || transaction.getLoyaltyId() == null
                || !transaction.getLoyaltyId()
                        .equalsIgnoreCase(loyaltyId)
                || transaction.getRemainingPoints() <= 0) {

            return false;
        }

        TransactionType type =
                transaction.getTransactionType();

        boolean pointBatch =
                type == TransactionType.EARN
                || type == TransactionType.ADJUST;

        return pointBatch
                && transaction.getExpiryTime() != null
                && currentTime != null
                && currentTime.isBefore(transaction.getExpiryTime());
    }

    /** Records the latest successful Earn or Redeem. */
    private void recordPointsActivity(
            LoyaltyAccount account,
            LocalDateTime activityTime) {

        if (account == null || activityTime == null) {
            return;
        }

        account.setLastPointsActivityTime(activityTime);
    }

    /** Saves the member's next point-batch expiry. */
    private void updateNextPointsExpiry(
            LoyaltyAccount account,
            LocalDateTime currentTime) {

        if (account == null) {
            return;
        }

        LocalDateTime nextExpiry = null;

        Iterator<LoyaltyTransaction> iterator =
                loyaltyTransactions.getIterator(); // ADT method call: getIterator()

        while (iterator.hasNext()) {
            LoyaltyTransaction transaction = iterator.next();
            TransactionType type = transaction.getTransactionType();

            if (transaction.getLoyaltyId() != null
                    && transaction.getLoyaltyId().equalsIgnoreCase(
                    account.getLoyaltyId())
                    && transaction.getRemainingPoints() > 0
                    && (type == TransactionType.EARN
                    || type == TransactionType.ADJUST)
                    && transaction.getExpiryTime() != null
                    && (currentTime == null
                    || currentTime.isBefore(transaction.getExpiryTime()))
                    && (nextExpiry == null
                    || transaction.getExpiryTime().isBefore(nextExpiry))) {
                nextExpiry = transaction.getExpiryTime();
            }
        }

        account.setPointsExpiryTime(nextExpiry);
    }

    /** Sorts point batches by the earliest expiry. */
    private int comparePointBatchExpiry(
            LoyaltyTransaction first,
            LoyaltyTransaction second) {

        LocalDateTime firstExpiry =
                first.getExpiryTime();
        LocalDateTime secondExpiry =
                second.getExpiryTime();

        if (firstExpiry == null && secondExpiry == null) {
            return 0;
        }

        if (firstExpiry == null) {
            return 1;
        }

        if (secondExpiry == null) {
            return -1;
        }

        return firstExpiry.compareTo(secondExpiry);
    }

    // -------------------------------------------------------------------------
    // TRANSACTION ID
    // -------------------------------------------------------------------------

    /** Creates the next transaction ID. */
    private String generateTransactionId() {

        int nextNumber =
                loyaltyTransactions.getNumberOfEntries() + 1; // ADT method call: getNumberOfEntries()

        return String.format("T%03d", nextNumber);
    }

    // -------------------------------------------------------------------------
    // TIER MANAGEMENT AND PROMOTIONS
    // -------------------------------------------------------------------------

    /** Updates the tier from qualifying points. */
    public void recalculateTier(LoyaltyAccount account) {

        if (account == null) {
            return;
        }

        if (!account.isActive()) {
            account.setMembershipTier(MembershipTier.NONE);
            return;
        }

        account.setMembershipTier(
                MembershipTier.fromTierQualifyingPoints(
                        account.getTierQualifyingPoints()));
    }

    /** Refreshes every member tier when Loyalty starts. */
    private void recalculateAllTiers() {

        Iterator<LoyaltyAccount> iterator =
                loyaltyAccounts.getIterator(); // ADT method call: getIterator()

        while (iterator.hasNext()) {
            recalculateTier(iterator.next());
        }
    }

    /** Returns the promotion offered to a tier. */
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
    
/** Returns the room discount rate for a member tier. */
public double getRoomDiscountRate(MembershipTier tier) {

    if (tier == null) {
        return 0.0;
    }

    return tier.getRoomDiscountRate();
}

/** Calculates the room discount without changing the booking. */
public double calculateRoomDiscountAmount(Booking booking) {

    if (booking == null) {
        return 0.0;
    }

    MembershipTier tier = MembershipTier.NONE;

    if (booking.getGuest() != null
            && booking.getGuest().getMembershipTier() != null) {

        tier = booking.getGuest().getMembershipTier();
    }

    return booking.getAmount()
            * getRoomDiscountRate(tier);
}

/** Calculates the amount payable after the room discount. */
public double calculatePayableAmount(Booking booking) {

    if (booking == null) {
        return 0.0;
    }

    return booking.getAmount()
            - calculateRoomDiscountAmount(booking);
}

    // -------------------------------------------------------------------------
    // ACCOUNT STATUS MANAGEMENT
    // -------------------------------------------------------------------------

    /** Activates or deactivates a member account. */
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

        LocalDateTime currentTime =
                LoyaltyClock.now();

        processInactiveAccountTierExpirations(
                currentTime);

        if (account.isActive() == active) {
            return true;
        }

        if (active) {
            account.setActive(true);
            account.setDeactivatedAt(null);
            account.setLastPointsActivityTime(currentTime);
            updateNextPointsExpiry(account, currentTime);
            recalculateTier(account);
        } else {
            account.setActive(false);
            account.setDeactivatedAt(currentTime);
            account.setMembershipTier(MembershipTier.NONE);
        }

        return true;
    }

    /** Deactivates accounts with no activity for one year. */
    public int processInactiveAccountTierExpirations(
            LocalDateTime currentTime) {

        if (currentTime == null) {
            return 0;
        }

        int changedAccounts = 0;

        Iterator<LoyaltyAccount> iterator =
                loyaltyAccounts.getIterator(); // ADT method call: getIterator()

        while (iterator.hasNext()) {

            LoyaltyAccount account =
                    iterator.next();

            if (account == null
                    || !account.isActive()
                    || account.getLastPointsActivityTime() == null
                    || currentTime.isBefore(
                            account.getLastPointsActivityTime()
                                    .plusYears(
                                            ACCOUNT_INACTIVITY_YEARS))) {

                continue;
            }

            account.setActive(false);
            account.setDeactivatedAt(currentTime);
            account.setMembershipTier(MembershipTier.NONE);
            changedAccounts++;
        }

        return changedAccounts;
    }

    // -------------------------------------------------------------------------
    // TIER AND POINTS REPORT
    // -------------------------------------------------------------------------

    /** Filters members and lists the highest balances first. */
    public ListQueueInterface<LoyaltyAccount> filterMembers(
            MembershipTier tier,
            int statusFilter,
            int minimumPoints) {

        ListQueueInterface<LoyaltyAccount> emptyResult = // ADT collection declaration
                new DoublyLinkedListQueue<>(); // ADT implementation creation

        if (minimumPoints < 0
                || statusFilter < 0
                || statusFilter > 2) {

            return emptyResult;
        }

        ListQueueInterface<LoyaltyAccount> filteredAccounts = // ADT collection declaration
                loyaltyAccounts.filter(account -> { // ADT method call: filter()

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

        ListQueueInterface<LoyaltyAccount> orderedAccounts = // ADT collection declaration
                new DoublyLinkedListQueue<>(); // ADT implementation creation

        Iterator<LoyaltyAccount> iterator =
                filteredAccounts.getIterator(); // ADT method call: getIterator()

        while (iterator.hasNext()) {

            LoyaltyAccount account =
                    iterator.next();

            orderedAccounts.priorityEnqueue( // ADT method call: priorityEnqueue()
                    account,
                    (firstAccount, secondAccount) ->
                            Integer.compare(
                                    secondAccount.getPointsBalance(),
                                    firstAccount.getPointsBalance())
            );
        }

        return orderedAccounts;
    }

    /** One row of the Loyalty Performance Report grouped by current tier. */
    public static final class TierPerformance {

        private final MembershipTier tier;
        private int members;
        private long availablePoints;
        private long issuedPoints;
        private long redeemedPoints;
        private long expiredPoints;

        private TierPerformance(MembershipTier tier) {
            this.tier = tier;
        }

        // Returns the tier for this report row.
        public MembershipTier getTier() {
            return tier;
        }

        // Returns the number of members in the tier.
        public int getMembers() {
            return members;
        }

        // Returns the tier's available points.
        public long getAvailablePoints() {
            return availablePoints;
        }

        // Returns the points issued to this tier.
        public long getIssuedPoints() {
            return issuedPoints;
        }

        // Returns the points redeemed by this tier.
        public long getRedeemedPoints() {
            return redeemedPoints;
        }

        // Returns the points expired for this tier.
        public long getExpiredPoints() {
            return expiredPoints;
        }

        // Calculates the average member balance for this tier.
        public double getAverageBalance() {
            return members == 0
                    ? 0.0
                    : (double) availablePoints / members;
        }

        // Calculates this tier's redemption rate.
        public double getRedemptionRate() {
            return issuedPoints == 0
                    ? 0.0
                    : redeemedPoints * 100.0 / issuedPoints;
        }
    }

    /** One row of completed reward-redemption popularity statistics. */
    public static final class RewardPerformance {

        private final RewardPackage reward;
        private int redemptionCount;
        private long pointsUsed;

        private RewardPerformance(RewardPackage reward) {
            this.reward = reward;
        }

        // Returns the reward for this report row.
        public RewardPackage getReward() {
            return reward;
        }

        // Returns how many times the reward was redeemed.
        public int getRedemptionCount() {
            return redemptionCount;
        }

        // Returns the points spent on this reward.
        public long getPointsUsed() {
            return pointsUsed;
        }
    }

    /** Complete management summary for the Loyalty Performance Report. */
    public static final class LoyaltyPerformanceReport {

        private int totalMembers;
        private int activeMembers;
        private long earnedPoints;
        private long adjustedPoints;
        private long redeemedPoints;
        private long expiredPoints;
        private long availablePoints;
        private long expiringSoonPoints;
        private String topRedeemingLoyaltyId;
        private String topRedeemingMember;
        private long topMemberRedeemedPoints;
        private MembershipTier highestRedeemingTier;
        private RewardPackage mostPopularReward;
        private int mostPopularRewardCount;
        private final TierPerformance[] tierRows;
        private final RewardPerformance[] rewardRows;

        private LoyaltyPerformanceReport() {
            MembershipTier[] tiers = MembershipTier.values();
            tierRows = new TierPerformance[tiers.length];

            for (int index = 0; index < tiers.length; index++) {
                tierRows[index] = new TierPerformance(tiers[index]);
            }

            RewardPackage[] rewards = RewardPackage.values();
            rewardRows = new RewardPerformance[rewards.length];

            for (int index = 0; index < rewards.length; index++) {
                rewardRows[index] = new RewardPerformance(rewards[index]);
            }
        }

        // Returns the total number of members.
        public int getTotalMembers() {
            return totalMembers;
        }

        // Returns the number of active members.
        public int getActiveMembers() {
            return activeMembers;
        }

        // Returns the total points earned.
        public long getEarnedPoints() {
            return earnedPoints;
        }

        // Returns the total adjusted points.
        public long getAdjustedPoints() {
            return adjustedPoints;
        }

        // Returns the total points redeemed.
        public long getRedeemedPoints() {
            return redeemedPoints;
        }

        // Returns the total points expired.
        public long getExpiredPoints() {
            return expiredPoints;
        }

        // Returns the current point liability.
        public long getAvailablePoints() {
            return availablePoints;
        }

        // Returns the points expiring soon.
        public long getExpiringSoonPoints() {
            return expiringSoonPoints;
        }

        // Returns the top redeeming member's Loyalty ID.
        public String getTopRedeemingLoyaltyId() {
            return topRedeemingLoyaltyId;
        }

        // Returns the top redeeming member's name.
        public String getTopRedeemingMember() {
            return topRedeemingMember;
        }

        // Returns the top member's redeemed points.
        public long getTopMemberRedeemedPoints() {
            return topMemberRedeemedPoints;
        }

        // Returns the tier that redeemed the most points.
        public MembershipTier getHighestRedeemingTier() {
            return highestRedeemingTier;
        }

        // Returns the most frequently redeemed reward.
        public RewardPackage getMostPopularReward() {
            return mostPopularReward;
        }

        // Returns how often the top reward was redeemed.
        public int getMostPopularRewardCount() {
            return mostPopularRewardCount;
        }

        // Returns a safe copy of the tier rows.
        public TierPerformance[] getTierRows() {
            return tierRows.clone();
        }

        // Returns a safe copy of the reward rows.
        public RewardPerformance[] getRewardRows() {
            return rewardRows.clone();
        }

        // Calculates the overall redemption rate.
        public double getRedemptionRate() {
            long issuedPoints = earnedPoints + adjustedPoints;
            return issuedPoints == 0
                    ? 0.0
                    : redeemedPoints * 100.0 / issuedPoints;
        }
    }

    /** Builds the Loyalty performance report from current records. */
    public LoyaltyPerformanceReport generateLoyaltyPerformanceReport(
            LocalDateTime currentTime,
            int expiringDays) {

        LoyaltyPerformanceReport report =
                new LoyaltyPerformanceReport();

        if (currentTime == null || expiringDays < 0) {
            return report;
        }

        LocalDateTime expiryEndTime =
                currentTime.plusDays(expiringDays);

        Iterator<LoyaltyAccount> accountIterator =
                loyaltyAccounts.getIterator(); // ADT method call: getIterator()

        while (accountIterator.hasNext()) {
            LoyaltyAccount account = accountIterator.next();
            MembershipTier tier = account.getMembershipTier() == null
                    ? MembershipTier.NONE
                    : account.getMembershipTier();
            TierPerformance tierRow = report.tierRows[tier.ordinal()];

            report.totalMembers++;
            report.availablePoints += account.getPointsBalance();
            tierRow.members++;
            tierRow.availablePoints += account.getPointsBalance();

            if (account.isActive()) {
                report.activeMembers++;
            }

        }

        Iterator<LoyaltyTransaction> transactionIterator =
                loyaltyTransactions.getIterator(); // ADT method call: getIterator()

        while (transactionIterator.hasNext()) {
            LoyaltyTransaction transaction = transactionIterator.next();
            LoyaltyAccount account = findMemberByLoyaltyId(
                    transaction.getLoyaltyId());

            if (account == null) {
                continue;
            }

            MembershipTier tier = account.getMembershipTier() == null
                    ? MembershipTier.NONE
                    : account.getMembershipTier();
            TierPerformance tierRow = report.tierRows[tier.ordinal()];

            if ((transaction.getTransactionType() == TransactionType.EARN
                    || transaction.getTransactionType()
                            == TransactionType.ADJUST)
                    && transaction.getRemainingPoints() > 0
                    && transaction.getExpiryTime() != null
                    && !transaction.getExpiryTime().isBefore(currentTime)
                    && !transaction.getExpiryTime().isAfter(expiryEndTime)) {
                report.expiringSoonPoints +=
                        transaction.getRemainingPoints();
            }

            switch (transaction.getTransactionType()) {
                case EARN -> {
                    report.earnedPoints += transaction.getPoints();
                    tierRow.issuedPoints += transaction.getPoints();
                }
                case ADJUST -> {
                    report.adjustedPoints += transaction.getPoints();
                    tierRow.issuedPoints += transaction.getPoints();
                }
                case REDEEM -> {
                    report.redeemedPoints += transaction.getPoints();
                    tierRow.redeemedPoints += transaction.getPoints();

                    RewardPackage reward =
                            transaction.getRewardPackage();

                    if (reward != null) {
                        RewardPerformance rewardRow =
                                report.rewardRows[reward.ordinal()];
                        rewardRow.redemptionCount++;
                        rewardRow.pointsUsed += transaction.getPoints();

                        if (rewardRow.redemptionCount
                                > report.mostPopularRewardCount) {
                            report.mostPopularReward = reward;
                            report.mostPopularRewardCount =
                                    rewardRow.redemptionCount;
                        }
                    }
                }
                case EXPIRE -> {
                    report.expiredPoints += transaction.getPoints();
                    tierRow.expiredPoints += transaction.getPoints();
                }
            }

        }

        for (TierPerformance tierRow : report.tierRows) {
            if (tierRow.redeemedPoints > 0
                    && (report.highestRedeemingTier == null
                    || tierRow.redeemedPoints
                    > report.tierRows[
                            report.highestRedeemingTier.ordinal()]
                            .redeemedPoints)) {
                report.highestRedeemingTier = tierRow.tier;
            }
        }

        accountIterator = loyaltyAccounts.getIterator(); // ADT method call: getIterator()

        while (accountIterator.hasNext()) {
            LoyaltyAccount account = accountIterator.next();
            long memberRedeemedPoints = 0;
            transactionIterator = loyaltyTransactions.getIterator(); // ADT method call: getIterator()

            while (transactionIterator.hasNext()) {
                LoyaltyTransaction transaction = transactionIterator.next();

                if (transaction.getTransactionType()
                        == TransactionType.REDEEM
                        && account.getLoyaltyId().equalsIgnoreCase(
                                transaction.getLoyaltyId())) {
                    memberRedeemedPoints += transaction.getPoints();
                }
            }

            if (memberRedeemedPoints > report.topMemberRedeemedPoints) {
                report.topMemberRedeemedPoints = memberRedeemedPoints;
                report.topRedeemingLoyaltyId = account.getLoyaltyId();
                report.topRedeemingMember = account.getMemberName();
            }
        }

        return report;
    }

    // -------------------------------------------------------------------------
    // EXPIRING POINTS REPORTS
    // -------------------------------------------------------------------------

    /** Finds members with points expiring inside the date range. */
    public ListQueueInterface<LoyaltyAccount>
            generateExpiringAccountAlerts(
                    LocalDateTime currentTime,
                    int daysAhead) {

        if (currentTime == null || daysAhead < 0) {
            return new DoublyLinkedListQueue<>();
        }

        LocalDateTime endTime = currentTime.plusDays(daysAhead);
        ListQueueInterface<LoyaltyAccount> matches = // ADT collection declaration
                loyaltyAccounts.filter(account -> // ADT method call: filter()
                        account != null
                        && account.getPointsBalance() > 0
                        && account.getPointsExpiryTime() != null
                        && !account.getPointsExpiryTime()
                                .isBefore(currentTime)
                        && !account.getPointsExpiryTime()
                                .isAfter(endTime));
        ListQueueInterface<LoyaltyAccount> ordered = // ADT collection declaration
                new DoublyLinkedListQueue<>(); // ADT implementation creation
        Iterator<LoyaltyAccount> iterator = matches.getIterator(); // ADT method call: getIterator()

        while (iterator.hasNext()) {
            ordered.priorityEnqueue( // ADT method call: priorityEnqueue()
                    iterator.next(),
                    (first, second) -> first.getPointsExpiryTime()
                            .compareTo(second.getPointsExpiryTime()));
        }

        return ordered;
    }

    /** Lists expiring point batches with the earliest one first. */
    public ListQueueInterface<LoyaltyTransaction>
            generateExpiringPointsReport(
                    LocalDateTime startTime,
                    LocalDateTime endTime,
                    MembershipTier tier) {

        ListQueueInterface<LoyaltyTransaction> emptyResult = // ADT collection declaration
                new DoublyLinkedListQueue<>(); // ADT implementation creation

        if (startTime == null
                || endTime == null
                || endTime.isBefore(startTime)) {

            return emptyResult;
        }

        ListQueueInterface<LoyaltyTransaction> filteredTransactions = // ADT collection declaration
                loyaltyTransactions.filter(transaction -> { // ADT method call: filter()

                    if (transaction == null
                            || !transaction.isExpiringBetween(
                                    startTime,
                                    endTime)) {

                        return false;
                    }

                    LoyaltyAccount account =
                            findMemberByLoyaltyId(
                                    transaction.getLoyaltyId());

                    return account != null
                            && (tier == null
                            || account.getMembershipTier() == tier);
                });

        ListQueueInterface<LoyaltyTransaction> orderedTransactions = // ADT collection declaration
                new DoublyLinkedListQueue<>(); // ADT implementation creation

        Iterator<LoyaltyTransaction> iterator =
                filteredTransactions.getIterator(); // ADT method call: getIterator()

        while (iterator.hasNext()) {

            LoyaltyTransaction transaction =
                    iterator.next();

            orderedTransactions.priorityEnqueue( // ADT method call: priorityEnqueue()
                    transaction,
                    this::comparePointBatchExpiry);
        }

        return orderedTransactions;
    }

    /** Finds point batches that need an expiry warning. */
    public ListQueueInterface<LoyaltyTransaction>
            generateExpiringPointsAlerts(
                    LocalDateTime currentTime,
                    int daysAhead) {

        if (currentTime == null || daysAhead < 0) {
            return new DoublyLinkedListQueue<>();
        }

        return generateExpiringPointsReport(
                currentTime,
                currentTime.plusDays(daysAhead),
                null
        );
    }

    // -------------------------------------------------------------------------
    // EXPIRED POINTS PROCESSING
    // -------------------------------------------------------------------------

    /** Expires each due point batch and records the deduction. */
    public int processExpiredPoints(
            LocalDateTime currentTime) {

        if (currentTime == null) {
            return 0;
        }

        int totalExpiredPoints = 0;
        ListQueueInterface<LoyaltyTransaction> expiredBatches = // ADT collection declaration
                loyaltyTransactions.filter(transaction -> { // ADT method call: filter()
                    if (transaction == null
                            || transaction.getRemainingPoints() <= 0
                            || transaction.getExpiryTime() == null
                            || currentTime.isBefore(
                                    transaction.getExpiryTime())) {
                        return false;
                    }

                    TransactionType type = transaction.getTransactionType();
                    return type == TransactionType.EARN
                            || type == TransactionType.ADJUST;
                });

        Iterator<LoyaltyTransaction> expiredIterator =
                expiredBatches.getIterator(); // ADT method call: getIterator()

        while (expiredIterator.hasNext()) {
            LoyaltyTransaction expiredBatch = expiredIterator.next();
            LoyaltyAccount account = findMemberByLoyaltyId(
                    expiredBatch.getLoyaltyId());

            if (account == null) {
                continue;
            }

            int expiredPoints = expiredBatch.getRemainingPoints();

            if (expiredPoints <= 0) {
                continue;
            }

            int balanceAfterExpiry = Math.max(
                    0, account.getPointsBalance() - expiredPoints);
            LoyaltyTransaction expiryTransaction =
                    new LoyaltyTransaction(
                            generateTransactionId(),
                            account.getLoyaltyId(),
                            expiredBatch.getBookingId(),
                            TransactionType.EXPIRE,
                            expiredPoints,
                            currentTime,
                            null);

            if (!loyaltyTransactions.enqueue(expiryTransaction)) { // ADT method call: enqueue()
                continue;
            }

            expiredBatch.expireRemainingPoints();
            account.setPointsBalance(balanceAfterExpiry);
            expiryTransaction.recordRemovalResult(
                    balanceAfterExpiry,
                    expiredBatch.getExpiryTime());
            totalExpiredPoints += expiredPoints;
            updateNextPointsExpiry(account, currentTime);
        }

        return totalExpiredPoints;
    }
    // Returns all reward requests waiting to be processed.
    public ListQueueInterface<RedemptionRequest>
        getPendingRedemptionRequests() {

    return redemptionRequests;
}
        // Processes the first reward request in FIFO order.
        public boolean processNextRedemptionRequest() {

    if (redemptionRequests == null
            || redemptionRequests.isEmpty()) { // ADT method call: isEmpty()

        return false;
    }

    // Read the first request without removing it.
    RedemptionRequest nextRequest =
            redemptionRequests.peek(); // ADT method call: peek()

    if (nextRequest == null) {
        return false;
    }

    // Try to redeem the requested reward.
    boolean redeemed =
            redeemPoints(
                    nextRequest.getLoyaltyId(),
                    nextRequest.getPoints(),
                    nextRequest.getRewardPackage());

    if (!redeemed) {
        // Keep the request in the queue if processing fails.
        return false;
    }

    // FIFO: the completed reward is already retained in the REDEEM ledger.
    redemptionRequests.dequeue(); // ADT method call: dequeue()

    return true;
}
        // Removes one selected request from the pending queue.
        public boolean cancelRedemptionRequest(String requestId) {

    if (requestId == null
            || requestId.trim().isEmpty()) {

        return false;
    }

    if (redemptionRequests == null
            || redemptionRequests.isEmpty()) { // ADT method call: isEmpty()

        return false;
    }

    int originalSize =
            redemptionRequests.getNumberOfEntries(); // ADT method call: getNumberOfEntries()

    boolean cancelled = false;

    for (int i = 0; i < originalSize; i++) {

        RedemptionRequest currentRequest =
                redemptionRequests.dequeue(); // ADT method call: dequeue()

        if (!cancelled
                && currentRequest.getRequestId()
                        .equalsIgnoreCase(requestId.trim())) {

            // Skip this request so it is cancelled.
            cancelled = true;

        } else {

            // Put all other requests back into the queue.
            redemptionRequests.enqueue(currentRequest); // ADT method call: enqueue()
        }
    }

    return cancelled;
}
}
