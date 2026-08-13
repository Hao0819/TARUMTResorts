package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.VIPAllocationControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.VIPAllocationRequest;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * VIPAllocationUI.java
 * Console interface for the VIP & Loyalty Tier Priority Room Allocation
 * module.
 *
 * @author brian
 */
public class VIPAllocationUI {

    private VIPAllocationControl control;
    private Scanner sc;

    public VIPAllocationUI() {
        this(new VIPAllocationControl(), new Scanner(System.in));
    }

    public VIPAllocationUI(VIPAllocationControl sharedControl) {
        this(sharedControl, new Scanner(System.in));
    }

    public VIPAllocationUI(VIPAllocationControl sharedControl, Scanner sharedScanner) {
        control = sharedControl;
        sc = sharedScanner;
    }

    public void showMenu() {
        int choice;
        do {
            int menuWidth = 64;
            String menuBorder = "+" + "-".repeat(menuWidth + 2) + "+";
            String menuTitle = "VIP PRIORITY ALLOCATION MODULE";
            int leftPadding = (menuWidth - menuTitle.length()) / 2;
            int rightPadding = menuWidth - menuTitle.length() - leftPadding;

            System.out.println();
            System.out.println(menuBorder);
            System.out.println("| " + " ".repeat(leftPadding) + menuTitle
                    + " ".repeat(rightPadding) + " |");
            System.out.println(menuBorder);
            System.out.printf("| %-64s |%n", "1. Register new VIP allocation request");
            System.out.printf("| %-64s |%n", "2. Allocate next VIP guest");
            System.out.printf("| %-64s |%n", "3. View VIP priority queue");
            System.out.printf("| %-64s |%n", "4. Update waiting VIP request");
            System.out.printf("| %-64s |%n", "5. Cancel waiting VIP request");
            System.out.printf("| %-64s |%n", "6. VIP Priority Queue Report");
            System.out.printf("| %-64s |%n", "7. VIP Allocation History Report");
            System.out.printf("| %-64s |%n", "0. Back to main menu");
            System.out.println(menuBorder);
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                choice = -1;
                continue;
            }

            switch (choice) {
                case 1 -> registerRequest();
                case 2 -> allocateNextGuest();
                case 3 -> displayPriorityQueue();
                case 4 -> updateWaitingRequest();
                case 5 -> cancelWaitingRequest();
                case 6 -> displayPriorityQueueReport();
                case 7 -> displayAllocationHistoryReport();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }

            // Keep the result on screen instead of immediately reprinting
            // the menu underneath it - the user decides when to continue.
            if (choice != 0) {
                System.out.print("\nPress Enter to return to the VIP Allocation menu...");
                sc.nextLine();
            }
        } while (choice != 0);
    }

    // =====================================================================
    // Shared prompt helpers. Each returns null when the user enters "0",
    // so every flow that uses them can be backed out of at any step.
    // =====================================================================

    private String promptRoomTypeCode() {
        while (true) {
            System.out.print("Enter room type (D = Deluxe, S = Standard, SU = Suite, 0 = back): ");
            String code = sc.nextLine().trim().toUpperCase();
            if (code.equals("0")) {
                return null;
            }
            String roomType;
            switch (code) {
                case "D" -> { roomType = "Deluxe"; }
                case "S" -> { roomType = "Standard"; }
                case "SU" -> { roomType = "Suite"; }
                default -> { roomType = null; }
            }
            if (roomType != null) {
                return roomType;
            }
            System.out.println("Invalid room type code. Please try again.");
        }
    }

    private LocalDate promptFutureDate(String label) {
        while (true) {
            System.out.print(label + " (yyyy-MM-dd, 0 = back): ");
            String input = sc.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            try {
                LocalDate parsed = LocalDate.parse(input);
                if (parsed.isBefore(LocalDate.now())) {
                    System.out.println("Date cannot be in the past.");
                    continue;
                }
                return parsed;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Example: 2026-08-20");
            }
        }
    }

    /** Returns null when the user enters "0" (back) instead of a duration. */
    private Integer promptStayDuration() {
        while (true) {
            System.out.print("Enter stay duration in nights (1-30, 0 = back): ");
            String input = sc.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            try {
                int nights = Integer.parseInt(input);
                if (nights >= 1 && nights <= 30) {
                    return nights;
                }
            } catch (NumberFormatException ignored) {
                // message below handles it
            }
            System.out.println("Stay duration must be between 1 and 30 nights.");
        }
    }

    // =====================================================================
    // Register a new VIP request.
    // =====================================================================

    private void registerRequest() {
        System.out.print("\nEnter Guest ID (0 = back): ");
        String guestId = sc.nextLine().trim();
        if (guestId.equals("0")) {
            System.out.println("Registration cancelled.");
            return;
        }

        String roomType = promptRoomTypeCode();
        if (roomType == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        LocalDate checkInDate = promptFutureDate("Enter requested check-in date");
        if (checkInDate == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        Integer stayDurationDays = promptStayDuration();
        if (stayDurationDays == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        VIPAllocationRequest request = control.registerVIPRequest(
                guestId, roomType, checkInDate, stayDurationDays);

        if (request == null) {
            System.out.println(
                    "Unable to register request. The Guest ID may not exist, "
                    + "may not hold a priority membership tier, "
                    + "or the requested room type / schedule may be invalid.");
            return;
        }

        System.out.println();
        System.out.println("VIP request registered successfully.");
        System.out.println("Request ID : " + request.getRequestId());
        System.out.println("Guest      : " + request.getGuest().getName()
                + " (" + request.getGuest().getMembershipTier() + ")");
        System.out.println("Room Type  : " + request.getRequestedRoomType());
        System.out.println("Check-In   : " + request.getRequestedCheckInDate());
        System.out.println("Check-Out  : " + request.getRequestedCheckOutDate());
        System.out.println("Nights     : " + request.getStayDurationDays());
        System.out.println("Time       : " + request.getRequestTime());
    }

    // =====================================================================
    // Update / cancel a WAITING request.
    // =====================================================================

    private void updateWaitingRequest() {
        if (control.getWaitingCount() == 0) {
            System.out.println("No VIP requests are waiting.");
            return;
        }

        displayPriorityQueue();

        System.out.print("\nEnter Request ID to update (0 = back): ");
        String requestId = sc.nextLine().trim();
        if (requestId.equals("0")) {
            System.out.println("Update cancelled.");
            return;
        }

        System.out.print("Enter Guest ID for verification (0 = back): ");
        String guestId = sc.nextLine().trim();
        if (guestId.equals("0")) {
            System.out.println("Update cancelled.");
            return;
        }

        if (requestId.isEmpty() || guestId.isEmpty()) {
            System.out.println("Request ID and Guest ID cannot be blank.");
            return;
        }

        VIPAllocationRequest selected = control.findWaitingRequestById(requestId, guestId);
        if (selected == null) {
            System.out.println(
                    "No active WAITING VIP request matches the entered Request ID and Guest ID.");
            return;
        }

        System.out.println();
        System.out.println("Current Room Type : " + selected.getRequestedRoomType());
        System.out.println("Current Check-In  : " + selected.getRequestedCheckInDate());
        System.out.println("Current Nights    : " + selected.getStayDurationDays());
        System.out.println();

        String newRoomType = promptRoomTypeCode();
        if (newRoomType == null) {
            System.out.println("Update cancelled.");
            return;
        }

        LocalDate newCheckInDate = promptFutureDate("Enter new check-in date");
        if (newCheckInDate == null) {
            System.out.println("Update cancelled.");
            return;
        }

        Integer newStayDurationDays = promptStayDuration();
        if (newStayDurationDays == null) {
            System.out.println("Update cancelled.");
            return;
        }

        boolean updated = control.updateVIPRequest(
                requestId, guestId, newRoomType, newCheckInDate, newStayDurationDays);

        if (updated) {
            System.out.println("VIP request updated successfully.");
        } else {
            System.out.println("Unable to update the VIP request.");
        }
    }

    private void cancelWaitingRequest() {
        if (control.getWaitingCount() == 0) {
            System.out.println("No VIP requests are waiting.");
            return;
        }

        displayPriorityQueue();

        System.out.print("\nEnter Request ID to cancel (0 = back): ");
        String requestId = sc.nextLine().trim();
        if (requestId.equals("0")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        System.out.print("Enter Guest ID for verification (0 = back): ");
        String guestId = sc.nextLine().trim();
        if (guestId.equals("0")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        if (requestId.isEmpty() || guestId.isEmpty()) {
            System.out.println("Request ID and Guest ID cannot be blank.");
            return;
        }

        VIPAllocationRequest selected = control.findWaitingRequestById(requestId, guestId);
        if (selected == null) {
            System.out.println(
                    "No active WAITING VIP request matches the entered Request ID and Guest ID.");
            return;
        }

        System.out.print("Confirm cancellation (Y/N): ");
        String confirmation = sc.nextLine().trim();
        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        boolean cancelled = control.cancelVIPRequest(requestId, guestId);
        if (cancelled) {
            System.out.println("VIP request cancelled successfully.");
        } else {
            System.out.println("Cancellation failed.");
        }
    }

    // =====================================================================
    // Allocate next guest.
    // =====================================================================

    private void allocateNextGuest() {
        if (control.getWaitingCount() == 0) {
            System.out.println("The VIP priority queue is empty.");
            return;
        }

        Booking booking = control.allocateNextVIPGuest();

        if (booking == null) {
            System.out.println(
                    "No matching available room for the front VIP guest's requested dates. "
                    + "The guest remains at the front of the priority queue.");
            return;
        }

        System.out.println();
        System.out.println("VIP booking created successfully.");
        System.out.println("Confirmation No     : " + booking.getConfirmationNumber());
        System.out.println("Guest               : " + booking.getGuest().getName()
                + " (" + booking.getGuest().getMembershipTier() + ")");
        System.out.println("Room                : " + booking.getRoom().getRoomNumber()
                + " (" + booking.getRoom().getRoomType() + ")");
        System.out.println("Scheduled Check-In  : " + booking.getScheduledCheckInDate());
        System.out.println("Scheduled Check-Out : " + booking.getScheduledCheckOutDate());
        System.out.println("Stay Duration       : " + booking.getStayDurationDays() + " night(s)");
        System.out.printf("Amount              : RM %,.2f%n", booking.getAmount());
        System.out.println("Payment Status      : " + booking.getPaymentStatus());
        System.out.println("Booking Status      : " + booking.getStatus());
        System.out.println("Booking Time        : " + booking.getBookingCreatedTime());
    }

    // =====================================================================
    // View / display the priority list.
    // =====================================================================

    private void displayPriorityQueue() {
        VIPAllocationRequest[] list = control.getPriorityListInOrder();
        printPriorityTable(list, "VIP PRIORITY ALLOCATION QUEUE");
    }

    private void printPriorityTable(VIPAllocationRequest[] list, String title) {
        String border = "+-----+----------+----------------------+----------+"
                + "------------+------------------+------------+------------+";
        int contentWidth = border.length() - 4;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title
                + " ".repeat(rightPadding) + " |");
        System.out.println(border);

        if (list.length == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No VIP guests match the current view.");
            System.out.println(border);
            return;
        }

        System.out.printf("| %-3s | %-8s | %-20s | %-8s | %-10s | %-16s | %-10s | %-10s |%n",
                "Pos", "Req ID", "Guest Name", "Tier", "Room Type", "Request Time",
                "Check-In", "Check-Out");
        System.out.println(border);

        for (int i = 0; i < list.length; i++) {
            VIPAllocationRequest r = list[i];
            System.out.printf("| %-3d | %-8.8s | %-20.20s | %-8.8s | %-10.10s | %-16.16s | %-10s | %-10s |%n",
                    i + 1,
                    r.getRequestId(),
                    r.getGuest().getName(),
                    r.getGuest().getMembershipTier(),
                    r.getRequestedRoomType(),
                    r.getRequestTime(),
                    r.getRequestedCheckInDate(),
                    r.getRequestedCheckOutDate());
        }

        System.out.println(border);
        System.out.println("Total records: " + list.length);
    }

    // =====================================================================
    // Report 1: VIP Priority Queue Report (filter by tier).
    // =====================================================================

    private void displayPriorityQueueReport() {
        String tierFilter = readTierFilter();
        if (tierFilter == null) {
            System.out.println("Report cancelled.");
            return;
        }

        VIPAllocationRequest[] fullQueue = control.getPriorityListInOrder();

        int matchCount = 0;
        for (VIPAllocationRequest r : fullQueue) {
            if (tierFilter.equalsIgnoreCase("ALL")
                    || r.getGuest().getMembershipTier().toString().equalsIgnoreCase(tierFilter)) {
                matchCount++;
            }
        }

        VIPAllocationRequest[] filtered = new VIPAllocationRequest[matchCount];
        int index = 0;
        for (VIPAllocationRequest r : fullQueue) {
            if (tierFilter.equalsIgnoreCase("ALL")
                    || r.getGuest().getMembershipTier().toString().equalsIgnoreCase(tierFilter)) {
                filtered[index++] = r;
            }
        }

        printPriorityTable(filtered, "VIP PRIORITY QUEUE REPORT (Filter: " + tierFilter + ")");
    }

    // =====================================================================
    // Report 2: VIP Allocation History Report (filter tier + status, sorted).
    // =====================================================================

    private void displayAllocationHistoryReport() {
        String tierFilter = readTierFilter();
        if (tierFilter == null) {
            System.out.println("Report cancelled.");
            return;
        }

        String statusFilter = readStatusFilter();
        if (statusFilter == null) {
            System.out.println("Report cancelled.");
            return;
        }

        VIPAllocationRequest[] results = control.filterRequestHistory(tierFilter, statusFilter);
        control.sortByRequestTime(results);

        String border = "+----------+----------------------+----------+----------+------------+"
                + "------------------+------------+------------+-----------+";
        String title = "VIP ALLOCATION HISTORY REPORT";
        int contentWidth = border.length() - 4;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title
                + " ".repeat(rightPadding) + " |");
        System.out.println(border);
        System.out.printf("| %-" + contentWidth + "s |%n",
                "Filters: Tier = " + tierFilter + " | Status = " + statusFilter);
        System.out.printf("| %-" + contentWidth + "s |%n", "Sorted by: Request Time (Ascending)");
        System.out.println(border);
        System.out.printf("| %-8s | %-20s | %-8s | %-8s | %-10s | %-16s | %-10s | %-10s | %-9s |%n",
                "Req ID", "Guest Name", "Tier", "Guest ID", "Room Type",
                "Request Time", "Check-In", "Check-Out", "Status");
        System.out.println(border);

        if (results.length == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No records match the selected filters.");
        } else {
            for (VIPAllocationRequest r : results) {
                System.out.printf(
                        "| %-8.8s | %-20.20s | %-8.8s | %-8.8s | %-10.10s | %-16.16s | %-10s | %-10s | %-9.9s |%n",
                        r.getRequestId(),
                        r.getGuest().getName(),
                        r.getGuest().getMembershipTier(),
                        r.getGuest().getGuestId(),
                        r.getRequestedRoomType(),
                        r.getRequestTime(),
                        r.getRequestedCheckInDate(),
                        r.getRequestedCheckOutDate(),
                        r.getStatus());
            }
        }
        System.out.println(border);
        System.out.println("Total matching records: " + results.length);
    }

    // =====================================================================
    // Filter prompts.
    // =====================================================================

    /** Returns null when the user chooses "0. Back" instead of a tier. */
    private String readTierFilter() {
        while (true) {
            System.out.println("\nMembership Tier Filter");
            System.out.println("1. All Priority Tiers");
            System.out.println("2. PLATINUM");
            System.out.println("3. DIAMOND");
            System.out.println("4. ELITE");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "ALL";
                case "2": return "PLATINUM";
                case "3": return "DIAMOND";
                case "4": return "ELITE";
                case "0": return null;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /** Returns null when the user chooses "0. Back" instead of a status. */
    private String readStatusFilter() {
        while (true) {
            System.out.println("\nRequest Status Filter");
            System.out.println("1. All Statuses");
            System.out.println("2. Waiting");
            System.out.println("3. Assigned");
            System.out.println("4. Cancelled");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "ALL";
                case "2": return "WAITING";
                case "3": return "ASSIGNED";
                case "4": return "CANCELLED";
                case "0": return null;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}