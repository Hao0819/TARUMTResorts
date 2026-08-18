package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.HousekeepingControl;
import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.entity.StageDuration;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.adt.ListQueueInterface;
import java.util.Scanner;

/**
 * HouseKeepingUI.java
 * Console interface for the Housekeeping & Task Log module.
 *
 * @author KohJun
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

    /**
     * Added: prints a quick-reference table of every room in the system
     * (room number, type, current status) so staff can see at a glance
     * which room numbers exist and what state each one is in, before
     * being asked to type a room number. Used by logStatusChange(),
     * viewCurrentStatus(), and viewFullHistory() (functions 1, 2, 4).
     *
     * Uses control.getAllRooms() (a copy of the shared room list) and
     * control.getCurrentStatus() per room, so it always reflects the
     * latest logged status — including anything auto-logged by the
     * background CLEANING -> INSPECTED timer.
     */
    private void printAllRoomsTable() {
        ListQueueInterface<Room> allRooms = control.getAllRooms();
        int total = allRooms.getNumberOfEntries();

        String border = "+------------+------------+------------------+";
        int contentWidth = border.length() - 4;

        System.out.println();
        System.out.println(border);
        printCentered("ROOM LIST", contentWidth);
        System.out.println(border);
        System.out.printf("| %-10s | %-10s | %-16s |%n", "Room", "Type", "Status");
        System.out.println(border);
        if (total == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No rooms available in the system.");
        } else {
            for (int i = 0; i < total; i++) {
                Room room = allRooms.getEntry(i);
                RoomStatusLog current = control.getCurrentStatus(room.getRoomNumber());
                String status = (current != null) ? current.getStatus() : "UNKNOWN";
                System.out.printf("| %-10.10s | %-10.10s | %-16.16s |%n",
                        room.getRoomNumber(), room.getRoomType(), status);
            }
        }
        System.out.println(border);
    }

    /**
     * Added: like printAllRoomsTable(), but WITHOUT the Status column.
     * Used by viewCurrentStatus() (function 2) only — that function's
     * whole purpose is to look up a room's current status, so showing
     * the status in the pre-prompt table would spoil the answer before
     * the user even picks a room. Room number + type is still shown so
     * staff can see which room numbers exist.
     */
    private void printRoomsBasicTable() {
        ListQueueInterface<Room> allRooms = control.getAllRooms();
        int total = allRooms.getNumberOfEntries();

        String border = "+------------+------------+";
        int contentWidth = border.length() - 4;

        System.out.println();
        System.out.println(border);
        printCentered("ROOM LIST", contentWidth);
        System.out.println(border);
        System.out.printf("| %-10s | %-10s |%n", "Room", "Type");
        System.out.println(border);
        if (total == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No rooms available in the system.");
        } else {
            for (int i = 0; i < total; i++) {
                Room room = allRooms.getEntry(i);
                System.out.printf("| %-10.10s | %-10.10s |%n", room.getRoomNumber(), room.getRoomType());
            }
        }
        System.out.println(border);
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
            System.out.printf("| %-64s |%n", "3. Rollback last status change");
            System.out.printf("| %-64s |%n", "4. View full history of a room");
            System.out.printf("| %-64s |%n", "5. Report 1: Rooms by current status");
            System.out.printf("| %-64s |%n", "6. Report 2: Avg time per stage");
            System.out.printf("| %-64s |%n", "7. Summary Report (Room + Status Log)");
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
                case 7 -> generateSummaryReport();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    /**
     * Fix for the "Enter status" step: instead of making staff type the
     * whole status word, this shows the room's CURRENT status and asks
     * for a single letter:
     *   y = advance to the next status in the cycle
     *   n = cancel, back to the Housekeeping menu
     *   d = jump directly to DIRTY (checkout / early termination) —
     *       only offered when the room is NOT already DIRTY, since
     *       DIRTY -> DIRTY is not a real change (see
     *       HousekeepingControl.isValidNextStatus()).
     *
     * A room already at DIRTY only ever offers (y/n), because its next
     * step is CLEANING and "d" would be a no-op. A room at READY only
     * ever offers (y/n) too, because READY's next step IS DIRTY, so a
     * separate "d" option would just duplicate "y".
     *
     * Returns the chosen status ("" if the user cancelled with n).
     */
    private String promptNextStatus(String roomNumber) {
        RoomStatusLog current = control.getCurrentStatus(roomNumber);
        String currentStatus = (current != null) ? current.getStatus() : "UNKNOWN (no history yet)";
        String nextStatus = control.getNextStatusInSequence(roomNumber);
        boolean isCurrentlyDirty = current != null && current.getStatus().equalsIgnoreCase("DIRTY");
        // "d" (direct jump to DIRTY) only makes sense when the room isn't
        // already DIRTY, and only when it isn't already about to become
        // DIRTY anyway via "y" (i.e. current isn't READY/UNKNOWN either).
        boolean offerDirectDirty = current != null
                && !isCurrentlyDirty
                && !nextStatus.equalsIgnoreCase("DIRTY");

        System.out.println();
        System.out.println("Current status: " + currentStatus);
        while (true) {
            if (offerDirectDirty) {
                System.out.print("Change to " + nextStatus + "? (y = yes / n = cancel / d = jump directly to DIRTY): ");
            } else {
                System.out.print("Change to " + nextStatus + "? (y = yes / n = cancel): ");
            }
            String choice = sc.nextLine().trim().toLowerCase();
            switch (choice) {
                case "y":
                    return nextStatus;
                case "n":
                    return "";
                case "d":
                    if (offerDirectDirty) {
                        return "DIRTY";
                    }
                    System.out.println("'d' isn't available for this room's current status. Please try again.");
                    break;
                default:
                    System.out.println("Invalid input. Please enter y, n"
                            + (offerDirectDirty ? ", or d." : "."));
            }
        }
    }

    private void logStatusChange() {
        // Added: show all rooms + current status first, so staff know
        // which room numbers exist before being asked to type one.
        printAllRoomsTable();

        String roomNumber;
        while (true) {
            System.out.print("Enter room number (or 0 to cancel): ");
            roomNumber = sc.nextLine().trim();
            if (roomNumber.equals("0")) {
                System.out.println("Cancelled. Returning to Housekeeping menu...");
                return;
            }
            if (control.isValidRoomNumber(roomNumber)) {
                break;
            }
            System.out.println("Room number not found in the system. Please try again.");
        }

        String status = promptNextStatus(roomNumber);
        if (status.isEmpty()) {
            System.out.println("Cancelled. Returning to Housekeeping menu...");
            return;
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
        // Added: show room number + type first (no status — this
        // function's job IS to reveal the status, so pre-showing it
        // here would give the answer away before the lookup).
        printRoomsBasicTable();

        System.out.print("Enter room number (or 0 to cancel): ");
        String roomNumber = sc.nextLine().trim();
        if (roomNumber.equals("0")) {
            System.out.println("Cancelled. Returning to Housekeeping menu...");
            return;
        }

        // Occupied column removed per request — this report only needs to
        // show the room's housekeeping status, not occupancy.
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

        String previewBorder = "+------------+------------------+------------------+";
        int previewWidth = previewBorder.length() - 4;
        System.out.println();
        System.out.println(previewBorder);
        printCentered("MOST RECENT STATUS CHANGE", previewWidth);
        System.out.println(previewBorder);
        System.out.printf("| %-10s | %-16s | %-16s |%n", "Room", "Status", "Timestamp");
        System.out.println(previewBorder);
        System.out.printf("| %-10.10s | %-16.16s | %-16.16s |%n",
                preview.getRoomNumber(), preview.getStatus(), preview.getTimestamp());
        System.out.println(previewBorder);
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
        // Added: show all rooms + current status first, so staff can
        // see which room number is available before choosing one to
        // view the full history of.
        printAllRoomsTable();

        System.out.print("Enter room number (or 0 to cancel): ");
        String roomNumber = sc.nextLine().trim();
        if (roomNumber.equals("0")) {
            System.out.println("Cancelled. Returning to Housekeeping menu...");
            return;
        }

        String border = "+------------+------------------+";
        int contentWidth = border.length() - 4;

        if (!control.isValidRoomNumber(roomNumber)) {
            System.out.println();
            System.out.println(border);
            printCentered("STATUS HISTORY", contentWidth);
            System.out.println(border);
            System.out.printf("| %-" + contentWidth + "s |%n", "Room " + roomNumber + " does not exist.");
            System.out.println(border);
            return;
        }

        ListQueueInterface<RoomStatusLog> history = control.getHistoryForRoom(roomNumber);
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

    /**
     * Small menu table used by reportByStatus() and reportAverageDuration()
     * so staff pick a filter with a short keyword/number instead of typing
     * the whole status word. "0" is always the back/cancel option.
     */
    private void printFilterMenuTable(String title, String[] keys, String[] labels) {
        String border = "+------+------------------+";
        int contentWidth = border.length() - 4;
        System.out.println();
        System.out.println(border);
        printCentered(title, contentWidth);
        System.out.println(border);
        System.out.printf("| %-4s | %-16s |%n", "Key", "Option");
        System.out.println(border);
        for (int i = 0; i < keys.length; i++) {
            System.out.printf("| %-4.4s | %-16.16s |%n", keys[i], labels[i]);
        }
        System.out.printf("| %-4.4s | %-16.16s |%n", "0", "Back");
        System.out.println(border);
    }

    private void reportByStatus() {
        printFilterMenuTable("FILTER BY STATUS", new String[]{"D", "C", "I", "R"},
                new String[]{"DIRTY", "CLEANING", "INSPECTED", "READY"});

        String statusFilter;
        while (true) {
            System.out.print("Enter status keyword (D/C/I/R, or 0 to cancel): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                System.out.println("Cancelled. Returning to Housekeeping menu...");
                return;
            }
            statusFilter = switch (input) {
                case "D" -> "DIRTY";
                case "C" -> "CLEANING";
                case "I" -> "INSPECTED";
                case "R" -> "READY";
                default -> input; // allow the full word too, still validated below
            };
            if (control.isValidStatus(statusFilter)) {
                break;
            }
            System.out.println("Invalid choice. Please enter D, C, I, R, or 0 to cancel.");
        }

        printFilterMenuTable("FILTER BY ROOM TYPE", new String[]{"S", "D", "U", "A"},
                new String[]{"Standard", "Deluxe", "Suite", "ALL"});

        String roomTypeFilter;
        while (true) {
            System.out.print("Enter room type keyword (S/D/U/A, or 0 to cancel): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                System.out.println("Cancelled. Returning to Housekeeping menu...");
                return;
            }
            switch (input) {
                case "S" -> roomTypeFilter = "Standard";
                case "D" -> roomTypeFilter = "Deluxe";
                case "U" -> roomTypeFilter = "Suite";
                case "A", "" -> roomTypeFilter = "ALL";
                default -> roomTypeFilter = input; // allow the full word too
            }
            if (roomTypeFilter.equalsIgnoreCase("ALL") || control.isValidRoomType(roomTypeFilter)) {
                break;
            }
            System.out.println("Invalid choice. Please enter S, D, U, A, or 0 to cancel.");
        }

        ListQueueInterface<RoomStatusLog> filtered =
                control.getRoomsByCurrentStatus(statusFilter.toUpperCase(), roomTypeFilter);

        // Occupied column removed per request — this report only needs to
        // show housekeeping status, not occupancy.
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
    }

    /**
     * Report 2: average time per cleaning stage.
     *
     * Added: CLEANING removed from the selectable filter options.
     * Since HousekeepingControl now auto-logs INSPECTED at a fixed
     * delay (scheduleAutoInspect()), CLEANING duration is no longer a
     * real, staff-timed measurement — the control layer excludes it
     * from the calculation, so offering it here as a filter would only
     * ever return "No data available for this filter."
     */
    private void reportAverageDuration() {
        printFilterMenuTable("FILTER BY STAGE", new String[]{"D", "I", "A"},
                new String[]{"DIRTY", "INSPECTED", "ALL"});

        String stageFilter;
        while (true) {
            System.out.print("Enter stage keyword (D/I/A, or 0 to cancel): ");
            String input = sc.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                System.out.println("Cancelled. Returning to Housekeeping menu...");
                return;
            }
            switch (input) {
                case "D" -> stageFilter = "DIRTY";
                case "I" -> stageFilter = "INSPECTED";
                case "A", "" -> stageFilter = "ALL";
                default -> stageFilter = input; // allow the full word too
            }
            if (stageFilter.equalsIgnoreCase("ALL")
                    || stageFilter.equalsIgnoreCase("DIRTY")
                    || stageFilter.equalsIgnoreCase("INSPECTED")) {
                break;
            }
            System.out.println("Invalid choice. Please enter D, I, A, or 0 to cancel.");
        }

        ListQueueInterface<StageDuration> report = control.getAverageDurationPerStage(stageFilter);

        // Title shortened to "AVG TIME PER STAGE" per request — the
        // border below is kept at its wider size from the earlier fix
        // (was needed to stop the old longer title being truncated) but
        // works fine as extra padding around the shorter title too.
        String border = "+---------------------------+------------------+";
        int contentWidth = border.length() - 4;

        System.out.println();
        System.out.println(border);
        printCentered("AVG TIME PER STAGE", contentWidth);
        System.out.println(border);
        printCentered("Generated at: " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm a")), contentWidth);
        printCentered("Filter: Stage = " + (stageFilter.equalsIgnoreCase("ALL") ? "ALL" : stageFilter.toUpperCase()), contentWidth);
        System.out.println(border);
        System.out.printf("| %-25s | %-16s |%n", "Stage", "Avg (min)");
        System.out.println(border);

        int total = report.getNumberOfEntries();
        if (total == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No data available for this filter.");
        } else {
            for (int i = 0; i < total; i++) {
                StageDuration sd = report.getEntry(i);
                System.out.printf("| %-25.25s | %-16d |%n", sd.getStageName(), sd.getAverageMinutes());
            }
        }
        System.out.println(border);
        System.out.println("Total stages measured: " + total);
    }

    /**
     * Summary Report: combines the Room entity (room type) with the
     * RoomStatusLog entity (current status, total logged changes, last
     * updated) into one report — two entity classes joined into a
     * single summary, per the tutor's required format. Styled after
     * the university report template: plain-text letterhead, a main
     * table, totals, an ASCII bar-chart section, and closing insight
     * lines.
     */
    private void generateSummaryReport() {
        String thickBorder = "=".repeat(90);
        String thinBorder = "-".repeat(90);
        // Closed box border for the main table (was previously just bare
        // "col | col | col" printf lines with no left/right edge, which
        // is why the table looked "open"/unclosed on screen).
        // Occupied column removed per request, so its segment is dropped too.
        String tableBorder = "+" + "-".repeat(12) + "+" + "-".repeat(12) + "+" + "-".repeat(18)
                + "+" + "-".repeat(23) + "+" + "-".repeat(18) + "+";

        System.out.println();
        System.out.println(thickBorder);
        printCenteredPlain("TARUMT RESORTS", 90);
        printCenteredPlain("HOUSEKEEPING & TASK LOG MODULE SUBSYSTEM", 90);
        System.out.println();
        printCenteredPlain("SUMMARY OF HOUSEKEEPING REPORT", 90);
        System.out.println(thinBorder);
        System.out.println("Generated at: " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println(thickBorder);
        System.out.println();

        // --- Main table: Room (type) joined with RoomStatusLog (status,
        // history count, last updated) — the two classes combined. Also
        // Occupied column removed per request — only status/type/count
        // are shown here now. ---
        ListQueueInterface<Room> allRooms = control.getAllRooms();
        int totalRooms = allRooms.getNumberOfEntries();

        String[] statusNames = {"DIRTY", "CLEANING", "INSPECTED", "READY"};
        int[] statusCounts = new int[statusNames.length];
        int totalLogEntries = 0;

        // Occupied column removed per request.
        System.out.println(tableBorder);
        System.out.printf("| %-10s | %-10s | %-16s | %-21s | %-16s |%n",
                "Room No.", "Type", "Current Status", "Total Status Changes", "Last Updated");
        System.out.println(tableBorder);

        for (int i = 0; i < totalRooms; i++) {
            Room room = allRooms.getEntry(i);
            RoomStatusLog current = control.getCurrentStatus(room.getRoomNumber());
            int logCount = control.getTotalLogCountForRoom(room.getRoomNumber());
            totalLogEntries += logCount;

            String status = (current != null) ? current.getStatus() : "UNKNOWN";
            String lastUpdated = (current != null) ? current.getTimestamp() : "-";

            System.out.printf("| %-10.10s | %-10.10s | %-16.16s | %-21d | %-16.16s |%n",
                    room.getRoomNumber(), room.getRoomType(), status, logCount, lastUpdated);

            for (int s = 0; s < statusNames.length; s++) {
                if (statusNames[s].equalsIgnoreCase(status)) {
                    statusCounts[s]++;
                }
            }
        }

        System.out.println(tableBorder);
        System.out.println("Total Number of Rooms: " + totalRooms);
        System.out.println("Total Number of Status Log Entries: " + totalLogEntries);
        System.out.println();

        printCenteredPlain("GRAPHICAL REPRESENTATION OF HOUSEKEEPING MODULE", 90);

        // Chart 1: how many rooms currently sit in each status.
        printBarChart("Rooms by Current Status", statusNames, statusCounts);

        // Chart 2: average minutes per stage (from Report 2's own logic,
        // so both reports always agree with each other).
        ListQueueInterface<StageDuration> stageReport = control.getAverageDurationPerStage("ALL");
        int stageTotal = stageReport.getNumberOfEntries();
        String[] stageLabels = new String[stageTotal];
        int[] stageValues = new int[stageTotal];
        for (int i = 0; i < stageTotal; i++) {
            StageDuration sd = stageReport.getEntry(i);
            stageLabels[i] = sd.getStageName();
            stageValues[i] = (int) sd.getAverageMinutes();
        }
        printBarChart("Avg Time per Stage (min)", stageLabels, stageValues);

        // --- Insight lines: room with fewest / most logged status
        // changes, mirroring the sample report's closing "fewest / most"
        // style, using RoomStatusLog data joined back onto each Room. ---
        String fewestRoom = null;
        String mostRoom = null;
        int fewestCount = Integer.MAX_VALUE;
        int mostCount = -1;
        for (int i = 0; i < totalRooms; i++) {
            Room room = allRooms.getEntry(i);
            int logCount = control.getTotalLogCountForRoom(room.getRoomNumber());
            if (logCount < fewestCount) {
                fewestCount = logCount;
                fewestRoom = room.getRoomNumber();
            }
            if (logCount > mostCount) {
                mostCount = logCount;
                mostRoom = room.getRoomNumber();
            }
        }

        System.out.println();
        if (totalRooms == 0) {
            System.out.println("No rooms available to report on.");
        } else {
            System.out.println("Room with fewest status changes (" + fewestCount + "): < Room " + fewestRoom + " >");
            System.out.println("Room with most status changes (" + mostCount + "): < Room " + mostRoom + " >");
        }
        System.out.println(thickBorder);
        printCenteredPlain("END OF THE REPORT", 90);
        System.out.println(thickBorder);
    }

    /** Centers plain text (no pipe borders) within the given width — used by generateSummaryReport(). */
    private void printCenteredPlain(String text, int width) {
        if (text.length() >= width) {
            System.out.println(text.substring(0, width));
            return;
        }
        int leftPadding = (width - text.length()) / 2;
        System.out.println(" ".repeat(leftPadding) + text);
    }

    /**
     * Renders a simple ASCII vertical bar chart: a numbered y-axis, one
     * column per label, an x-axis line, the labels beneath, and the
     * exact value of every column printed underneath its label.
     * Handles an all-zero dataset without dividing by zero.
     *
     * Fix (round 3 — issue found from screenshot testing):
     *
     *   "INSPECTED is 9 but the graph shows it at 79" — this was NOT a
     *   calculation bug. With DIRTY=1582 and INSPECTED=9, the chart has
     *   to compress a 1582-unit range down into 20 display rows, so
     *   INSPECTED's bar correctly rounds up to the smallest possible
     *   bar — 1 row tall. The problem was that the row it landed on was
     *   LABELLED "79" (the y-axis scale value for that row), which
     *   reads exactly like it's claiming INSPECTED = 79. The axis
     *   number was never meant to be read as "this bar's value" — it's
     *   just a ruler marking — but nothing on screen said so.
     *
     *   Fix: the TRUE value of each bar is now printed directly at the
     *   TOP of that bar itself (replacing the top "***" with the real
     *   number, e.g. "9"), instead of only appearing once in small text
     *   underneath the whole chart. Every bar is now self-labelled with
     *   its own real number at a glance, so it can never be read
     *   against the y-axis ruler by mistake. The "(9)" summary line
     *   underneath each column is kept as well for a second, unambiguous
     *   confirmation of the exact figure.
     *
     *   Also carried over from the previous fix: charts that already
     *   fit within CHART_MAX_DISPLAY_ROWS are drawn UNSCALED (1 row =
     *   1 unit, no rounding at all), and the row-number column width is
     *   calculated from the largest real value so wide numbers (e.g.
     *   1582) never break alignment with the axis/labels beneath.
     *
     * Each data column is a fixed COLUMN_WIDTH (12 chars) with the
     * bar/label/value CENTERED inside it, so longer labels like
     * "CLEANING" (8 chars) and "INSPECTED" (9 chars) still line up
     * cleanly with a gap between columns.
     */
    private static final int CHART_COLUMN_WIDTH = 12;
    private static final int CHART_MAX_DISPLAY_ROWS = 20;

    private void printBarChart(String title, String[] labels, int[] values) {
        System.out.println();
        printCenteredPlain(title, 90);
        System.out.println();

        if (labels.length == 0) {
            System.out.println("(No data to chart.)");
            return;
        }

        int maxValue = 0;
        for (int v : values) {
            maxValue = Math.max(maxValue, v);
        }
        if (maxValue == 0) {
            System.out.println("(No data to chart.)");
            return;
        }

        // Only scale/compress when the real range is taller than what
        // can be displayed. Otherwise draw it 1:1 so no two values can
        // ever be rounded onto the same row.
        boolean scaled = maxValue > CHART_MAX_DISPLAY_ROWS;
        int chartRows = scaled ? CHART_MAX_DISPLAY_ROWS : maxValue;

        // Row-number column width, sized to the largest real value so
        // wide numbers (e.g. 1582) never overflow it and break the
        // alignment of the rows/axis/labels beneath.
        int numWidth = Math.max(2, String.valueOf(maxValue).length());
        String rowFormat = "%" + numWidth + "d |";
        String axisPrefix = " ".repeat(numWidth + 1) + "+";
        String labelPrefix = " ".repeat(numWidth + 2);

        for (int row = chartRows; row >= 1; row--) {
            int rowValue = scaled
                    ? (int) Math.round((double) row / chartRows * maxValue)
                    : row;
            StringBuilder line = new StringBuilder(String.format(rowFormat, rowValue));
            for (int v : values) {
                int barLevel = scaled
                        ? (int) Math.ceil((double) v / maxValue * chartRows)
                        : v;
                String cell;
                if (barLevel == row) {
                    // Top of this bar: print its TRUE value here, right
                    // on the bar itself, so it's never mistaken for the
                    // y-axis ruler number sitting on the same line.
                    cell = String.valueOf(v);
                } else if (barLevel > row) {
                    cell = "***";
                } else {
                    cell = "";
                }
                line.append(centerInField(cell, CHART_COLUMN_WIDTH));
            }
            System.out.println(line);
        }

        StringBuilder axis = new StringBuilder(axisPrefix);
        for (int i = 0; i < values.length; i++) {
            axis.append("-".repeat(CHART_COLUMN_WIDTH));
        }
        System.out.println(axis);

        StringBuilder labelLine = new StringBuilder(labelPrefix);
        for (String label : labels) {
            labelLine.append(centerInField(label, CHART_COLUMN_WIDTH));
        }
        System.out.println(labelLine);

        // Exact value of every column printed under its label too, as a
        // second, unambiguous confirmation of the real figure.
        StringBuilder valueLine = new StringBuilder(labelPrefix);
        for (int v : values) {
            valueLine.append(centerInField("(" + v + ")", CHART_COLUMN_WIDTH));
        }
        System.out.println(valueLine);
    }

    /** Centers text within a fixed-width field, padding with spaces on both sides. */
    private String centerInField(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }
}
