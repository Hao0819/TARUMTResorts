package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.VIPAllocationControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.VIPAllocationRequest;

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
            System.out.printf("| %-64s |%n", "4. VIP Priority Queue Report");
            System.out.printf("| %-64s |%n", "5. VIP Allocation History Report");
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
                case 4 -> displayPriorityQueueReport();
                case 5 -> displayAllocationHistoryReport();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    // =====================================================================
    // Register a new VIP request.
    // =====================================================================

    private void registerRequest() {
        System.out.print("\nEnter Guest ID: ");
        String guestId = sc.nextLine().trim();

        String roomType;
        while (true) {
            System.out.print("Enter room type (D = Deluxe, S = Standard, SU = Suite): ");
            String code = sc.nextLine().trim().toUpperCase();
            switch (code) {
                case "D" -> { roomType = "Deluxe"; }
                case "S" -> { roomType = "Standard"; }
                case "SU" -> { roomType = "Suite"; }
                default -> { roomType = null; }
            }
            if (roomType != null) {
                break;
            }
            System.out.println("Invalid room type code. Please try again.");
        }

        VIPAllocationRequest request = control.registerVIPRequest(guestId, roomType);

        if (request == null) {
            System.out.println(
                    "Unable to register request. The Guest ID may not exist, "
                    + "may not hold a priority membership tier, "
                    + "or already has an active request.");
            return;
        }

        System.out.println();
        System.out.println("VIP request registered successfully.");
        System.out.println("Request ID : " + request.getRequestId());
        System.out.println("Guest      : " + request.getGuest().getName()
                + " (" + request.getGuest().getMembershipTier() + ")");
        System.out.println("Room Type  : " + request.getRequestedRoomType());
        System.out.println("Time       : " + request.getRequestTime());
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
                    "No matching available room for the front VIP guest. "
                    + "The guest remains at the front of the priority queue.");
            return;
        }

        System.out.println();
        System.out.println("VIP booking created successfully.");
        System.out.println("Confirmation No : " + booking.getConfirmationNumber());
        System.out.println("Guest           : " + booking.getGuest().getName()
                + " (" + booking.getGuest().getMembershipTier() + ")");
        System.out.println("Room            : " + booking.getRoom().getRoomNumber()
                + " (" + booking.getRoom().getRoomType() + ")");
        System.out.println("Booking Time    : " + booking.getBookingCreatedTime());
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
                + "------------+------------------+";
        int contentWidth = 92;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title
                + " ".repeat(rightPadding) + " |");
        System.out.println(border);

        if (list.length == 0) {
            System.out.printf("| %-92s |%n", "No VIP guests match the current view.");
            System.out.println(border);
            return;
        }

        System.out.printf("| %-3s | %-8s | %-20s | %-8s | %-10s | %-16s |%n",
                "Pos", "Req ID", "Guest Name", "Tier", "Room Type", "Request Time");
        System.out.println(border);

        for (int i = 0; i < list.length; i++) {
            VIPAllocationRequest r = list[i];
            System.out.printf("| %-3d | %-8.8s | %-20.20s | %-8.8s | %-10.10s | %-16.16s |%n",
                    i + 1,
                    r.getRequestId(),
                    r.getGuest().getName(),
                    r.getGuest().getMembershipTier(),
                    r.getRequestedRoomType(),
                    r.getRequestTime());
        }

        System.out.println(border);
        System.out.println("Total records: " + list.length);
    }

    // =====================================================================
    // Report 1: VIP Priority Queue Report (filter by tier).
    // =====================================================================

    private void displayPriorityQueueReport() {
        String tierFilter = readTierFilter();
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
        String statusFilter = readStatusFilter();

        VIPAllocationRequest[] results = control.filterRequestHistory(tierFilter, statusFilter);
        control.sortByRequestTime(results);

        String border = "+----------+----------------------+----------+----------+------------+------------------+-----------+";
        String title = "VIP ALLOCATION HISTORY REPORT";
        int contentWidth = 105;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title
                + " ".repeat(rightPadding) + " |");
        System.out.println(border);
        System.out.printf("| %-105s |%n",
                "Filters: Tier = " + tierFilter + " | Status = " + statusFilter);
        System.out.printf("| %-105s |%n", "Sorted by: Request Time (Ascending)");
        System.out.println(border);
        System.out.printf("| %-8s | %-20s | %-8s | %-8s | %-10s | %-16s | %-9s |%n",
                "Req ID", "Guest Name", "Tier", "Guest ID", "Room Type",
                "Request Time", "Status");
        System.out.println(border);

        if (results.length == 0) {
            System.out.printf("| %-105s |%n", "No records match the selected filters.");
        } else {
            for (VIPAllocationRequest r : results) {
                System.out.printf("| %-8.8s | %-20.20s | %-8.8s | %-8.8s | %-10.10s | %-16.16s | %-9.9s |%n",
                        r.getRequestId(),
                        r.getGuest().getName(),
                        r.getGuest().getMembershipTier(),
                        r.getGuest().getGuestId(),
                        r.getRequestedRoomType(),
                        r.getRequestTime(),
                        r.getStatus());
            }
        }
        System.out.println(border);
        System.out.println("Total matching records: " + results.length);
    }

    // =====================================================================
    // Filter prompts.
    // =====================================================================

    private String readTierFilter() {
        while (true) {
            System.out.println("\nMembership Tier Filter");
            System.out.println("1. All Priority Tiers");
            System.out.println("2. PLATINUM");
            System.out.println("3. DIAMOND");
            System.out.println("4. ELITE");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "ALL";
                case "2": return "PLATINUM";
                case "3": return "DIAMOND";
                case "4": return "ELITE";
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private String readStatusFilter() {
        while (true) {
            System.out.println("\nRequest Status Filter");
            System.out.println("1. All Statuses");
            System.out.println("2. Waiting");
            System.out.println("3. Assigned");
            System.out.println("4. Cancelled");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "ALL";
                case "2": return "WAITING";
                case "3": return "ASSIGNED";
                case "4": return "CANCELLED";
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
