package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.HousekeepingControl;
import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.entity.StageDuration;
import com.tarumt.resorts.adt.Queue;
import java.util.Scanner;

/**
 * HouseKeepingUI.java
 * Console interface for the Housekeeping & Task Log module.
 *
 * @author YourName
 */
public class HouseKeepingUI {

    private HousekeepingControl control;
    private Scanner sc;

    public HouseKeepingUI() {
        control = new HousekeepingControl();
        sc = new Scanner(System.in);
    }

    public void showMenu() {
        int choice;
        do {
            System.out.println("\n=== Housekeeping & Task Log Module ===");
            System.out.println("1. Log new room status change");
            System.out.println("2. View current status of a room");
            System.out.println("3. Rollback last status change");
            System.out.println("4. View full history of a room");
            System.out.println("5. Report 1: Rooms by current status");
            System.out.println("6. Report 2: Avg time per stage");
            System.out.println("0. Back to main menu");
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                choice = -1;
                continue;
            }

            switch (choice) {
                case 1 -> logStatusChange();
                case 2 -> viewCurrentStatus();
                case 3 -> rollbackLastChange();
                case 4 -> viewFullHistory();
                case 5 -> reportByStatus();
                case 6 -> reportAverageDuration();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private void logStatusChange() {
        String roomNumber;
        while (true) {
            System.out.print("Enter room number: ");
            roomNumber = sc.nextLine();
            if (control.isValidRoomNumber(roomNumber)) {
                break;
            }
            System.out.println("Room number not found in the system. Please try again.");
        }

        String status;
        while (true) {
            System.out.print("Enter status (DIRTY, CLEANING, INSPECTED, READY): ");
            status = sc.nextLine();
            if (control.isValidNextStatus(roomNumber, status)) {
                break;
            }
            System.out.println("Invalid status. Please try again.");
        }

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        boolean success = control.logStatusChange(roomNumber, status.toUpperCase(), timestamp);
        if (success) {
            System.out.println("Status logged: Room " + roomNumber + " -> " + status.toUpperCase());
        } else if (!control.isValidRoomNumber(roomNumber)) {
            System.out.println("Failed to log status — room not found.");
        } else {
            System.out.println("Failed to log status — invalid sequence. Status must follow DIRTY -> CLEANING -> INSPECTED -> READY.");
        }
    }

    private void viewCurrentStatus() {
        System.out.print("Enter room number: ");
        String roomNumber = sc.nextLine();

        RoomStatusLog current = control.getCurrentStatus(roomNumber);
        if (current == null) {
            System.out.println("No status log found for this room.");
        } else {
            String roomType = control.getRoomType(roomNumber);
            System.out.println("Room Type: " + roomType);
            System.out.println("Current status: " + current);
        }
    }

    private void rollbackLastChange() {
        RoomStatusLog preview = control.previewLastChange();
        if (preview == null) {
            System.out.println("No status log entries to rollback.");
            return;
        }

        System.out.println("Most recent entry: " + preview);
        System.out.print("Confirm rollback? (Y/N): ");
        String confirm = sc.nextLine();

        if (confirm.equalsIgnoreCase("Y")) {
            String roomNumber = preview.getRoomNumber();
            RoomStatusLog removed = control.rollbackLastChange();
            RoomStatusLog newCurrent = control.getCurrentStatus(roomNumber);

            System.out.println("Removed entry: " + removed);
            if (newCurrent != null) {
                System.out.println("Room " + roomNumber + " status reverted to: " + newCurrent.getStatus());
            } else {
                System.out.println("Room " + roomNumber + " now has no status history (this was its only entry).");
            }
        } else {
            System.out.println("Rollback cancelled.");
        }
    }

    private void viewFullHistory() {
        System.out.print("Enter room number: ");
        String roomNumber = sc.nextLine();

        Queue<RoomStatusLog> history = control.getHistoryForRoom(roomNumber);
        int total = history.getNumberOfEntries();

        if (total == 0) {
            System.out.println("No status history found for this room.");
            return;
        }

        System.out.println("\n--------------------------------------------------------");
        System.out.println("Status history for Room " + roomNumber + ":");
        System.out.println("--------------------------------------------------------");
        System.out.printf("%-15s %-16s%n", "Status", "Timestamp");
        System.out.println("--------------------------------------------------------");
        for (int i = 0; i < total; i++) {
            RoomStatusLog entry = history.getEntry(i);
            System.out.printf("%-15s %-16s%n", entry.getStatus(), entry.getTimestamp());
        }
        System.out.println("--------------------------------------------------------");
        System.out.println("Total entries: " + total);
    }

    private void reportByStatus() {
        String statusFilter;
        while (true) {
            System.out.print("Enter status to filter by (DIRTY, CLEANING, INSPECTED, READY): ");
            statusFilter = sc.nextLine();
            if (control.isValidStatus(statusFilter)) {
                break;
            }
            System.out.println("Invalid status entered. Please enter one of: DIRTY, CLEANING, INSPECTED, READY.");
        }

        String roomTypeFilter;
        while (true) {
            System.out.print("Filter by room type (Standard/Deluxe/Suite), or press Enter for ALL: ");
            String roomTypeInput = sc.nextLine().trim();
            if (roomTypeInput.isEmpty()) {
                roomTypeFilter = "ALL";
                break;
            }
            if (control.isValidRoomType(roomTypeInput)) {
                roomTypeFilter = roomTypeInput;
                break;
            }
            System.out.println("Invalid room type entered. Please enter Standard, Deluxe, Suite, or press Enter for ALL.");
        }

        Queue<RoomStatusLog> filtered = control.getRoomsByCurrentStatus(statusFilter.toUpperCase(), roomTypeFilter);

        System.out.println("\n========================================================");
        System.out.println("        TARUMT RESORTS - HOUSEKEEPING STATUS REPORT");
        System.out.println("--------------------------------------------------------");
        System.out.println("Generated at: " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm a")));
        System.out.println("Filter: Status = " + statusFilter.toUpperCase()
                + ", Room Type = " + (roomTypeFilter.equalsIgnoreCase("ALL") ? "ALL" : roomTypeFilter));
        System.out.println("Sorted by: Room number (ascending)");
        System.out.println("========================================================");
        System.out.printf("%-10s %-12s %-15s %-16s%n", "Room", "Type", "Status", "Last Updated");
        System.out.println("--------------------------------------------------------");

        int total = filtered.getNumberOfEntries();
        if (total == 0) {
            System.out.println("No matching records found.");
        } else {
            for (int i = 0; i < total; i++) {
                RoomStatusLog entry = filtered.getEntry(i);
                String roomType = control.getRoomType(entry.getRoomNumber());
                System.out.printf("%-10s %-12s %-15s %-16s%n",
                        entry.getRoomNumber(), roomType, entry.getStatus(), entry.getTimestamp());
            }
        }

        System.out.println("--------------------------------------------------------");
        System.out.println("Total records: " + total);
        System.out.println("========================================================");
    }

    private void reportAverageDuration() {
        String stageFilter;
        while (true) {
            System.out.print("Filter by stage (DIRTY/CLEANING/INSPECTED/READY), or press Enter for ALL: ");
            String stageInput = sc.nextLine().trim();
            if (stageInput.isEmpty()) {
                stageFilter = "ALL";
                break;
            }
            if (control.isValidStatus(stageInput)) {
                stageFilter = stageInput;
                break;
            }
            System.out.println("Invalid stage entered. Please enter DIRTY, CLEANING, INSPECTED, READY, or press Enter for ALL.");
        }

        Queue<StageDuration> report = control.getAverageDurationPerStage(stageFilter);

        System.out.println("\n========================================================");
        System.out.println("     TARUMT RESORTS - AVG TIME PER CLEANING STAGE REPORT");
        System.out.println("--------------------------------------------------------");
        System.out.println("Generated at: " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm a")));
        System.out.println("Filter: Stage = " + (stageFilter.equalsIgnoreCase("ALL") ? "ALL" : stageFilter.toUpperCase()));
        System.out.println("Sorted by: Average duration (longest first)");
        System.out.println("========================================================");
        System.out.printf("%-15s %-10s%n", "Stage", "Avg (min)");
        System.out.println("--------------------------------------------------------");

        int total = report.getNumberOfEntries();
        if (total == 0) {
            System.out.println("No data available for this filter.");
        } else {
            for (int i = 0; i < total; i++) {
                System.out.println(report.getEntry(i));
            }
        }

        System.out.println("--------------------------------------------------------");
        System.out.println("Total stages measured: " + total);
        System.out.println("========================================================");
    }
}