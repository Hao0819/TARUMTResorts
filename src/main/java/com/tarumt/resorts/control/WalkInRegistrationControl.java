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
import java.time.LocalDate;

/**
 * WalkInRegistrationControl.java
 * Handles the business logic for the Walk-In Registration module.
 *
 * @author Junhao
 */
public class WalkInRegistrationControl {

    // Active registrations processed using strict FIFO behaviour.
    private ListQueueInterface<WalkInRegistration> registrationQueue = new DoublyLinkedListQueue<>();// ADT declaration

    // Complete registration records used for searching and reporting.
    private ListQueueInterface<WalkInRegistration> registrationHistory;

    // Shared room collection provided by Main.
    private ListQueueInterface<Room> roomList;
    private int confirmationCounter;
    private int registrationCounter;
    private int guestCounter;
    // Shared bookings created by Standard and VIP allocation modules.
    private ListQueueInterface<Booking> bookingList;
    private ListQueueInterface<Guest> guestList;

    public WalkInRegistrationControl() {
        this(new RoomDAO().getAllRooms(),
                new GuestDAO().getAllGuests(),
                new DoublyLinkedListQueue<>());
    }

    // Constructor used when Main does not provide registration history.
    public WalkInRegistrationControl(
            ListQueueInterface<Room> sharedRooms,
            ListQueueInterface<Guest> sharedGuests,
            ListQueueInterface<Booking> sharedBookings) {

        this(
                sharedRooms,
                sharedGuests,
                sharedBookings,
                new DoublyLinkedListQueue<>());
    }

    // Constructor used when Main provides hard-coded registration history.
    public WalkInRegistrationControl(
            ListQueueInterface<Room> sharedRooms,
            ListQueueInterface<Guest> sharedGuests,
            ListQueueInterface<Booking> sharedBookings,
            ListQueueInterface<WalkInRegistration> sharedRegistrationHistory) {

        // use the registration history created by WalkInRegistrationDAO
        registrationHistory = sharedRegistrationHistory;

        // Copy history references and arrange them by arrival time.
        WalkInRegistration[] chronologicalHistory = getAllRegistrationHistory();

        sortByRegistrationTime(chronologicalHistory);

        // Only WAITING records belong to the active FIFO queue.
        for (int i = 0; i < chronologicalHistory.length; i++) {
            WalkInRegistration registrationRecord = chronologicalHistory[i];

            if (registrationRecord.getStatus()
                    .equalsIgnoreCase("WAITING")) {
                registrationQueue.enqueue(registrationRecord);// ADT method called enque
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

    /**
     * Returns each Guest who has Walk-In registration history.
     * A Guest is included only once even when multiple history records exist.
     */
    public Guest[] getGuestsWithRegistrationHistory() {

        WalkInRegistration[] registrationHistoryRecords = getAllRegistrationHistory();

        Guest[] temporaryGuests = new Guest[registrationHistoryRecords.length];

        int uniqueGuestCount = 0;

        for (int historyIndex = 0; historyIndex < registrationHistoryRecords.length; historyIndex++) {

            Guest currentGuest = registrationHistoryRecords[historyIndex].getGuest();

            boolean guestAlreadyAdded = false;

            for (int guestIndex = 0; guestIndex < uniqueGuestCount; guestIndex++) {

                boolean guestIdMatches = temporaryGuests[guestIndex]
                        .getGuestId()
                        .equalsIgnoreCase(
                                currentGuest.getGuestId());

                if (guestIdMatches) {
                    guestAlreadyAdded = true;
                    break;
                }
            }

            if (!guestAlreadyAdded) {
                temporaryGuests[uniqueGuestCount] = currentGuest;
                uniqueGuestCount++;
            }
        }

        Guest[] guestsWithHistory = new Guest[uniqueGuestCount];

        for (int guestIndex = 0; guestIndex < uniqueGuestCount; guestIndex++) {

            guestsWithHistory[guestIndex] = temporaryGuests[guestIndex];
        }

        return guestsWithHistory;
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
     * Updates one WAITING registration identified by both
     * Registration ID and Guest ID.
     */
    public boolean updateRequestedRoomType(
            String registrationId,
            String guestId,
            String newRoomType) {

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

        WalkInRegistration selectedRegistration = findWaitingRegistration(
                registrationId,
                guestId);

        if (selectedRegistration == null) {
            return false;
        }

        boolean roomAvailable = hasAvailableRoomForSchedule(
                normalizedRoomType,
                selectedRegistration.getRequestedCheckInDate(),
                selectedRegistration.getStayDurationDays());

        if (!roomAvailable) {
            return false;
        }

        String previousRoomType = selectedRegistration.getRequestedRoomType();

        selectedRegistration.setRequestedRoomType(
                normalizedRoomType);

        // Record the successful room-type change in the Entity ADT.
        selectedRegistration.recordChange(
                "Room Type",
                previousRoomType,
                normalizedRoomType);

        return true;
    }

    /**
     * Updates the requested check-in date of one WAITING booking request.
     * The new date must have an available room for the complete stay.
     */
    public boolean updateRequestedCheckInDate(
            String registrationId,
            String guestId,
            LocalDate newCheckInDate) {

        if (newCheckInDate == null
                || newCheckInDate.isBefore(LocalDate.now())) {

            return false;
        }

        WalkInRegistration selectedRegistration = findWaitingRegistration(
                registrationId,
                guestId);

        if (selectedRegistration == null) {
            return false;
        }

        boolean roomAvailable = hasAvailableRoomForSchedule(
                selectedRegistration.getRequestedRoomType(),
                newCheckInDate,
                selectedRegistration.getStayDurationDays());

        if (!roomAvailable) {
            return false;
        }

        LocalDate previousCheckInDate = selectedRegistration.getRequestedCheckInDate();

        selectedRegistration.setRequestedCheckInDate(
                newCheckInDate);

        // Record the successful check-in date change in the Entity ADT.
        selectedRegistration.recordChange(
                "Check-In Date",
                previousCheckInDate.toString(),
                newCheckInDate.toString());

        return true;
    }

    /**
     * Updates the stay duration of one WAITING booking request.
     * The complete updated stay period must have an available room.
     */
    public boolean updateStayDuration(
            String registrationId,
            String guestId,
            int newStayDurationDays) {

        if (newStayDurationDays < 1
                || newStayDurationDays > 30) {

            return false;
        }

        WalkInRegistration selectedRegistration = findWaitingRegistration(
                registrationId,
                guestId);

        if (selectedRegistration == null) {
            return false;
        }

        boolean roomAvailable = hasAvailableRoomForSchedule(
                selectedRegistration.getRequestedRoomType(),
                selectedRegistration.getRequestedCheckInDate(),
                newStayDurationDays);

        if (!roomAvailable) {
            return false;
        }

        int previousStayDurationDays = selectedRegistration.getStayDurationDays();

        selectedRegistration.setStayDurationDays(
                newStayDurationDays);

        // Record the successful stay-duration change in the Entity ADT.
        selectedRegistration.recordChange(
                "Stay Duration (Nights)",
                String.valueOf(previousStayDurationDays),
                String.valueOf(newStayDurationDays));

        return true;
    }

    /**
     * Finds one active WAITING registration using both Registration ID
     * and Guest ID.
     */
    public WalkInRegistration findWaitingRegistration(
            String registrationId,
            String guestId) {

        if (registrationId == null
                || registrationId.trim().isEmpty()
                || guestId == null
                || guestId.trim().isEmpty()) {

            return null;
        }

        String targetRegistrationId = registrationId.trim();

        String targetGuestId = guestId.trim();

        Iterator<WalkInRegistration> registrationIterator = registrationQueue.getIterator();

        while (registrationIterator.hasNext()) {

            WalkInRegistration currentRegistration = registrationIterator.next();

            boolean registrationIdMatches = currentRegistration.getRegistrationId()
                    .equalsIgnoreCase(
                            targetRegistrationId);

            boolean guestIdMatches = currentRegistration.getGuest() != null
                    && currentRegistration.getGuest()
                            .getGuestId()
                            .equalsIgnoreCase(targetGuestId);

            boolean isWaiting = "WAITING".equalsIgnoreCase(
                    currentRegistration.getStatus());

            if (registrationIdMatches
                    && guestIdMatches
                    && isWaiting) {

                return currentRegistration;
            }
        }

        return null;
    }

    /**
     * Cancels one WAITING registration only when both the registration ID
     * and guest ID match. All other registrations retain their FIFO order.
     */
    public boolean cancelWaitingRegistration(
            String registrationId,
            String guestId) {

        if (registrationId == null
                || registrationId.trim().isEmpty()
                || guestId == null
                || guestId.trim().isEmpty()) {

            return false;
        }

        String targetRegistrationId = registrationId.trim();
        String targetGuestId = guestId.trim();

        int originalQueueSize = registrationQueue.getNumberOfEntries();

        boolean registrationCancelled = false;

        // Examine every original queue entry exactly once.
        for (int queuePosition = 0; queuePosition < originalQueueSize; queuePosition++) {

            WalkInRegistration currentRegistration = registrationQueue.dequeue();

            boolean registrationIdMatches = currentRegistration.getRegistrationId()
                    .equalsIgnoreCase(targetRegistrationId);

            boolean guestIdMatches = currentRegistration.getGuest() != null
                    && currentRegistration.getGuest().getGuestId() != null
                    && currentRegistration.getGuest()
                            .getGuestId()
                            .equalsIgnoreCase(targetGuestId);

            boolean isWaiting = "WAITING".equalsIgnoreCase(
                    currentRegistration.getStatus());

            if (!registrationCancelled
                    && registrationIdMatches
                    && guestIdMatches
                    && isWaiting) {

                // Keep the record in history but remove it from the active queue.
                String previousStatus = currentRegistration.getStatus();

                currentRegistration.setStatus("CANCELLED");

                // Record the successful status change in the Entity ADT.
                currentRegistration.recordChange(
                        "Status",
                        previousStatus,
                        "CANCELLED");

                registrationCancelled = true;
            } else {
                // Restore non-target registrations in their original FIFO order.
                registrationQueue.enqueue(currentRegistration);
            }
        }

        return registrationCancelled;
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

    /**
     * Compatibility method for existing code that does not provide a schedule.
     */
    public Guest registerGuest(
            String name,
            String contactNumber,
            String email,
            String registrationTime,
            String requestedRoomType) {

        return registerGuest(
                name,
                contactNumber,
                email,
                registrationTime,
                requestedRoomType,
                LocalDate.now(),
                1);
    }

    /**
     * Creates a walk-in registration with the requested stay schedule.
     */
    public Guest registerGuest(
            String name,
            String contactNumber,
            String email,
            String registrationTime,
            String requestedRoomType,
            LocalDate requestedCheckInDate,
            int stayDurationDays) {

        // Every registration record must have a creation timestamp.
        if (registrationTime == null
                || registrationTime.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Registration time cannot be blank.");
        }

        // Protect registration data even when this method is called without the UI.
        if (!isValidName(name)) {
            throw new IllegalArgumentException(
                    "Guest name cannot be blank.");
        }

        if (!isValidContact(contactNumber)) {
            throw new IllegalArgumentException(
                    "Invalid Malaysian mobile number.");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(
                    "Invalid email address.");
        }

        boolean validRoomType = requestedRoomType != null
                && (requestedRoomType.equalsIgnoreCase("Standard")
                        || requestedRoomType.equalsIgnoreCase("Deluxe")
                        || requestedRoomType.equalsIgnoreCase("Suite"));

        if (!validRoomType) {
            throw new IllegalArgumentException(
                    "Invalid requested room type.");
        }

        // A booking request cannot use a past or missing check-in date.
        if (requestedCheckInDate == null
                || requestedCheckInDate.isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Requested check-in date cannot be in the past.");
        }

        // Keep the accepted stay duration consistent with the UI.
        if (stayDurationDays < 1 || stayDurationDays > 30) {
            throw new IllegalArgumentException(
                    "Stay duration must be between 1 and 30 nights.");
        }

        Guest guest = findOrCreateGuest(
                name,
                contactNumber,
                email);

        // Priority-tier guests must use the VIP allocation queue.
        if (guest.getMembershipTier().isPriorityTier()) {
            throw new IllegalArgumentException(
                    "Priority-tier guests must use VIP Priority Allocation.");
        }

        // Every room request is stored as a separate FIFO registration.
        // The same Guest may submit multiple room requests.
        String registrationId = generateRegistrationId();

        WalkInRegistration registration = new WalkInRegistration(
                registrationId,
                guest,
                registrationTime,
                requestedRoomType,
                requestedCheckInDate,
                stayDurationDays);

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

    /**
     * Checks whether a room has no CONFIRMED or ACTIVE booking that
     * overlaps the requested stay period.
     */
    private boolean isRoomAvailableForSchedule(
            Room room,
            LocalDate requestedCheckInDate,
            LocalDate requestedCheckOutDate) {

        if (room == null
                || requestedCheckInDate == null
                || requestedCheckOutDate == null
                || !requestedCheckInDate.isBefore(requestedCheckOutDate)) {

            return false;
        }

        Iterator<Booking> bookingIterator = bookingList.getIterator();

        while (bookingIterator.hasNext()) {
            Booking existingBooking = bookingIterator.next();

            boolean sameRoom = existingBooking.getRoom() != null
                    && existingBooking.getRoom()
                            .getRoomNumber()
                            .equalsIgnoreCase(
                                    room.getRoomNumber());

            boolean blocksSchedule = "CONFIRMED".equalsIgnoreCase(
                    existingBooking.getStatus())
                    || "ACTIVE".equalsIgnoreCase(
                            existingBooking.getStatus());

            LocalDate existingCheckInDate = existingBooking.getScheduledCheckInDate();

            LocalDate existingCheckOutDate = existingBooking.getScheduledCheckOutDate();

            if (sameRoom
                    && blocksSchedule
                    && existingCheckInDate != null
                    && existingCheckOutDate != null) {

                boolean datesOverlap = requestedCheckInDate.isBefore(
                        existingCheckOutDate)
                        && requestedCheckOutDate.isAfter(
                                existingCheckInDate);

                if (datesOverlap) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks whether at least one room of the requested type is available
     * for the guest's complete stay period.
     */
    public boolean hasAvailableRoomForSchedule(
            String roomType,
            LocalDate requestedCheckInDate,
            int stayDurationDays) {

        if (roomType == null
                || roomType.trim().isEmpty()
                || requestedCheckInDate == null
                || requestedCheckInDate.isBefore(LocalDate.now())
                || stayDurationDays <= 0) {

            return false;
        }

        LocalDate requestedCheckOutDate = requestedCheckInDate.plusDays(
                stayDurationDays);

        boolean immediateCheckIn = requestedCheckInDate.equals(
                LocalDate.now());

        Iterator<Room> roomIterator = roomList.getIterator();

        while (roomIterator.hasNext()) {
            Room candidateRoom = roomIterator.next();

            boolean roomTypeMatches = candidateRoom.getRoomType()
                    .equalsIgnoreCase(
                            roomType.trim());

            boolean operationallyReady = !immediateCheckIn
                    || (candidateRoom.isAvailable()
                            && isReadyForAllocation(
                                    candidateRoom));

            boolean scheduleAvailable = isRoomAvailableForSchedule(
                    candidateRoom,
                    requestedCheckInDate,
                    requestedCheckOutDate);

            if (roomTypeMatches
                    && operationallyReady
                    && scheduleAvailable) {

                return true;
            }
        }

        return false;
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
        LocalDate requestedCheckInDate = nextGuest.getRequestedCheckInDate();

        LocalDate requestedCheckOutDate = nextGuest.getRequestedCheckOutDate();

        boolean immediateCheckIn = requestedCheckInDate.equals(LocalDate.now());

        Room assignedRoom = null;

        // roomIterator only traversal Room nodes
        Iterator<Room> roomIterator = roomList.getIterator();

        while (roomIterator.hasNext()) {
            Room candidateRoom = roomIterator.next();

            boolean roomTypeMatches = candidateRoom.getRoomType().equalsIgnoreCase(
                    nextGuest.getRequestedRoomType());

            boolean operationallyReady = !immediateCheckIn
                    || (candidateRoom.isAvailable()
                            && isReadyForAllocation(candidateRoom));

            boolean scheduleAvailable = isRoomAvailableForSchedule(
                    candidateRoom,
                    requestedCheckInDate,
                    requestedCheckOutDate);

            if (roomTypeMatches
                    && operationallyReady
                    && scheduleAvailable) {

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
                null,
                nextGuest.getRequestedCheckInDate(),
                nextGuest.getStayDurationDays());

        boolean bookingSaved = bookingList.enqueue(booking);

        if (!bookingSaved) {
            return null;
        }

        // Link the completed registration to its created Booking.
        nextGuest.setBookingConfirmationNumber(
                booking.getConfirmationNumber());

        String previousStatus = nextGuest.getStatus();

        nextGuest.setStatus("ASSIGNED");

        // Record the successful status change in the Entity ADT.
        nextGuest.recordChange(
                "Status",
                previousStatus,
                "ASSIGNED");

        // Remove the front request only after the Booking is saved successfully.
        registrationQueue.dequeue();

        return booking;
    }

    /**
     * Returns the most recently added WAITING registration
     * belonging to one Guest.
     */
    public WalkInRegistration findLatestWaitingRegistrationByGuestId(
            String guestId) {

        if (guestId == null
                || guestId.trim().isEmpty()) {

            return null;
        }

        String targetGuestId = guestId.trim();

        WalkInRegistration latestRegistration = null;

        Iterator<WalkInRegistration> registrationIterator = registrationQueue.getIterator();

        while (registrationIterator.hasNext()) {

            WalkInRegistration currentRegistration = registrationIterator.next();

            boolean guestIdMatches = currentRegistration.getGuest() != null
                    && currentRegistration.getGuest()
                            .getGuestId()
                            .equalsIgnoreCase(targetGuestId);

            boolean isWaiting = "WAITING".equalsIgnoreCase(
                    currentRegistration.getStatus());

            if (guestIdMatches && isWaiting) {
                // Continue traversing so the last matching entry is returned.
                latestRegistration = currentRegistration;
            }
        }

        return latestRegistration;
    }

    /**
     * Finds one registration-history record using its Registration ID.
     *
     * The complete history is searched so WAITING, ASSIGNED and
     * CANCELLED records can all be found.
     *
     * @param registrationId Registration ID entered by staff
     * @return matching registration, or null when no record exists
     */
    public WalkInRegistration findRegistrationById(
            String registrationId) {

        if (registrationId == null
                || registrationId.trim().isEmpty()) {

            return null;
        }

        return registrationHistory.searchByKey(
                registrationId.trim(),
                registration -> registration.getRegistrationId());
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

    /**
     * Counts rooms of one type that can be allocated for tonight.
     *
     * A room must:
     * 1. Match the requested room type.
     * 2. Be operationally available.
     * 3. Be ready according to Housekeeping.
     * 4. Have no CONFIRMED or ACTIVE booking overlapping tonight.
     */
    public int countAvailableRoomsByType(String roomType) {

        if (roomType == null
                || roomType.trim().isEmpty()) {

            return 0;
        }

        int availableRoomCount = 0;

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        Iterator<Room> roomIterator = roomList.getIterator();

        while (roomIterator.hasNext()) {

            Room room = roomIterator.next();

            boolean roomTypeMatches = room.getRoomType().equalsIgnoreCase(
                    roomType.trim());

            boolean operationallyAvailable = room.isAvailable()
                    && isReadyForAllocation(room);

            boolean scheduleAvailableTonight = isRoomAvailableForSchedule(
                    room,
                    today,
                    tomorrow);

            if (roomTypeMatches
                    && operationallyAvailable
                    && scheduleAvailableTonight) {

                availableRoomCount++;
            }
        }

        return availableRoomCount;
    }

    /**
     * Checks whether the request at the front of the FIFO queue
     * has a requested check-in date earlier than today.
     */
    public boolean isFrontWaitingRequestExpired() {

        WalkInRegistration frontRegistration = registrationQueue.peek();

        if (frontRegistration == null
                || frontRegistration.getRequestedCheckInDate() == null) {

            return false;
        }

        return frontRegistration
                .getRequestedCheckInDate()
                .isBefore(LocalDate.now());
    }

    // Returns how many guests are currently waiting in the queue.
    public int getWaitingCount() {
        return registrationQueue.getNumberOfEntries();
    }

    public ListQueueInterface<Booking> getBookingList() {
        return bookingList;
    }
}