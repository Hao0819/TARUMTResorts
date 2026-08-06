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
            System.out.printf("| %-64s |%n", "2. Search bookings (by any keyword)");
            System.out.printf("| %-64s |%n", "3. Filter bookings (multi-field: name/ID/tier/type/date)");
            System.out.printf("| %-64s |%n", "4. Check room availability");
            System.out.printf("| %-64s |%n", "5. Check in guest");
            System.out.printf("| %-64s |%n", "6. Check out guest");
            System.out.printf("| %-64s |%n", "7. Cancel booking");
            System.out.printf("| %-64s |%n", "8. Booking / Occupancy Report");
            System.out.printf("| %-64s |%n", "9. Billing Summary Report");
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
                case 2 -> searchBookings();
                case 3 -> filterBookings();
                case 4 -> checkAvailability();
                case 5 -> checkInGuest();
                case 6 -> checkOutGuest();
                case 7 -> cancelBooking();
                case 8 -> displayOccupancyReport();
                case 9 -> displayBillingReport();
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
            printBookingDetails(booking);
        }
    }

    /**
     * Full detail card for one booking: guest identification, room, stay
     * dates/status, and the hardcoded billing details (amount + payment status)
     * a front-desk agent needs when answering a billing enquiry.
     */
    private void printBookingDetails(Booking booking) {
        printReportHeader("BOOKING & BILLING DETAILS", null, null);
        System.out.printf("  Confirmation No : %s%n", booking.getConfirmationNumber());
        System.out.printf("  Guest ID        : %s%n", booking.getGuest().getGuestId());
        System.out.printf("  Guest Name      : %s%n", booking.getGuest().getName());
        System.out.printf("  Membership Tier : %s%n", booking.getGuest().getMembershipTier());
        System.out.printf("  Room            : %s (%s)%n",
                booking.getRoom().getRoomNumber(), booking.getRoom().getRoomType());
        System.out.printf("  Check-In        : %s%n", booking.getCheckInTime());
        System.out.printf("  Check-Out       : %s%n",
                booking.getCheckOutTime() == null ? "-" : booking.getCheckOutTime());
        System.out.printf("  Booking Status  : %s%n", booking.getStatus());
        System.out.println("  ----------------- Billing -----------------");
        System.out.printf("  Total Amount    : RM %,.2f%n", booking.getAmount());
        System.out.printf("  Payment Status  : %s%n", booking.getPaymentStatus());
    }

    // =====================================================================
    // Free-text search: one keyword, matched against any field of a booking.
    // =====================================================================

    private void searchBookings() {
        System.out.println("\nSearch bookings - type one or more keywords separated by spaces.");
        System.out.println("  Fields searched: name, guest ID, room no/type, tier, status, payment, date, amount.");
        System.out.println("  Extra keywords narrow the results, e.g. \"siti deluxe\" = Siti's Deluxe booking.");
        System.out.print("  Keywords: ");
        String query = sc.nextLine().trim();

        Booking[] results = control.search(query);
        printReportHeader("SEARCH RESULTS",
                "Keywords: " + (query.isEmpty() ? "(blank - all bookings)" : query),
                "A booking matches only if it contains EVERY keyword (in any field)");
        printBookingTable(results);
    }

    // =====================================================================
    // Structured multi-field filter: narrow by specific fields (AND).
    // =====================================================================

    private void filterBookings() {
        System.out.println("\nFilter bookings - leave a field blank to ignore it (all conditions AND).");
        System.out.print("  Confirmation number contains : ");
        String conf = sc.nextLine().trim();
        System.out.print("  Guest name contains          : ");
        String name = sc.nextLine().trim();
        System.out.print("  Guest ID contains            : ");
        String gid = sc.nextLine().trim();
        System.out.print("  Membership tier (NONE/SILVER/GOLD/PLATINUM/DIAMOND/ELITE): ");
        String tier = sc.nextLine().trim();
        System.out.print("  Room type (Standard/Deluxe/Suite): ");
        String type = sc.nextLine().trim();
        System.out.print("  Check-in date (e.g. 2026-07-17): ");
        String date = sc.nextLine().trim();

        Booking[] results = control.filterBookings(conf, name, gid, tier, type, date);
        printReportHeader("FILTER RESULTS", buildCriteriaLine(conf, name, gid, tier, type, date), null);
        printBookingTable(results);
    }

    /** Builds a one-line summary of which filter criteria were actually supplied. */
    private String buildCriteriaLine(String conf, String name, String gid,
            String tier, String type, String date) {
        StringBuilder sb = new StringBuilder("Filters: ");
        int len = sb.length();
        if (!conf.isEmpty()) sb.append("ConfNo~").append(conf).append("  ");
        if (!name.isEmpty()) sb.append("Name~").append(name).append("  ");
        if (!gid.isEmpty()) sb.append("GuestID~").append(gid).append("  ");
        if (!tier.isEmpty()) sb.append("Tier=").append(tier).append("  ");
        if (!type.isEmpty()) sb.append("RoomType=").append(type).append("  ");
        if (!date.isEmpty()) sb.append("CheckIn~").append(date).append("  ");
        if (sb.length() == len) sb.append("(none - showing all bookings)");
        return sb.toString().trim();
    }

    // =====================================================================
    // Availability query over the shared room collection.
    // =====================================================================

    private void checkAvailability() {
        String roomType = readRoomTypeFilter();
        Room[] available = control.getAvailableRooms(roomType);

        printReportHeader("ROOM AVAILABILITY",
                "Room type filter: " + roomType,
                "Bookable = vacant AND Housekeeping status READY");

        String border = "+----------+------------+-------------+--------------+------------+";
        System.out.println(border);
        System.out.printf("| %-8s | %-10s | %-11s | %-12s | %-10s |%n",
                "Room No", "Room Type", "Occupancy", "Housekeeping", "Bookable");
        System.out.println(border);

        int bookable = 0;
        if (available.length == 0) {
            System.out.printf("| %-62s |%n", "No vacant rooms for this filter.");
        } else {
            for (Room r : available) {
                boolean ok = control.isBookable(r);
                if (ok) {
                    bookable++;
                }
                System.out.printf("| %-8.8s | %-10.10s | %-11s | %-12.12s | %-10s |%n",
                        r.getRoomNumber(), r.getRoomType(), "VACANT",
                        r.getCleaningStatus(), ok ? "YES" : "NO");
            }
        }
        System.out.println(border);
        System.out.println("Vacant rooms: " + available.length
                + "   |   Bookable now: " + bookable);
    }

    // =====================================================================
    // Front-Desk write operations: check-in, check-out, cancel.
    // (Payment status is fixed hard-coded billing data — read-only, so
    // there is no payment-update operation.)
    // =====================================================================

    private void checkOutGuest() {
        Booking booking = control.findByConfirmationNumber(promptConfirmationNumber());
        if (booking == null) {
            System.out.println("No booking found for that confirmation number.");
            return;
        }
        String status = booking.getStatus();
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            if ("CONFIRMED".equalsIgnoreCase(status)) {
                System.out.println("Guest has not checked in yet - please Check in first (or Cancel the booking).");
            } else if ("CHECKED_OUT".equalsIgnoreCase(status)) {
                System.out.println("This booking is already checked out.");
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                System.out.println("This booking has been cancelled.");
            } else {
                System.out.println("Only an active (checked-in) booking can be checked out. Current status: " + status);
            }
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

    private void checkInGuest() {
        Booking booking = control.findByConfirmationNumber(promptConfirmationNumber());
        if (booking == null) {
            System.out.println("No booking found for that confirmation number.");
            return;
        }
        if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            System.out.println("Only a CONFIRMED booking (booked, not yet arrived) can be checked in. "
                    + "Current status: " + booking.getStatus());
            return;
        }

        String checkInTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        if (control.checkInBooking(booking.getConfirmationNumber(), checkInTime)) {
            System.out.println("Guest checked in. Booking is now ACTIVE. Room "
                    + booking.getRoom().getRoomNumber() + " | Check-in time: " + checkInTime);
        } else {
            System.out.println("Check-in failed.");
        }
    }

    private void cancelBooking() {
        Booking booking = control.findByConfirmationNumber(promptConfirmationNumber());
        if (booking == null) {
            System.out.println("No booking found for that confirmation number.");
            return;
        }
        String status = booking.getStatus();
        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                System.out.println("Guest has already checked in - use Check out (early check-out) instead of cancel.");
            } else if ("CHECKED_OUT".equalsIgnoreCase(status)) {
                System.out.println("A completed (checked-out) stay cannot be cancelled.");
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                System.out.println("This booking is already cancelled.");
            } else {
                System.out.println("Only a CONFIRMED booking (booked, not yet checked in) can be cancelled. "
                        + "Current status: " + status);
            }
            return;
        }

        printBookingDetails(booking);
        System.out.print("\nCancel this booking? (Y/N): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        String room = booking.getRoom().getRoomNumber();
        if (control.cancelBooking(booking.getConfirmationNumber())) {
            System.out.println("Booking " + booking.getConfirmationNumber()
                    + " cancelled (kept on record as CANCELLED). Room " + room + " has been released.");
        } else {
            System.out.println("Cancellation failed.");
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
            System.out.println("4. Confirmed (booked, not yet checked in)");
            System.out.println("5. Cancelled");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "ALL";
                case "2": return "ACTIVE";
                case "3": return "CHECKED_OUT";
                case "4": return "CONFIRMED";
                case "5": return "CANCELLED";
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
                        b.getCheckInTime() == null ? "-" : b.getCheckInTime(),
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
