package com.tarumt.resorts.control;

import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.StageDuration;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.dao.RoomStatusLogDAO;
import com.tarumt.resorts.dao.RoomDAO;

/**
 * HousekeepingControl.java
 * Handles the business logic for the Housekeeping & Task Log module.
 *
 * NOTE ON ROLLBACK: rollbackLastChange() / previewLastChange() operate on
 * the queue's rear entry, i.e. "the most recently logged status change
 * across ALL rooms", not a room-specific undo. removeLast() is O(n) on
 * this singly linked Queue (it must walk from front to find the node
 * before rear); peekLast() is O(1).
 *
 * @author KohJun
 */
public class HousekeepingControl {

    private DoublyLinkedListQueue<RoomStatusLog> statusLog;
    private DoublyLinkedListQueue<Room> roomList;

    private static final String[] STATUS_SEQUENCE = {
        "DIRTY", "CLEANING", "INSPECTED", "READY"
    };

    public HousekeepingControl() {
        this(
            new RoomDAO().getAllRooms(),
            new RoomStatusLogDAO().getAllLogs());
    }

    // Constructor used when Main provides shared application data.
    public HousekeepingControl(
            DoublyLinkedListQueue<Room> sharedRooms,
            DoublyLinkedListQueue<RoomStatusLog> sharedStatusLog) {
        // Keep the same Queue references provided by Main.
        roomList = sharedRooms;
        statusLog = sharedStatusLog;
        // Bring every shared Room's cleaningStatus in line with the
        // latest log already loaded, so Walk-In sees correct readiness
        // immediately, without waiting for a new log to be entered.
        syncAllRoomCleaningStatus();
    }

    /**
     * Synchronizes every shared Room's cleaningStatus field from its
     * latest RoomStatusLog entry. A room with no history is set to
     * "UNKNOWN" rather than left at whatever default Room.java has.
     */
    private void syncAllRoomCleaningStatus() {
        int total = roomList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            Room room = roomList.getEntry(i);
            RoomStatusLog latest = getCurrentStatus(room.getRoomNumber());
            room.setCleaningStatus(latest != null ? latest.getStatus() : "UNKNOWN");
        }
    }

    /**
     * Helper: finds the actual shared Room object (not a copy) for a
     * given room number, using a self-implemented linear search.
     */
    private Room findRoomByNumber(String roomNumber) {
        int total = roomList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            Room room = roomList.getEntry(i);
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return room;
            }
        }
        return null;
    }

    public boolean isValidRoomNumber(String roomNumber) {
        if (roomNumber == null) {
            return false;
        }
        int total = roomList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            Room room = roomList.getEntry(i);
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return true;
            }
        }
        return false;
    }

    public String getRoomType(String roomNumber) {
        int total = roomList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            Room room = roomList.getEntry(i);
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return room.getRoomType();
            }
        }
        return "Unknown";
    }

    public boolean isValidRoomType(String roomType) {
        if (roomType == null) {
            return false;
        }
        int total = roomList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            if (roomList.getEntry(i).getRoomType().equalsIgnoreCase(roomType)) {
                return true;
            }
        }
        return false;
    }

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

    /**
     * Determines whether newStatus is a valid NEXT step from the room's
     * current status: DIRTY -> CLEANING -> INSPECTED -> READY, or READY
     * restarting at DIRTY. A room with no prior log may only start at
     * DIRTY. Null/unknown status values are safely rejected instead of
     * throwing a NullPointerException.
     */
    public boolean isValidNextStatus(String roomNumber, String newStatus) {
        if (newStatus == null || !isValidStatus(newStatus)) {
            return false;
        }
        RoomStatusLog current = getCurrentStatus(roomNumber);
        if (current == null) {
            return newStatus.equalsIgnoreCase("DIRTY");
        }
        int currentIndex = indexOfStatus(current.getStatus());
        int newIndex = indexOfStatus(newStatus);
        // Guard against corrupted/unknown stored data — never let an
        // unmapped index (-1) accidentally satisfy the "+1" check below.
        if (currentIndex == -1 || newIndex == -1) {
            return false;
        }
        if (currentIndex == STATUS_SEQUENCE.length - 1 && newStatus.equalsIgnoreCase("DIRTY")) {
            return true;
        }
        return newIndex == currentIndex + 1;
    }

    private int indexOfStatus(String status) {
        for (int i = 0; i < STATUS_SEQUENCE.length; i++) {
            if (STATUS_SEQUENCE[i].equalsIgnoreCase(status)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Logs a new status change for a room. Validation order is:
     * (1) room exists, (2) status is a supported value,
     * (3) the transition is legal, (4) enqueue actually succeeds.
     * After a successful log, the shared Room's cleaningStatus is
     * updated so Walk-In immediately sees the new readiness state.
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
        boolean enqueued = statusLog.enqueue(entry);
        if (!enqueued) {
            // Honour the ADT contract — don't report success if the
            // Queue itself refused the entry.
            return false;
        }
        Room room = findRoomByNumber(roomNumber);
        if (room != null) {
            room.setCleaningStatus(status.toUpperCase());
        }
        return true;
    }

    /**
     * Finds the CURRENT (most recent) status of a given room. Because
     * this is a singly linked Queue, "most recent" is found by scanning
     * forward and keeping the last match — this relies on the DAO/log
     * insertion order being chronological (see RoomStatusLogDAO).
     */
    public RoomStatusLog getCurrentStatus(String roomNumber) {
        RoomStatusLog latest = null;
        int total = statusLog.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            RoomStatusLog entry = statusLog.getEntry(i);
            if (entry.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                latest = entry;
            }
        }
        return latest;
    }

    /**
     * Retrieves the FULL status history for a given room, in
     * chronological order, using a self-implemented linear filter.
     */
    public DoublyLinkedListQueue<RoomStatusLog> getHistoryForRoom(String roomNumber) {
        DoublyLinkedListQueue<RoomStatusLog> history = new DoublyLinkedListQueue<>();
        int total = statusLog.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            RoomStatusLog entry = statusLog.getEntry(i);
            if (entry.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                history.enqueue(entry);
            }
        }
        return history;
    }

    /**
     * Rolls back the most recently logged status change GLOBALLY
     * (across all rooms) using removeLast() — an O(n) operation on this
     * singly linked Queue. After removal, the affected shared Room's
     * cleaningStatus is restored to whatever its new latest log says,
     * or "UNKNOWN" if no log remains for that room.
     */
    public RoomStatusLog rollbackLastChange() {
        RoomStatusLog removed = statusLog.removeLast();
        if (removed != null) {
            Room room = findRoomByNumber(removed.getRoomNumber());
            if (room != null) {
                RoomStatusLog newLatest = getCurrentStatus(removed.getRoomNumber());
                room.setCleaningStatus(newLatest != null ? newLatest.getStatus() : "UNKNOWN");
            }
        }
        return removed;
    }

    /**
     * Previews what rollback would remove (O(1) — peekLast()), without
     * removing it, so the supervisor can confirm first.
     */
    public RoomStatusLog previewLastChange() {
        return statusLog.peekLast();
    }

    /**
     * Report 1: lists all rooms whose CURRENT status matches the given
     * filter, optionally further filtered by room type, sorted by room
     * number (self-implemented insertion sort).
     *
     * NOTE: only rooms that have at least one status log can appear
     * here, since "UNKNOWN" (no log yet) is not one of the four
     * filterable stages. Rooms without any log are intentionally
     * excluded from this report rather than silently misreported as
     * DIRTY/READY/etc.
     */
    public DoublyLinkedListQueue<RoomStatusLog> getRoomsByCurrentStatus(String statusFilter, String roomTypeFilter) {
        DoublyLinkedListQueue<RoomStatusLog> result = new DoublyLinkedListQueue<>();
        DoublyLinkedListQueue<String> seenRooms = new DoublyLinkedListQueue<>();
        int total = statusLog.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            String roomNumber = statusLog.getEntry(i).getRoomNumber();
            if (!seenRooms.contains(roomNumber)) {
                seenRooms.enqueue(roomNumber);
            }
        }

        int totalRooms = seenRooms.getNumberOfEntries();
        for (int i = 0; i < totalRooms; i++) {
            String roomNumber = seenRooms.getEntry(i);
            RoomStatusLog current = getCurrentStatus(roomNumber);
            if (current == null || !current.getStatus().equalsIgnoreCase(statusFilter)) {
                continue;
            }
            boolean roomTypeMatches = roomTypeFilter.equalsIgnoreCase("ALL")
                    || getRoomType(roomNumber).equalsIgnoreCase(roomTypeFilter);
            if (roomTypeMatches) {
                result.enqueue(current);
            }
        }
        return sortByRoomNumber(result);
    }

    /**
     * Report 2: average time (minutes) spent per cleaning stage.
     *
     * Business decision: READY is the END of a cleaning cycle. The gap
     * from READY to the next DIRTY includes guest occupancy / waiting
     * time, not actual cleaning-stage duration, so READY is
     * intentionally excluded from this average. Malformed timestamps
     * or accidental negative/zero gaps are skipped rather than
     * corrupting the average or crashing the report.
     */
    public DoublyLinkedListQueue<StageDuration> getAverageDurationPerStage(String stageFilter) {
        DoublyLinkedListQueue<String> stageNames = new DoublyLinkedListQueue<>();
        DoublyLinkedListQueue<Long> stageTotalMinutes = new DoublyLinkedListQueue<>();
        DoublyLinkedListQueue<Integer> stageCount = new DoublyLinkedListQueue<>();

        DoublyLinkedListQueue<String> distinctRooms = new DoublyLinkedListQueue<>();
        int total = statusLog.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            String roomNumber = statusLog.getEntry(i).getRoomNumber();
            if (!distinctRooms.contains(roomNumber)) {
                distinctRooms.enqueue(roomNumber);
            }
        }

        int totalRooms = distinctRooms.getNumberOfEntries();
        for (int r = 0; r < totalRooms; r++) {
            String roomNumber = distinctRooms.getEntry(r);
            DoublyLinkedListQueue<RoomStatusLog> roomEntries = new DoublyLinkedListQueue<>();
            for (int i = 0; i < total; i++) {
                RoomStatusLog entry = statusLog.getEntry(i);
                if (entry.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                    roomEntries.enqueue(entry);
                }
            }

            int roomTotal = roomEntries.getNumberOfEntries();
            for (int i = 0; i < roomTotal - 1; i++) {
                RoomStatusLog current = roomEntries.getEntry(i);
                RoomStatusLog next = roomEntries.getEntry(i + 1);
                String stage = current.getStatus();

                if (stage.equalsIgnoreCase("READY")) {
                    // READY -> next DIRTY spans guest occupancy, not a
                    // cleaning stage — excluded by design (see above).
                    continue;
                }
                if (!stageFilter.equalsIgnoreCase("ALL") && !stage.equalsIgnoreCase(stageFilter)) {
                    continue;
                }

                long minutes = minutesBetween(current.getTimestamp(), next.getTimestamp());
                if (minutes < 0) {
                    // Malformed timestamp or out-of-order data — skip
                    // rather than pollute the average with a negative.
                    continue;
                }

                int index = indexOfStageName(stageNames, stage);
                if (index == -1) {
                    stageNames.enqueue(stage);
                    stageTotalMinutes.enqueue(minutes);
                    stageCount.enqueue(1);
                } else {
                    long updatedTotal = stageTotalMinutes.getEntry(index) + minutes;
                    int updatedCount = stageCount.getEntry(index) + 1;
                    replaceAt(stageTotalMinutes, index, updatedTotal);
                    replaceAt(stageCount, index, updatedCount);
                }
            }
        }

        DoublyLinkedListQueue<StageDuration> resultList = new DoublyLinkedListQueue<>();
        int stagesFound = stageNames.getNumberOfEntries();
        for (int i = 0; i < stagesFound; i++) {
            String stage = stageNames.getEntry(i);
            long avgMinutes = stageTotalMinutes.getEntry(i) / stageCount.getEntry(i);
            resultList.enqueue(new StageDuration(stage, avgMinutes));
        }
        return sortByDurationDescending(resultList);
    }

    /**
     * Calculates whole minutes between two "yyyy-MM-dd HH:mm" timestamp
     * strings. Returns -1 (an impossible real duration) if the
     * timestamps are malformed, so callers can safely skip the pair
     * instead of crashing the whole report.
     */
    private long minutesBetween(String startTimestamp, String endTimestamp) {
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try {
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(startTimestamp, formatter);
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(endTimestamp, formatter);
            return java.time.Duration.between(start, end).toMinutes();
        } catch (java.time.format.DateTimeParseException e) {
            return -1;
        }
    }

    private int indexOfStageName(DoublyLinkedListQueue<String> stageNames, String stage) {
        int total = stageNames.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            if (stageNames.getEntry(i).equalsIgnoreCase(stage)) {
                return i;
            }
        }
        return -1;
    }

    private void replaceAt(DoublyLinkedListQueue<Long> queue, int position, long newValue) {
        DoublyLinkedListQueue<Long> rebuilt = new DoublyLinkedListQueue<>();
        int total = queue.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            rebuilt.enqueue(i == position ? newValue : queue.getEntry(i));
        }
        queue.clear();
        for (int i = 0; i < rebuilt.getNumberOfEntries(); i++) {
            queue.enqueue(rebuilt.getEntry(i));
        }
    }

    private void replaceAt(DoublyLinkedListQueue<Integer> queue, int position, int newValue) {
        DoublyLinkedListQueue<Integer> rebuilt = new DoublyLinkedListQueue<>();
        int total = queue.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            rebuilt.enqueue(i == position ? newValue : queue.getEntry(i));
        }
        queue.clear();
        for (int i = 0; i < rebuilt.getNumberOfEntries(); i++) {
            queue.enqueue(rebuilt.getEntry(i));
        }
    }

    private DoublyLinkedListQueue<RoomStatusLog> sortByRoomNumber(DoublyLinkedListQueue<RoomStatusLog> input) {
        int n = input.getNumberOfEntries();
        RoomStatusLog[] arr = new RoomStatusLog[n];
        for (int i = 0; i < n; i++) {
            arr[i] = input.getEntry(i);
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
        DoublyLinkedListQueue<RoomStatusLog> sorted = new DoublyLinkedListQueue<>();
        for (RoomStatusLog r : arr) {
            sorted.enqueue(r);
        }
        return sorted;
    }

    private DoublyLinkedListQueue<StageDuration> sortByDurationDescending(DoublyLinkedListQueue<StageDuration> input) {
        int n = input.getNumberOfEntries();
        StageDuration[] arr = new StageDuration[n];
        for (int i = 0; i < n; i++) {
            arr[i] = input.getEntry(i);
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
        DoublyLinkedListQueue<StageDuration> sorted = new DoublyLinkedListQueue<>();
        for (StageDuration sd : arr) {
            sorted.enqueue(sd);
        }
        return sorted;
    }

    /**
     * Exposes the full raw log (all entries, all rooms) for reporting.
     * Returns a COPY of the master log, not the live reference — callers
     * cannot accidentally clear(), dequeue(), or removeLast() on the
     * real shared statusLog by mutating what this method returns.
     */
    public DoublyLinkedListQueue<RoomStatusLog> getFullLog() {
        DoublyLinkedListQueue<RoomStatusLog> copy = new DoublyLinkedListQueue<>();
        int total = statusLog.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            copy.enqueue(statusLog.getEntry(i));
        }
        return copy;
    }
}