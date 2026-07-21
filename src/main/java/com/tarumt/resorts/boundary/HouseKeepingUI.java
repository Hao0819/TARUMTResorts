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
        this(new HousekeepingControl(), new Scanner(System.in));
    }

    public HouseKeepingUI(HousekeepingControl sharedControl) {
        this(sharedControl, new Scanner(System.in));
    }

    public HouseKeepingUI(HousekeepingControl sharedControl, Scanner sharedScanner) {
        control = sharedControl;
        sc = sharedScanner;
    }

    // Prints a title centered inside a "| ... |" row of the given width.
    private void printCentered(String text, int width) {
        if (text.length() >= width) {
            System.out.printf("| %-" + width + "s |%n", text.substring(0, width));
            return;
        }
        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        System.out.println("| " + " ".repeat(leftPadding) + text + " ".repeat(rightPadding) + " |");
    }

    public void showMenu() {
        int choice;
        do {
            int menuWidth = 64;
            String menuBorder = "+" + "-".repeat(menuWidth + 2) + "+";
            String menuTitle = "HOUSEKEEPING & TASK LOG MODULE";
            int leftPadding = (menuWidth - menuTitle.length()) / 2;
            int rightPadding = menuWidth - menuTitle.length() - leftPadding;

            System.out.println();
            System.out.println(menuBorder);
            System.out.println("| " + " ".repeat(leftPadding) + menuTitle + " ".repeat(rightPadding) + " |");
            System.out.println(menuBorder);
            System.out.printf("| %-64s |%n", "1. Log new room status change");
            System.out.printf("| %-64s |%n", "2. View current status of a room");
            System.out.printf("| %-64s |%n", "3. Rollback last status change (global)");
            System.out.printf("| %-64s |%n", "4. View full history of a room");
            System.out.printf("| %-64s |%n", "5. Report 1: Rooms by current status");
            System.out.printf("| %-64s |%n", "6. Report 2: Avg time per stage");
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
            String border = "+------------+------------------+------------------+";
            int contentWidth = border.length() - 4;
            System.out.println();
            System.out.println(border);
            printCentered("STATUS LOGGED", contentWidth);
            System.out.println(border);
            System.out.printf("| %-10s | %-16s | %-16s |%n", "Room", "New Status", "Timestamp");
            System.out.println(border);
            System.out.printf("| %-10.10s | %-16.16s | %-16.16s |%n",
                    roomNumber, status.toUpperCase(), timestamp);
            System.out.println(border);
        } else if (!control.isValidRoomNumber(roomNumber)) {
            System.out.println("Failed to log status - room not found.");
        } else {
            System.out.println(
                    "Failed to log status - invalid sequence. Status must follow DIRTY -> CLEANING -> INSPECTED -> READY.");
        }
    }

    /**
     * Fix for issue #25: validate the room number BEFORE searching for
     * a status log, so the user gets a clear, distinct message for
     * "this room doesn't exist" versus "this room exists but has no
     * cleaning history yet" — instead of both cases looking identical.
     */
    private void viewCurrentStatus() {
        System.out.print("Enter room number: ");
        String roomNumber = sc.nextLine();

        String border = "+------------+------------+------------------+------------------+";
        int contentWidth = border.length() - 4;

        if (!control.isValidRoomNumber(roomNumber)) {
            System.out.println();
            System.out.println(border);
            printCentered("CURRENT ROOM STATUS", contentWidth);
            System.out.println(border);
            System.out.printf("| %-" + contentWidth + "s |%n",
                    "Room " + roomNumber + " does not exist in the system.");
            System.out.println(border);
            return;
        }

        RoomStatusLog current = control.getCurrentStatus(roomNumber);
        String roomType = control.getRoomType(roomNumber);

        System.out.println();
        System.out.println(border);
        printCentered("CURRENT ROOM STATUS", contentWidth);
        System.out.println(border);
        System.out.printf("| %-10s | %-10s | %-16s | %-16s |%n", "Room", "Type", "Status", "Last Updated");
        System.out.println(border);
        if (current == null) {
            System.out.printf("| %-10.10s | %-10.10s | %-16s | %-16s |%n",
                    roomNumber, roomType, "UNKNOWN", "No history yet");
        } else {
            System.out.printf("| %-10.10s | %-10.10s | %-16.16s | %-16.16s |%n",
                    roomNumber, roomType, current.getStatus(), current.getTimestamp());
        }
        System.out.println(border);
    }

    private void rollbackLastChange() {
        RoomStatusLog preview = control.previewLastChange();
        if (preview == null) {
            System.out.println("No status log entries to rollback.");
            return;
        }
        System.out.println("Most recent entry (global, across all rooms): " + preview);
        System.out.print("Confirm rollback? (Y/N): ");
        String confirm = sc.nextLine();
        if (confirm.equalsIgnoreCase("Y")) {
            String roomNumber = preview.getRoomNumber();
            RoomStatusLog removed = control.rollbackLastChange();
            RoomStatusLog newCurrent = control.getCurrentStatus(roomNumber);

            String border = "+------------+------------------+------------------+";
            int contentWidth = border.length() - 4;
            System.out.println();
            System.out.println(border);
            printCentered("ROLLBACK RESULT", contentWidth);
            System.out.println(border);
            System.out.printf("| %-10s | %-16s | %-16s |%n", "Room", "Removed Status", "Removed Time");
            System.out.println(border);
            System.out.printf("| %-10.10s | %-16.16s | %-16.16s |%n",
                    removed.getRoomNumber(), removed.getStatus(), removed.getTimestamp());
            System.out.println(border);
            if (newCurrent != null) {
                System.out.println("Room " + roomNumber + " status reverted to: " + newCurrent.getStatus());
            } else {
                System.out.println("Room " + roomNumber + " now has no status history (reset to UNKNOWN).");
            }
        } else {
            System.out.println("Rollback cancelled.");
        }
    }

    /**
     * Fix for issue #25: validate the room number BEFORE searching for
     * history, so an invalid room number gets its own clear message
     * instead of being shown as "No status history found" — which
     * would otherwise look identical to a valid room with no entries.
     */
    private void viewFullHistory() {
        System.out.print("Enter room number: ");
        String roomNumber = sc.nextLine();

        String border = "+------------+------------------+";
        int contentWidth = border.length() - 4;

        if (!control.isValidRoomNumber(roomNumber)) {
            System.out.println();
            System.out.println(border);
            printCentered("STATUS HISTORY", contentWidth);
            System.out.println(border);
            System.out.printf("| %-" + contentWidth + "s |%n",
                    "Room " + roomNumber + " does not exist.");
            System.out.println(border);
            return;
        }

        Queue<RoomStatusLog> history = control.getHistoryForRoom(roomNumber);
        int total = history.getNumberOfEntries();

        System.out.println();
        System.out.println(border);
        printCentered("STATUS HISTORY - ROOM " + roomNumber, contentWidth);
        System.out.println(border);
        System.out.printf("| %-10s | %-16s |%n", "Status", "Timestamp");
        System.out.println(border);
        if (total == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "Room exists, but has no history yet.");
        } else {
            for (int i = 0; i < total; i++) {
                RoomStatusLog entry = history.getEntry(i);
                System.out.printf("| %-10.10s | %-16.16s |%n", entry.getStatus(), entry.getTimestamp());
            }
        }
        System.out.println(border);
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

        String border = "+------------+------------+------------------+------------------+";
        int contentWidth = border.length() - 4;

        System.out.println();
        System.out.println(border);
        printCentered("TARUMT RESORTS - HOUSEKEEPING STATUS REPORT", contentWidth);
        System.out.println(border);
        printCentered("Generated at: " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm a")), contentWidth);
        printCentered("Filter: Status = " + statusFilter.toUpperCase()
                + ", Room Type = " + (roomTypeFilter.equalsIgnoreCase("ALL") ? "ALL" : roomTypeFilter), contentWidth);
        System.out.println(border);
        System.out.printf("| %-10s | %-10s | %-16s | %-16s |%n", "Room", "Type", "Status", "Last Updated");
        System.out.println(border);

        int total = filtered.getNumberOfEntries();
        if (total == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No matching records found.");
        } else {
            for (int i = 0; i < total; i++) {
                RoomStatusLog entry = filtered.getEntry(i);
                String roomType = control.getRoomType(entry.getRoomNumber());
                System.out.printf("| %-10.10s | %-10.10s | %-16.16s | %-16.16s |%n",
                        entry.getRoomNumber(), roomType, entry.getStatus(), entry.getTimestamp());
            }
        }
        System.out.println(border);
        System.out.println("Total records: " + total);
        System.out.println("Note: rooms with no status log yet are excluded (their status is UNKNOWN).");
    }

    private void reportAverageDuration() {
        String stageFilter;
        while (true) {
            System.out.print("Filter by stage (DIRTY/CLEANING/INSPECTED), or press Enter for ALL: ");
            String stageInput = sc.nextLine().trim();
            if (stageInput.isEmpty()) {
                stageFilter = "ALL";
                break;
            }
            if (stageInput.equalsIgnoreCase("DIRTY")
                    || stageInput.equalsIgnoreCase("CLEANING")
                    || stageInput.equalsIgnoreCase("INSPECTED")) {
                stageFilter = stageInput;
                break;
            }
            System.out.println("Invalid stage entered. Please enter DIRTY, CLEANING, INSPECTED, or press Enter for ALL.");
        }

        Queue<StageDuration> report = control.getAverageDurationPerStage(stageFilter);

        String border = "+----------------------+------------------+";
        int contentWidth = border.length() - 4;

        System.out.println();
        System.out.println(border);
        printCentered("TARUMT RESORTS - AVG TIME PER CLEANING STAGE", contentWidth);
        System.out.println(border);
        printCentered("Generated at: " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm a")), contentWidth);
        printCentered("Filter: Stage = " + (stageFilter.equalsIgnoreCase("ALL") ? "ALL" : stageFilter.toUpperCase()), contentWidth);
        System.out.println(border);
        System.out.printf("| %-20s | %-16s |%n", "Stage", "Avg (min)");
        System.out.println(border);

        int total = report.getNumberOfEntries();
        if (total == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No data available for this filter.");
        } else {
            for (int i = 0; i < total; i++) {
                StageDuration sd = report.getEntry(i);
                System.out.printf("| %-20.20s | %-16d |%n", sd.getStageName(), sd.getAverageMinutes());
            }
        }
        System.out.println(border);
        System.out.println("Total stages measured: " + total);
        System.out.println("Note: READY duration is intentionally excluded - it would measure");
        System.out.println("guest occupancy/waiting time, not an actual cleaning stage.");
    }
}