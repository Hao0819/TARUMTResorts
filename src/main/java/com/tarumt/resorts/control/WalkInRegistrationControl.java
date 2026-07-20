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

    public WalkInRegistrationControl() {
        this(
            new RoomDAO().getAllRooms(),
            new GuestDAO().getAllGuests(),
            new Queue<>());
    }

    public WalkInRegistrationControl(
            Queue<Room> sharedRooms,
            Queue<Guest> sharedGuests,
            Queue<Booking> sharedBookings) {
        registrationQueue = new Queue<>();
        registrationHistory = new Queue<>();
        roomList = sharedRooms;
        guestList = sharedGuests;
        bookingList = sharedBookings;
        registrationCounter = 1;
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

    private Guest findOrCreateGuest(String name, String contactNumber, String email) {
        String normalizedContact = normalizeContact(contactNumber);
        int totalGuest = guestList.getNumberOfEntries();
        for (int i = 0; i < totalGuest; i++) {
            Guest existing = guestList.getEntry(i);
            String existingContact = normalizeContact(existing.getContactNumber());
            if (normalizedContact != null && normalizedContact.equals(existingContact)) {
                return existing;
            }
        }
        String newGuestId = generateGuestId();
        Guest newGuest = new Guest(newGuestId, name, normalizedContact, email, "None");
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

    public WalkInRegistration[] getAllRegistrationHistory() {
        int total = registrationHistory.getNumberOfEntries();
        WalkInRegistration[] history = new WalkInRegistration[total];
        for (int i = 0; i < total; i++) {
            history[i] = registrationHistory.getEntry(i);
        }
        return history;
    }

    public int getWaitingCount() {
        return registrationQueue.getNumberOfEntries();
    }

    public Queue<Booking> getBookingList() {
        return bookingList;
    }
}