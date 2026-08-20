package com.tarumt.resorts.control;

import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.VIPAllocationRequest;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.dao.GuestDAO;
import com.tarumt.resorts.dao.RoomDAO;
import com.tarumt.resorts.dao.VIPAllocationDAO;
import com.tarumt.resorts.util.RoomScheduleAvailability;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Iterator;

/**
 * VIPAllocationControl.java
 * Handles the business logic for the VIP & Loyalty Tier Priority Room
 * Allocation module. Guests with a priority membership tier (PLATINUM,
 * DIAMOND, ELITE) are inserted into a priority-ordered queue: higher
 * tier guests are placed ahead of lower tier guests. Guests of the same
 * tier keep their original arrival order (first registered, first
 * allocated).
 *
 * Booking integration mirrors Walk-In Registration: a request carries a
 * requested room type, check-in date, and stay duration; allocation
 * checks for CONFIRMED/ACTIVE booking overlaps on the shared booking
 * collection rather than trusting Room.isAvailable() alone; and a
 * successful allocation creates a CONFIRMED booking without touching the
 * room's live availability flag - that is Front-Desk's job at actual
 * check-in / check-out time.
 *
 * @author brian
 */
public class VIPAllocationControl {

    // Priority-ordered active requests (highest tier at the front).
    private ListQueueInterface<VIPAllocationRequest> priorityQueue;

    // Complete request records used for searching and reporting.
    private ListQueueInterface<VIPAllocationRequest> requestHistory;

    private ListQueueInterface<Room> roomList;
    private ListQueueInterface<Guest> guestList;
    private ListQueueInterface<Booking> bookingList;

    private int requestCounter;
    private int confirmationCounter;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Standalone constructor - lets this module run/demo on its own. */
    public VIPAllocationControl() {
        ListQueueInterface<Room> rooms = new RoomDAO().getAllRooms();
        ListQueueInterface<Guest> guests = new GuestDAO().getAllGuests();
        ListQueueInterface<Booking> bookings = new DoublyLinkedListQueue<>();
        ListQueueInterface<VIPAllocationRequest> history =
                new VIPAllocationDAO().getAllRequests(guests);

        init(rooms, guests, bookings, history);
    }

    /**
     * Integrated constructor - receives the same shared collection
     * references used by the rest of the system (sharedRooms,
     * sharedGuests, sharedBookings), so VIP Allocation reads/writes the
     * exact same Room, Guest and Booking objects as every other module.
     * No new DAO collections are created here.
     */
    public VIPAllocationControl(
            ListQueueInterface<Room> sharedRooms,
            ListQueueInterface<Guest> sharedGuests,
            ListQueueInterface<Booking> sharedBookings,
            ListQueueInterface<VIPAllocationRequest> sharedRequestHistory) {

        init(sharedRooms, sharedGuests, sharedBookings, sharedRequestHistory);
    }

    private void init(
            ListQueueInterface<Room> sharedRooms,
            ListQueueInterface<Guest> sharedGuests,
            ListQueueInterface<Booking> sharedBookings,
            ListQueueInterface<VIPAllocationRequest> sharedRequestHistory) {

        roomList = sharedRooms;
        guestList = sharedGuests;
        bookingList = sharedBookings;
        requestHistory = sharedRequestHistory;
        priorityQueue = new DoublyLinkedListQueue<>();

        // Sort history chronologically first, so that when we replay
        // WAITING entries through priorityEnqueue(), guests of the same
        // tier land in the priority queue in their original arrival
        // order (earliest first).
        VIPAllocationRequest[] chronological = getAllRequestHistory();
        sortByRequestTime(chronological);

        for (int i = 0; i < chronological.length; i++) {
            if (chronological[i].getStatus().equalsIgnoreCase("WAITING")) {
                priorityQueue.priorityEnqueue(
                        chronological[i],
                        tierPriorityComparator());
            }
        }

        requestCounter = requestHistory.getNumberOfEntries() + 1;
        confirmationCounter = bookingList.getNumberOfEntries() + 1;
    }

    // =====================================================================
    // Core feature: priority insertion by membership tier.
    // =====================================================================

    /**
     * Comparator used to order VIP requests: higher membership tier
     * priority level comes first (ELITE > DIAMOND > PLATINUM). When two
     * requests belong to the same tier, this comparator returns 0, which
     * makes priorityEnqueue() skip past all existing same-tier entries
     * and insert the new one AFTER them - preserving first-come-first-
     * served (arrival time) order within a tier.
     */
    private Comparator<VIPAllocationRequest> tierPriorityComparator() {
        return (newRequest, existingRequest) ->
                existingRequest.getGuest().getMembershipTier().getPriorityLevel()
                - newRequest.getGuest().getMembershipTier().getPriorityLevel();
    }

    /**
     * Registers a new VIP allocation request. The guest must already
     * exist and hold a priority membership tier. A guest may hold
     * multiple simultaneous VIP requests - a Guest ID already having a
     * WAITING request no longer blocks a new one.
     *
     * @return the created request, or null if the guest is invalid, not
     *         a priority-tier member, or the room type / schedule is
     *         invalid
     */
    public VIPAllocationRequest registerVIPRequest(
            String guestId,
            String requestedRoomType,
            LocalDate requestedCheckInDate,
            int stayDurationDays) {

        Guest guest = findGuestById(guestId);

        if (guest == null || !guest.getMembershipTier().isPriorityTier()) {
            return null;
        }

        boolean validRoomType = requestedRoomType != null
                && (requestedRoomType.equalsIgnoreCase("Standard")
                        || requestedRoomType.equalsIgnoreCase("Deluxe")
                        || requestedRoomType.equalsIgnoreCase("Suite"));
        if (!validRoomType) {
            return null;
        }

        if (requestedCheckInDate == null
                || requestedCheckInDate.isBefore(LocalDate.now())) {
            return null;
        }

        if (stayDurationDays < 1 || stayDurationDays > 30) {
            return null;
        }

        String requestId = generateRequestId();
        String requestTime = LocalDateTime.now().format(TIME_FORMAT);

        VIPAllocationRequest request = new VIPAllocationRequest(
                requestId, guest, requestTime, requestedRoomType,
                requestedCheckInDate, stayDurationDays);

        boolean insertedIntoQueue =
                priorityQueue.priorityEnqueue(request, tierPriorityComparator());

        if (!insertedIntoQueue) {
            return null;
        }

        requestHistory.enqueue(request);
        return request;
    }

    // =====================================================================
    // Update / cancel a WAITING request (verified by Request ID + Guest ID).
    // =====================================================================

    /**
     * Finds one active WAITING VIP request using both Request ID and
     * Guest ID, so staff cannot accidentally modify another guest's
     * request just by guessing a Request ID.
     */
    public VIPAllocationRequest findWaitingRequestById(
            String requestId, String guestId) {

        if (requestId == null || requestId.trim().isEmpty()
                || guestId == null || guestId.trim().isEmpty()) {
            return null;
        }

        String targetRequestId = requestId.trim();
        String targetGuestId = guestId.trim();

        Iterator<VIPAllocationRequest> iterator = priorityQueue.getIterator();
        while (iterator.hasNext()) {
            VIPAllocationRequest current = iterator.next();

            boolean requestIdMatches = current.getRequestId()
                    .equalsIgnoreCase(targetRequestId);
            boolean guestIdMatches = current.getGuest() != null
                    && current.getGuest().getGuestId() != null
                    && current.getGuest().getGuestId().equalsIgnoreCase(targetGuestId);
            boolean isWaiting = "WAITING".equalsIgnoreCase(current.getStatus());

            if (requestIdMatches && guestIdMatches && isWaiting) {
                return current;
            }
        }
        return null;
    }

    /**
     * Finds ANY VIP request (WAITING, ASSIGNED, or CANCELLED) by Request
     * ID, searching the full history rather than only the live priority
     * queue. Used by the Search feature so a request stays findable
     * after it has already been allocated or cancelled.
     */
    public VIPAllocationRequest findRequestById(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            return null;
        }
        return requestHistory.searchByKey(
                requestId.trim(),
                request -> request.getRequestId());
    }

    /**
     * Updates the room type / schedule of one WAITING request. The
     * request's position in the priority queue does not need to change,
     * since re-ordering only ever depends on the guest's membership
     * tier, which this method does not modify.
     *
     * @return true if a matching WAITING request was found and updated
     */
    public boolean updateVIPRequest(
            String requestId,
            String guestId,
            String newRoomType,
            LocalDate newCheckInDate,
            int newStayDurationDays) {

        if (newRoomType == null) {
            return false;
        }

        String normalizedRoomType;
        if (newRoomType.equalsIgnoreCase("Standard")) {
            normalizedRoomType = "Standard";
        } else if (newRoomType.equalsIgnoreCase("Deluxe")) {
            normalizedRoomType = "Deluxe";
        } else if (newRoomType.equalsIgnoreCase("Suite")) {
            normalizedRoomType = "Suite";
        } else {
            return false;
        }

        if (newCheckInDate == null || newCheckInDate.isBefore(LocalDate.now())) {
            return false;
        }

        if (newStayDurationDays < 1 || newStayDurationDays > 30) {
            return false;
        }

        VIPAllocationRequest selected = findWaitingRequestById(requestId, guestId);
        if (selected == null) {
            return false;
        }

        selected.setRequestedRoomType(normalizedRoomType);
        selected.setRequestedCheckInDate(newCheckInDate);
        selected.setStayDurationDays(newStayDurationDays);
        return true;
    }

    /**
     * Cancels one WAITING request identified by both Request ID and
     * Guest ID. The record is kept in requestHistory (soft-cancel, same
     * object reference) but removed from the active priority queue. The
     * queue is rebuilt with dequeue + priorityEnqueue so every other
     * request keeps its original relative order - mirrors
     * LoyaltyRewardsControl.cancelRedemptionRequest()'s rebuild pattern.
     */
    public boolean cancelVIPRequest(String requestId, String guestId) {
        if (requestId == null || requestId.trim().isEmpty()
                || guestId == null || guestId.trim().isEmpty()) {
            return false;
        }

        String targetRequestId = requestId.trim();
        String targetGuestId = guestId.trim();

        int originalSize = priorityQueue.getNumberOfEntries();
        boolean cancelled = false;

        for (int i = 0; i < originalSize; i++) {
            VIPAllocationRequest current = priorityQueue.dequeue();

            boolean requestIdMatches = current.getRequestId()
                    .equalsIgnoreCase(targetRequestId);
            boolean guestIdMatches = current.getGuest() != null
                    && current.getGuest().getGuestId() != null
                    && current.getGuest().getGuestId().equalsIgnoreCase(targetGuestId);
            boolean isWaiting = "WAITING".equalsIgnoreCase(current.getStatus());

            if (!cancelled && requestIdMatches && guestIdMatches && isWaiting) {
                current.setStatus("CANCELLED");
                cancelled = true;
                // Not re-enqueued - removed from the active priority queue.
            } else {
                priorityQueue.priorityEnqueue(current, tierPriorityComparator());
            }
        }

        return cancelled;
    }

    // =====================================================================
    // Allocation.
    // =====================================================================

    private boolean isReadyForAllocation(Room room) {
        String cleaningStatus = room.getCleaningStatus();
        return cleaningStatus == null
                || cleaningStatus.equalsIgnoreCase("READY")
                || cleaningStatus.equalsIgnoreCase("UNKNOWN");
    }

    /**
     * True if the room has NO CONFIRMED/ACTIVE booking whose scheduled
     * stay overlaps [checkIn, checkOut). Delegates to the shared
     * RoomScheduleAvailability utility so every module that needs this
     * check (Walk-In, VIP) agrees on what "available for these dates"
     * means from one place.
     */
    private boolean isRoomAvailableForSchedule(
            Room room,
            LocalDate requestedCheckInDate,
            LocalDate requestedCheckOutDate) {
        return RoomScheduleAvailability.isAvailable(
                bookingList,
                room,
                requestedCheckInDate,
                requestedCheckOutDate);
    }

    /**
     * Checks whether at least one room of the requested type is free for
     * the full requested stay period [checkInDate, checkInDate +
     * stayDurationDays). Public so the boundary layer can drive a
     * calendar view (one call per candidate day) as well as validate the
     * final chosen date, exactly like
     * WalkInRegistrationControl.hasAvailableRoomForSchedule().
     */
    public boolean hasAvailableRoomForSchedule(
            String roomType,
            LocalDate requestedCheckInDate,
            int stayDurationDays) {

        if (roomType == null || roomType.trim().isEmpty()
                || requestedCheckInDate == null
                || requestedCheckInDate.isBefore(LocalDate.now())
                || stayDurationDays <= 0) {
            return false;
        }

        LocalDate requestedCheckOutDate = requestedCheckInDate.plusDays(stayDurationDays);
        boolean immediateCheckIn = requestedCheckInDate.equals(LocalDate.now());

        Iterator<Room> roomIterator = roomList.getIterator();
        while (roomIterator.hasNext()) {
            Room candidateRoom = roomIterator.next();

            boolean roomTypeMatches = candidateRoom.getRoomType()
                    .equalsIgnoreCase(roomType.trim());

            boolean operationallyReady = !immediateCheckIn
                    || (candidateRoom.isAvailable() && isReadyForAllocation(candidateRoom));

            boolean scheduleAvailable = isRoomAvailableForSchedule(
                    candidateRoom, requestedCheckInDate, requestedCheckOutDate);

            if (roomTypeMatches && operationallyReady && scheduleAvailable) {
                return true;
            }
        }
        return false;
    }

    /**
     * Allocates a room to the guest currently at the front of the
     * priority queue (highest tier, earliest among ties). A room
     * qualifies only when its type matches AND it has no overlapping
     * CONFIRMED/ACTIVE booking for the requested dates - Room.isAvailable()
     * alone is checked only for a same-day check-in, since that also
     * needs the room to be physically free and Housekeeping-ready right
     * now. If no matching room is available, the guest remains at the
     * front and null is returned.
     */
    public Booking allocateNextVIPGuest() {
        VIPAllocationRequest frontRequest = priorityQueue.peek();
        if (frontRequest == null) {
            return null;
        }

        LocalDate requestedCheckInDate = frontRequest.getRequestedCheckInDate();
        LocalDate requestedCheckOutDate = frontRequest.getRequestedCheckOutDate();
        boolean immediateCheckIn = requestedCheckInDate.equals(LocalDate.now());

        Room assignedRoom = null;
        Iterator<Room> roomIterator = roomList.getIterator();

        while (roomIterator.hasNext()) {
            Room candidate = roomIterator.next();
            boolean typeMatches = candidate.getRoomType()
                    .equalsIgnoreCase(frontRequest.getRequestedRoomType());

            boolean operationallyReady = !immediateCheckIn
                    || (candidate.isAvailable() && isReadyForAllocation(candidate));

            boolean scheduleAvailable = isRoomAvailableForSchedule(
                    candidate, requestedCheckInDate, requestedCheckOutDate);

            if (typeMatches && operationallyReady && scheduleAvailable) {
                assignedRoom = candidate;
                break;
            }
        }

        if (assignedRoom == null) {
            return null;
        }

        String confirmationNumber = generateConfirmationNumber();
        String bookingTime = LocalDateTime.now().format(TIME_FORMAT);

        // Full scheduled booking: checkInTime = null resolves status to
        // CONFIRMED, amount is computed from the room's daily rate x
        // nights, and paymentStatus defaults to UNPAID - Front-Desk
        // settles it at actual check-in, same as the Walk-In flow.
        Booking booking = new Booking(
                confirmationNumber,
                frontRequest.getGuest(),
                assignedRoom,
                bookingTime,
                null,
                requestedCheckInDate,
                frontRequest.getStayDurationDays());

        boolean bookingSaved = bookingList.enqueue(booking);
        if (!bookingSaved) {
            return null;
        }

        // Physical room availability is only updated by Front-Desk at
        // actual check-in/check-out. A future/CONFIRMED booking does not
        // occupy the room yet, so room.setAvailable(false) is
        // intentionally NOT called here.
        frontRequest.setStatus("ASSIGNED");
        priorityQueue.dequeue();
        return booking;
    }

    // =====================================================================
    // Display / reporting support.
    // =====================================================================

    /** Returns the current priority queue in order (front = next to be allocated). */
    public VIPAllocationRequest[] getPriorityListInOrder() {
        int total = priorityQueue.getNumberOfEntries();
        VIPAllocationRequest[] result = new VIPAllocationRequest[total];
        for (int i = 0; i < total; i++) {
            result[i] = priorityQueue.getEntry(i);
        }
        return result;
    }

    public int getWaitingCount() {
        return priorityQueue.getNumberOfEntries();
    }

    /**
     * Returns every guest in the shared guest collection, regardless of
     * membership tier. Used to show a full guest directory before VIP
     * registration, so staff can see every valid Guest ID rather than
     * only priority-tier ones. Registration eligibility (priority tier
     * only) is still enforced separately by registerVIPRequest().
     */
    public Guest[] getAllGuests() {
        int total = guestList.getNumberOfEntries();
        Guest[] result = new Guest[total];
        for (int i = 0; i < total; i++) {
            result[i] = guestList.getEntry(i);
        }
        return result;
    }

    /**
     * Report support: filters request history by membership tier and
     * status. "ALL" matches everything for either filter.
     */
    public VIPAllocationRequest[] filterRequestHistory(
            String tierFilter, String statusFilter) {

        VIPAllocationRequest[] history = getAllRequestHistory();
        int matchCount = 0;

        for (int i = 0; i < history.length; i++) {
            if (matchesFilters(history[i], tierFilter, statusFilter)) {
                matchCount++;
            }
        }

        VIPAllocationRequest[] filtered = new VIPAllocationRequest[matchCount];
        int index = 0;
        for (int i = 0; i < history.length; i++) {
            if (matchesFilters(history[i], tierFilter, statusFilter)) {
                filtered[index++] = history[i];
            }
        }
        return filtered;
    }

    private boolean matchesFilters(
            VIPAllocationRequest request, String tierFilter, String statusFilter) {

        boolean tierMatches = tierFilter.equalsIgnoreCase("ALL")
                || request.getGuest().getMembershipTier().toString()
                        .equalsIgnoreCase(tierFilter);

        boolean statusMatches = statusFilter.equalsIgnoreCase("ALL")
                || request.getStatus().equalsIgnoreCase(statusFilter);

        return tierMatches && statusMatches;
    }

    /** Self-implemented insertion sort by request time ascending. */
    public void sortByRequestTime(VIPAllocationRequest[] requests) {
        for (int i = 1; i < requests.length; i++) {
            VIPAllocationRequest key = requests[i];
            int j = i - 1;
            while (j >= 0
                    && requests[j].getRequestTime().compareTo(key.getRequestTime()) > 0) {
                requests[j + 1] = requests[j];
                j--;
            }
            requests[j + 1] = key;
        }
    }

    public VIPAllocationRequest searchByGuestId(String guestId) {
        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }
        return priorityQueue.searchByKey(
                guestId.trim(),
                request -> request.getGuest().getGuestId());
    }

    public VIPAllocationRequest[] getAllRequestHistory() {
        int total = requestHistory.getNumberOfEntries();
        VIPAllocationRequest[] result = new VIPAllocationRequest[total];
        Iterator<VIPAllocationRequest> iterator = requestHistory.getIterator();
        int index = 0;
        while (iterator.hasNext()) {
            result[index++] = iterator.next();
        }
        return result;
    }
    
    public VIPAllocationRequest[] getRequestsByGuestId(String guestId) {
        if (guestId == null || guestId.trim().isEmpty()) {
            return new VIPAllocationRequest[0];
        }
        String targetGuestId = guestId.trim();

        VIPAllocationRequest[] all = getAllRequestHistory();
        int matchCount = 0;
        for (VIPAllocationRequest r : all) {
            if (r.getGuest() != null && r.getGuest().getGuestId() != null
                    && r.getGuest().getGuestId().equalsIgnoreCase(targetGuestId)) {
                matchCount++;
            }
        }

        VIPAllocationRequest[] result = new VIPAllocationRequest[matchCount];
        int index = 0;
        for (VIPAllocationRequest r : all) {
            if (r.getGuest() != null && r.getGuest().getGuestId() != null
                    && r.getGuest().getGuestId().equalsIgnoreCase(targetGuestId)) {
                result[index++] = r;
            }
        }
        return result;
    }

    // =====================================================================
    // Helpers.
    // =====================================================================

    public Guest findGuestById(String guestId) {
        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }
        return guestList.searchByKey(guestId.trim(), guest -> guest.getGuestId());
    }

    private boolean requestIdExists(String requestId) {
        VIPAllocationRequest existing = requestHistory.searchByKey(
                requestId, request -> request.getRequestId());
        return existing != null;
    }

    private String generateRequestId() {
        String requestId;
        do {
            requestId = String.format("VR%04d", requestCounter);
            requestCounter++;
        } while (requestIdExists(requestId));
        return requestId;
    }

    private boolean confirmationNumberExists(String confirmationNumber) {
        Booking existing = bookingList.searchByKey(
                confirmationNumber, booking -> booking.getConfirmationNumber());
        return existing != null;
    }

    private String generateConfirmationNumber() {
        String confirmationNumber;
        do {
            confirmationNumber = String.format(
                    "%04d%04d",
                    LocalDateTime.now().getYear(),
                    confirmationCounter);
            confirmationCounter++;
        } while (confirmationNumberExists(confirmationNumber));
        return confirmationNumber;
    }
}