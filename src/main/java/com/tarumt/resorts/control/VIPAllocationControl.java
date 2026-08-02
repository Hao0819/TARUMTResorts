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
     * references used by the rest of the system, so VIP Allocation
     * shares Rooms/Guests/Bookings with every other module.
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
     * priority level comes first. When two requests belong to the same
     * tier, this comparator returns 0, which makes priorityEnqueue()
     * skip past all existing same-tier entries and insert the new one
     * AFTER them - preserving first-come-first-served order within a
     * tier.
     */
    private Comparator<VIPAllocationRequest> tierPriorityComparator() {
        return (newRequest, existingRequest) ->
                existingRequest.getGuest().getMembershipTier().getPriorityLevel()
                - newRequest.getGuest().getMembershipTier().getPriorityLevel();
    }

    /**
     * Registers a new VIP allocation request. The guest must already
     * exist and hold a priority membership tier. The request is
     * inserted into the priority queue: higher tier guests are placed
     * ahead of lower tier guests, and guests of the same tier keep
     * their original registration order.
     *
     * @return the created request, or null if the guest is invalid,
     *         not a priority-tier member, or already has a WAITING
     *         request
     */
    public VIPAllocationRequest registerVIPRequest(
            String guestId,
            String requestedRoomType) {

        Guest guest = findGuestById(guestId);

        if (guest == null || !guest.getMembershipTier().isPriorityTier()) {
            return null;
        }

        VIPAllocationRequest existing = searchByGuestId(guestId);
        if (existing != null && "WAITING".equalsIgnoreCase(existing.getStatus())) {
            return null;
        }

        String requestId = generateRequestId();
        String requestTime = LocalDateTime.now().format(TIME_FORMAT);

        VIPAllocationRequest request = new VIPAllocationRequest(
                requestId, guest, requestTime, requestedRoomType);

        boolean insertedIntoQueue =
                priorityQueue.priorityEnqueue(request, tierPriorityComparator());

        if (!insertedIntoQueue) {
            return null;
        }

        requestHistory.enqueue(request);
        return request;
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
     * Allocates a room to the guest currently at the front of the
     * priority queue (highest tier, earliest among ties). If no
     * matching room is available, the guest remains at the front and
     * null is returned.
     */
    public Booking allocateNextVIPGuest() {
        VIPAllocationRequest frontRequest = priorityQueue.peek();
        if (frontRequest == null) {
            return null;
        }

        Room assignedRoom = null;
        Iterator<Room> roomIterator = roomList.getIterator();

        while (roomIterator.hasNext()) {
            Room candidate = roomIterator.next();
            boolean typeMatches = candidate.getRoomType()
                    .equalsIgnoreCase(frontRequest.getRequestedRoomType());

            if (typeMatches
                    && candidate.isAvailable()
                    && isReadyForAllocation(candidate)) {
                assignedRoom = candidate;
                break;
            }
        }

        if (assignedRoom == null) {
            return null;
        }

        String confirmationNumber = generateConfirmationNumber();
        String bookingTime = LocalDateTime.now().format(TIME_FORMAT);

        Booking booking = new Booking(
                confirmationNumber,
                frontRequest.getGuest(),
                assignedRoom,
                bookingTime,
                null);

        boolean bookingSaved = bookingList.enqueue(booking);
        if (!bookingSaved) {
            return null;
        }

        assignedRoom.setAvailable(false);
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
