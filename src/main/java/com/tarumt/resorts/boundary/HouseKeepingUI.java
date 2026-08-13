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
        printCentered("AVAILABLE ROOMS", contentWidth);
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
        printCentered("AVAILABLE ROOMS", contentWidth);
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

    private void logStatusChange() {
        // Added: show all rooms + current status first, so staff know
        // which room numbers exist before being asked to type one.
        printAllRoomsTable();

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
        // Added: show room number + type first (no status — this
        // function's job IS to reveal the status, so pre-showing it
        // here would give the answer away before the lookup).
        printRoomsBasicTable();

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

        System.out.print("Enter room number: ");
        String roomNumber = sc.nextLine();

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

        ListQueueInterface<RoomStatusLog> filtered =
                control.getRoomsByCurrentStatus(statusFilter.toUpperCase(), roomTypeFilter);

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
        String stageFilter;
        while (true) {
            System.out.print("Filter by stage (DIRTY/INSPECTED), or press Enter for ALL: ");
            String stageInput = sc.nextLine().trim();
            if (stageInput.isEmpty()) {
                stageFilter = "ALL";
                break;
            }
            if (stageInput.equalsIgnoreCase("DIRTY")
                    || stageInput.equalsIgnoreCase("INSPECTED")) {
                stageFilter = stageInput;
                break;
            }
            System.out.println("Invalid stage entered. Please enter DIRTY, INSPECTED, or press Enter for ALL.");
        }

        ListQueueInterface<StageDuration> report = control.getAverageDurationPerStage(stageFilter);

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

        System.out.println();
        System.out.println(thickBorder);
        printCenteredPlain("TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY", 90);
        printCenteredPlain("HOUSEKEEPING & TASK LOG MODULE SUBSYSTEM", 90);
        System.out.println();
        printCenteredPlain("SUMMARY OF HOUSEKEEPING REPORT", 90);
        System.out.println(thinBorder);
        System.out.println("Generated at: " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println(thickBorder);
        System.out.println();
        System.out.println("TUNKU ABDUL RAHMAN UNIVERSITY OF MANAGEMENT AND TECHNOLOGY HIGHLY CONFIDENTIAL DOCUMENT");
        System.out.println();

        // --- Main table: Room (type) joined with RoomStatusLog (status,
        // history count, last updated) — the two classes combined. ---
        ListQueueInterface<Room> allRooms = control.getAllRooms();
        int totalRooms = allRooms.getNumberOfEntries();

        String[] statusNames = {"DIRTY", "CLEANING", "INSPECTED", "READY"};
        int[] statusCounts = new int[statusNames.length];
        int totalLogEntries = 0;

        System.out.printf("%-10s | %-10s | %-16s | %-21s | %-16s%n",
                "Room No.", "Type", "Current Status", "Total Status Changes", "Last Updated");
        System.out.println(thinBorder);

        for (int i = 0; i < totalRooms; i++) {
            Room room = allRooms.getEntry(i);
            RoomStatusLog current = control.getCurrentStatus(room.getRoomNumber());
            int logCount = control.getTotalLogCountForRoom(room.getRoomNumber());
            totalLogEntries += logCount;

            String status = (current != null) ? current.getStatus() : "UNKNOWN";
            String lastUpdated = (current != null) ? current.getTimestamp() : "-";

            System.out.printf("%-10.10s | %-10.10s | %-16.16s | %-21d | %-16.16s%n",
                    room.getRoomNumber(), room.getRoomType(), status, logCount, lastUpdated);

            for (int s = 0; s < statusNames.length; s++) {
                if (statusNames[s].equalsIgnoreCase(status)) {
                    statusCounts[s]++;
                }
            }
        }

        System.out.println(thinBorder);
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
        printBarChart("Avg Time per Cleaning Stage (min)", stageLabels, stageValues);

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
     * Renders a simple ASCII vertical bar chart: a numbered y-axis
     * scaled to the largest value (max height 10 rows, matching the
     * sample report's y-axis), one column per label, an x-axis line,
     * and the labels beneath. Handles an all-zero dataset without
     * dividing by zero.
     *
     * Fix: each column is now a fixed COLUMN_WIDTH (12 chars) with the
     * bar/label CENTERED inside it, instead of a plain 7-char printf
     * field. The old version truncated and ran together longer labels
     * like "CLEANING" (8 chars) and "INSPECTED" (9 chars) since they
     * didn't fit in 7 characters with no gap between columns.
     */
    private static final int CHART_COLUMN_WIDTH = 12;

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
        int chartHeight = Math.min(10, maxValue);

        for (int row = chartHeight; row >= 1; row--) {
            StringBuilder line = new StringBuilder(String.format("%2d |", row));
            for (int v : values) {
                int barLevel = (int) Math.ceil((double) v / maxValue * chartHeight);
                line.append(centerInField(barLevel >= row ? "***" : "", CHART_COLUMN_WIDTH));
            }
            System.out.println(line);
        }

        StringBuilder axis = new StringBuilder("   +");
        for (int i = 0; i < values.length; i++) {
            axis.append("-".repeat(CHART_COLUMN_WIDTH));
        }
        System.out.println(axis);

        StringBuilder labelLine = new StringBuilder("    ");
        for (String label : labels) {
            labelLine.append(centerInField(label, CHART_COLUMN_WIDTH));
        }
        System.out.println(labelLine);
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