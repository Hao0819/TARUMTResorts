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
import com.tarumt.resorts.entity.RedemptionRequest;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Boundary class for the Loyalty and Rewards Service.
 * Handles user input/output only and delegates business logic to
 * {@link LoyaltyRewardsControl}.
 */
public class LoyaltyRewardsUI {

    private LoyaltyRewardsControl loyaltyControl;
    private Scanner scanner;

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
        displayMenu();
        choice = readChoice();

        switch (choice) {

            case 1 -> findLoyaltyMember();

            case 2 -> createLoyaltyAccount();

            case 3 -> addPointsFromCompletedStay();

            case 4 -> submitRedemptionRequest();

            case 5 -> displayPendingRedemptionQueue();

            case 6 -> processNextRedemptionRequest();

            case 7 -> cancelPendingRedemptionRequest();

            case 8 -> filterLoyaltyMembers();

            case 9 -> updateLoyaltyAccountStatus();

            case 10 -> displayAllLoyaltyAccounts();

            case 11 -> displayExpiringPointsReport();

            case 12 -> displayExpiringPointsAlerts();

            case 13 -> processExpiredPoints();

            case 0 -> System.out.println(
                    "Returning to the main menu.");

            default -> System.out.println(
                    "Invalid choice. Please try again.");
        }

    } while (choice != 0);
}

    /**
    * Allows the user to find a loyalty member using either
    * the loyalty ID or guest ID.
    */
    private void findLoyaltyMember() {

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

        System.out.printf(
            "Booking Amount  : RM %.2f%n",
            booking.getAmount());

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
    }

    
    
    /**
    * Submits a loyalty-points redemption request
    * to the FIFO redemption queue.
    */
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

    System.out.print(
            "\nEnter points to redeem: ");

    int points;

    try {
        points = Integer.parseInt(
                scanner.nextLine().trim());

    } catch (NumberFormatException e) {
        System.out.println(
                "Invalid points. Please enter a number.");
        return;
    }

    if (points <= 0) {
        System.out.println(
                "Points must be greater than zero.");
        return;
    }

    if (points > account.getPointsBalance()) {
        System.out.println(
                "Insufficient loyalty points.");
        return;
    }

    System.out.print(
            "Confirm redemption request for "
            + points
            + " points? (Y/N): ");

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
                    points);

    if (request == null) {
        System.out.println(
                "Unable to submit redemption request.");
        return;
    }

    System.out.println();
    System.out.println(
            "Redemption request submitted successfully.");

    System.out.println(
            "Request ID : "
            + request.getRequestId());

    System.out.println(
            "Loyalty ID : "
            + request.getLoyaltyId());

    System.out.println(
            "Points     : "
            + request.getPoints());

    System.out.println(
            "Date       : "
            + request.getRequestDate());
    }
    
    /**
    * Displays all pending redemption requests
    * in FIFO order.
    */
    private void displayPendingRedemptionQueue() {

    System.out.println();
    System.out.println(
            "+------------------------------------------------------------+");
    System.out.println(
            "|              PENDING REDEMPTION QUEUE                      |");
    System.out.println(
            "+------------------------------------------------------------+");

    ListQueueInterface<RedemptionRequest> requests =
            loyaltyControl.getPendingRedemptionRequests();

    if (requests == null || requests.isEmpty()) {
        System.out.println(
                "No pending redemption requests.");
        return;
    }

    System.out.printf(
            "| %-8s | %-10s | %10s | %-12s |%n",
            "Request",
            "Loyalty ID",
            "Points",
            "Request Date");

    System.out.println(
            "+------------------------------------------------------------+");

    Iterator<RedemptionRequest> requestIterator =
            requests.getIterator();

    while (requestIterator.hasNext()) {

        RedemptionRequest request =
                requestIterator.next();

        System.out.printf(
                "| %-8s | %-10s | %10d | %-12s |%n",
                request.getRequestId(),
                request.getLoyaltyId(),
                request.getPoints(),
                request.getRequestDate());
    }

    System.out.println(
            "+------------------------------------------------------------+");

    System.out.println(
            "Total pending requests: "
            + requests.getNumberOfEntries());

    System.out.println(
            "Requests are processed in FIFO order.");
}
    
    /**
 * Processes the next redemption request
 * according to FIFO order.
 */
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
    
    /**
    * Cancels a selected pending redemption request.
    */
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

    // Show the queue before asking which request to cancel.
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
    * Allows the user to filter loyalty accounts by tier,
    * account status, and minimum points.
    */
    private void filterLoyaltyMembers() {

        System.out.println();
        System.out.println(
            "+------------------------------------------------+");
        System.out.println(
            "|            FILTER LOYALTY MEMBERS              |");
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

    /**
    * Displays all loyalty accounts currently stored
    * in the custom ADT collection.
    */
    private void displayAllLoyaltyAccounts() {

        System.out.println();
        System.out.println(
            "+------------------------------------------------+");
        System.out.println(
            "|           ALL LOYALTY ACCOUNTS                 |");
        System.out.println(
            "+------------------------------------------------+");

        ListQueueInterface<LoyaltyAccount> accounts =
        loyaltyControl.getLoyaltyAccounts();

        displayAccountList(accounts);
    }

    /**
    * Accepts expiry-report filters and displays matching
    * earned-points transactions.
    */
    private void displayExpiringPointsReport() {

        System.out.println();
        System.out.println(
            "+------------------------------------------------+");
        System.out.println(
            "|            EXPIRING POINTS REPORT              |");
        System.out.println(
            "+------------------------------------------------+");

        LocalDate startDate;
        LocalDate endDate;

        try {
            System.out.print(
                "Enter start date (YYYY-MM-DD): ");

            startDate = LocalDate.parse(
                scanner.nextLine().trim());

            System.out.print(
                "Enter end date (YYYY-MM-DD): ");

            endDate = LocalDate.parse(
                scanner.nextLine().trim());

        } catch (DateTimeParseException e) {
            System.out.println(
                "Invalid date. Use the format YYYY-MM-DD.");
            return;
        }

        if (endDate.isBefore(startDate)) {
            System.out.println(
                "End date cannot be before the start date.");
            return;
        }

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

        System.out.print(
            "Enter minimum expiring points: ");

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

        ListQueueInterface<LoyaltyTransaction> report =
        loyaltyControl.generateExpiringPointsReport(
            startDate,
            endDate,
            selectedTier,
            minimumPoints
        );

    // -------------------------------------------------------------------------
    // DISPLAY HELPERS
    // -------------------------------------------------------------------------

        displayExpiringTransactionList(report);
    }

    /**
    * Displays notifications for points expiring within
    * the next 30 days.
    */
    private void displayExpiringPointsAlerts() {

        LocalDate currentDate = LocalDate.now();
        int alertPeriodDays = 30;

        ListQueueInterface<LoyaltyTransaction> alerts =
        loyaltyControl.generateExpiringPointsAlerts(
            currentDate,
            alertPeriodDays
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
            + currentDate
            + " to "
            + currentDate.plusDays(alertPeriodDays));

        if (alerts == null || alerts.isEmpty()) {
            System.out.println();
            System.out.println(
                "No points will expire within the next "
                + alertPeriodDays
                + " days.");
            return;
        }

        System.out.println();

        Iterator<LoyaltyTransaction> alertIterator =
        alerts.getIterator();

        int notificationNumber = 1;

        while (alertIterator.hasNext()) {

            LoyaltyTransaction transaction =
            alertIterator.next();

            LoyaltyAccount account =
            loyaltyControl.findMemberByLoyaltyId(
                transaction.getLoyaltyId());

            if (account == null) {
                continue;
            }

            long daysRemaining =
            ChronoUnit.DAYS.between(
                currentDate,
                transaction.getExpiryDate());

            System.out.println(
                "Notification " + notificationNumber);

            System.out.println(
                "Member       : "
                + account.getMemberName());

            System.out.println(
                "Loyalty ID   : "
                + account.getLoyaltyId());

            System.out.println(
                "Points       : "
                + transaction.getRemainingPoints());

            System.out.println(
                "Expiry Date  : "
                + transaction.getExpiryDate());

            System.out.println(
                "Days Left    : "
                + daysRemaining);

            System.out.println(
                "Message      : You have "
                + transaction.getRemainingPoints()
                + " loyalty points expiring in "
                + daysRemaining
                + " day(s). Please redeem them before expiry.");

            System.out.println(
                "--------------------------------------------------");

            notificationNumber++;
        }

        System.out.println(
            "Total notifications: "
            + alerts.getNumberOfEntries());
    }

    /**
    * Displays the Expiring Points Report.
    *
    * @param transactions custom ADT containing expiring records
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

        System.out.println(
            "+------------------------------------------------------------------------------------------+");

        System.out.printf(
            "| %-8s | %-8s | %-18s | %10s | %-12s | %-10s |%n",
            "Trans ID",
            "Loyalty",
            "Member Name",
            "Remaining",
            "Expiry Date",
            "Tier"
        );

        System.out.println(
            "+------------------------------------------------------------------------------------------+");

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
                "| %-8s | %-8s | %-18s | %10d | %-12s | %-10s |%n",
                transaction.getTransactionId(),
                transaction.getLoyaltyId(),
                account.getMemberName(),
                transaction.getRemainingPoints(),
                transaction.getExpiryDate(),
                account.getMembershipTier()
            );
        }

        System.out.println(
            "+------------------------------------------------------------------------------------------+");

        System.out.println(
            "Total expiring records: "
            + transactions.getNumberOfEntries());
    }

    /**
    * Displays a collection of loyalty accounts.
    *
    * @param accounts custom ADT containing loyalty accounts
    */
    private void displayAccountList(
        ListQueueInterface<LoyaltyAccount> accounts) {

        System.out.println();

        if (accounts == null || accounts.isEmpty()) {
            System.out.println(
                "No loyalty members match the selected filters.");
            return;
        }

        System.out.println(
            "+-------------------------------------------------------------------------------+");
        System.out.printf(
            "| %-8s | %-8s | %-20s | %10s | %-10s | %-8s |%n",
            "Loyalty",
            "Guest",
            "Member Name",
            "Points",
            "Tier",
            "Status"
        );
        System.out.println(
            "+-------------------------------------------------------------------------------+");

        Iterator<LoyaltyAccount> accountIterator =
        accounts.getIterator();

        while (accountIterator.hasNext()) {

            LoyaltyAccount account =
            accountIterator.next();

            System.out.printf(
                "| %-8s | %-8s | %-20s | %10d | %-10s | %-8s |%n",
                account.getLoyaltyId(),
                account.getGuestId(),
                account.getMemberName(),
                account.getPointsBalance(),
                account.getMembershipTier(),
                account.isActive()
                ? "ACTIVE"
                : "INACTIVE"
            );
        }

        System.out.println(
            "+-------------------------------------------------------------------------------+");

        System.out.println(
            "Total matching members: "
            + accounts.getNumberOfEntries());
    }

    /**
    * Displays the details of one loyalty account.
    *
    * @param account loyalty account to display
    */
    private void displayAccountDetails(
        LoyaltyAccount account) {

        System.out.println();
        System.out.println(
            "+-------------------------------------------------+");
        System.out.println(
            "|             LOYALTY MEMBER DETAILS             |");
        System.out.println(
            "+-------------------------------------------------+");

        System.out.printf(
            "| %-18s : %-25s |%n",
            "Loyalty ID",
            account.getLoyaltyId());

        System.out.printf(
            "| %-18s : %-25s |%n",
            "Guest ID",
            account.getGuestId());

        System.out.printf(
            "| %-18s : %-25s |%n",
            "Member Name",
            account.getMemberName());

        System.out.printf(
            "| %-18s : %-25d |%n",
            "Points Balance",
            account.getPointsBalance());

        System.out.printf(
            "| %-18s : %-25s |%n",
            "Membership Tier",
            account.getMembershipTier());

        System.out.printf(
            "| %-18s : %-25s |%n",
            "Account Status",
            account.isActive()
            ? "ACTIVE"
            : "INACTIVE");

        System.out.println(
            "+-------------------------------------------------+");
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

    System.out.printf(
            "| %-46s |%n",
            "1. Find Loyalty Member");

    System.out.printf(
            "| %-46s |%n",
            "2. Create Loyalty Account");

    System.out.printf(
            "| %-46s |%n",
            "3. Add Points from Completed Stay");

    System.out.printf(
            "| %-46s |%n",
            "4. Submit Redemption Request");

    System.out.printf(
            "| %-46s |%n",
            "5. View Pending Redemption Queue");

    System.out.printf(
            "| %-46s |%n",
            "6. Process Next Redemption Request");

    System.out.printf(
            "| %-46s |%n",
            "7. Cancel Pending Redemption Request");

    System.out.printf(
            "| %-46s |%n",
            "8. Tier and Points Report");

    System.out.printf(
            "| %-46s |%n",
            "9. Activate / Deactivate Account");

    System.out.printf(
            "| %-46s |%n",
            "10. Display All Loyalty Accounts");

    System.out.printf(
            "| %-46s |%n",
            "11. Expiring Points Report");

    System.out.printf(
            "| %-46s |%n",
            "12. Expiring Points Notifications");

    System.out.printf(
            "| %-46s |%n",
            "13. Process Expired Points");

    System.out.printf(
            "| %-46s |%n",
            "0. Return to Main Menu");

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

    /**
    * Processes all points that have already expired.
    */
    private void processExpiredPoints() {

        System.out.println();
        System.out.println(
            "+------------------------------------------------+");
        System.out.println(
            "|             PROCESS EXPIRED POINTS             |");
        System.out.println(
            "+------------------------------------------------+");

        LocalDate currentDate = LocalDate.now();

        System.out.println(
            "Processing date: " + currentDate);

        System.out.print(
            "Confirm processing expired points? (Y/N): ");

        String confirmation =
        scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println(
                "Expired-points processing cancelled.");
            return;
        }

        int totalExpiredPoints =
        loyaltyControl.processExpiredPoints(
            currentDate);

        if (totalExpiredPoints == 0) {
            System.out.println(
                "No expired points were found.");
            return;
        }

        System.out.println();
        System.out.println(
            "Expired points processed successfully.");

        System.out.println(
            "Total points expired: "
            + totalExpiredPoints);
    }

}
