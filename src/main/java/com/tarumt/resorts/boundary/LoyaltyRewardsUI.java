/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.boundary;

import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.control.LoyaltyRewardsControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.MembershipTier;
import com.tarumt.resorts.entity.RewardPackage;
import com.tarumt.resorts.entity.RedemptionRequest;

import java.util.Iterator;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Boundary class for the Loyalty and Rewards Service.
 * Handles user input/output only and delegates business logic to
 * {@link LoyaltyRewardsControl}.
 */
public class LoyaltyRewardsUI {

    private final LoyaltyRewardsControl loyaltyControl;
    private final Scanner scanner;
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Creates the Loyalty and Rewards user interface.
     *
     * @param loyaltyControl control class containing loyalty operations
     * @param scanner shared Scanner from TARUMTResorts
     */
    public LoyaltyRewardsUI(
    LoyaltyRewardsControl loyaltyControl,
    Scanner scanner) {

        this.loyaltyControl = loyaltyControl;
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // MAIN MENU NAVIGATION
    // -------------------------------------------------------------------------

    /**
     * Displays the Loyalty and Rewards menu.
     */
    public void showMenu() {
        int choice;

        do {
            LocalDateTime currentTime =
            LocalDateTime.now();

            int automaticallyExpired =
            loyaltyControl.processExpiredPoints(
            currentTime);

            int inactiveTiersExpired =
            loyaltyControl.processInactiveAccountTierExpirations(
            currentTime);

            if (automaticallyExpired > 0) {
                System.out.println();
                System.out.println(
                "Automatic expiry processed: "
                + automaticallyExpired
                + " points expired.");
            }

            if (inactiveTiersExpired > 0) {
                System.out.println();
                System.out.println(
                "Inactive-account tier expiry processed: "
                + inactiveTiersExpired
                + " account(s) changed to NONE.");
            }

            displayMenu();
            choice = readChoice();

            switch (choice) {
                case 1 -> findLoyaltyMember();
                case 2 -> createLoyaltyAccount();
                case 3 -> addPointsFromCompletedStay();
                case 4 -> redemptionManagement();
                case 5 -> displayTierAndPointsReport();
                case 6 -> updateLoyaltyAccountStatus();
                case 7 -> displayExpiringPointsReport();
                case 8 -> displayExpiringPointsAlerts();
                case 9 -> displayPointTransactionHistory();
                case 0 -> System.out.println(
                "Returning to the main menu.");
                default -> System.out.println(
                "Invalid choice. Please try again.");
            }

        } while (choice != 0);
    }
    
    /**
 * Displays all loyalty accounts currently stored
 * in the custom ADT collection.
 */
private void displayAllLoyaltyAccounts() {

    ListQueueInterface<LoyaltyAccount> accounts =
            loyaltyControl.getLoyaltyAccounts();

    displayAccountList(accounts);
}
    
    /**
     * Triggers Loyalty processing for completed, paid bookings.
     * This method is exposed for integration with the checkout/front-desk flow.
     */
    public void processCompletedBookingsForLoyalty() {

        loyaltyControl.processCompletedBookingsForLoyalty();
    }

   private void findLoyaltyMember() {

    displayAllLoyaltyAccounts();

    System.out.println();
    System.out.println(
            "+------------------------------------------------+");
    System.out.println(
            "|              FIND LOYALTY MEMBER               |");
    System.out.println(
            "+------------------------------------------------+");
    System.out.println(
            "| 1. Search by Loyalty ID                        |");
    System.out.println(
            "| 2. Search by Guest ID                          |");
    System.out.println(
            "+------------------------------------------------+");

    System.out.print("Enter search option: ");

    int searchOption = readChoice();

    LoyaltyAccount account;

    switch (searchOption) {

        case 1 -> {
            System.out.print("Enter Loyalty ID: ");

            String loyaltyId =
                    scanner.nextLine().trim();

            account =
                    loyaltyControl.findMemberByLoyaltyId(
                            loyaltyId);
        }

        case 2 -> {
            System.out.print("Enter Guest ID: ");

            String guestId =
                    scanner.nextLine().trim();

            account =
                    loyaltyControl.findMemberByGuestId(
                            guestId);
        }

        default -> {
            System.out.println(
                    "Invalid search option.");
            return;
        }
    }

    if (account == null) {
        System.out.println(
                "Loyalty member not found.");
        return;
    }

    displayAccountDetails(account);
}
    
    
    private void redemptionManagement() {

    int choice;

    do {
        System.out.println();
        System.out.println(
                "+--------------------------------------------------+");
        System.out.println(
                "|              REDEMPTION MANAGEMENT               |");
        System.out.println(
                "+--------------------------------------------------+");
        System.out.println(
                "| 1. Submit Redemption Request                     |");
        System.out.println(
                "| 2. View Pending Redemption Queue                 |");
        System.out.println(
                "| 3. Process Next Redemption Request               |");
        System.out.println(
                "| 4. Cancel Pending Redemption Request             |");
        System.out.println(
                "| 0. Back to Loyalty Menu                          |");
        System.out.println(
                "+--------------------------------------------------+");

        System.out.print("Enter choice: ");
        choice = readChoice();

        switch (choice) {

            case 1 ->
                submitRedemptionRequest();

            case 2 ->
                displayPendingRedemptionQueue();

            case 3 ->
                processNextRedemptionRequest();

            case 4 ->
                cancelPendingRedemptionRequest();

            case 0 ->
                System.out.println(
                        "Returning to Loyalty Menu.");

            default ->
                System.out.println(
                        "Invalid choice. Please try again.");
        }

    } while (choice != 0);
}
    private void submitRedemptionRequest() {

    System.out.println();
    System.out.println(
            "+------------------------------------------------+");
    System.out.println(
            "|          SUBMIT REDEMPTION REQUEST             |");
    System.out.println(
            "+------------------------------------------------+");

    System.out.print("Enter Loyalty ID: ");

    String loyaltyId =
            scanner.nextLine().trim();

    if (loyaltyId.isEmpty()) {
        System.out.println(
                "Loyalty ID cannot be empty.");
        return;
    }

    LoyaltyAccount account =
            loyaltyControl.findMemberByLoyaltyId(
                    loyaltyId);

    if (account == null) {
        System.out.println(
                "Loyalty member not found.");
        return;
    }

    if (!account.isActive()) {
        System.out.println(
                "This loyalty account is inactive.");
        return;
    }

    System.out.println();
    System.out.println("Member found:");
    System.out.println(
            "Name           : "
            + account.getMemberName());

    System.out.println(
            "Current Points : "
            + account.getPointsBalance());

    System.out.println(
            "Current Tier   : "
            + account.getMembershipTier());

System.out.println();

String rewardBorder =
        "+-----+------------------------+------------+";

String rewardTitle =
        "AVAILABLE REWARDS";

int innerWidth =
        rewardBorder.length() - 2;

int leftPadding =
        (innerWidth - rewardTitle.length()) / 2;

int rightPadding =
        innerWidth
        - rewardTitle.length()
        - leftPadding;

System.out.println(rewardBorder);

System.out.println(
        "|"
        + " ".repeat(leftPadding)
        + rewardTitle
        + " ".repeat(rightPadding)
        + "|");

System.out.println(rewardBorder);

System.out.printf(
        "| %-3s | %-22s | %10s |%n",
        "No.",
        "Reward",
        "Points");

System.out.println(rewardBorder);

RewardPackage[] rewards =
        RewardPackage.values();

int optionNumber = 1;

for (RewardPackage reward : rewards) {

    if (reward.getPointsRequired()
            <= account.getPointsBalance()) {

        System.out.printf(
                "| %-3d | %-22s | %10d |%n",
                optionNumber,
                reward.getRewardName(),
                reward.getPointsRequired());

        optionNumber++;
    }
}

System.out.println(rewardBorder);

System.out.printf(
        "| %-3s | %-22s | %10s |%n",
        "0",
        "Cancel",
        "");

System.out.println(rewardBorder);

int selectedOption = readChoice();
    RewardPackage selectedReward = null;

    int currentOption = 1;

    for (RewardPackage reward : rewards) {

        if (reward.getPointsRequired()
                <= account.getPointsBalance()) {

            if (currentOption == selectedOption) {
                selectedReward = reward;
                break;
            }

            currentOption++;
        }
    }

    if (selectedReward == null) {
        System.out.println(
                "Invalid reward option.");
        return;
    }

    System.out.println();
    System.out.println(
            "Selected Reward : "
            + selectedReward.getRewardName());

    System.out.println(
            "Required Points : "
            + selectedReward.getPointsRequired());

    System.out.print(
            "Confirm redemption request? (Y/N): ");

    String confirmation =
            scanner.nextLine().trim();

    if (!confirmation.equalsIgnoreCase("Y")) {
        System.out.println(
                "Redemption request cancelled.");
        return;
    }

    RedemptionRequest request =
            loyaltyControl.submitRedemptionRequest(
                    loyaltyId,
                    selectedReward);

    if (request == null) {
        System.out.println(
                "Unable to submit redemption request.");
        System.out.println(
                "Check available points after pending requests.");
        return;
    }

    System.out.println();
    System.out.println(
            "Redemption request submitted successfully.");

    System.out.println(
            "Request ID : "
            + request.getRequestId());

    System.out.println(
            "Reward     : "
            + request.getRewardPackage()
                    .getRewardName());

    System.out.println(
            "Points     : "
            + request.getPoints());

    System.out.println(
            "Date       : "
            + request.getRequestDate());
}
    
    private void displayPendingRedemptionQueue() {

    System.out.println();
    System.out.println(
            "+--------------------------------------------------------------------------------+");
    System.out.println(
            "|                         PENDING REDEMPTION QUEUE                               |");
    System.out.println(
            "+--------------------------------------------------------------------------------+");

    ListQueueInterface<RedemptionRequest> requests =
            loyaltyControl.getPendingRedemptionRequests();

    if (requests == null || requests.isEmpty()) {
        System.out.println(
                "No pending redemption requests.");
        return;
    }

    String border =
            "+----------+------------+----------------------+----------+--------------+";

    System.out.println(border);

    System.out.printf(
            "| %-8s | %-10s | %-20s | %8s | %-12s |%n",
            "Request",
            "Loyalty ID",
            "Reward",
            "Points",
            "Date");

    System.out.println(border);

    Iterator<RedemptionRequest> requestIterator =
            requests.getIterator();

    while (requestIterator.hasNext()) {

        RedemptionRequest request =
                requestIterator.next();

        String rewardName =
                request.getRewardPackage() == null
                        ? "-"
                        : request.getRewardPackage()
                                .getRewardName();

        System.out.printf(
                "| %-8s | %-10s | %-20s | %8d | %-12s |%n",
                request.getRequestId(),
                request.getLoyaltyId(),
                rewardName,
                request.getPoints(),
                request.getRequestDate());
    }

    System.out.println(border);

    System.out.println(
            "Total pending requests: "
            + requests.getNumberOfEntries());

    System.out.println(
            "Requests are processed in FIFO order.");
}
    
    private void processNextRedemptionRequest() {

    System.out.println();
    System.out.println(
            "+------------------------------------------------+");
    System.out.println(
            "|        PROCESS NEXT REDEMPTION REQUEST         |");
    System.out.println(
            "+------------------------------------------------+");

    ListQueueInterface<RedemptionRequest> requests =
            loyaltyControl.getPendingRedemptionRequests();

    if (requests == null || requests.isEmpty()) {
        System.out.println(
                "No pending redemption requests.");
        return;
    }

    RedemptionRequest nextRequest =
            requests.peek();

    System.out.println("Next request:");
    System.out.println(
            "Request ID : "
            + nextRequest.getRequestId());

    System.out.println(
            "Loyalty ID : "
            + nextRequest.getLoyaltyId());

    System.out.println(
            "Points     : "
            + nextRequest.getPoints());

    System.out.println(
            "Date       : "
            + nextRequest.getRequestDate());

    System.out.print(
            "Process this request? (Y/N): ");

    String confirmation =
            scanner.nextLine().trim();

    if (!confirmation.equalsIgnoreCase("Y")) {
        System.out.println(
                "Processing cancelled.");
        return;
    }

    boolean processed =
            loyaltyControl.processNextRedemptionRequest();

    if (!processed) {
        System.out.println(
                "Unable to process the redemption request.");
        System.out.println(
                "The request remains in the pending queue.");
        return;
    }

    System.out.println();
    System.out.println(
            "Redemption request processed successfully.");

    System.out.println(
            "Request "
            + nextRequest.getRequestId()
            + " has been removed from the queue.");
}
    
    private void cancelPendingRedemptionRequest() {

    System.out.println();
    System.out.println(
            "+------------------------------------------------+");
    System.out.println(
            "|        CANCEL REDEMPTION REQUEST               |");
    System.out.println(
            "+------------------------------------------------+");

    ListQueueInterface<RedemptionRequest> requests =
            loyaltyControl.getPendingRedemptionRequests();

    if (requests == null || requests.isEmpty()) {
        System.out.println(
                "No pending redemption requests.");
        return;
    }

    // Show current queue first.
    displayPendingRedemptionQueue();

    System.out.print(
            "\nEnter Request ID to cancel: ");

    String requestId =
            scanner.nextLine().trim();

    if (requestId.isEmpty()) {
        System.out.println(
                "Request ID cannot be empty.");
        return;
    }

    System.out.print(
            "Confirm cancellation of "
            + requestId
            + "? (Y/N): ");

    String confirmation =
            scanner.nextLine().trim();

    if (!confirmation.equalsIgnoreCase("Y")) {
        System.out.println(
                "Cancellation cancelled.");
        return;
    }

    boolean cancelled =
            loyaltyControl.cancelRedemptionRequest(
                    requestId);

    if (!cancelled) {
        System.out.println(
                "Redemption request not found.");
        return;
    }

    System.out.println();
    System.out.println(
            "Redemption request cancelled successfully.");

    System.out.println(
            "Request ID: " + requestId);
}

    

    /**
     * Creates a loyalty account for an existing guest.
     */
    private void createLoyaltyAccount() {

        System.out.println();
        System.out.println(
        "+------------------------------------------------+");
        System.out.println(
        "|             CREATE LOYALTY ACCOUNT             |");
        System.out.println(
        "+------------------------------------------------+");

        System.out.print("Enter existing Guest ID: ");

        String guestId =
        scanner.nextLine().trim();

        if (guestId.isEmpty()) {
            System.out.println(
            "Guest ID cannot be empty.");
            return;
        }

        Guest guest =
        loyaltyControl.findGuestById(guestId);

        if (guest == null) {
            System.out.println(
            "Guest not found. Register the guest first.");
            return;
        }

        LoyaltyAccount existingAccount =
        loyaltyControl.findMemberByGuestId(guestId);

        if (existingAccount != null) {
            System.out.println(
            "This guest already has a loyalty account.");

            displayAccountDetails(existingAccount);
            return;
        }

        System.out.println();
        System.out.println("Guest found:");
        System.out.println("Guest ID : " + guest.getGuestId());
        System.out.println("Name     : " + guest.getName());
        System.out.println("Email    : " + guest.getEmail());

        System.out.print(
        "Confirm account creation? (Y/N): ");

        String confirmation =
        scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println(
            "Account creation cancelled.");
            return;
        }

        LoyaltyAccount newAccount =
        loyaltyControl.createAccountForGuest(
        guestId);

        if (newAccount == null) {
            System.out.println(
            "Unable to create loyalty account.");
            return;
        }

        System.out.println();
        System.out.println(
        "Loyalty account created successfully.");

        displayAccountDetails(newAccount);
    }

    /**
     * Adds loyalty points after a completed stay.
     */
    private void addPointsFromCompletedStay() {

        System.out.println();
        System.out.println(
        "+------------------------------------------------+");
        System.out.println(
        "|        ADD POINTS FROM COMPLETED STAY          |");
        System.out.println(
        "+------------------------------------------------+");

        System.out.print("Enter Loyalty ID: ");
        String loyaltyId =
        scanner.nextLine().trim();

        if (loyaltyId.isEmpty()) {
            System.out.println(
            "Loyalty ID cannot be empty.");
            return;
        }

        LoyaltyAccount account =
        loyaltyControl.findMemberByLoyaltyId(
        loyaltyId);

        if (account == null) {
            System.out.println(
            "Loyalty member not found.");
            return;
        }

        if (!account.isActive()) {
            System.out.println(
            "This loyalty account is inactive.");
            return;
        }

        System.out.println();
        System.out.println("Member found:");
        System.out.println(
        "Name            : "
        + account.getMemberName());
        System.out.println(
        "Current Points  : "
        + account.getPointsBalance());
        System.out.println(
        "Current Tier    : "
        + account.getMembershipTier());

        System.out.print(
        "\nEnter completed Booking ID: ");

        String bookingId =
        scanner.nextLine().trim();

        if (bookingId.isEmpty()) {
            System.out.println(
            "Booking ID cannot be empty.");
            return;
        }

        Booking booking =
        loyaltyControl.findBookingByConfirmationNumber(
        bookingId);

        if (booking == null) {
            System.out.println(
            "Booking not found.");
            return;
        }

        // Booking must contain valid Guest information
        if (booking.getGuest() == null
        || booking.getGuest().getGuestId() == null) {

            System.out.println(
            "Booking does not contain valid Guest information.");
            return;
        }

        // Booking must belong to the Loyalty member
        if (account.getGuestId() == null
        || !account.getGuestId()
        .equalsIgnoreCase(
        booking.getGuest().getGuestId())) {

            System.out.println(
            "This booking belongs to a different Guest.");
            return;
        }

        // Booking must already be checked out
        if (booking.getStatus() == null
        || !booking.getStatus()
        .equalsIgnoreCase("CHECKED_OUT")) {

            System.out.println(
            "Loyalty points can only be awarded "
            + "after the booking is CHECKED_OUT.");
            return;
        }

        // Booking must already be paid
        if (booking.getPaymentStatus() == null
        || !booking.getPaymentStatus()
        .equalsIgnoreCase("PAID")) {

            System.out.println(
            "Loyalty points cannot be awarded "
            + "because this booking is not PAID.");
            return;
        }

        int points =
        loyaltyControl.calculateRewardPoints(
        booking);

        System.out.println();
        System.out.println("Booking found:");
        System.out.println(
        "Confirmation No : "
        + booking.getConfirmationNumber());

        System.out.println(
        "Guest ID        : "
        + booking.getGuest().getGuestId());

        System.out.println(
        "Status          : "
        + booking.getStatus());

        System.out.println(
        "Payment Status  : "
        + booking.getPaymentStatus());

        double roomDiscountRate =
        loyaltyControl.getRoomDiscountRate(
                account.getMembershipTier());

double discountAmount =
        loyaltyControl.calculateRoomDiscountAmount(
                booking);

double payableAmount =
        loyaltyControl.calculatePayableAmount(
                booking);

System.out.printf(
        "Original Amount : RM %.2f%n",
        booking.getAmount());

System.out.printf(
        "Room Discount   : %.0f%%%n",
        roomDiscountRate * 100);

System.out.printf(
        "Discount Amount : RM %.2f%n",
        discountAmount);

System.out.printf(
        "Payable Amount  : RM %.2f%n",
        payableAmount);

System.out.println(
        "Points Earned   : "
        + points);

        if (loyaltyControl.hasBookingReceivedPoints(
        bookingId)) {

            System.out.println(
            "This booking has already received loyalty points.");
            return;
        }

        int previousPoints =
        account.getPointsBalance();

        String previousTier =
        account.getMembershipTier().toString();

        System.out.print(
        "Confirm adding "
        + points
        + " points? (Y/N): ");

        String confirmation =
        scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println(
            "Add-points operation cancelled.");
            return;
        }

        boolean added =
        loyaltyControl.addPointsFromCompletedStay(
        loyaltyId,
        bookingId);

        if (!added) {
            System.out.println(
            "Unable to add loyalty points.");
            return;
        }

        System.out.println();
        System.out.println(
        "Points added successfully.");
        System.out.println(
        "Previous Points : " + previousPoints);
        System.out.println(
        "Points Added    : " + points);
        System.out.println(
        "New Balance     : "
        + account.getPointsBalance());
        System.out.println(
        "Previous Tier   : " + previousTier);
        System.out.println(
        "Current Tier    : "
        + account.getMembershipTier());

        LoyaltyTransaction newEarnBatch =
                loyaltyControl.findEarnTransactionByBookingId(
                        bookingId);

        if (newEarnBatch != null) {
            System.out.println(
                    "EARN Batch ID   : "
                    + newEarnBatch.getTransactionId());
            System.out.println(
                    "Earned Time     : "
                    + formatDateTime(
                            newEarnBatch.getTransactionTime()));
            System.out.println(
                    "Expiry Time     : "
                    + formatDateTime(
                            newEarnBatch.getExpiryTime()));
        }
    }


    /**
     * Allows the user to filter loyalty accounts by tier,
     * account status, and minimum points.
     */
    private void displayTierAndPointsReport() {

        System.out.println();
        System.out.println(
        "+------------------------------------------------+");
        System.out.println(
        "|             TIER AND POINTS REPORT              |");
        System.out.println(
        "+------------------------------------------------+");

        System.out.println("Membership Tier:");
        System.out.println("0. All Tiers");
        System.out.println("1. NONE");
        System.out.println("2. SILVER");
        System.out.println("3. GOLD");
        System.out.println("4. PLATINUM");
        System.out.println("5. DIAMOND");
        System.out.println("6. ELITE");
        System.out.print("Enter tier option: ");

        int tierOption = readChoice();

        MembershipTier selectedTier;

        switch (tierOption) {
            case 0 ->
            selectedTier = null;

            case 1 ->
            selectedTier = MembershipTier.NONE;

            case 2 ->
            selectedTier = MembershipTier.SILVER;

            case 3 ->
            selectedTier = MembershipTier.GOLD;

            case 4 ->
            selectedTier = MembershipTier.PLATINUM;

            case 5 ->
            selectedTier = MembershipTier.DIAMOND;

            case 6 ->
            selectedTier = MembershipTier.ELITE;

            default -> {
                System.out.println(
                "Invalid membership tier option.");
                return;
            }
        }

        System.out.println();
        System.out.println("Account Status:");
        System.out.println("0. All Accounts");
        System.out.println("1. Active Accounts");
        System.out.println("2. Inactive Accounts");
        System.out.print("Enter status option: ");

        int statusOption = readChoice();

        if (statusOption < 0 || statusOption > 2) {
            System.out.println(
            "Invalid account status option.");
            return;
        }

        System.out.print("Enter minimum points: ");

        int minimumPoints;

        try {
            minimumPoints = Integer.parseInt(
            scanner.nextLine().trim());

        } catch (NumberFormatException e) {
            System.out.println(
            "Invalid points. Please enter a number.");
            return;
        }

        if (minimumPoints < 0) {
            System.out.println(
            "Minimum points cannot be negative.");
            return;
        }

        ListQueueInterface<LoyaltyAccount> filteredAccounts =
        loyaltyControl.filterMembers(
        selectedTier,
        statusOption,
        minimumPoints
        );

        displayAccountList(filteredAccounts);
    }

    /**
     * Allows staff to activate or deactivate a loyalty account
     * without permanently deleting its history.
     */
    private void updateLoyaltyAccountStatus() {

        displayAllLoyaltyAccounts();

        System.out.println();
        System.out.println(
        "+------------------------------------------------+");
        System.out.println(
        "|        ACTIVATE / DEACTIVATE ACCOUNT           |");
        System.out.println(
        "+------------------------------------------------+");

        System.out.print("Enter Loyalty ID: ");

        String loyaltyId =
        scanner.nextLine().trim();

        if (loyaltyId.isEmpty()) {
            System.out.println(
            "Loyalty ID cannot be empty.");
            return;
        }

        LoyaltyAccount account =
        loyaltyControl.findMemberByLoyaltyId(
        loyaltyId);

        if (account == null) {
            System.out.println(
            "Loyalty member not found.");
            return;
        }

        displayAccountDetails(account);

        System.out.println();
        System.out.println("1. Activate Account");
        System.out.println("2. Deactivate Account");
        System.out.print("Enter option: ");

        int option = readChoice();

        boolean newStatus;

        switch (option) {

            case 1 ->
            newStatus = true;

            case 2 ->
            newStatus = false;

            default -> {
                System.out.println(
                "Invalid option.");
                return;
            }
        }

        if (account.isActive() == newStatus) {

            if (newStatus) {
                System.out.println(
                "This account is already active.");
            } else {
                System.out.println(
                "This account is already inactive.");
            }

            return;
        }

        System.out.print(
        "Confirm status change? (Y/N): ");

        String confirmation =
        scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println(
            "Status update cancelled.");
            return;
        }

        boolean updated =
        loyaltyControl.updateAccountStatus(
        loyaltyId,
        newStatus);

        if (!updated) {
            System.out.println(
            "Unable to update account status.");
            return;
        }

        System.out.println();
        System.out.println(
        "Account status updated successfully.");

        displayAccountDetails(account);
    }

    private void displayAccountList(
        ListQueueInterface<LoyaltyAccount> accounts) {

    System.out.println();

    if (accounts == null || accounts.isEmpty()) {
        System.out.println(
                "No loyalty members match the selected filters.");
        return;
    }

    String border =
            "+----------+----------+--------------------------+------------+------------+------------+----------+";

    System.out.println(
            "+--------------------------------------------------------------------------------------------------+");
    System.out.println(
            "|                                      LOYALTY MEMBER LIST                                     |");
    System.out.println(
            "+--------------------------------------------------------------------------------------------------+");

    System.out.println(border);

    System.out.printf(
            "| %-8s | %-8s | %-24s | %10s | %10s | %-10s | %-8s |%n",
            "Loyalty",
            "Guest",
            "Member Name",
            "Available",
            "Tier Pts",
            "Tier",
            "Status");

    System.out.println(border);

    Iterator<LoyaltyAccount> accountIterator =
            accounts.getIterator();

    while (accountIterator.hasNext()) {

        LoyaltyAccount account =
                accountIterator.next();

        System.out.printf(
                "| %-8s | %-8s | %-24s | %10d | %10d | %-10s | %-8s |%n",
                account.getLoyaltyId(),
                account.getGuestId(),
                account.getMemberName(),
                account.getPointsBalance(),
                account.getTierQualifyingPoints(),
                account.getMembershipTier(),
                account.isActive()
                        ? "ACTIVE"
                        : "INACTIVE");
    }

    System.out.println(border);

    System.out.printf(
            "Total matching members: %d%n",
            accounts.getNumberOfEntries());
}

    /**
     * Accepts expiry-report filters and displays matching Loyalty accounts.
     */
    private void displayExpiringPointsReport() {

        System.out.println();
        System.out.println(
        "+------------------------------------------------+");
        System.out.println(
        "|            EXPIRING POINTS REPORT              |");
        System.out.println(
        "+------------------------------------------------+");

        LocalDateTime startTime =
        LocalDateTime.now();

        LocalDateTime endTime =
        startTime.plusMinutes(4);

        System.out.println(
        "Checking from : " + startTime.format(TIME_FORMATTER));

        System.out.println(
        "Checking until: " + endTime.format(TIME_FORMATTER));

        System.out.println();
        System.out.println("Membership Tier:");
        System.out.println("0. All Tiers");
        System.out.println("1. NONE");
        System.out.println("2. SILVER");
        System.out.println("3. GOLD");
        System.out.println("4. PLATINUM");
        System.out.println("5. DIAMOND");
        System.out.println("6. ELITE");
        System.out.print("Enter tier option: ");

        int tierOption =
        readChoice();

        MembershipTier selectedTier;

        switch (tierOption) {

            case 0 ->
            selectedTier = null;

            case 1 ->
            selectedTier = MembershipTier.NONE;

            case 2 ->
            selectedTier = MembershipTier.SILVER;

            case 3 ->
            selectedTier = MembershipTier.GOLD;

            case 4 ->
            selectedTier = MembershipTier.PLATINUM;

            case 5 ->
            selectedTier = MembershipTier.DIAMOND;

            case 6 ->
            selectedTier = MembershipTier.ELITE;

            default -> {
                System.out.println(
                "Invalid membership tier option.");
                return;
            }
        }

        ListQueueInterface<LoyaltyTransaction> report =
        loyaltyControl.generateExpiringPointsReport(
        startTime,
        endTime,
        selectedTier
        );

        displayExpiringTransactionList(
        report);
    }

    /**
     * Displays individual point batches expiring within four minutes.
     */
    private void displayExpiringPointsAlerts() {

        LocalDateTime currentTime =
        LocalDateTime.now();

        int alertPeriodMinutes = 4;

        ListQueueInterface<LoyaltyTransaction> alerts =
        loyaltyControl.generateExpiringPointsAlerts(
        currentTime,
        alertPeriodMinutes
        );

        System.out.println();
        System.out.println(
        "+------------------------------------------------+");
        System.out.println(
        "|          EXPIRING POINTS NOTIFICATIONS         |");
        System.out.println(
        "+------------------------------------------------+");

        System.out.println(
        "Checking period: "
        + currentTime.format(TIME_FORMATTER)
        + " to "
        + currentTime.plusMinutes(
        alertPeriodMinutes).format(TIME_FORMATTER));

        if (alerts == null || alerts.isEmpty()) {

            System.out.println();
            System.out.println(
            "No points will expire within the next "
            + alertPeriodMinutes
            + " minutes.");

            return;
        }

        System.out.println();

        Iterator<LoyaltyTransaction> alertIterator =
        alerts.getIterator();

        while (alertIterator.hasNext()) {

            LoyaltyTransaction transaction =
            alertIterator.next();

            LoyaltyAccount account =
            loyaltyControl.findMemberByLoyaltyId(
                    transaction.getLoyaltyId());

            if (account == null) {
                continue;
            }

            System.out.println(
            "Batch ID     : "
            + transaction.getTransactionId());

            System.out.println(
            "Loyalty ID   : "
            + account.getLoyaltyId());

            System.out.println(
            "Guest ID     : "
            + account.getGuestId());

            System.out.println(
            "Member Name  : "
            + account.getMemberName());

            System.out.println(
            "Batch Type   : "
            + transaction.getTransactionType());

            System.out.println(
            "Expiring Pts : "
            + transaction.getRemainingPoints());

            System.out.println(
            "Tier         : "
            + account.getMembershipTier());

            System.out.println(
            "Expiry Time  : "
            + formatDateTime(
                    transaction.getExpiryTime()));

            System.out.println(
            "------------------------------------------------");
        }
    }

    /**
     * Displays the Expiring Points Report.
     *
     * @param transactions custom ADT containing expiring point batches
     */
    private void displayExpiringTransactionList(
    ListQueueInterface<LoyaltyTransaction> transactions) {

        System.out.println();

        if (transactions == null
        || transactions.isEmpty()) {

            System.out.println(
            "No expiring points match the selected filters.");
            return;
        }

       String border =
        "+----------+------------+----------+--------------------------+----------+------------+------------+---------------------+";

System.out.println(border);

System.out.printf(
        "| %-8s | %-10s | %-8s | %-24s | %-8s | %10s | %-10s | %-19s |%n",
        "Batch ID",
        "Loyalty ID",
        "Guest ID",
        "Member Name",
        "Type",
        "Points",
        "Tier",
        "Expiry Time");

System.out.println(border);

Iterator<LoyaltyTransaction> transactionIterator =
        transactions.getIterator();

while (transactionIterator.hasNext()) {

    LoyaltyTransaction transaction =
            transactionIterator.next();

    LoyaltyAccount account =
            loyaltyControl.findMemberByLoyaltyId(
                    transaction.getLoyaltyId());

    if (account == null) {
        continue;
    }

    System.out.printf(
            "| %-8s | %-10s | %-8s | %-24.24s | %-8s | %10d | %-10s | %-19s |%n",
            transaction.getTransactionId(),
            transaction.getLoyaltyId(),
            account.getGuestId(),
            account.getMemberName(),
            transaction.getTransactionType(),
            transaction.getRemainingPoints(),
            account.getMembershipTier(),
            formatDateTime(
                    transaction.getExpiryTime()));
}

System.out.println(border);

System.out.println(
        "Total expiring point batches: "
        + transactions.getNumberOfEntries());
    }

    /**
     * Formats an account activity or expiry timestamp for display.
     */
    private String formatDateTime(LocalDateTime dateTime) {

        return dateTime == null
                ? "-"
                : dateTime.format(DATE_TIME_FORMATTER);
    }

    /** Displays a member's complete point ledger, not an expiry report. */
    private void displayPointTransactionHistory() {

        System.out.println();
        System.out.println(
                "+------------------------------------------------+");
        System.out.println(
                "|           POINT TRANSACTION HISTORY            |");
        System.out.println(
                "+------------------------------------------------+");
        System.out.print("Enter Loyalty ID: ");
        String loyaltyId = scanner.nextLine().trim();
        LoyaltyAccount account =
                loyaltyControl.findMemberByLoyaltyId(loyaltyId);

        if (account == null) {
            System.out.println("Loyalty member not found.");
            return;
        }

        displayAccountDetails(account);

        ListQueueInterface<LoyaltyTransaction> transactions =
                loyaltyControl.getLoyaltyTransactions().filter(
                        transaction -> transaction != null
                        && transaction.getLoyaltyId() != null
                        && transaction.getLoyaltyId()
                                .equalsIgnoreCase(loyaltyId));

        if (transactions.isEmpty()) {
            System.out.println();
            System.out.println("No point transactions for this member.");
            return;
        }

        String border =
                "+----------+------------+----------+----------+------------+---------------------+---------------------+";
        System.out.println();
        System.out.println(border);
        System.out.printf(
                "| %-8s | %-10s | %-8s | %8s | %10s | %-19s | %-19s |%n",
                "Txn ID", "Booking", "Type", "Points", "Remaining",
                "Transaction Time", "Expiry Time");
        System.out.println(border);

        Iterator<LoyaltyTransaction> iterator =
                transactions.getIterator();

        while (iterator.hasNext()) {
            LoyaltyTransaction transaction = iterator.next();
            System.out.printf(
                    "| %-8s | %-10s | %-8s | %8d | %10d | %-19s | %-19s |%n",
                    transaction.getTransactionId(),
                    transaction.getBookingId() == null
                            ? "-" : transaction.getBookingId(),
                    transaction.getTransactionType(),
                    transaction.getPoints(),
                    transaction.getRemainingPoints(),
                    formatDateTime(transaction.getTransactionTime()),
                    formatDateTime(transaction.getExpiryTime()));
        }

        System.out.println(border);
        System.out.println("Total transactions: "
                + transactions.getNumberOfEntries());
    }
    
    /**
     * Displays the details of one loyalty account.
     *
     * @param account loyalty account to display
     */
    private void displayAccountDetails(
    LoyaltyAccount account) {

        String outerBorder =
                "+-----------------------------------------------------------+";

        String columnBorder =
                "+--------------------+--------------------------------------+";

        String title = "LOYALTY MEMBER DETAILS";
        int leftPadding = (57 - title.length()) / 2;

        System.out.println();
        System.out.println(outerBorder);
        System.out.printf(
                "| %-57s |%n",
                " ".repeat(leftPadding) + title);
        System.out.println(columnBorder);

        printAccountDetailRow(
                "Loyalty ID",
                account.getLoyaltyId());

        printAccountDetailRow(
                "Guest ID",
                account.getGuestId());

        printAccountDetailRow(
                "Member Name",
                account.getMemberName());

        printAccountDetailRow(
                "Points Balance",
                account.getPointsBalance());

        printAccountDetailRow(
                "Tier Points",
                account.getTierQualifyingPoints());

        printAccountDetailRow(
                "Membership Tier",
                account.getMembershipTier());
        
        String roomDiscount =
        String.format(
                "%.0f%%",
                loyaltyControl.getRoomDiscountPercentage(
                        account.getMembershipTier()));

        printAccountDetailRow(
                "Room Discount",
                roomDiscount);

        printAccountDetailRow(
                "Account Status",
                account.isActive()
                        ? "ACTIVE"
                        : "INACTIVE");

        if (!account.isActive()
                && account.getDeactivatedAt() != null) {

            printAccountDetailRow(
                    "Deactivated At",
                    formatDateTime(
                            account.getDeactivatedAt()));

            printAccountDetailRow(
                    "Tier Becomes NONE",
                    formatDateTime(
                            account.getDeactivatedAt()
                                    .plusMinutes(10)));
        }

        System.out.println(columnBorder);
    }

    /**
     * Prints one consistently aligned row in the member-details table.
     */
    private void printAccountDetailRow(
            String label,
            Object value) {

        System.out.printf(
                "| %-18s | %-36s |%n",
                label,
                value == null ? "-" : value);
    }
    /**
     * Prints the Loyalty and Rewards menu.
     */
    private void displayMenu() {
        System.out.println();
        System.out.println(
        "+------------------------------------------------+");
        System.out.println(
        "|          LOYALTY & REWARDS SERVICE             |");
        System.out.println(
        "+------------------------------------------------+");

        System.out.printf("| %-46s |%n", "1. Find Loyalty Member");
        System.out.printf("| %-46s |%n", "2. Create Loyalty Account");
        System.out.printf("| %-46s |%n", "3. Add Points from Completed Stay");
        System.out.printf("| %-46s |%n", "4. Redeem Rewards");
        System.out.printf("| %-46s |%n", "5. Tier and Points Report");
        System.out.printf("| %-46s |%n", "6. Activate / Deactivate Account");
        System.out.printf("| %-46s |%n", "7. Expiring Points Report");
        System.out.printf("| %-46s |%n", "8. Expiring Points Notifications");
        System.out.printf("| %-46s |%n", "9. Point Transaction History");
        System.out.printf("| %-46s |%n", "0. Return to Main Menu");

        System.out.println(
        "+------------------------------------------------+");
        System.out.print("Enter choice: ");
    }

    /**
     * Reads and validates a numerical menu choice.
     *
     * @return entered menu choice, or -1 for invalid input
     */
    private int readChoice() {

        try {
            return Integer.parseInt(
            scanner.nextLine().trim());

        } catch (NumberFormatException e) {

            System.out.println(
            "Invalid input. Please enter a number.");

            return -1;
        }
    }

}
