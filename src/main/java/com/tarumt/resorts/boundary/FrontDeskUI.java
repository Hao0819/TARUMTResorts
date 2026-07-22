package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.FrontDeskControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Room;

import java.util.Scanner;

/**
 * FrontDeskUI.java
 * Console interface for the Front-Desk Service module. Front-desk agents look
 * up a booking by its 8-digit confirmation number, check room availability,
 * update payment and check-out status, and generate two management reports
 * (Booking/Occupancy and Billing Summary) over the shared collection ADT.
 *
 * @author Keng Ting
 */
public class FrontDeskUI {

    private FrontDeskControl control;
    private Scanner sc;

    // Constructor for running this module independently.
    public FrontDeskUI() {
        this(new FrontDeskControl(), new Scanner(System.in));
    }

    // Constructor that accepts an initialized Control.
    public FrontDeskUI(FrontDeskControl sharedControl) {
        this(sharedControl, new Scanner(System.in));
    }

    // Constructor used when Main provides the Control and Scanner.
    public FrontDeskUI(FrontDeskControl sharedControl, Scanner sharedScanner) {
        control = sharedControl;
        sc = sharedScanner;
    }

    public void showMenu() {
        int choice;
        do {
            int menuWidth = 64;
            String menuBorder = "+" + "-".repeat(menuWidth + 2) + "+";
            String menuTitle = "FRONT-DESK SERVICE MODULE";

            int leftPadding = (menuWidth - menuTitle.length()) / 2;
            int rightPadding = menuWidth - menuTitle.length() - leftPadding;

            System.out.println();
            System.out.println(menuBorder);
            System.out.println("| " + " ".repeat(leftPadding) + menuTitle
                    + " ".repeat(rightPadding) + " |");
            System.out.println(menuBorder);

            System.out.printf("| %-64s |%n", "1. Look up booking by confirmation number");
            System.out.printf("| %-64s |%n", "2. Check room availability");
            System.out.printf("| %-64s |%n", "3. Update payment status");
            System.out.printf("| %-64s |%n", "4. Check out guest");
            System.out.printf("| %-64s |%n", "5. Booking / Occupancy Report");
            System.out.printf("| %-64s |%n", "6. Billing Summary Report");
            System.out.printf("| %-64s |%n", "0. Back to main menu");

            System.out.println(menuBorder);
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                choice = -1;
                continue;
            }

            switch (choice) {
                case 1 -> lookupBooking();
                case 2 -> checkAvailability();
                case 3 -> updatePayment();
                case 4 -> checkOutGuest();
                case 5 -> displayOccupancyReport();
                case 6 -> displayBillingReport();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    // =====================================================================
    // Core feature: look up a booking by its 8-digit confirmation number.
    // =====================================================================

    private void lookupBooking() {
        if (control.getBookingCount() == 0) {
            System.out.println("\nNo bookings are available to search.");
            return;
        }

        Booking booking = control.findByConfirmationNumber(promptConfirmationNumber());
        if (booking == null) {
            System.out.println("No booking found for that confirmation number.");
        } else {
            printReportHeader("BOOKING DETAILS", null, null);
            printBookingTable(new Booking[] { booking });
        }
    }

    // =====================================================================
    // Availability query over the shared room collection.
    // =====================================================================

    private void checkAvailability() {
        String roomType = readRoomTypeFilter();
        Room[] available = control.getAvailableRooms(roomType);

        printReportHeader("ROOM AVAILABILITY",
                "Room type filter: " + roomType, null);

        String border = "+----------+------------+-------------+";
        System.out.println(border);
        System.out.printf("| %-8s | %-10s | %-11s |%n", "Room No", "Room Type", "Status");
        System.out.println(border);
        if (available.length == 0) {
            System.out.printf("| %-35s |%n", "No available rooms for this filter.");
        } else {
            for (Room r : available) {
                System.out.printf("| %-8.8s | %-10.10s | %-11s |%n",
                        r.getRoomNumber(), r.getRoomType(), "AVAILABLE");
            }
        }
        System.out.println(border);
        System.out.println("Total available rooms: " + available.length);
    }

    // =====================================================================
    // Front-Desk write operations: payment update and check-out.
    // =====================================================================

    private void updatePayment() {
        Booking booking = control.findByConfirmationNumber(promptConfirmationNumber());
        if (booking == null) {
            System.out.println("No booking found for that confirmation number.");
            return;
        }
        System.out.printf("Current: %s | Amount RM %.2f | Payment: %s%n",
                booking.getConfirmationNumber(), booking.getAmount(), booking.getPaymentStatus());

        String newStatus;
        while (true) {
            System.out.println("New payment status: 1. PAID  2. PARTIAL  3. UNPAID");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1" -> { newStatus = "PAID"; }
                case "2" -> { newStatus = "PARTIAL"; }
                case "3" -> { newStatus = "UNPAID"; }
                default -> { System.out.println("Invalid choice. Please try again."); continue; }
            }
            break;
        }

        if (control.updatePaymentStatus(booking.getConfirmationNumber(), newStatus)) {
            System.out.println("Payment status updated to " + newStatus + ".");
        } else {
            System.out.println("Update failed.");
        }
    }

    private void checkOutGuest() {
        Booking booking = control.findByConfirmationNumber(promptConfirmationNumber());
        if (booking == null) {
            System.out.println("No booking found for that confirmation number.");
            return;
        }
        if ("CHECKED_OUT".equalsIgnoreCase(booking.getStatus())) {
            System.out.println("This booking is already checked out.");
            return;
        }

        String checkOutTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        if (control.checkOutBooking(booking.getConfirmationNumber(), checkOutTime)) {
            System.out.println("Guest checked out. Room " + booking.getRoom().getRoomNumber()
                    + " is now available. Check-out time: " + checkOutTime);
        } else {
            System.out.println("Check-out failed.");
        }
    }

    // =====================================================================
    // Report 1: Booking / Occupancy (filter status + room type, sort time).
    // =====================================================================

    private void displayOccupancyReport() {
        String statusFilter = readStatusFilter();
        String roomType = readRoomTypeFilter();

        Booking[] results = control.filterByStatusAndType(statusFilter, roomType);
        control.sortByCheckInTime(results);

        printReportHeader("BOOKING / OCCUPANCY REPORT",
                "Filters: Status = " + statusFilter + " | Room type = " + roomType,
                "Sorted by: Check-in time (ascending)");
        printBookingTable(results);
    }

    // =====================================================================
    // Report 2: Billing Summary (filter payment + room type, sort amount).
    // =====================================================================

    private void displayBillingReport() {
        String paymentFilter = readPaymentFilter();
        String roomType = readRoomTypeFilter();

        Booking[] results = control.filterByPayment(paymentFilter, roomType);
        control.sortByAmountDescending(results);

        printReportHeader("BILLING SUMMARY REPORT",
                "Filters: Payment = " + paymentFilter + " | Room type = " + roomType,
                "Sorted by: Amount owed (highest first)");
        printBillingTable(results);
    }

    // =====================================================================
    // Filter prompts.
    // =====================================================================

    private String promptConfirmationNumber() {
        String confirmationNumber;
        while (true) {
            System.out.print("\nEnter 8-digit confirmation number: ");
            confirmationNumber = sc.nextLine().trim();
            if (control.isValidConfirmationNumber(confirmationNumber)) {
                return confirmationNumber;
            }
            System.out.println("Invalid format. A confirmation number must be exactly 8 digits.");
        }
    }

    private String readStatusFilter() {
        while (true) {
            System.out.println("\nBooking Status Filter");
            System.out.println("1. All Statuses");
            System.out.println("2. Active (currently in-house)");
            System.out.println("3. Checked-Out");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "ALL";
                case "2": return "ACTIVE";
                case "3": return "CHECKED_OUT";
                default: System.out.println("Invalid status filter. Please try again.");
            }
        }
    }

    private String readRoomTypeFilter() {
        while (true) {
            System.out.println("\nRoom Type Filter");
            System.out.println("1. All Room Types");
            System.out.println("2. Standard");
            System.out.println("3. Deluxe");
            System.out.println("4. Suite");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "ALL";
                case "2": return "Standard";
                case "3": return "Deluxe";
                case "4": return "Suite";
                default: System.out.println("Invalid room type filter. Please try again.");
            }
        }
    }

    private String readPaymentFilter() {
        while (true) {
            System.out.println("\nPayment Status Filter");
            System.out.println("1. Outstanding (UNPAID or PARTIAL)");
            System.out.println("2. Unpaid");
            System.out.println("3. Partial");
            System.out.println("4. Paid");
            System.out.println("5. All");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "OUTSTANDING";
                case "2": return "UNPAID";
                case "3": return "PARTIAL";
                case "4": return "PAID";
                case "5": return "ALL";
                default: System.out.println("Invalid payment filter. Please try again.");
            }
        }
    }

    // =====================================================================
    // Shared display helpers.
    // =====================================================================

    private void printReportHeader(String title, String filterLine, String sortLine) {
        int contentWidth = 100;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        String generatedTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        System.out.println();
        System.out.println("+" + "-".repeat(contentWidth + 2) + "+");
        System.out.println("| " + " ".repeat(leftPadding) + title
                + " ".repeat(rightPadding) + " |");
        System.out.println("+" + "-".repeat(contentWidth + 2) + "+");
        System.out.printf("| %-100s |%n", "Generated at: " + generatedTime);
        if (filterLine != null) {
            System.out.printf("| %-100s |%n", filterLine);
        }
        if (sortLine != null) {
            System.out.printf("| %-100s |%n", sortLine);
        }
        System.out.println("+" + "-".repeat(contentWidth + 2) + "+");
    }

    private static final String BOOKING_BORDER =
            "+------------+----------+----------------------+----------+"
            + "------------+------------------+--------------+";

    private void printBookingTable(Booking[] bookings) {
        System.out.println(BOOKING_BORDER);
        System.out.printf("| %-10s | %-8s | %-20s | %-8s | %-10s | %-16s | %-12s |%n",
                "Confirm No", "Guest ID", "Guest Name", "Room No", "Room Type",
                "Check-In Time", "Status");
        System.out.println(BOOKING_BORDER);

        if (bookings.length == 0) {
            System.out.printf("| %-100s |%n", "No bookings match the selected criteria.");
        } else {
            for (Booking b : bookings) {
                System.out.printf("| %-10.10s | %-8.8s | %-20.20s | %-8.8s | %-10.10s | %-16.16s | %-12.12s |%n",
                        b.getConfirmationNumber(),
                        b.getGuest().getGuestId(),
                        b.getGuest().getName(),
                        b.getRoom().getRoomNumber(),
                        b.getRoom().getRoomType(),
                        b.getCheckInTime(),
                        b.getStatus());
            }
        }
        System.out.println(BOOKING_BORDER);
        System.out.println("Total records displayed: " + bookings.length);
    }

    private static final String BILLING_BORDER =
            "+------------+----------------------+----------+------------+"
            + "--------------+--------------+";

    private void printBillingTable(Booking[] bookings) {
        System.out.println(BILLING_BORDER);
        System.out.printf("| %-10s | %-20s | %-8s | %-10s | %-12s | %-12s |%n",
                "Confirm No", "Guest Name", "Room No", "Room Type", "Amount (RM)", "Payment");
        System.out.println(BILLING_BORDER);

        if (bookings.length == 0) {
            System.out.printf("| %-78s |%n", "No bookings match the selected criteria.");
            System.out.println(BILLING_BORDER);
            System.out.println("Total records displayed: 0");
            return;
        }
        for (Booking b : bookings) {
            System.out.printf("| %-10.10s | %-20.20s | %-8.8s | %-10.10s | %,12.2f | %-12.12s |%n",
                    b.getConfirmationNumber(),
                    b.getGuest().getName(),
                    b.getRoom().getRoomNumber(),
                    b.getRoom().getRoomType(),
                    b.getAmount(),
                    b.getPaymentStatus());
        }
        System.out.println(BILLING_BORDER);
        System.out.println("Total records displayed: " + bookings.length);
        System.out.printf("Total amount in this report: RM %,.2f%n", control.totalAmount(bookings));
    }
}
