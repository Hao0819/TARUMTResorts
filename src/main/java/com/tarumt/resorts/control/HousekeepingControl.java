package com.tarumt.resorts.control;

import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.StageDuration;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.dao.RoomStatusLogDAO;
import com.tarumt.resorts.dao.RoomDAO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HousekeepingControl.java
 * Handles the business logic for the Housekeeping & Task Log module.
 * Tracks each room's cleaning status (DIRTY -> CLEANING -> INSPECTED ->
 * READY) and auto-advances the whole cycle in the background using a
 * scheduler, while still letting staff log any step manually at any time.
 *
 * @author KohJun
 */
public class HousekeepingControl {

    // Every logged status change, across every room, in chronological order.
    private ListQueueInterface<RoomStatusLog> statusLog; // ADT collection declaration

    // Shared room collection provided by Main.
    private ListQueueInterface<Room> roomList; // ADT collection declaration

    // The four cleaning stages, in the order a room must move through them.
    private static final String[] STATUS_SEQUENCE = {
        "DIRTY", "CLEANING", "INSPECTED", "READY"
    };

    // Auto DIRTY -> CLEANING delay - simulates dispatching a cleaner.
    private static final long AUTO_CLEANING_DELAY_SECONDS = 1800; // 30 minutes

    // Auto CLEANING -> INSPECTED delay - simulates the supervisor's check.
    private static final long AUTO_INSPECT_DELAY_SECONDS = 60; // 1 minute

    // Auto INSPECTED -> READY delay - simulates the supervisor's sign-off.
    private static final long AUTO_READY_DELAY_SECONDS = 60; // 1 minute

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Background daemon thread that fires the three auto-transitions above.
    // Daemon = true so it never blocks the program from exiting.
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread t = new Thread(runnable, "housekeeping-auto-inspect");
                t.setDaemon(true);
                return t;
            });

    public HousekeepingControl() {
        this(
            new RoomDAO().getAllRooms(),
            new RoomStatusLogDAO().getAllLogs());
    }

    // Constructor used when Main provides the shared application data.
    public HousekeepingControl(
            ListQueueInterface<Room> sharedRooms, // ADT collection declaration
            ListQueueInterface<RoomStatusLog> sharedStatusLog) { // ADT collection declaration

        // Keep the same Queue references provided by Main.
        roomList = sharedRooms;
        statusLog = sharedStatusLog;

        // Bring every shared Room's cleaningStatus in line with the latest
        // log already loaded, so Walk-In sees correct readiness immediately.
        syncAllRoomCleaningStatus();
    }

    /*
     * Expected flow
     * For every Room in roomList
     * -> look up its latest RoomStatusLog entry
     * -> found: copy that status onto the Room
     * -> not found: mark the Room "UNKNOWN"
     */
    private void syncAllRoomCleaningStatus() {
        int total = roomList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            Room room = roomList.getEntry(i); // ADT method call: getEntry()
            RoomStatusLog latest = getCurrentStatus(room.getRoomNumber());
            room.setCleaningStatus(latest != null ? latest.getStatus() : "UNKNOWN");
        }
    }

    // Finds the actual shared Room object (not a copy) for a room number.
    private Room findRoomByNumber(String roomNumber) {
        int total = roomList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            Room room = roomList.getEntry(i); // ADT method call: getEntry()
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return room;
            }
        }
        return null;
    }

    // Checks whether a room number exists in the shared room collection.
    public boolean isValidRoomNumber(String roomNumber) {
        if (roomNumber == null) {
            return false;
        }
        int total = roomList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            Room room = roomList.getEntry(i); // ADT method call: getEntry()
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return true;
            }
        }
        return false;
    }

    // Returns the room type (Standard/Deluxe/Suite) for a given room number.
    public String getRoomType(String roomNumber) {
        int total = roomList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            Room room = roomList.getEntry(i); // ADT method call: getEntry()
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return room.getRoomType();
            }
        }
        return "Unknown";
    }

    // Checks whether a room type keyword matches an existing room's type.
    public boolean isValidRoomType(String roomType) {
        if (roomType == null) {
            return false;
        }
        int total = roomList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            if (roomList.getEntry(i).getRoomType().equalsIgnoreCase(roomType)) { // ADT method call: getEntry()
                return true;
            }
        }
        return false;
    }

    // Checks whether a status keyword is one of the four valid stages.
    public boolean isValidStatus(String status) {
        if (status == null) {
            return false;
        }
        for (String s : STATUS_SEQUENCE) {
            if (s.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Expected flow
     * No history yet -> only DIRTY is legal (first-ever log for a room)
     * -> newStatus is DIRTY -> always legal (checkout can dirty any room)
     * -> otherwise -> newStatus must be exactly one step ahead of current
     */
    public boolean isValidNextStatus(String roomNumber, String newStatus) {
        if (newStatus == null || !isValidStatus(newStatus)) {
            return false;
        }
        RoomStatusLog current = getCurrentStatus(roomNumber);
        if (current == null) {
            // No history yet - a room must start at DIRTY.
            return newStatus.equalsIgnoreCase("DIRTY");
        }
        int currentIndex = indexOfStatus(current.getStatus());
        int newIndex = indexOfStatus(newStatus);
        // Guard against corrupted/unknown stored data.
        if (currentIndex == -1 || newIndex == -1) {
            return false;
        }
        // A room that is ALREADY DIRTY cannot be marked DIRTY again.
        if (currentIndex == 0 && newIndex == 0) {
            return false;
        }
        // Front-Desk checkout rule: a checkout can always dirty a room,
        // regardless of its previous housekeeping status.
        if (newIndex == 0) {
            return true;
        }
        return newIndex == currentIndex + 1;
    }

    // Returns whether a room currently has a guest inside (the inverse of
    // Room.isAvailable()), so Housekeeping can show occupancy alongside
    // cleaning status.
    public boolean isRoomOccupied(String roomNumber) {
        Room room = findRoomByNumber(roomNumber);
        return room != null && !room.isAvailable();
    }

    // Returns the next status in the cleaning cycle for this room, so the
    // boundary's y/n/d prompt never needs staff to type the status name.
    public String getNextStatusInSequence(String roomNumber) {
        RoomStatusLog current = getCurrentStatus(roomNumber);
        if (current == null) {
            return "DIRTY";
        }
        int currentIndex = indexOfStatus(current.getStatus());
        if (currentIndex == -1 || currentIndex == STATUS_SEQUENCE.length - 1) {
            return "DIRTY";
        }
        return STATUS_SEQUENCE[currentIndex + 1];
    }

    // Finds where a status name sits in the fixed STATUS_SEQUENCE array.
    private int indexOfStatus(String status) {
        for (int i = 0; i < STATUS_SEQUENCE.length; i++) {
            if (STATUS_SEQUENCE[i].equalsIgnoreCase(status)) {
                return i;
            }
        }
        return -1;
    }

    /*
     * Expected flow
     * Validate room, status, and the transition itself
     * -> enqueue the new RoomStatusLog entry
     * -> sync the shared Room's cleaningStatus field
     * -> DIRTY logged -> schedule the auto DIRTY -> CLEANING timer
     * -> CLEANING logged -> schedule the auto CLEANING -> INSPECTED timer
     * -> INSPECTED logged -> schedule the auto INSPECTED -> READY timer
     */
    public boolean logStatusChange(String roomNumber, String status, String timestamp) {
        if (!isValidRoomNumber(roomNumber)) {
            return false;
        }
        if (!isValidStatus(status)) {
            return false;
        }
        if (!isValidNextStatus(roomNumber, status)) {
            return false;
        }
        RoomStatusLog entry = new RoomStatusLog(roomNumber, status, timestamp);
        boolean enqueued = statusLog.enqueue(entry); // ADT method call: enqueue()
        if (!enqueued) {
            // Honour the ADT contract - don't report success if the
            // Queue itself refused the entry.
            return false;
        }
        Room room = findRoomByNumber(roomNumber);
        if (room != null) {
            room.setCleaningStatus(status.toUpperCase());
        }
        // Schedule the automatic DIRTY -> CLEANING step.
        if (status.equalsIgnoreCase("DIRTY")) {
            scheduleAutoCleaning(roomNumber);
        }
        // Schedule the automatic CLEANING -> INSPECTED step.
        if (status.equalsIgnoreCase("CLEANING")) {
            scheduleAutoInspect(roomNumber);
        }
        // Schedule the automatic INSPECTED -> READY step.
        if (status.equalsIgnoreCase("INSPECTED")) {
            scheduleAutoReady(roomNumber);
        }
        return true;
    }

    // Auto-logs CLEANING, AUTO_CLEANING_DELAY_SECONDS after a room is
    // logged DIRTY (from any source - checkout, manual "d" jump, etc.).
    // Re-validates isValidNextStatus() at fire time, not just at scheduling
    // time, so a room a staff member already advanced (or rolled back) in
    // the meantime is left alone instead of getting a stale/illegal status.
    private void scheduleAutoCleaning(String roomNumber) {
        scheduler.schedule(() -> {
            if (isValidNextStatus(roomNumber, "CLEANING")) {
                String autoTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                logStatusChange(roomNumber, "CLEANING", autoTimestamp);
            }
        }, AUTO_CLEANING_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    // Auto-logs INSPECTED, AUTO_INSPECT_DELAY_SECONDS after CLEANING.
    // Same stale-timer protection as scheduleAutoCleaning() above.
    private void scheduleAutoInspect(String roomNumber) {
        scheduler.schedule(() -> {
            if (isValidNextStatus(roomNumber, "INSPECTED")) {
                String autoTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                logStatusChange(roomNumber, "INSPECTED", autoTimestamp);
            }
        }, AUTO_INSPECT_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    // Auto-logs READY, AUTO_READY_DELAY_SECONDS after INSPECTED, so every
    // room reaches READY within the team's shared 1-day SLA even if no
    // supervisor manually confirms it. Same stale-timer protection as above.
    private void scheduleAutoReady(String roomNumber) {
        scheduler.schedule(() -> {
            if (isValidNextStatus(roomNumber, "READY")) {
                String autoTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                logStatusChange(roomNumber, "READY", autoTimestamp);
            }
        }, AUTO_READY_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    // Finds the CURRENT (most recent) status of a given room by scanning
    // forward and keeping the last match - relies on log entries being in
    // chronological order.
    public RoomStatusLog getCurrentStatus(String roomNumber) {
        RoomStatusLog latest = null;
        int total = statusLog.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            RoomStatusLog entry = statusLog.getEntry(i); // ADT method call: getEntry()
            if (entry.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                latest = entry;
            }
        }
        return latest;
    }

    // Retrieves the full status history for a given room, in order.
    public ListQueueInterface<RoomStatusLog> getHistoryForRoom(String roomNumber) {
        DoublyLinkedListQueue<RoomStatusLog> history = new DoublyLinkedListQueue<>(); // ADT collection declaration
        int total = statusLog.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            RoomStatusLog entry = statusLog.getEntry(i); // ADT method call: getEntry()
            if (entry.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                history.enqueue(entry); // ADT method call: enqueue()
            }
        }
        return history;
    }

    // Rolls back the most recently logged status change GLOBALLY (across
    // ALL rooms, not just one room) - removeLast() is O(1) on the shared
    // Doubly Linked List. Still O(n) overall because getCurrentStatus()
    // then re-scans to find the affected room's new current status.
    public RoomStatusLog rollbackLastChange() {
        RoomStatusLog removed = statusLog.removeLast(); // ADT method call: removeLast()
        if (removed != null) {
            Room room = findRoomByNumber(removed.getRoomNumber());
            if (room != null) {
                RoomStatusLog newLatest = getCurrentStatus(removed.getRoomNumber());
                room.setCleaningStatus(newLatest != null ? newLatest.getStatus() : "UNKNOWN");
            }
        }
        return removed;
    }

    // Previews what rollback would remove (O(1) - peekLast()), without
    // removing it, so a supervisor can confirm before committing.
    public RoomStatusLog previewLastChange() {
        return statusLog.peekLast(); // ADT method call: peekLast()
    }

    /*
     * Report 1 - expected flow
     * Collect every distinct room number that has at least one log entry
     * -> for each one, look up its CURRENT status
     * -> keep it only if the status and room type both match the filters
     */
    public ListQueueInterface<RoomStatusLog> getRoomsByCurrentStatus(
            String statusFilter, String roomTypeFilter) {
        DoublyLinkedListQueue<RoomStatusLog> result = new DoublyLinkedListQueue<>(); // ADT collection declaration
        DoublyLinkedListQueue<String> seenRooms = new DoublyLinkedListQueue<>(); // ADT collection declaration

        int total = statusLog.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            String roomNumber = statusLog.getEntry(i).getRoomNumber(); // ADT method call: getEntry()
            if (!seenRooms.contains(roomNumber)) { // ADT method call: contains()
                seenRooms.enqueue(roomNumber); // ADT method call: enqueue()
            }
        }

        int totalRooms = seenRooms.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < totalRooms; i++) {
            String roomNumber = seenRooms.getEntry(i); // ADT method call: getEntry()
            RoomStatusLog current = getCurrentStatus(roomNumber);
            if (current == null || !current.getStatus().equalsIgnoreCase(statusFilter)) {
                continue;
            }
            boolean roomTypeMatches = roomTypeFilter.equalsIgnoreCase("ALL")
                    || getRoomType(roomNumber).equalsIgnoreCase(roomTypeFilter);
            if (roomTypeMatches) {
                result.enqueue(current); // ADT method call: enqueue()
            }
        }
        return sortByRoomNumber(result);
    }

    /*
     * Report 2 - average time (minutes) spent per cleaning stage.
     *
     * READY is excluded: the gap from READY to the next DIRTY spans guest
     * occupancy, not a cleaning stage.
     *
     * INSPECTED is excluded: scheduleAutoReady() logs READY automatically
     * at a FIXED delay, so that gap is not real staff-timed work.
     *
     * CLEANING IS included: a supervisor can roll back a failed INSPECTED
     * check, sending the room back to CLEANING for real rework before it
     * is manually re-inspected - so its duration can genuinely vary and is
     * worth reporting.
     *
     * DIRTY is included: staff can manually start cleaning early, before
     * the auto-timer fires, so this duration is genuine too.
     */
    public ListQueueInterface<StageDuration> getAverageDurationPerStage(String stageFilter) {
        DoublyLinkedListQueue<String> stageNames = new DoublyLinkedListQueue<>(); // ADT collection declaration
        DoublyLinkedListQueue<Long> stageTotalMinutes = new DoublyLinkedListQueue<>(); // ADT collection declaration
        DoublyLinkedListQueue<Integer> stageCount = new DoublyLinkedListQueue<>(); // ADT collection declaration
        DoublyLinkedListQueue<String> distinctRooms = new DoublyLinkedListQueue<>(); // ADT collection declaration

        int total = statusLog.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            String roomNumber = statusLog.getEntry(i).getRoomNumber(); // ADT method call: getEntry()
            if (!distinctRooms.contains(roomNumber)) { // ADT method call: contains()
                distinctRooms.enqueue(roomNumber); // ADT method call: enqueue()
            }
        }

        int totalRooms = distinctRooms.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int r = 0; r < totalRooms; r++) {
            String roomNumber = distinctRooms.getEntry(r); // ADT method call: getEntry()
            DoublyLinkedListQueue<RoomStatusLog> roomEntries = new DoublyLinkedListQueue<>(); // ADT collection declaration
            for (int i = 0; i < total; i++) {
                RoomStatusLog entry = statusLog.getEntry(i); // ADT method call: getEntry()
                if (entry.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                    roomEntries.enqueue(entry); // ADT method call: enqueue()
                }
            }

            int roomTotal = roomEntries.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
            for (int i = 0; i < roomTotal - 1; i++) {
                RoomStatusLog current = roomEntries.getEntry(i); // ADT method call: getEntry()
                RoomStatusLog next = roomEntries.getEntry(i + 1); // ADT method call: getEntry()
                String stage = current.getStatus();

                if (stage.equalsIgnoreCase("READY")) {
                    continue;
                }
                if (stage.equalsIgnoreCase("INSPECTED")) {
                    continue;
                }
                if (!stageFilter.equalsIgnoreCase("ALL") && !stage.equalsIgnoreCase(stageFilter)) {
                    continue;
                }

                long minutes = minutesBetween(current.getTimestamp(), next.getTimestamp());
                if (minutes < 0) {
                    // Malformed timestamp or out-of-order data - skip
                    // rather than pollute the average with a negative.
                    continue;
                }

                int index = indexOfStageName(stageNames, stage);
                if (index == -1) {
                    stageNames.enqueue(stage); // ADT method call: enqueue()
                    stageTotalMinutes.enqueue(minutes); // ADT method call: enqueue()
                    stageCount.enqueue(1); // ADT method call: enqueue()
                } else {
                    long updatedTotal = stageTotalMinutes.getEntry(index) + minutes; // ADT method call: getEntry()
                    int updatedCount = stageCount.getEntry(index) + 1; // ADT method call: getEntry()
                    replaceAt(stageTotalMinutes, index, updatedTotal);
                    replaceAt(stageCount, index, updatedCount);
                }
            }
        }

        DoublyLinkedListQueue<StageDuration> resultList = new DoublyLinkedListQueue<>(); // ADT collection declaration
        int stagesFound = stageNames.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < stagesFound; i++) {
            String stage = stageNames.getEntry(i); // ADT method call: getEntry()
            long avgMinutes = stageTotalMinutes.getEntry(i) / stageCount.getEntry(i); // ADT method call: getEntry()
            resultList.enqueue(new StageDuration(stage, avgMinutes)); // ADT method call: enqueue()
        }
        return sortByDurationDescending(resultList);
    }

    // Calculates whole minutes between two "yyyy-MM-dd HH:mm" timestamps.
    // Returns -1 (an impossible real duration) on a malformed timestamp,
    // so callers can safely skip the pair instead of crashing the report.
    private long minutesBetween(String startTimestamp, String endTimestamp) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTimestamp, TIMESTAMP_FORMAT);
            LocalDateTime end = LocalDateTime.parse(endTimestamp, TIMESTAMP_FORMAT);
            return java.time.Duration.between(start, end).toMinutes();
        } catch (java.time.format.DateTimeParseException e) {
            return -1;
        }
    }

    // Finds where a stage name already sits in the running stageNames list.
    private int indexOfStageName(DoublyLinkedListQueue<String> stageNames, String stage) { // ADT collection declaration
        int total = stageNames.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            if (stageNames.getEntry(i).equalsIgnoreCase(stage)) { // ADT method call: getEntry()
                return i;
            }
        }
        return -1;
    }

    /*
     * Expected flow
     * Rebuild the whole Queue from scratch, replacing only the value at
     * the target position - the ADT interface has no direct "set(index)".
     */
    private void replaceAt(DoublyLinkedListQueue<Long> queue, int position, long newValue) { // ADT collection declaration
        DoublyLinkedListQueue<Long> rebuilt = new DoublyLinkedListQueue<>(); // ADT collection declaration
        int total = queue.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            rebuilt.enqueue(i == position ? newValue : queue.getEntry(i)); // ADT method call: enqueue()/getEntry()
        }
        queue.clear(); // ADT method call: clear()
        for (int i = 0; i < rebuilt.getNumberOfEntries(); i++) { // ADT method call: getNumberOfEntries()
            queue.enqueue(rebuilt.getEntry(i)); // ADT method call: enqueue()/getEntry()
        }
    }

    // Same rebuild-and-replace approach as above, for Integer values.
    private void replaceAt(DoublyLinkedListQueue<Integer> queue, int position, int newValue) { // ADT collection declaration
        DoublyLinkedListQueue<Integer> rebuilt = new DoublyLinkedListQueue<>(); // ADT collection declaration
        int total = queue.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            rebuilt.enqueue(i == position ? newValue : queue.getEntry(i)); // ADT method call: enqueue()/getEntry()
        }
        queue.clear(); // ADT method call: clear()
        for (int i = 0; i < rebuilt.getNumberOfEntries(); i++) { // ADT method call: getNumberOfEntries()
            queue.enqueue(rebuilt.getEntry(i)); // ADT method call: enqueue()/getEntry()
        }
    }

    // Self-implemented insertion sort by room number - copies the Queue
    // into an array, sorts the array, then rebuilds a sorted Queue.
    private DoublyLinkedListQueue<RoomStatusLog> sortByRoomNumber(
            DoublyLinkedListQueue<RoomStatusLog> input) { // ADT collection declaration
        int n = input.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        RoomStatusLog[] arr = new RoomStatusLog[n];
        for (int i = 0; i < n; i++) {
            arr[i] = input.getEntry(i); // ADT method call: getEntry()
        }
        for (int i = 1; i < n; i++) {
            RoomStatusLog key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j].getRoomNumber().compareTo(key.getRoomNumber()) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
        DoublyLinkedListQueue<RoomStatusLog> sorted = new DoublyLinkedListQueue<>(); // ADT collection declaration
        for (RoomStatusLog r : arr) {
            sorted.enqueue(r); // ADT method call: enqueue()
        }
        return sorted;
    }

    // Self-implemented selection sort by average duration, descending.
    private DoublyLinkedListQueue<StageDuration> sortByDurationDescending(
            DoublyLinkedListQueue<StageDuration> input) { // ADT collection declaration
        int n = input.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        StageDuration[] arr = new StageDuration[n];
        for (int i = 0; i < n; i++) {
            arr[i] = input.getEntry(i); // ADT method call: getEntry()
        }
        for (int i = 0; i < n - 1; i++) {
            int maxIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j].getAverageMinutes() > arr[maxIndex].getAverageMinutes()) {
                    maxIndex = j;
                }
            }
            StageDuration swapTemp = arr[i];
            arr[i] = arr[maxIndex];
            arr[maxIndex] = swapTemp;
        }
        DoublyLinkedListQueue<StageDuration> sorted = new DoublyLinkedListQueue<>(); // ADT collection declaration
        for (StageDuration sd : arr) {
            sorted.enqueue(sd); // ADT method call: enqueue()
        }
        return sorted;
    }

    // Exposes the full raw log as a COPY, so callers cannot accidentally
    // clear()/removeLast() the real shared statusLog by mutating this.
    public ListQueueInterface<RoomStatusLog> getFullLog() {
        DoublyLinkedListQueue<RoomStatusLog> copy = new DoublyLinkedListQueue<>(); // ADT collection declaration
        int total = statusLog.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            copy.enqueue(statusLog.getEntry(i)); // ADT method call: enqueue()/getEntry()
        }
        return copy;
    }

    // Exposes every Room as a COPY (not the live reference), for the
    // Summary Report to join Room data with RoomStatusLog data.
    public ListQueueInterface<Room> getAllRooms() {
        DoublyLinkedListQueue<Room> copy = new DoublyLinkedListQueue<>(); // ADT collection declaration
        int total = roomList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            copy.enqueue(roomList.getEntry(i)); // ADT method call: enqueue()/getEntry()
        }
        return copy;
    }

    // Counts how many status-log entries exist for a given room - used as
    // the "Total Status Changes" column in the Summary Report.
    public int getTotalLogCountForRoom(String roomNumber) {
        int count = 0;
        int total = statusLog.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < total; i++) {
            if (statusLog.getEntry(i).getRoomNumber().equalsIgnoreCase(roomNumber)) { // ADT method call: getEntry()
                count++;
            }
        }
        return count;
    }
}