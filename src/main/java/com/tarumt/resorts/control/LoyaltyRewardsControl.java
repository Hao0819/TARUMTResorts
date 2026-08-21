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

    /**
     * Creates an empty Loyalty and Rewards control.
     */
    public LoyaltyRewardsControl() {
        loyaltyAccounts = new DoublyLinkedListQueue<>(); // ADT implementation creation
        loyaltyTransactions = new DoublyLinkedListQueue<>(); // ADT implementation creation
        guests = new DoublyLinkedListQueue<>(); // ADT implementation creation
        bookings = new DoublyLinkedListQueue<>(); // ADT implementation creation
        redemptionRequests = new DoublyLinkedListQueue<>(); // ADT implementation creation
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

    /** Returns the configured advance-warning period for expiring batches. */
    public int getExpiryNotificationDays() {
        return EXPIRY_NOTIFICATION_DAYS;
    }

    private String generateRedemptionRequestId() {

    String requestId = String.format(
            "R%03d",
            nextRedemptionRequestNumber);

    nextRedemptionRequestNumber++;

    return requestId;
}
    
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

        return loyaltyAccounts.searchByKey( // ADT method call: searchByKey()
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

        return loyaltyAccounts.searchByKey( // ADT method call: searchByKey()
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

        return guests.searchByKey( // ADT method call: searchByKey()
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

        return bookings.searchByKey( // ADT method call: searchByKey()
                confirmationNumber.trim(),
                booking -> booking.getConfirmationNumber()
        );
    }

    /**
     * Returns operational completed stays for the Loyalty lookup table.
     * Historical seed bookings (20261xxx) are already represented in the
     * opening ledger and are omitted to keep the selection table concise.
     */
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

        if (!newAccount.isActive()
                && newAccount.getDeactivatedAt() == null) {

            newAccount.setDeactivatedAt(
                    LoyaltyClock.now());
        }

        return loyaltyAccounts.enqueue(newAccount); // ADT method call: enqueue()
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
     * Front-Desk does not call this method. Loyalty invokes it when its menu
     * opens and discovers changes through the shared Booking collection.
     *
     * @return counters used by the UI to display a concise automation summary
     */
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

        public int getAccountsCreated() {
            return accountsCreated;
        }

        public int getBookingsProcessed() {
            return bookingsProcessed;
        }

        public int getPointsAwarded() {
            return pointsAwarded;
        }

        public ListQueueInterface<ProcessedBookingResult>
                getProcessedItems() {

            return processedItems;
        }

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

        public String getLoyaltyId() {
            return loyaltyId;
        }

        public String getBookingId() {
            return bookingId;
        }

        public int getPreviousPoints() {
            return previousPoints;
        }

        public int getPointsEarned() {
            return pointsEarned;
        }

        public int getNewPoints() {
            return newPoints;
        }

        public MembershipTier getPreviousTier() {
            return previousTier;
        }

        public MembershipTier getNewTier() {
            return newTier;
        }

        public String getEarnBatchId() {
            return earnBatchId;
        }

        public LocalDateTime getTransactionTime() {
            return transactionTime;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
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

    /** Returns the EARN batch created for a booking, or null when unprocessed. */
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

   /**
 * Calculates loyalty points using the ORIGINAL booking amount.
 *
 * Membership room discounts do not reduce the amount used
 * for loyalty-point calculation.
 *
 * @param booking booking to calculate
 * @return calculated loyalty points
 */
public int calculateRewardPoints(Booking booking) {

    if (booking == null || booking.getAmount() <= 0) {
        return 0;
    }

    double originalBookingAmount =
            booking.getAmount();

    return (int) Math.floor(
            originalBookingAmount * POINTS_PER_RM);
}

/**
 * Calculates the amount the guest actually needs to pay
 * after applying the Loyalty room discount.
 *
 * The original Booking amount is never modified.
 *
 * @param booking booking to calculate
 * @return final payable amount after room discount
 */
public double getActualPaymentAmount(Booking booking) {

    return calculatePayableAmount(booking);
}

/**
 * Returns the room discount percentage for display.
 *
 * Example: 0.10 becomes 10.0.
 */
public double getRoomDiscountPercentage(MembershipTier tier) {

    return getRoomDiscountRate(tier) * 100.0;
}

    /**
     * Adds Loyalty points from an eligible completed stay.
     *
     * The booking must exist, belong to the same Guest, be CHECKED_OUT,
     * be PAID, and must not have received points before.
     *
     * Each successful earn creates a separate point batch and resets the
     * account-level three-month points-expiry date.
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
                rewardPackage.getPointsRequired(),
                rewardPackage);
    }

    /**
     * Redeems points from individual batches. Batches with the earliest
     * expiry time are consumed first so fewer points are unnecessarily lost.
     */
    private boolean redeemPoints(
            String loyaltyId,
            int points) {

        return redeemPoints(loyaltyId, points, null);
    }

    /** Redeems points and retains the selected reward in the audit ledger. */
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

    /**
     * Checks whether a transaction still contains redeemable points.
     */
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

    /** Records activity for the separate one-year inactivity rule. */
    private void recordPointsActivity(
            LoyaltyAccount account,
            LocalDateTime activityTime) {

        if (account == null || activityTime == null) {
            return;
        }

        account.setLastPointsActivityTime(activityTime);
    }

    /** Stores the earliest expiry among the account's unused point batches. */
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

    /**
     * Orders expiring batches first and non-expiring legacy batches last.
     */
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

    /**
     * Generates the next transaction ID.
     *
     * @return ID such as T001, T002, T003
     */
    private String generateTransactionId() {

        int nextNumber =
                loyaltyTransactions.getNumberOfEntries() + 1; // ADT method call: getNumberOfEntries()

        return String.format("T%03d", nextNumber);
    }

    // -------------------------------------------------------------------------
    // TIER MANAGEMENT AND PROMOTIONS
    // -------------------------------------------------------------------------

    /**
     * Recalculates a member's tier from qualifying points. Redeemable balance
     * changes caused by redemption or expiry do not affect this calculation.
     *
     * NONE: 0-499
     * SILVER: 500-1,499
     * GOLD: 1,500-2,999
     * PLATINUM: 3,000-4,999
     * DIAMOND: 5,000-6,999
     * ELITE: 7,000+
     *
     * An inactive account always has Tier NONE.
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

        account.setMembershipTier(
                MembershipTier.fromTierQualifyingPoints(
                        account.getTierQualifyingPoints()));
    }

    /**
     * Recalculates all tiers when the Loyalty module is initialized.
     */
    private void recalculateAllTiers() {

        Iterator<LoyaltyAccount> iterator =
                loyaltyAccounts.getIterator(); // ADT method call: getIterator()

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
    
    /**
 * Returns the room discount rate for a membership tier.
 *
 * This is separate from personalized dining/promotional benefits.
 *
 * @param tier membership tier
 * @return room discount rate as a decimal
 */
public double getRoomDiscountRate(MembershipTier tier) {

    if (tier == null) {
        return 0.0;
    }

    return tier.getRoomDiscountRate();
}

/**
 * Calculates the room discount amount without modifying
 * the original Booking amount.
 */
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

/**
 * Calculates the actual amount the guest needs to pay
 * after applying the membership room discount.
 *
 * Booking.amount remains the original amount.
 */
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

    /**
     * Activates or deactivates a Loyalty account.
     *
     * Inactive accounts cannot earn or redeem points and always use Tier NONE.
     * Reactivation restores the tier from saved qualifying points and begins
     * a new inactivity period.
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

    /**
     * Automatically deactivates accounts with no successful EARN or REDEEM
     * for one year and immediately applies Tier NONE.
     *
     * @param currentTime time used for the inactivity check
     * @return number of accounts whose tier changed to NONE
     */
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

        public MembershipTier getTier() {
            return tier;
        }

        public int getMembers() {
            return members;
        }

        public long getAvailablePoints() {
            return availablePoints;
        }

        public long getIssuedPoints() {
            return issuedPoints;
        }

        public long getRedeemedPoints() {
            return redeemedPoints;
        }

        public long getExpiredPoints() {
            return expiredPoints;
        }

        public double getAverageBalance() {
            return members == 0
                    ? 0.0
                    : (double) availablePoints / members;
        }

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

        public RewardPackage getReward() {
            return reward;
        }

        public int getRedemptionCount() {
            return redemptionCount;
        }

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

        public int getTotalMembers() {
            return totalMembers;
        }

        public int getActiveMembers() {
            return activeMembers;
        }

        public long getEarnedPoints() {
            return earnedPoints;
        }

        public long getAdjustedPoints() {
            return adjustedPoints;
        }

        public long getRedeemedPoints() {
            return redeemedPoints;
        }

        public long getExpiredPoints() {
            return expiredPoints;
        }

        public long getAvailablePoints() {
            return availablePoints;
        }

        public long getExpiringSoonPoints() {
            return expiringSoonPoints;
        }

        public String getTopRedeemingLoyaltyId() {
            return topRedeemingLoyaltyId;
        }

        public String getTopRedeemingMember() {
            return topRedeemingMember;
        }

        public long getTopMemberRedeemedPoints() {
            return topMemberRedeemedPoints;
        }

        public MembershipTier getHighestRedeemingTier() {
            return highestRedeemingTier;
        }

        public RewardPackage getMostPopularReward() {
            return mostPopularReward;
        }

        public int getMostPopularRewardCount() {
            return mostPopularRewardCount;
        }

        public TierPerformance[] getTierRows() {
            return tierRows.clone();
        }

        public RewardPerformance[] getRewardRows() {
            return rewardRows.clone();
        }

        public double getRedemptionRate() {
            long issuedPoints = earnedPoints + adjustedPoints;
            return issuedPoints == 0
                    ? 0.0
                    : redeemedPoints * 100.0 / issuedPoints;
        }
    }

    /**
     * Builds a read-only management report from the account, transaction and
     * completed-redemption ledgers.
     *
     * @param currentTime report generation time
     * @param expiringDays number of days used for expiry risk
     * @return calculated performance report
     */
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

    /** Returns accounts whose next point batch expires within the window. */
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

    /**
     * Generates the expiring-points report.
     *
     * Each result is one point batch with unused points expiring inside the
     * selected window. Results are ordered by earliest expiry time first.
     *
     * @param startTime start of expiry window
     * @param endTime end of expiry window
     * @param tier selected tier, or null for all tiers
     * @return matching point-batch transactions
     */
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

    /**
     * Generates notifications for point batches expiring within several days.
     *
     * @param currentTime current date and time
     * @param daysAhead notification window in days
     * @return matching point-batch transactions
     */
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

    /**
     * Expires each unused EARN/ADJUST batch independently three months after
     * that batch was earned. Each expired batch creates its own EXPIRE entry.
     */
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
    public ListQueueInterface<RedemptionRequest>
        getPendingRedemptionRequests() {

    return redemptionRequests;
}
        public boolean processNextRedemptionRequest() {

    if (redemptionRequests == null
            || redemptionRequests.isEmpty()) { // ADT method call: isEmpty()

        return false;
    }

    // View the first request without removing it.
    RedemptionRequest nextRequest =
            redemptionRequests.peek(); // ADT method call: peek()

    if (nextRequest == null) {
        return false;
    }

    // Deduct the points using your existing redemption logic.
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
