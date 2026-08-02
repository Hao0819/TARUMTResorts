/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.LoyaltyRewardsControl;
import java.util.Scanner;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.entity.MembershipTier;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import java.time.temporal.ChronoUnit;


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

                case 4 -> redeemPoints();

                case 5 -> updatePointsBalance();

                case 6 -> filterLoyaltyMembers();

                case 7 -> deleteLoyaltyMember();

                case 8 -> displayAllLoyaltyAccounts();
                
                case 9 -> displayExpiringPointsReport();
                
                case 10 -> displayExpiringPointsAlerts();
                
                case 11 -> processExpiredPoints();

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

    if (loyaltyControl.hasBookingReceivedPoints(
            bookingId)) {

        System.out.println(
                "This booking has already received loyalty points.");
        return;
    }

    System.out.print("Enter points earned: ");

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
                    bookingId,
                    points
            );

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
 * Allows a loyalty member to redeem available points.
 */
    private void redeemPoints() {

    System.out.println();
    System.out.println(
            "+------------------------------------------------+");
    System.out.println(
            "|                 REDEEM POINTS                  |");
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

    int previousPoints =
            account.getPointsBalance();

    String previousTier =
            account.getMembershipTier().toString();

    System.out.print(
            "Confirm redemption of "
            + points
            + " points? (Y/N): ");

    String confirmation =
            scanner.nextLine().trim();

    if (!confirmation.equalsIgnoreCase("Y")) {
        System.out.println(
                "Redemption cancelled.");
        return;
    }

    boolean redeemed =
            loyaltyControl.redeemPoints(
                    loyaltyId,
                    points);

    if (!redeemed) {
        System.out.println(
                "Unable to redeem points. The points may be expired or unavailable.");
        return;
    }

    System.out.println();
    System.out.println(
            "Points redeemed successfully.");

    System.out.println(
            "Previous Points : "
            + previousPoints);

    System.out.println(
            "Points Redeemed : "
            + points);

    System.out.println(
            "New Balance     : "
            + account.getPointsBalance());

    System.out.println(
            "Previous Tier   : "
            + previousTier);

    System.out.println(
            "Current Tier    : "
            + account.getMembershipTier());
}
    
    /**
 * Allows an administrator to correct a member's
 * loyalty points balance.
 */
    private void updatePointsBalance() {

    System.out.println();
    System.out.println(
            "+------------------------------------------------+");
    System.out.println(
            "|             UPDATE POINTS BALANCE              |");
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

    int previousPoints =
            account.getPointsBalance();

    String previousTier =
            account.getMembershipTier().toString();

    System.out.println();
    System.out.println("Member found:");
    System.out.println(
            "Name            : "
            + account.getMemberName());

    System.out.println(
            "Current Points  : "
            + previousPoints);

    System.out.println(
            "Current Tier    : "
            + previousTier);

    System.out.print(
            "\nEnter new points balance: ");

    int newPointsBalance;

    try {
        newPointsBalance = Integer.parseInt(
                scanner.nextLine().trim());

    } catch (NumberFormatException e) {
        System.out.println(
                "Invalid points. Please enter a number.");
        return;
    }

    if (newPointsBalance < 0) {
        System.out.println(
                "Points balance cannot be negative.");
        return;
    }

    System.out.print(
            "Confirm changing the balance from "
            + previousPoints
            + " to "
            + newPointsBalance
            + "? (Y/N): ");

    String confirmation =
            scanner.nextLine().trim();

    if (!confirmation.equalsIgnoreCase("Y")) {
        System.out.println(
                "Points update cancelled.");
        return;
    }

    boolean updated =
            loyaltyControl.updatePointsBalance(
                    loyaltyId,
                    newPointsBalance);

    if (!updated) {
        System.out.println(
                "Unable to update points balance.");
        return;
    }

    System.out.println();
    System.out.println(
            "Points balance updated successfully.");

    System.out.println(
            "Previous Points : "
            + previousPoints);

    System.out.println(
            "New Balance     : "
            + account.getPointsBalance());

    System.out.println(
            "Previous Tier   : "
            + previousTier);

    System.out.println(
            "Current Tier    : "
            + account.getMembershipTier());
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
 * Deletes an existing loyalty account after confirmation.
 */
    private void deleteLoyaltyMember() {

    System.out.println();
    System.out.println(
            "+------------------------------------------------+");
    System.out.println(
            "|             DELETE LOYALTY MEMBER              |");
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
    System.out.println(
            "Warning: This loyalty account will be removed.");

    System.out.print(
            "Confirm deletion? (Y/N): ");

    String confirmation =
            scanner.nextLine().trim();

    if (!confirmation.equalsIgnoreCase("Y")) {
        System.out.println(
                "Deletion cancelled.");
        return;
    }

    boolean deleted =
            loyaltyControl.deleteLoyaltyMember(
                    loyaltyId);

    if (!deleted) {
        System.out.println(
                "Unable to delete loyalty member.");
        return;
    }

    System.out.println(
            "Loyalty member deleted successfully.");
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

    for (int i = 0;
            i < alerts.getNumberOfEntries();
            i++) {

        LoyaltyTransaction transaction =
                alerts.getEntry(i);

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
                "Notification " + (i + 1));

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

    for (int i = 0;
            i < transactions.getNumberOfEntries();
            i++) {

        LoyaltyTransaction transaction =
                transactions.getEntry(i);

        LoyaltyAccount account =
                loyaltyControl.findMemberByLoyaltyId(
                        transaction.getLoyaltyId());

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

    for (int i = 0;
            i < accounts.getNumberOfEntries();
            i++) {

        LoyaltyAccount account =
                accounts.getEntry(i);

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
                "4. Redeem Points");

        System.out.printf(
                "| %-46s |%n",
                "5. Update Points Balance");

        System.out.printf(
                "| %-46s |%n",
                "6. Tier and Points Report");

        System.out.printf(
                "| %-46s |%n",
                "7. Delete Loyalty Member");

        System.out.printf(
                "| %-46s |%n",
                "8. Display All Loyalty Accounts");
        
        System.out.printf(
                "| %-46s |%n",
                "9. Expiring Points Report");
        
        System.out.printf(
                "| %-46s |%n",
                "10. Expiring Points Notifications");
        
        System.out.printf(
                "| %-46s |%n",
                "11. Process Expired Points");

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
