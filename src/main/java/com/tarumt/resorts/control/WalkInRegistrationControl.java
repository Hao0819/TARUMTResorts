package com.tarumt.resorts.control;

import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.WalkInRegistration;
import com.tarumt.resorts.adt.Queue;
import com.tarumt.resorts.dao.GuestDAO;
import com.tarumt.resorts.dao.RoomDAO;

/**
 * WalkInRegistrationControl.java
 * Handles the business logic for the Walk-In Registration module.
 *
 * @author Junhao
 */
public class WalkInRegistrationControl {

    private Queue<WalkInRegistration> registrationQueue;
    private Queue<WalkInRegistration> registrationHistory;
    private Queue<Room> roomList;
    private int confirmationCounter;
    private int registrationCounter;
    private int guestCounter;
    private Queue<Booking> bookingList;
    private Queue<Guest> guestList;

    public WalkInRegistrationControl() {
        this(
                new RoomDAO().getAllRooms(),
                new GuestDAO().getAllGuests(),
                new Queue<>());
    }

    // Constructor used when Main does not provide registration history.
    public WalkInRegistrationControl(
            Queue<Room> sharedRooms,
            Queue<Guest> sharedGuests,
            Queue<Booking> sharedBookings) {
        this(sharedRooms, sharedGuests, sharedBookings, new Queue<>());
    }

    // Constructor used when Main provides hard-coded registration history.
    public WalkInRegistrationControl(
            Queue<Room> sharedRooms,
            Queue<Guest> sharedGuests,
            Queue<Booking> sharedBookings,
            Queue<WalkInRegistration> sharedRegistrationHistory) {

        // use the registration history created by WalkInRegistrationDAO
        registrationHistory = sharedRegistrationHistory;
        registrationQueue = new Queue<>();

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
        confirmationCounter = 1;
        guestCounter = guestList.getNumberOfEntries() + 1;
    }

    private boolean registrationIdExists(String registrationId) {
        int total = registrationHistory.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            WalkInRegistration existing = registrationHistory.getEntry(i);
            if (existing.getRegistrationId().equalsIgnoreCase(registrationId)) {
                return true;
            }
        }
        return false;
    }

    private String generateRegistrationId() {
        String registrationId;
        do {
            registrationId = String.format("WR%04d", registrationCounter);
            registrationCounter++;
        } while (registrationIdExists(registrationId));
        return registrationId;
    }

    private boolean guestIdExists(String guestId) {
        int total = guestList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            Guest existing = guestList.getEntry(i);
            if (existing.getGuestId().equalsIgnoreCase(guestId)) {
                return true;
            }
        }
        return false;
    }

    private String generateGuestId() {
        String guestId;
        do {
            guestId = String.format("G%03d", guestCounter);
            guestCounter++;
        } while (guestIdExists(guestId));
        return guestId;
    }

    private boolean confirmationNumberExists(String confirmationNumber) {
        int total = bookingList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            Booking existing = bookingList.getEntry(i);
            if (existing.getConfirmationNumber().equalsIgnoreCase(confirmationNumber)) {
                return true;
            }
        }
        return false;
    }

    private String generateConfirmationNumber() {
        String confirmationNumber;
        do {
            confirmationNumber = String.format("CNF%04d", confirmationCounter);
            confirmationCounter++;
        } while (confirmationNumberExists(confirmationNumber));
        return confirmationNumber;
    }

    public Guest findGuestById(String guestId) {
        if (guestId == null || guestId.trim().isEmpty()) {
            return null;
        }
        String normalizedId = guestId.trim();
        int total = guestList.getNumberOfEntries();
        for (int i = 0; i < total; i++) {
            Guest existing = guestList.getEntry(i);
            if (existing.getGuestId().equalsIgnoreCase(normalizedId)) {
                return existing;
            }
        }
        return null;
    }

    // Search the shared Guest Queue using a normalized contact number.
    public Guest findGuestByContact(String contactNumber) {
        String normalizedContact = normalizeContact(contactNumber);

        if (normalizedContact == null
                || normalizedContact.isEmpty()) {
            return null;
        }

        int totalGuests = guestList.getNumberOfEntries();

        for (int i = 0; i < totalGuests; i++) {
            Guest existingGuest = guestList.getEntry(i);

            String existingContact = normalizeContact(
                    existingGuest.getContactNumber());

            if (normalizedContact.equals(existingContact)) {
                return existingGuest;
            }
        }

        return null;
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
                "None");

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
        Guest guest = findOrCreateGuest(name, contactNumber, email);
        int totalWaiting = registrationQueue.getNumberOfEntries();
        for (int i = 0; i < totalWaiting; i++) {
            WalkInRegistration existing = registrationQueue.getEntry(i);
            if (existing.getGuest().getGuestId().equalsIgnoreCase(guest.getGuestId())
                    && existing.getStatus().equals("WAITING")) {
                return null;
            }
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

    public Booking processNextGuest() {
        WalkInRegistration nextGuest = registrationQueue.peek();
        if (nextGuest == null) {
            return null;
        }

        Room assignedRoom = null;
        int totalRoom = roomList.getNumberOfEntries();
        for (int i = 0; i < totalRoom; i++) {
            Room candidate = roomList.getEntry(i);
            if (candidate.isAvailable()
                    && candidate.getRoomType().equalsIgnoreCase(nextGuest.getRequestedRoomType())
                    && isReadyForAllocation(candidate)) {
                assignedRoom = candidate;
                break;
            }
        }

        if (assignedRoom == null) {
            return null;
        }

        String confirmationNumber = generateConfirmationNumber();
        Booking booking = new Booking(confirmationNumber, nextGuest.getGuest(), assignedRoom,
                nextGuest.getRegistrationTime());

        boolean bookingSaved = bookingList.enqueue(booking);
        if (!bookingSaved) {
            return null;
        }

        assignedRoom.setAvailable(false);
        nextGuest.setStatus("ASSIGNED");
        registrationQueue.dequeue();
        return booking;
    }

    public WalkInRegistration searchByGuestId(String guestId) {
        int totalWaiting = registrationQueue.getNumberOfEntries();
        for (int i = 0; i < totalWaiting; i++) {
            WalkInRegistration reg = registrationQueue.getEntry(i);
            if (reg.getGuest().getGuestId().equalsIgnoreCase(guestId)) {
                return reg;
            }
        }
        return null;
    }

    public WalkInRegistration[] getAllWaitingRegistrations() {
        int total = registrationQueue.getNumberOfEntries();
        WalkInRegistration[] all = new WalkInRegistration[total];
        for (int i = 0; i < total; i++) {
            all[i] = registrationQueue.getEntry(i);
        }
        return all;
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

    public WalkInRegistration[] getAllRegistrationHistory() {
        int total = registrationHistory.getNumberOfEntries();
        WalkInRegistration[] history = new WalkInRegistration[total];
        for (int i = 0; i < total; i++) {
            history[i] = registrationHistory.getEntry(i);
        }
        return history;
    }

    // Count active waiting registrations requesting one room type.
    public int countWaitingByRoomType(String roomType) {
        int demandCount = 0;
        int totalWaiting = registrationQueue.getNumberOfEntries(); // get active waiting queue counter

        // check waiting registrations
        for (int i = 0; i < totalWaiting; i++) {
            // get current registration object reference
            WalkInRegistration registrationRecord = registrationQueue.getEntry(i);

            // compare room type guest request with method parameter
            if (registrationRecord.getRequestedRoomType().equalsIgnoreCase(roomType)) {
                demandCount++; // aech matching record found, counter ++
            }
        }

        return demandCount;
    }

    // count all rooms belonging to one room type
    public int countTotalRoomsByType(String roomType) {
        int totalRoomCount = 0;
        int totalRooms = roomList.getNumberOfEntries();

        // check every room in the shared Room Queue
        for (int i = 0; i < totalRooms; i++) {
            Room room = roomList.getEntry(i);

            // count the room when its type matches the parameter
            if (room.getRoomType().equalsIgnoreCase(roomType)) {
                totalRoomCount++;
            }
        }
        return totalRoomCount;
    }

    // count currently available rooms belogingd to one room type
    public int countAvailableRoomsByType(String roomType) {
        int availableRoomCount = 0;
        // get total number of current shared Room Queue
        int totalRooms = roomList.getNumberOfEntries();

        for (int i = 0; i < totalRooms; i++) {
            Room room = roomList.getEntry(i);

            // Count only rooms that meet the same conditions used during allocation.
            if (room.getRoomType().equalsIgnoreCase(roomType)
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

    public Queue<Booking> getBookingList() {
        return bookingList;
    }
}