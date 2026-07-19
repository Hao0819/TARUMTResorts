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
 * @author junha
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

    // Temporary constructor for running this module independently.
    public WalkInRegistrationControl() {
        this(
                new RoomDAO().getAllRooms(),
                new GuestDAO().getAllGuests(),
                new Queue<>());
    }

    // Constructor used when Main provides shared application data.
    public WalkInRegistrationControl(
            Queue<Room> sharedRooms,
            Queue<Guest> sharedGuests,
            Queue<Booking> sharedBookings) {

        // Walk-In owns its active FIFO queue and registration history.
        registrationQueue = new Queue<>();
        registrationHistory = new Queue<>();

        // Keep the same shared Queue references provided by Main.
        roomList = sharedRooms;
        guestList = sharedGuests;
        bookingList = sharedBookings;

        registrationCounter = 1;
        confirmationCounter = 1;

        // Continue after the hard-coded Guest records.
        guestCounter = guestList.getNumberOfEntries() + 1;
    }

    private boolean registrationIdExists(String registrationId) {
        int total = registrationHistory.getNumberOfEntries();

        // Search the history manually to prevent duplicate registration ID
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
        // Keep generating until an unused registration ID is found.
        do {
            registrationId = String.format("WR%04d", registrationCounter);
            registrationCounter++;
        } while (registrationIdExists(registrationId));
        return registrationId;
    }

    private boolean guestIdExists(String guestId) {
        int total = guestList.getNumberOfEntries();

        // search all guests manually to prevent duplicate guest ID
        for (int i = 0; i < total; i++) {
            Guest existing = guestList.getEntry(i); // get existing guest object reference
            if (existing.getGuestId().equalsIgnoreCase(guestId)) { // compare existing id and candidate id
                return true;
            }
        }

        return false;

    }

    // generate unused guest id
    private String generateGuestId() {
        String guestId;

        // Keep generating until an unused guest ID is found.
        do {
            guestId = String.format("G%03d", guestCounter);
            guestCounter++;
        } while (guestIdExists(guestId));

        return guestId;
    }

    private boolean confirmationNumberExists(String confirmationNumber) {
        int total = bookingList.getNumberOfEntries();

        // Search all bookings manually to prevent duplicate confirmation number
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

        // Keep generate until an unused confirmation number is found
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
        int total = guestList.getNumberOfEntries();// get master guest collection size

        // search the master guest collection manually bu Guest Id
        for (int i = 0; i < total; i++) {
            Guest existing = guestList.getEntry(i); // get existing guest object reference , not build copy

            if (existing.getGuestId().equalsIgnoreCase(normalizedId)) {
                return existing;
            }
        }
        return null;
    }

    // use contact number to search guestList ,
    private Guest findOrCreateGuest(String name, String contactNumber, String email) {
        // Convert the entered contact to a consistent digits-only format.
        String normalizedContact = normalizeContact(contactNumber);
        int totalGuest = guestList.getNumberOfEntries();
        // Search for an existing guest using the normalized contact number.
        for (int i = 0; i < totalGuest; i++) {
            Guest existing = guestList.getEntry(i);

            String existingContact = normalizeContact(existing.getContactNumber());
            if (normalizedContact != null && normalizedContact.equals(existingContact)) {
                // Return the same existing Guest object reference.
                return existing;
            }
        }
        // No matching guest was found, so generate a unique Guest ID.
        String newGuestId = generateGuestId();
        // Store new contact numbers in a consistent digits-only format
        Guest newGuest = new Guest(newGuestId, name, normalizedContact, email, "None");
        guestList.enqueue(newGuest);
        return newGuest;
    }

    private String normalizeContact(String contact) {
        if (contact == null) {
            return null;
        }

        // Remove spaces and hyphens before validation and comparison
        return contact.trim().replaceAll("[\\s-]", "");
    }

    public boolean isValidName(String name) {
        // a guest name must not be null or blank
        return name != null && !name.trim().isEmpty();
    }

    /**
     * validates contact number :digit only, 9 to 11 digit long
     * reject symbols, letters and invalid lengths.
     **/
    public boolean isValidContact(String contact) {
        String normalizedContact = normalizeContact(contact);

        // Malaysian mobile numbers start with 01 and contain 10-11 digits.
        return normalizedContact != null && normalizedContact.matches("^01[0-9]{8,9}$");
    }

    // Validates email addess against a standard email pattern
    public boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String trimmedEmail = email.trim();
        // Check a simple email format: local name, @, domain and suffix
        return trimmedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Converts a short staff-entered room type code into the full room
     * type string used by RoomDAO's sample data ("Standard", "Deluxe",
     * "Suite"). Returns null if the code isn't recognized, so the
     * Boundary layer knows to reject it and ask again.
     */
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

    /*
     * use the guest's guestId and "WAITING" status to search registartionQueue,
     * prevent duplicate active registration
     */
    public Guest registerGuest(
            String name, String contactNumber, String email,
            String registrationTime, String requestedRoomType) {

        Guest guest = findOrCreateGuest(name, contactNumber, email);

        // Search the queue for an existing WAITING registration with the
        // same guestId — same linear-search shape used elsewhere.
        int totalWaiting = registrationQueue.getNumberOfEntries();
        for (int i = 0; i < totalWaiting; i++) {
            WalkInRegistration existing = registrationQueue.getEntry(i);
            if (existing.getGuest().getGuestId().equalsIgnoreCase(guest.getGuestId())
                    && existing.getStatus().equals("WAITING")) {
                return null; // this guest is already in line
            }
        }
        // Generate the ID only after the duplicate check succeeds.
        String registrationId = generateRegistrationId();

        WalkInRegistration registration = new WalkInRegistration(
                registrationId, guest, registrationTime, requestedRoomType);

        // add to active queue for FIFO processing
        registrationQueue.enqueue(registration);
        // Keep the same object reference in history for futuer reports
        registrationHistory.enqueue(registration);
        return guest;
    }

    public Booking processNextGuest() {
        // look at front guest without remove them
        WalkInRegistration nextGuest = registrationQueue.peek();
        if (nextGuest == null) {
            return null; // queue is empty , nobody to process
        }

        // linear search through roomList for a matching, available room
        // find the room is matching the condition
        Room assignedRoom = null;
        int totalRoom = roomList.getNumberOfEntries();
        for (int i = 0; i < totalRoom; i++) {
            Room candidate = roomList.getEntry(i); // doesn't crteate a new Room, copies the memory address into
                                                   // candidate
            if (candidate.isAvailable() && candidate.getRoomType().equalsIgnoreCase(nextGuest.getRequestedRoomType())) {// equalsIgnoreCase
                                                                                                                        // use
                                                                                                                        // to
                                                                                                                        // ignore
                                                                                                                        // upper
                                                                                                                        // or
                                                                                                                        // lower
                                                                                                                        // case
                assignedRoom = candidate; // assigned room number
                break;
            }
        }

        // if no matching room found , guest stays in queue
        // if not match the assignedRoom would still null
        if (assignedRoom == null) {
            return null;
        }

        // Generate a unique confirmation number for the new booking only after a room
        // is found
        String confirmationNumber = generateConfirmationNumber();

        // Create the booking before changing the queue or room state.
        Booking booking = new Booking(confirmationNumber, nextGuest.getGuest(), assignedRoom,
                nextGuest.getRegistrationTime());

        // save the booking before completing the allocation
        boolean bookingSaved = bookingList.enqueue(booking);

        if (!bookingSaved) {
            return null;
        }

        // Update the same Room and WalkInRegistration objects.
        assignedRoom.setAvailable(false);
        nextGuest.setStatus("ASSIGNED");

        // Remove the front registration only after allocation succeeds.
        registrationQueue.dequeue();

        return booking;
    }

    // search the queue for a registration by guestId, using a self-implemented
    // linear search
    // only find active waiting registration
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

    // get every single entry , copies into array
    public WalkInRegistration[] getAllWaitingRegistrations() {
        int total = registrationQueue.getNumberOfEntries();
        WalkInRegistration[] all = new WalkInRegistration[total];
        for (int i = 0; i < total; i++) {
            all[i] = registrationQueue.getEntry(i);// directly assigns by index
        }
        return all;
    }

    public WalkInRegistration[] getAllRegistrationHistory() {
        int total = registrationHistory.getNumberOfEntries();
        WalkInRegistration[] history = new WalkInRegistration[total];
        for (int i = 0; i < total; i++) {
            history[i] = registrationHistory.getEntry(i);
        }
        return history;
    }

    // public WalkInRegistration[] filterRegistrationsHistory(String roomTypeFilter,
    // String statusFilter){
    // WalkInRegistration[] history = getAllRegistrationHistory();

    // return filtered;
    // }

    // Returns how many guests are currently waiting in the queue.
    public int getWaitingCount() {
        return registrationQueue.getNumberOfEntries();
    }

    /**
     * exposes the full list of booking created.
     * Other modules cam use this to display or search all completed bookings
     **/
    public Queue<Booking> getBookingList() {
        return bookingList;
    }

}