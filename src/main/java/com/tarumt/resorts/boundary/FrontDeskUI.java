package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.FrontDeskControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Room;

import java.util.Scanner;

/**
 * FrontDeskUI.java
 * Console screens for the Front-Desk module.
 *
 * @author Tan Keng Ting
 */
public class FrontDeskUI {

    private FrontDeskControl control;
    private Scanner sc;

    public FrontDeskUI() {
        this(new FrontDeskControl(), new Scanner(System.in));
    }

    public FrontDeskUI(FrontDeskControl sharedControl) {
        this(sharedControl, new Scanner(System.in));
    }

    public FrontDeskUI(FrontDeskControl sharedControl, Scanner sharedScanner) {
        control = sharedControl;
        sc = sharedScanner;
    }

    // main menu loop
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

            System.out.printf("| %-64s |%n", "1. Search bookings (by confirmation / room / guest ID / etc.)");
            System.out.printf("| %-64s |%n", "2. Check room availability (now)");
            System.out.printf("| %-64s |%n", "3. Room availability calendar (by month)");
            System.out.printf("| %-64s |%n", "4. Check in guest");
            System.out.printf("| %-64s |%n", "5. Check out guest");
            System.out.printf("| %-64s |%n", "6. Cancel booking");
            System.out.printf("| %-64s |%n", "7. Booking / Occupancy Report");
            System.out.printf("| %-64s |%n", "8. Billing Summary Report");
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
                case 1 -> searchBookings();
                case 2 -> checkAvailability();
                case 3 -> checkAvailabilityByDate();
                case 4 -> checkInGuest();
                case 5 -> checkOutGuest();
                case 6 -> cancelBooking();
                case 7 -> displayOccupancyReport();
                case 8 -> displayBillingReport();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    // the receipt card (default title)
    private void printBookingDetails(Booking booking) {
        printBookingDetails(booking, "BOOKING & BILLING DETAILS");
    }

    // receipt card with a custom title
    private void printBookingDetails(Booking booking, String title) {
        printReportHeader(title, null, null);
        System.out.printf("  Confirmation No : %s%n", booking.getConfirmationNumber());
        System.out.printf("  Guest ID        : %s%n", booking.getGuest().getGuestId());
        System.out.printf("  Guest Name      : %s%n", booking.getGuest().getName());
        System.out.printf("  Membership Tier : %s%n", booking.getGuest().getMembershipTier());
        System.out.printf("  Loyalty ID      : %s%n", control.getLoyaltyIdFor(booking));
        System.out.printf("  Current Points  : %s%n", control.getLoyaltyPointsFor(booking));
        System.out.printf("  Room            : %s (%s)%n",
                booking.getRoom().getRoomNumber(), booking.getRoom().getRoomType());
        System.out.printf("  Check-In        : %s%n",
                booking.getCheckInTime() == null ? "-" : booking.getCheckInTime());
        System.out.printf("  Check-Out       : %s%n",
                booking.getCheckOutTime() == null ? "-" : booking.getCheckOutTime());
        System.out.printf("  Booking Status  : %s%n", booking.getStatus());
        System.out.println("  ----------------- Billing -----------------");
        System.out.printf(
                "  Gross Amount    : RM %,.2f%n",
                booking.getAmount());
        System.out.printf(
                "  Discount Rate   : %.0f%%%n",
                booking.getDiscountRate() * 100);
        System.out.printf(
                "  Discount Amount : RM %,.2f%n",
                booking.getDiscountAmount());
        System.out.printf(
                "  Amount Payable  : RM %,.2f%n",
                booking.getFinalAmount());
        System.out.printf("  Payment Status  : %s%n", booking.getPaymentStatus());
    }

    // keyword search 
    // search by keyword, list the matches, then open one receipt by conf no
    private void searchBookings() {
        System.out.println("\nSearch bookings - type one or more keywords separated by spaces.");
        System.out.println("  Fields searched: name, guest ID, loyalty ID, points, room no/type, tier");
        System.out.println("  Extra keywords narrow the results, e.g. \"siti deluxe\" = Siti's Deluxe booking.");
        System.out.print("  Keywords (0 = back to Front-Desk menu): ");
        String query = sc.nextLine().trim();
        if (query.equals("0")) {
            return;
        }

        Booking[] results = control.search(query);
        printReportHeader("SEARCH RESULTS",
                "Keywords: " + (query.isEmpty() ? "(blank - all bookings)" : query),
                "A booking matches only if it contains EVERY keyword (in any field)");
        printBookingTable(results);

        // one match -> just show it. many matches -> let them pick by the conf
        // no, since a room / guest id can belong to several bookings.
        if (results.length == 1) {
            printBookingDetails(results[0]);
        } else if (results.length > 1) {
            System.out.print("\nEnter a confirmation no to view its full receipt (0 = back): ");
            String pick = sc.nextLine().trim();
            if (!pick.equals("0")) {
                Booking chosen = null;
                for (Booking b : results) {
                    if (b.getConfirmationNumber().equalsIgnoreCase(pick)) {
                        chosen = b;
                        break;
                    }
                }
                if (chosen != null) {
                    printBookingDetails(chosen);
                } else {
                    System.out.println("No booking with that confirmation no in the results above.");
                }
            }
        }
    }

    // availability (now) 
    // rooms free right now for a chosen type
    private void checkAvailability() {
        String roomType = readRoomTypeFilter();
        if (roomType == null) {
            return;
        }
        Room[] available = control.getAvailableRooms(roomType);

        printReportHeader("ROOM AVAILABILITY",
                "Room type filter: " + roomType,
                "Available = vacant and not reserved today",
                ROOM_HEADER_WIDTH);
        printRoomTable(available);
        System.out.println("Available rooms now: " + available.length);
    }

    // room table is 48 wide, so the header uses width 44 to line up under it
    private static final int ROOM_HEADER_WIDTH = 44;

    // print a Room[] as a simple 3-col table (everything shown VACANT)
    private void printRoomTable(Room[] rooms) {
        String border = "+------------+----------------+----------------+";
        System.out.println(border);
        System.out.printf("| %-10s | %-14s | %-14s |%n", "Room No", "Room Type", "Occupancy");
        System.out.println(border);
        if (rooms.length == 0) {
            System.out.printf("| %-44s |%n", "No rooms available.");
        } else {
            for (Room r : rooms) {
                System.out.printf("| %-10.10s | %-14.14s | %-14s |%n",
                        r.getRoomNumber(), r.getRoomType(), "VACANT");
            }
        }
        System.out.println(border);
    }

    // availability calendar (by month) 
    // pick a type, browse months, drill into a day to see its free rooms
    private void checkAvailabilityByDate() {
        String roomType = readRoomTypeFilter();
        if (roomType == null) {
            return;
        }
        java.time.YearMonth month = java.time.YearMonth.now();

        while (true) {
            displayAvailabilityCalendar(month, roomType);
            System.out.print("\nEnter a day number (list that night's free rooms), "
                    + "YYYY-MM (change month), or 0 (back): ");
            String input = sc.nextLine().trim();

            if (input.equals("0")) {
                return;
            }
            // a number in range = open that day
            try {
                int day = Integer.parseInt(input);
                if (day >= 1 && day <= month.lengthOfMonth()) {
                    showRoomsForDay(month.atDay(day), roomType);
                    continue;
                }
                System.out.println("Day out of range for " + month.getMonth() + " " + month.getYear() + ".");
                continue;
            } catch (NumberFormatException ignored) {
                // not a number - maybe it's a month
            }
            try {
                month = java.time.YearMonth.parse(input);
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Invalid input. Enter a day number, YYYY-MM (e.g. 2026-08), or 0.");
            }
        }
    }

    // draw the month grid (day = has a free room, X = full, - = past date)
    private void displayAvailabilityCalendar(java.time.YearMonth month, String roomType) {
        String border = "+------+------+------+------+------+------+------+";
        String title = month.getMonth() + " " + month.getYear() + " - "
                + roomType.toUpperCase() + " AVAILABILITY";
        int contentWidth = 48;
        int leftPadding = Math.max(0, (contentWidth - title.length()) / 2);
        int rightPadding = Math.max(0, contentWidth - title.length() - leftPadding);

        System.out.println();
        System.out.println(border);
        System.out.printf("|%s%s%s|%n", " ".repeat(leftPadding), title, " ".repeat(rightPadding));
        System.out.println(border);
        System.out.println("| Sun  | Mon  | Tue  | Wed  | Thu  | Fri  | Sat  |");
        System.out.println(border);

        // sunday = col 0 (DayOfWeek is Mon=1..Sun=7, so %7 puts Sun at 0)
        int column = month.atDay(1).getDayOfWeek().getValue() % 7;
        for (int c = 0; c < column; c++) {
            System.out.print("|      ");
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        int days = month.lengthOfMonth();
        for (int day = 1; day <= days; day++) {
            java.time.LocalDate date = month.atDay(day);
            String cell;
            if (date.isBefore(today)) {
                cell = "-";
            } else {
                cell = control.hasAvailabilityOn(date, roomType) ? String.valueOf(day) : "X";
            }
            System.out.printf("| %4s ", cell);
            column++;
            if (column == 7) {
                System.out.println("|");
                column = 0;
            }
        }
        if (column != 0) {
            while (column < 7) {
                System.out.print("|      ");
                column++;
            }
            System.out.println("|");
        }
        System.out.println(border);
        System.out.println("Legend: day shown = has free " + roomType + " room(s) that night | X = "
                + roomType + " fully booked | - = past date");
        System.out.println("Total " + roomType + " room(s): " + control.countRoomsByType(roomType));
    }

    // list the actual free rooms for one night
    private void showRoomsForDay(java.time.LocalDate date, String roomType) {
        Room[] free = control.getAvailableRoomsForRange(date, date.plusDays(1), roomType);
        printReportHeader("ROOMS AVAILABLE ON " + date + " (" + date.getDayOfWeek() + ")",
                "Room type = " + roomType,
                free.length + " of " + control.countRoomsByType(roomType) + " room(s) free",
                ROOM_HEADER_WIDTH);
        printRoomTable(free);
    }

    // check-in / check-out / cancel 
    // check out an in-house (ACTIVE) guest
    private void checkOutGuest() {
        Booking[] inHouse = control.filterByStatusAndType("ACTIVE", "ALL");
        if (inHouse.length == 0) {
            printNotice("NO IN-HOUSE GUESTS",
                    "There are no ACTIVE bookings to check out right now.");
            return;
        }
        Booking booking = selectBooking(inHouse, "SELECT A GUEST TO CHECK OUT  (status: ACTIVE / in-house)");
        if (booking == null) {
            System.out.println("Check-out cancelled.");
            return;
        }

        String checkOutTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        if (control.checkOutBooking(booking.getConfirmationNumber(), checkOutTime)) {
            printNotice("CHECK-OUT SUCCESSFUL",
                    "Room " + booking.getRoom().getRoomNumber()
                            + " is now vacant, handed to Housekeeping (DIRTY).");
            printBookingDetails(booking, "BOOKING DETAILS");
        } else {
            printNotice("CHECK-OUT FAILED",
                    "Booking " + booking.getConfirmationNumber() + " could not be checked out.");
        }
    }

    // show the bookings numbered; pick by row no / room no / guest id (0 = back)
    private Booking selectBooking(Booking[] list, String title) {
        printReportHeader(title, "Pick by row number, room number, or guest ID (0 to go back)",
                "Check-In shows the actual time once checked in, else the scheduled arrival date");
        String border = "+-----+------------+----------+----------------------+----------+------------+------------------+";
        System.out.println(border);
        System.out.printf("| %-3s | %-10s | %-8s | %-20s | %-8s | %-10s | %-16s |%n",
                "No", "Confirm No", "Guest ID", "Guest Name", "Room No", "Room Type", "Check-In");
        System.out.println(border);
        for (int i = 0; i < list.length; i++) {
            Booking b = list[i];
            System.out.printf("| %-3d | %-10.10s | %-8.8s | %-20.20s | %-8.8s | %-10.10s | %-16.16s |%n",
                    i + 1, b.getConfirmationNumber(), b.getGuest().getGuestId(),
                    b.getGuest().getName(), b.getRoom().getRoomNumber(), b.getRoom().getRoomType(),
                    checkInDisplay(b));
        }
        System.out.println(border);

        while (true) {
            System.out.print("Enter selection (row no / room no / guest ID, 0 = back): ");
            String input = sc.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            // try a row number first
            try {
                int row = Integer.parseInt(input);
                if (row >= 1 && row <= list.length) {
                    return list[row - 1];
                }
            } catch (NumberFormatException ignored) {
                // not a number, fall through to room / guest matching
            }
            // otherwise match by room no, then guest id, inside this list
            for (Booking b : list) {
                if (b.getRoom().getRoomNumber().equalsIgnoreCase(input)
                        || b.getGuest().getGuestId().equalsIgnoreCase(input)) {
                    return b;
                }
            }
            System.out.println("No matching row/room/guest in the list above. Please try again.");
        }
    }

    // what to show in the Check-In column: real time if checked in else the booked date
    private String checkInDisplay(Booking b) {
        if (b.getCheckInTime() != null) {
            return b.getCheckInTime();
        }
        if (b.getScheduledCheckInDate() != null) {
            return b.getScheduledCheckInDate().toString();
        }
        return "-";
    }

    // check in a confirmed guest (blocks early arrival and a not-cleaned room)
    private void checkInGuest() {
        Booking[] awaiting = control.filterByStatusAndType("CONFIRMED", "ALL");
        if (awaiting.length == 0) {
            printNotice("NO BOOKINGS AWAITING CHECK-IN",
                    "There are no CONFIRMED bookings to check in right now.");
            return;
        }
        Booking booking = selectBooking(awaiting, "SELECT A BOOKING TO CHECK IN  (status: CONFIRMED)");
        if (booking == null) {
            System.out.println("Check-in cancelled.");
            return;
        }

        // block early arrival
        java.time.LocalDate today = java.time.LocalDate.now();
        if (control.isBeforeScheduledCheckIn(booking, today)) {
            printNotice("CANNOT CHECK IN YET",
                    "Booking " + booking.getConfirmationNumber()
                            + " is scheduled from " + booking.getScheduledCheckInDate() + ".",
                    "The guest is arriving early (today is " + today + ").");
            return;
        }

        // room has to be cleaned (READY) before the guest can go in
        if (!control.isBookingRoomReadyForCheckIn(booking)) {
            printNotice("ROOM NOT READY",
                    "Room " + booking.getRoom().getRoomNumber()
                            + " is currently " + booking.getRoom().getCleaningStatus() + ".",
                    "Housekeeping must bring this room to READY before the guest can check in.");
            return;
        }

        String checkInTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        if (control.checkInBooking(booking.getConfirmationNumber(), checkInTime)) {
            printNotice("CHECK-IN SUCCESSFUL",
                    "Booking " + booking.getConfirmationNumber()
                            + " is now ACTIVE. Payment marked PAID.");
            printBookingDetails(booking, "BOOKING DETAILS");
        } else {
            printNotice("CHECK-IN FAILED",
                    "Booking " + booking.getConfirmationNumber() + " could not be checked in.");
        }
    }

    // cancel a confirmed booking (kept on record as CANCELLED)
    private void cancelBooking() {
        Booking[] cancellable = control.filterByStatusAndType("CONFIRMED", "ALL");
        if (cancellable.length == 0) {
            printNotice("NO CANCELLABLE BOOKINGS",
                    "There are no CONFIRMED bookings to cancel right now.");
            return;
        }
        Booking booking = selectBooking(cancellable, "SELECT A BOOKING TO CANCEL  (status: CONFIRMED)");
        if (booking == null) {
            System.out.println("No booking cancelled.");
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
            printNotice("BOOKING CANCELLED",
                    "Booking " + booking.getConfirmationNumber() + " is kept on record as CANCELLED.",
                    "Room " + room + " is freed for that reserved period.");
            printBookingDetails(booking, "BOOKING DETAILS");
        } else {
            printNotice("CANCELLATION FAILED",
                    "Booking " + booking.getConfirmationNumber() + " could not be cancelled.");
        }
    }

    // report 1: booking / occupancy 
    // filter + sort by check-in time, bar chart by status
    private void displayOccupancyReport() {
        String statusFilter = readStatusFilter();
        if (statusFilter == null) {
            return;
        }
        String roomType = readRoomTypeFilter();
        if (roomType == null) {
            return;
        }

        Booking[] results = control.filterByStatusAndType(statusFilter, roomType);
        control.sortByCheckInTime(results);

        // walk-in style report - framed table + two graphs
        int[] widths = {10, 8, 20, 8, 10, 16, 12};
        String border = buildBorder(widths);
        int cw = contentWidth(widths);

        printFramedHeader(border, cw, "BOOKING / OCCUPANCY REPORT",
                "Filters: Status = " + statusFilter + " | Room type = " + roomType,
                "Sorted by: Check-in time (ascending)");

        System.out.printf("| %-10s | %-8s | %-20s | %-8s | %-10s | %-16s | %-12s |%n",
                "Confirm No", "Guest ID", "Guest Name", "Room No", "Room Type",
                "Check-In Time", "Status");
        System.out.println(border);
        if (results.length == 0) {
            System.out.printf("| %-" + cw + "s |%n", "No bookings match the selected criteria.");
        } else {
            for (Booking b : results) {
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
        System.out.println(border);
        System.out.printf("| %-" + cw + "s |%n", "Total matching records: " + results.length);
        System.out.println(border);

        String[] statusKeys = {"CONFIRMED", "ACTIVE", "CHECKED_OUT", "CANCELLED"};
        String[] statusLabels = {"Confirmed", "Active", "CheckedOut", "Cancelled"};
        int[] statusCounts = new int[statusKeys.length];
        String[] typeLabels = {"Standard", "Deluxe", "Suite"};
        int[] typeCounts = new int[typeLabels.length];
        for (Booking b : results) {
            for (int i = 0; i < statusKeys.length; i++) {
                if (statusKeys[i].equalsIgnoreCase(b.getStatus())) {
                    statusCounts[i]++;
                    break;
                }
            }
            for (int i = 0; i < typeLabels.length; i++) {
                if (typeLabels[i].equalsIgnoreCase(b.getRoom().getRoomType())) {
                    typeCounts[i]++;
                    break;
                }
            }
        }
        if (results.length > 0) {
            displaySideBySideBarCharts("BOOKINGS BY STATUS", statusLabels, statusCounts,
                    "BOOKINGS BY ROOM TYPE", typeLabels, typeCounts);
        }
        printFramedFooter(border, cw);
    }

    // report 2: billing summary 
    // filter + sort by amount, bar chart by payment
    private void displayBillingReport() {
        String paymentFilter = readPaymentFilter();
        if (paymentFilter == null) {
            return;
        }
        String roomType = readRoomTypeFilter();
        if (roomType == null) {
            return;
        }

        Booking[] results = control.filterByPayment(paymentFilter, roomType);
        control.sortByAmountDescending(results);

        // same framed style, plus a payable total row
        int[] widths = {10, 22, 8, 10, 14, 10};
        String border = buildBorder(widths);
        int cw = contentWidth(widths);

        printFramedHeader(border, cw, "BILLING SUMMARY REPORT",
                "Filters: Payment = " + paymentFilter + " | Room type = " + roomType,
                "Sorted by: Amount payable (highest first)");

        System.out.printf("| %-10s | %-22s | %-8s | %-10s | %14s | %-10s |%n",
                "Confirm No", "Guest Name", "Room No", "Room Type", "Payable (RM)", "Payment");
        System.out.println(border);
        if (results.length == 0) {
            System.out.printf("| %-" + cw + "s |%n", "No bookings match the selected criteria.");
            System.out.println(border);
            System.out.printf("| %-" + cw + "s |%n", "Total matching records: 0");
            System.out.println(border);
            printFramedFooter(border, cw);
            return;
        }
        for (Booking b : results) {
            System.out.printf("| %-10.10s | %-22.22s | %-8.8s | %-10.10s | %,14.2f | %-10.10s |%n",
                    b.getConfirmationNumber(),
                    b.getGuest().getName(),
                    b.getRoom().getRoomNumber(),
                    b.getRoom().getRoomType(),
                    b.getFinalAmount(),
                    b.getPaymentStatus());
        }
        System.out.println(border);
        // label spans the first 4 cols so the amount lines up under Payable
        System.out.printf("| %-59s | %,14.2f | %-10s |%n",
                "Total Payable (RM)", control.totalAmount(results), "");
        System.out.println(border);
        System.out.printf("| %-" + cw + "s |%n", "Total matching records: " + results.length);
        System.out.println(border);

        String[] payLabels = {"Paid", "Unpaid"};
        int[] payCounts = new int[payLabels.length];
        String[] typeLabels = {"Standard", "Deluxe", "Suite"};
        int[] typeCounts = new int[typeLabels.length];
        for (Booking b : results) {
            if ("PAID".equalsIgnoreCase(b.getPaymentStatus())) {
                payCounts[0]++;
            } else if ("UNPAID".equalsIgnoreCase(b.getPaymentStatus())) {
                payCounts[1]++;
            }
            for (int i = 0; i < typeLabels.length; i++) {
                if (typeLabels[i].equalsIgnoreCase(b.getRoom().getRoomType())) {
                    typeCounts[i]++;
                    break;
                }
            }
        }
        displaySideBySideBarCharts("BOOKINGS BY PAYMENT", payLabels, payCounts,
                "BOOKINGS BY ROOM TYPE", typeLabels, typeCounts);
        printFramedFooter(border, cw);
    }

    // filter menus (0 = back, returns null) 
    private String readStatusFilter() {
        while (true) {
            printFilterMenu("BOOKING STATUS FILTER",
                    "1. All Statuses",
                    "2. Active (currently in-house)",
                    "3. Checked-Out",
                    "4. Confirmed (booked, not yet checked in)",
                    "5. Cancelled",
                    "0. Back to Front-Desk menu");
            switch (sc.nextLine().trim()) {
                case "1":
                    return "ALL";
                case "2":
                    return "ACTIVE";
                case "3":
                    return "CHECKED_OUT";
                case "4":
                    return "CONFIRMED";
                case "5":
                    return "CANCELLED";
                case "0":
                    return null;
                default:
                    System.out.println("Invalid status filter. Please try again.");
            }
        }
    }

    private String readRoomTypeFilter() {
        while (true) {
            printFilterMenu("ROOM TYPE FILTER",
                    "1. All Room Types",
                    "2. Standard",
                    "3. Deluxe",
                    "4. Suite",
                    "0. Back to Front-Desk menu");
            switch (sc.nextLine().trim()) {
                case "1":
                    return "ALL";
                case "2":
                    return "Standard";
                case "3":
                    return "Deluxe";
                case "4":
                    return "Suite";
                case "0":
                    return null;
                default:
                    System.out.println("Invalid room type filter. Please try again.");
            }
        }
    }

    private String readPaymentFilter() {
        while (true) {
            printFilterMenu("PAYMENT STATUS FILTER",
                    "1. Unpaid (outstanding)",
                    "2. Paid",
                    "3. All",
                    "0. Back to Front-Desk menu");
            switch (sc.nextLine().trim()) {
                case "1":
                    return "UNPAID";
                case "2":
                    return "PAID";
                case "3":
                    return "ALL";
                case "0":
                    return null;
                default:
                    System.out.println("Invalid payment filter. Please try again.");
            }
        }
    }

    // draw a filter menu box, then the "Enter choice:" prompt
    private void printFilterMenu(String title, String... options) {
        int width = title.length();
        for (String option : options) {
            if (option.length() > width) {
                width = option.length();
            }
        }
        String border = "+" + "-".repeat(width + 2) + "+";
        int leftPadding = (width - title.length()) / 2;
        int rightPadding = width - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title
                + " ".repeat(rightPadding) + " |");
        System.out.println(border);
        for (String option : options) {
            System.out.printf("| %-" + width + "s |%n", option);
        }
        System.out.println(border);
        System.out.print("Enter choice: ");
    }

    // shared display helpers 
    // small boxed message for a success / warning / fail outcome
    private void printNotice(String heading, String... body) {
        int width = heading.length();
        for (String line : body) {
            if (line.length() > width) {
                width = line.length();
            }
        }
        String border = "  +" + "-".repeat(width + 2) + "+";
        System.out.println();
        System.out.println(border);
        System.out.printf("  | %-" + width + "s |%n", heading);
        if (body.length > 0) {
            System.out.println(border);
            for (String line : body) {
                System.out.printf("  | %-" + width + "s |%n", line);
            }
        }
        System.out.println(border);
    }

    // boxed header for the search / availability screens
    private void printReportHeader(String title, String filterLine, String sortLine) {
        printReportHeader(title, filterLine, sortLine, 100);
    }

    // same header but with a chosen width so it lines up with its table
    private void printReportHeader(String title, String filterLine, String sortLine, int contentWidth) {
        int leftPadding = Math.max(0, (contentWidth - title.length()) / 2);
        int rightPadding = Math.max(0, contentWidth - title.length() - leftPadding);

        String generatedTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        System.out.println();
        System.out.println("+" + "-".repeat(contentWidth + 2) + "+");
        System.out.println("| " + " ".repeat(leftPadding) + title
                + " ".repeat(rightPadding) + " |");
        System.out.println("+" + "-".repeat(contentWidth + 2) + "+");
        System.out.printf("| %-" + contentWidth + "s |%n", "Generated at: " + generatedTime);
        if (filterLine != null) {
            System.out.printf("| %-" + contentWidth + "s |%n", filterLine);
        }
        if (sortLine != null) {
            System.out.printf("| %-" + contentWidth + "s |%n", sortLine);
        }
        System.out.println("+" + "-".repeat(contentWidth + 2) + "+");
    }

    private static final String BOOKING_BORDER = "+------------+----------+----------------------+----------+"
            + "------------+------------------+--------------+";

    // boxed booking table (used by the search results)
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

    private static final String COMPANY_NAME = "TARUMT RESORTS";

    private String buildBorder(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    private int contentWidth(int[] widths) {
        int sum = 0;
        for (int w : widths) {
            sum += w;
        }
        return sum + 3 * (widths.length - 1);
    }

    private void printFramedHeader(String border, int cw, String title,
            String filterLine, String sortLine) {
        String generatedTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        System.out.println();
        System.out.println(border);
        printBoxedCentered(COMPANY_NAME, cw);
        printBoxedCentered(title, cw);
        System.out.println(border);
        System.out.printf("| %-" + cw + "s |%n", "Generated at: " + generatedTime);
        if (filterLine != null) {
            System.out.printf("| %-" + cw + "s |%n", filterLine);
        }
        if (sortLine != null) {
            System.out.printf("| %-" + cw + "s |%n", sortLine);
        }
        System.out.println(border);
    }

    private void printBoxedCentered(String text, int cw) {
        int left = Math.max(0, (cw - text.length()) / 2);
        int right = Math.max(0, cw - text.length() - left);
        System.out.println("| " + " ".repeat(left) + text + " ".repeat(right) + " |");
    }

    private void printFramedFooter(String border, int cw) {
        System.out.println();
        System.out.println(border);
        printBoxedCentered("END OF REPORT", cw);
        System.out.println(border);
    }

    private static final int CHART_MAX_ROWS = 10;

    // round the y-axis step up to a tidy 1/2/5 x 10^k so it reads 10, 20, 30...
    private int niceStep(int maxValue, int maxRows) {
        if (maxValue <= maxRows) {
            return 1;
        }
        int raw = (int) Math.ceil((double) maxValue / maxRows);
        int pow = 1;
        while (true) {
            int[] nice = {1, 2, 5};
            for (int n : nice) {
                int step = n * pow;
                if (step >= raw) {
                    return step;
                }
            }
            pow *= 10;
        }
    }

    // two bar charts side by side; tall counts get scaled down to keep it short
    private void displaySideBySideBarCharts(String leftTitle, String[] leftLabels, int[] leftValues,
            String rightTitle, String[] rightLabels, int[] rightValues) {
        int leftMax = 0;
        for (int v : leftValues) {
            if (v > leftMax) {
                leftMax = v;
            }
        }
        int rightMax = 0;
        for (int v : rightValues) {
            if (v > rightMax) {
                rightMax = v;
            }
        }
        int step = niceStep(Math.max(leftMax, rightMax), CHART_MAX_ROWS);

        int[] leftHeights = new int[leftValues.length];
        for (int i = 0; i < leftValues.length; i++) {
            leftHeights[i] = leftValues[i] <= 0 ? 0
                    : Math.max(1, (int) Math.round((double) leftValues[i] / step));
        }
        int[] rightHeights = new int[rightValues.length];
        for (int i = 0; i < rightValues.length; i++) {
            rightHeights[i] = rightValues[i] <= 0 ? 0
                    : Math.max(1, (int) Math.round((double) rightValues[i] / step));
        }
        int topRows = 1;
        for (int h : leftHeights) {
            if (h > topRows) {
                topRows = h;
            }
        }
        for (int h : rightHeights) {
            if (h > topRows) {
                topRows = h;
            }
        }

        int cats = Math.max(leftLabels.length, rightLabels.length);
        int colWidth = 6 + 11 * cats;

        System.out.println();
        System.out.println("GRAPHICAL REPRESENTATION OF FRONT-DESK MODULE");
        if (step > 1) {
            System.out.println("(each row = " + step + " bookings; exact count shown under each bar)");
        }
        System.out.println();
        System.out.printf("%-" + colWidth + "s | %-" + colWidth + "s%n", leftTitle, rightTitle);
        System.out.printf("%-" + colWidth + "s | %-" + colWidth + "s%n", "^", "^");

        for (int level = topRows; level >= 1; level--) {
            String leftRow = String.format("%4d |", level * step);
            for (int h : leftHeights) {
                leftRow += String.format(" %-10s", h >= level ? "####" : "");
            }
            String rightRow = String.format("%4d |", level * step);
            for (int h : rightHeights) {
                rightRow += String.format(" %-10s", h >= level ? "####" : "");
            }
            System.out.printf("%-" + colWidth + "s | %-" + colWidth + "s%n", leftRow, rightRow);
        }

        String leftAxis = "   0 +" + "-----------".repeat(leftLabels.length);
        String rightAxis = "   0 +" + "-----------".repeat(rightLabels.length);
        System.out.printf("%-" + colWidth + "s | %-" + colWidth + "s%n", leftAxis, rightAxis);

        String leftLabelRow = "      ";
        for (String label : leftLabels) {
            leftLabelRow += String.format(" %-10.10s", label);
        }
        String rightLabelRow = "      ";
        for (String label : rightLabels) {
            rightLabelRow += String.format(" %-10.10s", label);
        }
        System.out.printf("%-" + colWidth + "s | %-" + colWidth + "s%n", leftLabelRow, rightLabelRow);

        String leftValueRow = "      ";
        for (int v : leftValues) {
            leftValueRow += String.format(" %-10d", v);
        }
        String rightValueRow = "      ";
        for (int v : rightValues) {
            rightValueRow += String.format(" %-10d", v);
        }
        System.out.printf("%-" + colWidth + "s | %-" + colWidth + "s%n", leftValueRow, rightValueRow);
    }
}
