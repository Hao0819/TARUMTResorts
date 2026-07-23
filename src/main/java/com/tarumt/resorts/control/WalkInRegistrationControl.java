package com.tarumt.resorts.control;

import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.WalkInRegistration;
import com.tarumt.resorts.entity.MembershipTier;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.dao.GuestDAO;
import com.tarumt.resorts.dao.RoomDAO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

/**
 * WalkInRegistrationControl.java
 * Handles the business logic for the Walk-In Registration module.
 *
 * @author Junhao
 */
public class WalkInRegistrationControl {

    private DoublyLinkedListQueue<WalkInRegistration> registrationQueue;
    private DoublyLinkedListQueue<WalkInRegistration> registrationHistory;
    private DoublyLinkedListQueue<Room> roomList;
    private int confirmationCounter;
    private int registrationCounter;
    private int guestCounter;
    private DoublyLinkedListQueue<Booking> bookingList;
    private ListQueueInterface<Guest> guestList;

    public WalkInRegistrationControl() {
        this(new RoomDAO().getAllRooms(),
                new GuestDAO().getAllGuests(),
                new DoublyLinkedListQueue<>());
    }

    // Constructor used when Main does not provide registration history.
    public WalkInRegistrationControl(
            DoublyLinkedListQueue<Room> sharedRooms,
            DoublyLinkedListQueue<Guest> sharedGuests,
            DoublyLinkedListQueue<Booking> sharedBookings) {
        this(sharedRooms, sharedGuests, sharedBookings, new DoublyLinkedListQueue<>());
    }

    // Constructor used when Main provides hard-coded registration history.
    public WalkInRegistrationControl(
            DoublyLinkedListQueue<Room> sharedRooms,
            DoublyLinkedListQueue<Guest> sharedGuests,
            DoublyLinkedListQueue<Booking> sharedBookings,
            DoublyLinkedListQueue<WalkInRegistration> sharedRegistrationHistory) {

        // use the registration history created by WalkInRegistrationDAO
        registrationHistory = sharedRegistrationHistory;
        registrationQueue = new DoublyLinkedListQueue<>();

        // Copy history references and arrange them by arrival time.
        WalkInRegistration[] chronologicalHistory = getAllRegistrationHistory();

        sortByRegistrationTime(chronologicalHistory);

        // Only WAITING records belong to the active FIFO queue.
        for (int i = 0; i < chronologicalHistory.length; i++) {
            WalkInRegistration registrationRecord = chronologicalHistory[i];

            if (registrationRecord.getStatus()
                    .equalsIgnoreCase("WAITING")) {
                registrationQueue.enqueue(registrationRecord);
            }
        }

        // Keep the same shared Queue references provided by Main
        roomList = sharedRooms;
        guestList = sharedGuests;
        bookingList = sharedBookings;

        registrationCounter = registrationHistory.getNumberOfEntries() + 1;
        // Continue after the existing shared booking records.
        confirmationCounter = bookingList.getNumberOfEntries() + 1;
        guestCounter = guestList.getNumberOfEntries() + 1;
    }

    /*
     * Expected flow
     * Generate WR0021
     * → search history
     * → exists: try WR0022
     * → not exists: use generated ID
     */
    private boolean registrationIdExists(String registrationId) {
        WalkInRegistration existingRegistration =
                // search within the complete registrationHistory
                registrationHistory.searchByKey(
                        registrationId,
                        // every record using registrationId as key
                        registration -> registration.getRegistrationId());
        // Returns true if a record is found, otherwise returns false.
        return existingRegistration != null;
    }

    private String generateRegistrationId() {
        String registrationId;
        do {
            registrationId = String.format("WR%04d", registrationCounter);
            registrationCounter++;
        } while (registrationIdExists(registrationId));
        return registrationId;
    }

    /*
     * Expected flow
     * Generate G021
     * → search all shared Guests
     * → duplicate: try next ID
     * → unique: create Guest
     */
    private boolean guestIdExists(String guestId) {
        // Simply traverse the Queue nodes.
        Guest existingGuest = guestList.searchByKey(
                guestId,
                guest -> guest.getGuestId()); // Specify Guest ID as search key

        return existingGuest != null; // Return true if a matching Guest is found.
    }

    private String generateGuestId() {
        String guestId;
        do {
            guestId = String.format("G%03d", guestCounter);
            guestCounter++;
        } while (guestIdExists(guestId));
        return guestId;
    }

    /*
     * Expected flow
     * Generate candidate ID
     * → search shared booking Queue
     * → duplicate: generate next ID
     * → unique: return ID
     */
    private boolean confirmationNumberExists(
            String confirmationNumber) {
        // use shared ADT search
        Booking existingBooking = bookingList.searchByKey(
                confirmationNumber,
                booking -> booking.getConfirmationNumber());// Specify the search key for each Booking.

        return existingBooking != null;// found same number return true
    }

    private String generateConfirmationNumber() {
        String confirmationNumber;
        do {
            // Four-digit year + four-digit running number = 8 numeric digits.
            confirmationNumber = String.format(
                    "%04d%04d",
                    LocalDateTime.now().getYear(),
                    confirmationCounter);
            confirmationCounter++;
        } while (confirmationNumberExists(confirmationNumber));
        return confirmationNumber;
    }

    public Guest findGuestById(String guestId) {
        // reject invalid Guest ID
        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }
        return guestList.searchByKey(
                guestId.trim(), // Remove spaces before and after the input.
                guest -> guest.getGuestId());
    }

    /**
     * Returns all shared Guest references in their current stored order.
     */
    public Guest[] getAllGuests() {
        int guestCount = guestList.getNumberOfEntries();

        Guest[] guests = new Guest[guestCount];

        Iterator<Guest> iterator = guestList.getIterator();

        int arrayIndex = 0;

        while (iterator.hasNext()) {
            guests[arrayIndex] = iterator.next();
            arrayIndex++;
        }

        return guests;
    }

    // Search the shared Guest Queue using a normalized contact number.
    /*
     * Input 0123456789
     * → normalize
     * → traverse shared Guest Queue once
     * → DAO has 012-3456789
     * → existing Guest found
     */
    public Guest findGuestByContact(String contactNumber) {
        String normalizedContact = normalizeContact(contactNumber);// Remove spaces and -

        if (normalizedContact == null
                || normalizedContact.isEmpty()) {
            return null;
        }

        return guestList.searchByKey(
                normalizedContact,
                guest -> normalizeContact(guest.getContactNumber()));
    }

    /**
     * Updates the requested room type without changing the guest's FIFO position.
     */
    public boolean updateRequestedRoomType(String guestId, String newRoomType) {
        if (guestId == null || newRoomType == null) {
            return false;
        }

        String normalizedRoomType;

        // Convert the input into the standard room-type spelling.
        if (newRoomType.equalsIgnoreCase("Standard")) {
            normalizedRoomType = "Standard";
        } else if (newRoomType.equalsIgnoreCase("Deluxe")) {
            normalizedRoomType = "Deluxe";
        } else if (newRoomType.equalsIgnoreCase("Suite")) {
            normalizedRoomType = "Suite";
        } else {
            return false;
        }

        WalkInRegistration registration = searchByGuestId(guestId.trim());

        // Only an active WaITING registration may be updated
        if (registration == null || !"WAITING".equalsIgnoreCase(registration.getStatus())) {
            return false;
        }

        registration.setRequestedRoomType(normalizedRoomType);
        return true;

    }

    /**
     * Cancels one waiting registration while preserving the FIFO order
     * of all remaining registrations.
     */
    public boolean cancelWaitingRegistration(String guestId) {
        if (guestId == null || guestId.trim().isEmpty()) {
            return false;
        }

        String targetGuestId = guestId.trim();
        int queueSize = registrationQueue.getNumberOfEntries();
        boolean isCancelled = false;

        // Check every original queue entry once.
        for (int i = 0; i < queueSize; i++) {
            WalkInRegistration registration = registrationQueue.dequeue();

            if (!isCancelled && registration.getGuest().getGuestId().equalsIgnoreCase(targetGuestId)) {

                // Remove from active queue but retain it in history
                registration.setStatus("CANCELLED");
                isCancelled = true;
            } else {
                // Put non-target registrations back in their original FIFO order
                registrationQueue.enqueue(registration);
            }
        }
        return isCancelled;
    }

    private Guest findOrCreateGuest(
            String name,
            String contactNumber,
            String email) {

        // Reuse the existing Guest when the contact number is registered.
        Guest existingGuest = findGuestByContact(contactNumber);

        if (existingGuest != null) {
            return existingGuest;
        }

        // No matching contact was found, so create a new Guest.
        String newGuestId = generateGuestId();

        Guest newGuest = new Guest(
                newGuestId,
                name,
                normalizeContact(contactNumber),
                email,
                MembershipTier.NONE);

        guestList.enqueue(newGuest);
        return newGuest;
    }

    private String normalizeContact(String contact) {
        if (contact == null) {
            return null;
        }
        return contact.trim().replaceAll("[\\s-]", "");
    }

    public boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public boolean isValidContact(String contact) {
        String normalizedContact = normalizeContact(contact);
        return normalizedContact != null && normalizedContact.matches("^01[0-9]{8,9}$");
    }

    public boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String trimmedEmail = email.trim();
        return trimmedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public String parseRoomTypeCode(String code) {
        if (code == null) {
            return null;
        }
        switch (code.trim().toUpperCase()) {
            case "D":
                return "Deluxe";
            case "S":
                return "Standard";
            case "SU":
                return "Suite";
            default:
                return null;
        }
    }

    public Guest registerGuest(
            String name, String contactNumber, String email,
            String registrationTime, String requestedRoomType) {
        /*
         * Find/Create Guest
         * → Search active queue once
         * → Found WAITING record: reject
         * → Not found: create registration
         */
        Guest guest = findOrCreateGuest(name, contactNumber, email);
        // avoid O(n^2) indexede traversal, decrease duplicate code
        // Prevent the same guest from having two active waiting registrations.
        WalkInRegistration existingRegistration = searchByGuestId(guest.getGuestId());// using adt O(n) node traversal

        if (existingRegistration != null
                && "WAITING".equalsIgnoreCase(
                        existingRegistration.getStatus())) {
            return null;
        }
        String registrationId = generateRegistrationId();
        WalkInRegistration registration = new WalkInRegistration(
                registrationId, guest, registrationTime, requestedRoomType);
        registrationQueue.enqueue(registration);
        registrationHistory.enqueue(registration);
        return guest;
    }

    /**
     * A room is ready to be allocated to a walk-in guest only if it is
     * marked available AND Housekeeping has not logged it as currently
     * DIRTY, CLEANING, or INSPECTED. A room that has never been logged
     * by Housekeeping defaults to "UNKNOWN" and is treated as ready,
     * since no cleaning cycle has ever been started for it.
     */
    private boolean isReadyForAllocation(Room room) {
        String cleaningStatus = room.getCleaningStatus();
        return cleaningStatus == null
                || cleaningStatus.equalsIgnoreCase("READY")
                || cleaningStatus.equalsIgnoreCase("UNKNOWN");
    }
    /*
     * peek front guest
     * → iterate rooms
     * → matching room found or null
     * → no room: front guest remains
     */

    public Booking processNextGuest() {
        WalkInRegistration nextGuest = registrationQueue.peek();
        if (nextGuest == null) {
            return null;
        }

        Room assignedRoom = null;

        // roomIterator only traversal Room nodes
        Iterator<Room> roomIterator = roomList.getIterator();

        while (roomIterator.hasNext()) {
            Room candidateRoom = roomIterator.next();

            boolean roomTypeMatches = candidateRoom.getRoomType().equalsIgnoreCase(
                    nextGuest.getRequestedRoomType());

            if (roomTypeMatches
                    && candidateRoom.isAvailable()
                    && isReadyForAllocation(candidateRoom)) {

                assignedRoom = candidateRoom;
                break;
            }
        }

        if (assignedRoom == null) {
            return null;
        }

        String confirmationNumber = generateConfirmationNumber();

        // Record when the booking is created; actual check-in is handled by Front-Desk.
        String bookingCreatedTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        Booking booking = new Booking(
                confirmationNumber,
                nextGuest.getGuest(),
                assignedRoom,
                bookingCreatedTime,
                null);

        boolean bookingSaved = bookingList.enqueue(booking);
        if (!bookingSaved) {
            return null;
        }

        assignedRoom.setAvailable(false);
        nextGuest.setStatus("ASSIGNED");
        registrationQueue.dequeue();
        return booking;
    }

    /*
     * Guest ID input
     * → Queue.searchByKey()
     * → traverse linked nodes once
     * → return matching registration
     */
    public WalkInRegistration searchByGuestId(String guestId) {
        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }

        // Search linked nodes directly using each registration's Guest ID.
        return registrationQueue.searchByKey(
                guestId.trim(),
                registration -> registration.getGuest().getGuestId());
    }

    /**
     * Returns every historical registration belonging to one Guest ID.
     * Results retain their original chronological order.
     */
    public WalkInRegistration[] searchRegistrationHistoryByGuestId(
            String guestId) {

        if (guestId == null || guestId.trim().isEmpty()) {
            return new WalkInRegistration[0];
        }

        String targetGuestId = guestId.trim();
        int matchingRecordCount = 0;

        // First traversal: count matching records.
        Iterator<WalkInRegistration> countIterator = registrationHistory.getIterator();

        while (countIterator.hasNext()) {
            WalkInRegistration registration = countIterator.next();

            if (registration.getGuest().getGuestId()
                    .equalsIgnoreCase(targetGuestId)) {
                matchingRecordCount++;
            }
        }

        WalkInRegistration[] matchingRegistrations = new WalkInRegistration[matchingRecordCount];

        // Second traversal: store matching record references.
        Iterator<WalkInRegistration> storeIterator = registrationHistory.getIterator();

        int arrayIndex = 0;

        while (storeIterator.hasNext()) {
            WalkInRegistration registration = storeIterator.next();

            if (registration.getGuest().getGuestId()
                    .equalsIgnoreCase(targetGuestId)) {

                matchingRegistrations[arrayIndex] = registration;
                arrayIndex++;
            }
        }

        return matchingRegistrations;
    }

    /*
     * Queue: A → B → C
     * Iterator reads: A, B, C
     * Array: [A, B, C]
     */
    public WalkInRegistration[] getAllWaitingRegistrations() {
        // queueSize determine the length of array
        int queueSize = registrationQueue.getNumberOfEntries();
        WalkInRegistration[] registrations = new WalkInRegistration[queueSize];

        // getIterator() traversal start from Queue front
        Iterator<WalkInRegistration> iterator = registrationQueue.getIterator();

        int arrayIndex = 0;

        // hasNext() Check if there are still registrations.
        while (iterator.hasNext()) {
            // next() get the current entry then move to next dode
            // arrayIndex determine position of the entry in array
            registrations[arrayIndex] = iterator.next();
            arrayIndex++;
        }

        return registrations;
    }

    public WalkInRegistration[] filterRegistrationHistory(String roomTypeFilter, String statusFilter) {
        WalkInRegistration[] history = getAllRegistrationHistory();
        int matchCount = 0;

        // First pass : count records matching both criteria
        for (int i = 0; i < history.length; i++) {
            WalkInRegistration registration = history[i];

            boolean roomMatches = roomTypeFilter.equalsIgnoreCase("ALL")
                    || registration.getRequestedRoomType().equalsIgnoreCase(roomTypeFilter);

            boolean statusMatches = statusFilter.equalsIgnoreCase("ALL")
                    || registration.getStatus().equalsIgnoreCase(statusFilter);

            if (roomMatches && statusMatches) {
                matchCount++;
            }
        }
        // A fixed-size array requires the matching count in advance
        WalkInRegistration[] filtered = new WalkInRegistration[matchCount];

        int filteredIndex = 0;

        // Second passL store references to matching registration
        for (int i = 0; i < history.length; i++) {
            WalkInRegistration registration = history[i];

            boolean roomMatches = roomTypeFilter.equalsIgnoreCase("ALL")
                    || registration.getRequestedRoomType().equalsIgnoreCase(roomTypeFilter);

            boolean statusMatches = statusFilter.equalsIgnoreCase("ALL")
                    || registration.getStatus().equalsIgnoreCase(statusFilter);

            if (roomMatches && statusMatches) {
                filtered[filteredIndex] = registration;
                filteredIndex++;
            }
        }
        return filtered;

    }

    public WalkInRegistration[] sortByRegistrationTime(WalkInRegistration[] registrations) {

        // Start at index 1 because index is already a sorted section
        for (int i = 1; i < registrations.length; i++) {
            WalkInRegistration key = registrations[i];
            int j = i - 1;

            // Shift later registrations one position to the right
            while (j >= 0 && registrations[j].getRegistrationTime().compareTo(key.getRegistrationTime()) > 0) {
                registrations[j + 1] = registrations[j];
                j--;
            }
            // Insert the key into its correct position
            registrations[j + 1] = key;
        }
        return registrations;
    }

    /*
     * History Queue
     * → iterator traverses once
     * → array keeps chronological queue order
     * → filter and sort report
     */
    public WalkInRegistration[] getAllRegistrationHistory() {
        // historySize include WAITING, ASSIGNED, CANCELLED
        int historySize = registrationHistory.getNumberOfEntries();

        WalkInRegistration[] registrations = new WalkInRegistration[historySize];

        Iterator<WalkInRegistration> iterator = registrationHistory.getIterator();

        // Fill the array in sequence.
        int arrayIndex = 0;

        while (iterator.hasNext()) {
            registrations[arrayIndex] = iterator.next();
            arrayIndex++;
        }

        return registrations;
    }

    /*
     * Waiting Queue:
     * Standard → Deluxe → Standard
     * 
     * countWaitingByRoomType("Standard")
     * → result = 2
     */
    // Count active waiting registrations requesting one room type.
    public int countWaitingByRoomType(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) {
            return 0;
        }

        int demandCount = 0;

        // Direct access to linked nodes
        Iterator<WalkInRegistration> iterator = registrationQueue.getIterator();

        while (iterator.hasNext()) {
            WalkInRegistration registration = iterator.next();

            if (registration.getRequestedRoomType()
                    .equalsIgnoreCase(roomType.trim())) {
                demandCount++;
            }
        }

        return demandCount;
    }

    // count all rooms belonging to one room type , not check availability
    public int countTotalRoomsByType(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) {
            return 0;
        }

        int roomCount = 0;

        // From the first room to the last room
        Iterator<Room> iterator = roomList.getIterator();

        while (iterator.hasNext()) {
            Room room = iterator.next();

            if (room.getRoomType()
                    .equalsIgnoreCase(roomType.trim())) {
                roomCount++;
            }
        }

        return roomCount;
    }

    /*
     * Correct type
     * + available
     * + READY/UNKNOWN
     * = counted as allocatable
     */
    // count currently available rooms belogingd to one room type
    public int countAvailableRoomsByType(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) {
            return 0;
        }

        int availableRoomCount = 0;

        Iterator<Room> iterator = roomList.getIterator();

        while (iterator.hasNext()) {
            Room room = iterator.next();

            boolean roomTypeMatches = room.getRoomType()
                    .equalsIgnoreCase(roomType.trim());

            if (roomTypeMatches
                    && room.isAvailable()
                    && isReadyForAllocation(room)) {
                availableRoomCount++;
            }
        }

        return availableRoomCount;
    }

    // Returns how many guests are currently waiting in the queue.
    public int getWaitingCount() {
        return registrationQueue.getNumberOfEntries();
    }

    public DoublyLinkedListQueue<Booking> getBookingList() {
        return bookingList;
    }
}