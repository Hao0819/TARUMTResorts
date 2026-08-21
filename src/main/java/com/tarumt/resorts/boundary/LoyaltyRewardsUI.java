/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.boundary;

import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.control.LoyaltyRewardsControl;
import com.tarumt.resorts.control.LoyaltyRewardsControl.LoyaltyPerformanceReport;
import com.tarumt.resorts.control.LoyaltyRewardsControl.RewardPerformance;
import com.tarumt.resorts.control.LoyaltyRewardsControl.TierPerformance;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.MembershipTier;
import com.tarumt.resorts.entity.RewardPackage;
import com.tarumt.resorts.entity.RedemptionRequest;
import com.tarumt.resorts.util.LoyaltyClock;

import java.util.Iterator;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Boundary class for the Loyalty and Rewards Service.
 * Handles user input/output only and delegates business logic to
 * {@link LoyaltyRewardsControl}.
 * @author Gary Khor Wei Qi 
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
            // Scan shared completed bookings silently before showing the menu.
            loyaltyControl.processCompletedBookingsForLoyalty();

            LocalDateTime currentTime =
            LoyaltyClock.now();

            int automaticallyExpired =
            loyaltyControl.processExpiredPoints(
            currentTime);

            int automaticallyDeactivated =
            loyaltyControl.processInactiveAccountTierExpirations(
            currentTime);

            if (automaticallyExpired > 0) {
                System.out.println();
                System.out.println(
                "Automatic expiry processed: "
                + automaticallyExpired
                + " points expired.");
            }

            if (automaticallyDeactivated > 0) {
                System.out.println();
                System.out.println(
                "Automatic inactivity processed: "
                + automaticallyDeactivated
                + " account(s) deactivated with Tier NONE.");
            }

            displayMenu();
            choice = readChoice();

            switch (choice) {
                case 1 -> findLoyaltyMember();
                case 2 -> viewCompletedStayPointProcessing();
                case 3 -> redemptionManagement();
                case 4 -> displayTierAndPointsReport();
                case 5 -> displayLoyaltyPerformanceReport();
                case 6 -> displayExpiringPointsAlerts();
                case 7 -> displayPointTransactionHistory();
                case 8 -> displayDemoLoyaltyTimeMenu();
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

    ListQueueInterface<LoyaltyAccount> accounts = // ADT collection declaration
            loyaltyControl.getLoyaltyAccounts();

    displayAccountList(accounts);
}
    
    /**
     * Explicitly refreshes Loyalty from the shared Booking collection.
     * Front-Desk does not call or hold a reference to this boundary.
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

    displayAllLoyaltyAccounts();

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

System.out.print("Enter reward option: ");
int selectedOption = readChoice();

    if (selectedOption == 0) {
        System.out.println(
                "Redemption request cancelled.");
        return;
    }

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

    ListQueueInterface<RedemptionRequest> requests = // ADT collection declaration
            loyaltyControl.getPendingRedemptionRequests();

    if (requests == null || requests.isEmpty()) { // ADT method call: isEmpty()
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
            requests.getIterator(); // ADT method call: getIterator()

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
            + requests.getNumberOfEntries()); // ADT method call: getNumberOfEntries()

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

    ListQueueInterface<RedemptionRequest> requests = // ADT collection declaration
            loyaltyControl.getPendingRedemptionRequests();

    if (requests == null || requests.isEmpty()) { // ADT method call: isEmpty()
        System.out.println(
                "No pending redemption requests.");
        return;
    }

    RedemptionRequest nextRequest =
            requests.peek(); // ADT method call: peek()

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

    ListQueueInterface<RedemptionRequest> requests = // ADT collection declaration
            loyaltyControl.getPendingRedemptionRequests();

    if (requests == null || requests.isEmpty()) { // ADT method call: isEmpty()
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

    /** Displays completed-stay Loyalty processing without awarding points. */
    private void viewCompletedStayPointProcessing() {

        String outerBorder =
                "+-----------------------------------------------------------+";
        String columnBorder =
                "+----------------------+------------------------------------+";
        String title = "VIEW COMPLETED-STAY POINT PROCESSING";
        int leftPadding = (57 - title.length()) / 2;

        System.out.println();
        System.out.println(outerBorder);
        System.out.printf(
                "| %-57s |%n",
                " ".repeat(leftPadding) + title);
        System.out.println(outerBorder);
        displayCompletedStayBookingList();
        System.out.print("Enter Booking confirmation number: ");
        String bookingId = scanner.nextLine().trim();

        if (bookingId.isEmpty()) {
            System.out.println("Booking confirmation number cannot be empty.");
            return;
        }

        Booking booking =
                loyaltyControl.findBookingByConfirmationNumber(bookingId);

        if (booking == null) {
            System.out.println("Booking not found.");
            return;
        }

        Guest guest = booking.getGuest();
        LoyaltyAccount account = guest == null
                || guest.getGuestId() == null
                        ? null
                        : loyaltyControl.findMemberByGuestId(
                                guest.getGuestId());
        LoyaltyTransaction earnBatch =
                loyaltyControl.findEarnTransactionByBookingId(bookingId);

        System.out.println();
        System.out.println(columnBorder);
        printProcessingDetailRow(
                "Booking Confirmation",
                booking.getConfirmationNumber());
        printProcessingDetailRow(
                "Guest ID",
                guest == null ? "-" : guest.getGuestId());
        printProcessingDetailRow(
                "Guest Name",
                guest == null ? "-" : guest.getName());
        printProcessingDetailRow(
                "Loyalty ID",
                account == null ? "-" : account.getLoyaltyId());
        printProcessingDetailRow(
                "Booking Status",
                booking.getStatus());
        printProcessingDetailRow(
                "Payment Status",
                booking.getPaymentStatus());
        printProcessingDetailRow(
                "Original Amount",
                String.format("RM %.2f", booking.getAmount()));

        if (guest == null || guest.getGuestId() == null) {
            System.out.println(columnBorder);
            System.out.println(
                    "Not eligible: the Booking has no valid Guest.");
            return;
        }

        if (booking.getStatus() == null
                || !booking.getStatus().equalsIgnoreCase("CHECKED_OUT")) {
            System.out.println(columnBorder);
            System.out.println(
                    "Not eligible: Booking status must be CHECKED_OUT.");
            return;
        }

        if (booking.getPaymentStatus() == null
                || !booking.getPaymentStatus().equalsIgnoreCase("PAID")) {
            System.out.println(columnBorder);
            System.out.println(
                    "Not eligible: payment status must be PAID.");
            return;
        }

        if (earnBatch == null) {
            System.out.println(columnBorder);
            System.out.println();
            System.out.println(
                    "No Loyalty points have been processed for this booking.");
            return;
        }

        printProcessingDetailRow(
                "Points Earned",
                earnBatch.getPoints());
        printProcessingDetailRow(
                "EARN Batch ID",
                earnBatch.getTransactionId());
        printProcessingDetailRow(
                "Transaction Time",
                formatDateTime(earnBatch.getTransactionTime()));
        printProcessingDetailRow(
                "Expiry Time",
                formatDateTime(earnBatch.getExpiryTime()));
        printProcessingDetailRow(
                "Current Balance",
                account == null ? "-" : account.getPointsBalance());
        printProcessingDetailRow(
                "Current Tier",
                account == null ? "-" : account.getMembershipTier());
        System.out.println(columnBorder);
    }

    /** Displays completed Booking confirmation numbers before lookup input. */
    private void displayCompletedStayBookingList() {

        ListQueueInterface<Booking> completedBookings = // ADT collection declaration
                loyaltyControl.getCompletedStayBookingsForDisplay();

        System.out.println();
        System.out.println("COMPLETED-STAY BOOKING LIST");

        if (completedBookings == null || completedBookings.isEmpty()) { // ADT method call: isEmpty()
            System.out.println("No completed-stay bookings available.");
            System.out.println();
            return;
        }

        String border =
                "+------------+----------+------------------------+-------------+---------+----------+--------------+";
        System.out.println(border);
        System.out.printf(
                "| %-10s | %-8s | %-22s | %-11s | %-7s | %8s | %-12s |%n",
                "Booking", "Guest ID", "Guest Name", "Status",
                "Payment", "Amount", "Point Status");
        System.out.println(border);

        Iterator<Booking> iterator = completedBookings.getIterator(); // ADT method call: getIterator()

        while (iterator.hasNext()) {
            Booking booking = iterator.next();
            Guest guest = booking.getGuest();
            boolean processed =
                    loyaltyControl.findEarnTransactionByBookingId(
                            booking.getConfirmationNumber()) != null;

            System.out.printf(
                    "| %-10.10s | %-8.8s | %-22.22s | %-11.11s | %-7.7s | %8.2f | %-12.12s |%n",
                    booking.getConfirmationNumber(),
                    guest == null ? "-" : guest.getGuestId(),
                    guest == null ? "-" : guest.getName(),
                    booking.getStatus(),
                    booking.getPaymentStatus(),
                    booking.getAmount(),
                    processed ? "PROCESSED" : "PENDING");
        }

        System.out.println(border);
        System.out.println("Total completed stays: "
                + completedBookings.getNumberOfEntries()); // ADT method call: getNumberOfEntries()
        System.out.println();
    }

    /** Prints one aligned label/value row for completed-stay processing. */
    private void printProcessingDetailRow(
            String label,
            Object value) {

        System.out.printf(
                "| %-20.20s | %-34.34s |%n",
                label,
                value == null ? "-" : value);
    }


    /**
     * Allows the user to filter loyalty accounts by tier and account status.
     */
    private void displayTierAndPointsReport() {

        String border =
                "+------------------------------------------------+";
        String title = "TIER AND POINTS REPORT";
        int leftPadding = (46 - title.length()) / 2;

        System.out.println();
        System.out.println(border);
        System.out.printf(
                "| %-46s |%n",
                " ".repeat(leftPadding) + title);
        System.out.println(border);

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

        ListQueueInterface<LoyaltyAccount> filteredAccounts = // ADT collection declaration
        loyaltyControl.filterMembers(
        selectedTier,
        statusOption,
        0
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
        ListQueueInterface<LoyaltyAccount> accounts) { // ADT collection declaration

    System.out.println();

    if (accounts == null || accounts.isEmpty()) { // ADT method call: isEmpty()
        System.out.println(
                "No loyalty members match the selected filters.");
        return;
    }

    String border =
            "+------------+------------+--------------------------+------------+------------+------------+----------+";

    String titleBorder =
            "+" + "-".repeat(border.length() - 2) + "+";

    String title = "LOYALTY MEMBER LIST";
    int titleWidth = border.length() - 4;
    int titleLeftPadding =
            (titleWidth - title.length()) / 2;
    int titleRightPadding =
            titleWidth - title.length() - titleLeftPadding;

    System.out.println(titleBorder);
    System.out.println(
            "| "
            + " ".repeat(titleLeftPadding)
            + title
            + " ".repeat(titleRightPadding)
            + " |");
    System.out.println(border);

    System.out.printf(
            "| %-10s | %-10s | %-24s | %10s | %10s | %-10s | %-8s |%n",
            "Loyalty ID",
            "Guest ID",
            "Member Name",
            "Balance",
            "Tier Pts",
            "Tier",
            "Status");

    System.out.println(border);

    Iterator<LoyaltyAccount> accountIterator =
            accounts.getIterator(); // ADT method call: getIterator()

    while (accountIterator.hasNext()) {

        LoyaltyAccount account =
                accountIterator.next();

        System.out.printf(
                "| %-10.10s | %-10.10s | %-24.24s | %10d | %10d | %-10.10s | %-8.8s |%n",
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
            accounts.getNumberOfEntries()); // ADT method call: getNumberOfEntries()
}

    /** Displays management KPIs, tier results and reward popularity. */
    private void displayLoyaltyPerformanceReport() {

        LocalDateTime reportTime = LoyaltyClock.now();
        int expiryRiskDays = 7;
        LoyaltyPerformanceReport report =
                loyaltyControl.generateLoyaltyPerformanceReport(
                        reportTime, expiryRiskDays);

        System.out.println();
        System.out.println(
                "+----------------------------------------------------------------------+");
        System.out.println(
                "|                      LOYALTY PERFORMANCE REPORT                      |");
        System.out.println(
                "+----------------------------------------------------------------------+");

        printPerformanceSummaryRow(
                "Report Time", formatDateTime(reportTime));
        printPerformanceSummaryRow(
                "Total Members", report.getTotalMembers());
        printPerformanceSummaryRow(
                "Active Members", report.getActiveMembers());
        printPerformanceSummaryRow(
                "Points Earned", report.getEarnedPoints());
        printPerformanceSummaryRow(
                "Points Redeemed", report.getRedeemedPoints());
        printPerformanceSummaryRow(
                "Points Expired", report.getExpiredPoints());
        printPerformanceSummaryRow(
                "Current Point Liability",
                report.getAvailablePoints());
        printPerformanceSummaryRow(
                "Overall Redemption Rate",
                String.format("%.2f%%", report.getRedemptionRate()));
        printPerformanceSummaryRow(
                "Points Expiring in 7 Days",
                report.getExpiringSoonPoints());

        String highestTier = report.getHighestRedeemingTier() == null
                ? "-" : report.getHighestRedeemingTier().toString();
        printPerformanceSummaryRow(
                "Highest Redeeming Tier", highestTier);

        String topMember = report.getTopRedeemingLoyaltyId() == null
                ? "-"
                : report.getTopRedeemingLoyaltyId()
                + " - " + report.getTopRedeemingMember();
        printPerformanceSummaryRow("Top Redeeming Member", topMember);
        printPerformanceSummaryRow(
                "Top Member Redeemed Points",
                report.getTopMemberRedeemedPoints());

        String popularReward = report.getMostPopularReward() == null
                ? "No completed reward redemptions"
                : report.getMostPopularReward().getRewardName()
                + " (" + report.getMostPopularRewardCount() + " time(s))";
        printPerformanceSummaryRow("Most Popular Reward", popularReward);

        System.out.println(
                "+----------------------------------------------------------------------+");

        displayTierPerformanceTable(report.getTierRows());
        displayRewardPerformanceTable(report.getRewardRows());
    }

    /** Prints one aligned summary row in the performance report. */
    private void printPerformanceSummaryRow(String label, Object value) {
        System.out.printf(
                "| %-28s : %-37.37s |%n",
                label,
                value == null ? "-" : value);
    }

    /** Displays performance totals grouped by the member's current tier. */
    private void displayTierPerformanceTable(TierPerformance[] rows) {

        String border =
                "+----------+-----+---------+---------+---------+---------+---------+--------+";

        System.out.println();
        System.out.println("TIER PERFORMANCE");
        System.out.println(border);
        System.out.printf(
                "| %-8s | %3s | %7s | %7s | %7s | %7s | %7s | %6s |%n",
                "Tier", "Mem", "Balance", "Issued", "Redeem",
                "Expire", "Avg Bal", "Rate");
        System.out.println(border);

        for (TierPerformance row : rows) {
            System.out.printf(
                    "| %-8s | %3d | %7d | %7d | %7d | %7d | %7.1f | %5.1f%% |%n",
                    row.getTier(),
                    row.getMembers(),
                    row.getAvailablePoints(),
                    row.getIssuedPoints(),
                    row.getRedeemedPoints(),
                    row.getExpiredPoints(),
                    row.getAverageBalance(),
                    row.getRedemptionRate());
        }

        System.out.println(border);
        System.out.println(
                "Issued = EARN points. Rate = Redeemed / Issued.");
    }

    /** Displays popularity statistics from successfully processed rewards. */
    private void displayRewardPerformanceTable(RewardPerformance[] rows) {

        String border =
                "+--------------------------+-------------------+-------------+";

        System.out.println();
        System.out.println("REWARD POPULARITY");
        System.out.println(border);
        System.out.printf(
                "| %-24s | %17s | %11s |%n",
                "Reward", "Completed Redeems", "Points Used");
        System.out.println(border);

        for (RewardPerformance row : rows) {
            System.out.printf(
                    "| %-24s | %17d | %11d |%n",
                    row.getReward().getRewardName(),
                    row.getRedemptionCount(),
                    row.getPointsUsed());
        }

        System.out.println(border);
    }

    /** Displays individual point batches expiring within seven days. */
    private void displayExpiringPointsAlerts() {

        LocalDateTime currentTime =
                LoyaltyClock.now();
        int alertPeriodDays =
                loyaltyControl.getExpiryNotificationDays();

        ListQueueInterface<LoyaltyTransaction> alerts = // ADT collection declaration
                loyaltyControl.generateExpiringPointsReport(
                        currentTime,
                        currentTime.plusDays(alertPeriodDays),
                        null);

        System.out.println();
        System.out.println(
        "+------------------------------------------------+");
        System.out.println(
        "|          EXPIRING POINTS NOTIFICATIONS         |");
        System.out.println(
        "+------------------------------------------------+");

        System.out.println(
                "Checking period: "
                + currentTime.toLocalDate()
                + " to "
                + currentTime.plusDays(
                        alertPeriodDays).toLocalDate());

        if (alerts == null || alerts.isEmpty()) { // ADT method call: isEmpty()

            System.out.println();
            System.out.println(
                    "No point batches will expire within the next "
                    + alertPeriodDays
                    + " days.");

            return;
        }

        displayExpiringTransactionList(alerts);
    }

    /**
     * Displays the Expiring Points Report.
     *
     * @param transactions custom ADT containing expiring point batches
     */
    private void displayExpiringTransactionList(
    ListQueueInterface<LoyaltyTransaction> transactions) { // ADT collection declaration

        System.out.println();

        if (transactions == null
        || transactions.isEmpty()) { // ADT method call: isEmpty()

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
        transactions.getIterator(); // ADT method call: getIterator()

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
        + transactions.getNumberOfEntries()); // ADT method call: getNumberOfEntries()
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

        displayAllLoyaltyAccounts();

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

        ListQueueInterface<LoyaltyTransaction> transactions = // ADT collection declaration
                loyaltyControl.getLoyaltyTransactions().filter( // ADT method call: filter()
                        transaction -> transaction != null
                        && transaction.getLoyaltyId() != null
                        && transaction.getLoyaltyId()
                                .equalsIgnoreCase(loyaltyId));

        if (transactions.isEmpty()) { // ADT method call: isEmpty()
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
                transactions.getIterator(); // ADT method call: getIterator()

        while (iterator.hasNext()) {
            LoyaltyTransaction transaction = iterator.next();
            boolean pointBatch = transaction.getTransactionType()
                    == LoyaltyTransaction.TransactionType.EARN
                    || transaction.getTransactionType()
                    == LoyaltyTransaction.TransactionType.ADJUST;
            System.out.printf(
                    "| %-8s | %-10s | %-8s | %8d | %10d | %-19s | %-19s |%n",
                    transaction.getTransactionId(),
                    transaction.getBookingId() == null
                            ? "-" : transaction.getBookingId(),
                    transaction.getTransactionType(),
                    transaction.getPoints(),
                    pointBatch
                            ? transaction.getRemainingPoints()
                            : transaction.getHistoryRemainingPoints(),
                    formatDateTime(transaction.getTransactionTime()),
                    formatDateTime(transaction.getExpiryTime()));
        }

        System.out.println(border);
        System.out.println("Total transactions: "
                + transactions.getNumberOfEntries()); // ADT method call: getNumberOfEntries()
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
                "+----------------------+------------------------------------+";

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

        printAccountDetailRow(
                "Last Points Activity",
                formatDateTime(
                        account.getLastPointsActivityTime()));

        printAccountDetailRow(
                "Next Batch Expiry",
                formatDateTime(
                        account.getPointsExpiryTime()));

        if (!account.isActive()
                && account.getDeactivatedAt() != null) {

            printAccountDetailRow(
                    "Deactivated At",
                    formatDateTime(
                            account.getDeactivatedAt()));

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
                "| %-20.20s | %-34.34s |%n",
                label,
                value == null ? "-" : value);
    }

    /** Allows a presenter to change time for the Loyalty module only. */
    private void displayDemoLoyaltyTimeMenu() {

        int choice;

        do {
            System.out.println();
            System.out.println(
                    "+--------------------------------------------------------+");
            System.out.println(
                    "|                   DEMO LOYALTY DATE                    |");
            System.out.println(
                    "+--------------------------------------------------------+");
            System.out.printf(
                    "| %-20s : %-31s |%n",
                    "Current Loyalty Date",
                    LoyaltyClock.today());
            System.out.printf(
                    "| %-20s : %-31s |%n",
                    "Clock Mode",
                    LoyaltyClock.isDemoMode()
                            ? "DEMO" : "REAL SYSTEM TIME");
            System.out.println(
                    "+--------------------------------------------------------+");
            System.out.printf("| %-54s |%n", "1. Set Loyalty Date");
            System.out.printf("| %-54s |%n", "2. Reset to Real System Date");
            System.out.printf("| %-54s |%n", "0. Back to Loyalty Menu");
            System.out.println(
                    "+--------------------------------------------------------+");
            System.out.print("Enter choice: ");

            choice = readChoice();

            switch (choice) {
                case 1 -> setDemoLoyaltyDate();
                case 2 -> {
                    LoyaltyClock.resetToSystemTime();
                    System.out.println(
                            "Loyalty date reset to the real system date.");
                }
                case 0 -> {
                }
                default -> System.out.println(
                        "Invalid choice. Please try again.");
            }

        } while (choice != 0);
    }

    private void setDemoLoyaltyDate() {
        System.out.print(
                "Enter Loyalty date (yyyy-MM-dd): ");

        try {
            LocalDate targetDate = LocalDate.parse(
                    scanner.nextLine().trim());
            LoyaltyClock.setDate(targetDate);
            printChangedLoyaltyTime();
        } catch (DateTimeParseException exception) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
        }
    }

    private void printChangedLoyaltyTime() {
        System.out.println(
                "Loyalty date changed to "
                + LoyaltyClock.today()
                + ".");
        System.out.println(
                "Only Loyalty uses this demo time.");
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
        System.out.printf("| %-46s |%n", "2. View Completed-Stay Point Processing");
        System.out.printf("| %-46s |%n", "3. Redeem Rewards");
        System.out.printf("| %-46s |%n", "4. Tier and Points Report");
        System.out.printf("| %-46s |%n", "5. Loyalty Performance Report");
        System.out.printf("| %-46s |%n", "6. Expiring Points Notifications");
        System.out.printf("| %-46s |%n", "7. Point Transaction History");
        System.out.printf("| %-46s |%n", "8. Demo Loyalty Date");
        System.out.printf("| %-46s |%n", "0. Main Menu");

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
