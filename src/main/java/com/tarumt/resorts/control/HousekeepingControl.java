package com.tarumt.resorts.control;

import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.StageDuration;
import com.tarumt.resorts.adt.Queue;
import com.tarumt.resorts.dao.RoomStatusLogDAO;
import com.tarumt.resorts.dao.RoomDAO;

/**
 * HousekeepingControl.java
 * Handles the business logic for the Housekeeping & Task Log module.
 *
 * @author YourName
 */
public class HousekeepingControl {

    private Queue<RoomStatusLog> statusLog;
    private Queue<Room> roomList;

    // Defines the valid forward sequence of cleaning stages.
    private static final String[] STATUS_SEQUENCE = {
            "DIRTY", "CLEANING", "INSPECTED", "READY"
    };

    // Temporary constructor for running this module independently.
    public HousekeepingControl() {
        this(
                new RoomDAO().getAllRooms(),
                new RoomStatusLogDAO().getAllLogs());
    }

    // Constructor used when Main provides shared application data.
    public HousekeepingControl(
            Queue<Room> sharedRooms,
            Queue<RoomStatusLog> sharedStatusLog) {

        // Keep the same Queue references provided by Main.
        roomList = sharedRooms;
        statusLog = sharedStatusLog;
    }

    /**
     * Validates that the given room number actually exists in the
     * system's room list, using a self-implemented linear search.
     * Prevents logging status changes for rooms that don't exist
     * (e.g. a typo like "1234").
     */
    public boolean isValidRoomNumber(String roomNumber) {
        if (roomNumber == null)
            return false;
        int total = roomList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            Room room = roomList.getEntry(i);
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the room type (e.g. "Standard", "Deluxe", "Suite") for a
     * given room number, using a self-implemented linear search over the
     * shared roomList. Returns "Unknown" if the room isn't found (should
     * not normally happen since isValidRoomNumber() is checked first).
     */
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

    /**
     * Validates that the given room type actually exists among the
     * system's rooms, using a self-implemented linear search.
     */
    public boolean isValidRoomType(String roomType) {
        if (roomType == null)
            return false;
        int total = roomList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            if (roomList.getEntry(i).getRoomType().equalsIgnoreCase(roomType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that the given status is one of the defined stages.
     */
    public boolean isValidStatus(String status) {
        if (status == null)
            return false;
        for (String s : STATUS_SEQUENCE) {
            if (s.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether newStatus is a valid NEXT step from the room's
     * current status, enforcing the required sequence:
     * DIRTY -> CLEANING -> INSPECTED -> READY.
     * A room with no prior log may only start at DIRTY.
     */
    public boolean isValidNextStatus(String roomNumber, String newStatus) {
        RoomStatusLog current = getCurrentStatus(roomNumber);

        // No history yet — must start the cycle at DIRTY.
        if (current == null) {
            return newStatus.equalsIgnoreCase("DIRTY");
        }

        int currentIndex = indexOfStatus(current.getStatus());
        int newIndex = indexOfStatus(newStatus);

        // Special case: a room at READY (end of cycle) may restart the
        // cycle at DIRTY once a new guest checks in and the room needs
        // cleaning again.
        if (currentIndex == STATUS_SEQUENCE.length - 1 && newStatus.equalsIgnoreCase("DIRTY")) {
            return true;
        }

        // Otherwise, only allow moving exactly one step forward in the sequence.
        return newIndex == currentIndex + 1;
    }

    /**
     * Helper: finds the position of a status within STATUS_SEQUENCE.
     */
    private int indexOfStatus(String status) {
        for (int i = 0; i < STATUS_SEQUENCE.length; i++) {
            if (STATUS_SEQUENCE[i].equalsIgnoreCase(status)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Basic function: logs a new status change for a room by enqueueing
     * a new RoomStatusLog entry to the rear of the shared queue.
     */
    public boolean logStatusChange(String roomNumber, String status, String timestamp) {
        if (!isValidRoomNumber(roomNumber)) {
            return false; // reject — this room doesn't exist in the system
        }
        if (!isValidNextStatus(roomNumber, status)) {
            return false; // reject — breaks the required DIRTY->CLEANING->INSPECTED->READY sequence
        }
        RoomStatusLog entry = new RoomStatusLog(roomNumber, status, timestamp);
        statusLog.enqueue(entry);
        return true;
    }

    /**
     * Basic function: finds the CURRENT (most recent) status of a given
     * room using a self-implemented linear search — scanning from the
     * rear-most matching entry backwards is not possible with a singly
     * linked queue, so instead we scan forward and keep the last match.
     */
    public RoomStatusLog getCurrentStatus(String roomNumber) {
        RoomStatusLog latest = null;
        int total = statusLog.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            RoomStatusLog entry = statusLog.getEntry(i);
            if (entry.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                latest = entry; // keep overwriting; last match = most recent
            }
        }
        return latest;
    }

    /**
     * Additional function: retrieves the FULL status history for a given
     * room, in chronological order, using a self-implemented linear
     * search/filter over the shared log. Satisfies the requirement to
     * view a room's history, not just its current status.
     */
    public Queue<RoomStatusLog> getHistoryForRoom(String roomNumber) {
        Queue<RoomStatusLog> history = new Queue<>();
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
     * Additional/creative function: rolls back the most recently logged
     * status change using removeLast(). Returns the removed entry so the
     * Boundary can display what was undone, or null if the log is empty.
     */
    public RoomStatusLog rollbackLastChange() {
        return statusLog.removeLast();
    }

    /**
     * Additional/creative function: preview what rollback would remove,
     * without actually removing it — lets the supervisor confirm first.
     */
    public RoomStatusLog previewLastChange() {
        return statusLog.peekLast();
    }

    /**
     * Additional/creative function - Report 1: lists all rooms whose
     * CURRENT status matches the given filter (e.g. all "DIRTY" rooms),
     * optionally further filtered by room type. Combines a linear scan
     * with two filter conditions, then sorts the result by room number
     * using a self-implemented insertion sort.
     *
     * @param statusFilter   required status filter (e.g. "DIRTY")
     * @param roomTypeFilter optional room type filter; pass "ALL" to skip
     */
    public Queue<RoomStatusLog> getRoomsByCurrentStatus(String statusFilter, String roomTypeFilter) {
        Queue<RoomStatusLog> result = new Queue<>();
        // First, find distinct room numbers.
        Queue<String> seenRooms = new Queue<>();
        int total = statusLog.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            String roomNumber = statusLog.getEntry(i).getRoomNumber();
            if (!seenRooms.contains(roomNumber)) {
                seenRooms.enqueue(roomNumber);
            }
        }
        // For each distinct room, check its current status AND room type.
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
     * Additional/creative function - Report 2: calculates the average
     * time (in minutes) spent in each cleaning stage across all rooms,
     * by measuring the gap between consecutive status entries belonging
     * to the SAME room (grouped first, so interleaved entries from other
     * rooms do not break the pairing). Results may be filtered to a
     * single stage, and are sorted by average duration (self-implemented
     * selection sort), longest stage first.
     *
     * @param stageFilter optional stage name filter; pass "ALL" to skip
     */
    public Queue<StageDuration> getAverageDurationPerStage(String stageFilter) {
        Queue<String> stageNames = new Queue<>();
        Queue<Long> stageTotalMinutes = new Queue<>();
        Queue<Integer> stageCount = new Queue<>();

        // Step 1: find all distinct room numbers in the log.
        Queue<String> distinctRooms = new Queue<>();
        int total = statusLog.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            String roomNumber = statusLog.getEntry(i).getRoomNumber();
            if (!distinctRooms.contains(roomNumber)) {
                distinctRooms.enqueue(roomNumber);
            }
        }

        // Step 2: for EACH room, extract its own entries in original
        // order, then measure consecutive gaps within that room's own
        // sequence only — immune to other rooms' entries being
        // interleaved in the shared log.
        int totalRooms = distinctRooms.getNumberOfEntries();
        for (int r = 0; r < totalRooms; r++) {
            String roomNumber = distinctRooms.getEntry(r);
            Queue<RoomStatusLog> roomEntries = new Queue<>();

            for (int i = 0; i < total; i++) {
                RoomStatusLog entry = statusLog.getEntry(i);
                if (entry.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                    roomEntries.enqueue(entry);
                }
            }

            // Now measure consecutive pairs WITHIN this room's own sequence.
            int roomTotal = roomEntries.getNumberOfEntries();
            for (int i = 0; i < roomTotal - 1; i++) {
                RoomStatusLog current = roomEntries.getEntry(i);
                RoomStatusLog next = roomEntries.getEntry(i + 1);
                String stage = current.getStatus();

                // Apply the stage filter early — skip stages we don't care about.
                if (!stageFilter.equalsIgnoreCase("ALL") && !stage.equalsIgnoreCase(stageFilter)) {
                    continue;
                }

                long minutes = minutesBetween(current.getTimestamp(), next.getTimestamp());

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

        // Step 3: build the result list with averages.
        Queue<StageDuration> resultList = new Queue<>();
        int stagesFound = stageNames.getNumberOfEntries();
        for (int i = 0; i < stagesFound; i++) {
            String stage = stageNames.getEntry(i);
            long avgMinutes = stageTotalMinutes.getEntry(i) / stageCount.getEntry(i);
            resultList.enqueue(new StageDuration(stage, avgMinutes));
        }

        // Step 4: sort by average duration, longest first (selection sort).
        return sortByDurationDescending(resultList);
    }

    /**
     * Helper: calculates whole minutes between two "yyyy-MM-dd HH:mm"
     * formatted timestamp strings.
     */
    private long minutesBetween(String startTimestamp, String endTimestamp) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startTimestamp, formatter);
        java.time.LocalDateTime end = java.time.LocalDateTime.parse(endTimestamp, formatter);
        return java.time.Duration.between(start, end).toMinutes();
    }

    /**
     * Helper: finds the position of a stage name within a Queue<String>,
     * using a self-implemented linear search.
     */
    private int indexOfStageName(Queue<String> stageNames, String stage) {
        int total = stageNames.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            if (stageNames.getEntry(i).equalsIgnoreCase(stage)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper: replaces the Long value at a given position in a Queue,
     * since QueueInterface doesn't expose a direct index-based setter.
     * Rebuilds the queue with the updated value at that position.
     */
    private void replaceAt(Queue<Long> queue, int position, long newValue) {
        Queue<Long> rebuilt = new Queue<>();
        int total = queue.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            rebuilt.enqueue(i == position ? newValue : queue.getEntry(i));
        }
        queue.clear();
        for (int i = 0; i < rebuilt.getNumberOfEntries(); i++) {
            queue.enqueue(rebuilt.getEntry(i));
        }
    }

    /**
     * Helper: replaces the Integer value at a given position in a Queue,
     * same purpose as the Long overload above.
     */
    private void replaceAt(Queue<Integer> queue, int position, int newValue) {
        Queue<Integer> rebuilt = new Queue<>();
        int total = queue.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            rebuilt.enqueue(i == position ? newValue : queue.getEntry(i));
        }
        queue.clear();
        for (int i = 0; i < rebuilt.getNumberOfEntries(); i++) {
            queue.enqueue(rebuilt.getEntry(i));
        }
    }

    /**
     * Self-implemented insertion sort: sorts RoomStatusLog entries by
     * room number, ascending. Used by Report 1.
     */
    private Queue<RoomStatusLog> sortByRoomNumber(Queue<RoomStatusLog> input) {
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

        Queue<RoomStatusLog> sorted = new Queue<>();
        for (RoomStatusLog r : arr) {
            sorted.enqueue(r);
        }
        return sorted;
    }

    /**
     * Self-implemented selection sort: sorts StageDuration entries by
     * average minutes, descending (longest stage duration first). Used
     * by Report 2. Uses a plain array (not a Java Collections Framework
     * class) as scratch space for the swap logic.
     */
    private Queue<StageDuration> sortByDurationDescending(Queue<StageDuration> input) {
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

        Queue<StageDuration> sorted = new Queue<>();
        for (StageDuration sd : arr) {
            sorted.enqueue(sd);
        }
        return sorted;
    }

    /**
     * Exposes the full raw log (all entries, all rooms) for reporting.
     */
    public Queue<RoomStatusLog> getFullLog() {
        return statusLog;
    }
}