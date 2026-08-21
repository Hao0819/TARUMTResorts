package com.tarumt.resorts.control;

import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.adt.ListQueueInterface;
import java.time.LocalDate;
import com.tarumt.resorts.dao.BookingDAO;
import com.tarumt.resorts.dao.GuestDAO;
import com.tarumt.resorts.dao.RoomDAO;

/**
 * FrontDeskControl.java
 * Business logic for the Front-Desk Service module: look up a booking by its
 * 8-digit confirmation number, check room availability, and update the
 * statuses Front-Desk owns (check-in, check-out, cancel). Front-Desk does not
 * create bookings (Walk-In / VIP do); it manages the shared Booking records.
 *
 * @author Tan Keng Ting
 */
public class FrontDeskControl {

    private ListQueueInterface<Booking> bookingList; // ADT collection declaration
    private ListQueueInterface<Guest> guestList; // ADT collection declaration
    private ListQueueInterface<Room> roomList; // ADT collection declaration

    // Optional Housekeeping collaborator - lets a check-out also log DIRTY.
    private HousekeepingControl housekeeping;

    // Optional shared Loyalty accounts - lets Front-Desk show/search loyalty.
    private ListQueueInterface<LoyaltyAccount> loyaltyAccounts; // ADT collection declaration

    // Standalone constructor - loads hard-coded sample data for solo demo.
    public FrontDeskControl() {
        this.guestList = new GuestDAO().getAllGuests();
        this.roomList = new RoomDAO().getAllRooms();
        this.bookingList = new BookingDAO().getAllBookings(guestList, roomList);
    }

    // Integrated constructor - receives the shared collections from Main.
    public FrontDeskControl(
            ListQueueInterface<Booking> sharedBookings, // ADT collection declaration
            ListQueueInterface<Guest> sharedGuests, // ADT collection declaration
            ListQueueInterface<Room> sharedRooms) { // ADT collection declaration
        bookingList = sharedBookings;
        guestList = sharedGuests;
        roomList = sharedRooms;
    }

    // Wire in the shared Housekeeping control (optional).
    public void setHousekeepingControl(HousekeepingControl housekeeping) {
        this.housekeeping = housekeeping;
    }

    // Wire in the shared Loyalty accounts (optional).
    public void setLoyaltyAccounts(ListQueueInterface<LoyaltyAccount> loyaltyAccounts) { // ADT collection declaration
        this.loyaltyAccounts = loyaltyAccounts;
    }

    // Find the loyalty account for a booking's guest (linear scan by guest ID).
    public LoyaltyAccount getLoyaltyAccountFor(Booking booking) {
        if (loyaltyAccounts == null || booking == null || booking.getGuest() == null) {
            return null;
        }
        String guestId = booking.getGuest().getGuestId();
        if (guestId == null) {
            return null;
        }
        for (int i = 0; i < loyaltyAccounts.getNumberOfEntries(); i++) { // ADT method call: getNumberOfEntries()
            LoyaltyAccount account = loyaltyAccounts.getEntry(i); // ADT method call: getEntry()
            if (guestId.equalsIgnoreCase(account.getGuestId())) {
                return account;
            }
        }
        return null;
    }

    // The booking guest's loyalty ID, or "-" when there is no linked account.
    public String getLoyaltyIdFor(Booking booking) {
        LoyaltyAccount account = getLoyaltyAccountFor(booking);
        return (account == null || account.getLoyaltyId() == null) ? "-" : account.getLoyaltyId();
    }

    // The booking guest's current points, or "-" when there is no account.
    public String getLoyaltyPointsFor(Booking booking) {
        LoyaltyAccount account = getLoyaltyAccountFor(booking);
        return account == null ? "-" : String.valueOf(account.getPointsBalance());
    }

    // ================= Core: look up a booking by confirmation number =================

    // Retrieve a booking by confirmation number via the ADT key search - O(n).
    public Booking findByConfirmationNumber(String confirmationNumber) {
        if (confirmationNumber == null || confirmationNumber.trim().isEmpty()) {
            return null;
        }
        String key = confirmationNumber.trim();
        return bookingList.searchByKey(key, b -> b.getConfirmationNumber()); // ADT method call: searchByKey()
    }

    // A confirmation number must be exactly 8 digits.
    public boolean isValidConfirmationNumber(String confirmationNumber) {
        return confirmationNumber != null
                && confirmationNumber.trim().matches("^[0-9]{8}$");
    }

    // True if a booking already uses this confirmation number.
    public boolean confirmationNumberExists(String confirmationNumber) {
        return findByConfirmationNumber(confirmationNumber) != null;
    }

    // ================= Front-Desk write operation: check-out =================

    // Check out an ACTIVE booking: CHECKED_OUT, room vacated and marked DIRTY.
    public boolean checkOutBooking(String confirmationNumber, String checkOutTime) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"ACTIVE".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        booking.setStatus("CHECKED_OUT");
        booking.setCheckOutTime(checkOutTime);
        Room room = booking.getRoom();
        if (room != null) {
            room.setAvailable(true);            // no longer occupied
            room.setCleaningStatus("DIRTY");    // hand over to Housekeeping
            if (housekeeping != null) {
                housekeeping.logStatusChange(room.getRoomNumber(), "DIRTY", checkOutTime);
            }
        }
        return true;
    }

    // ================= Availability query over the room collection =================

    // Linear lookup of a room by room number.
    public Room findRoomByNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return null;
        }
        String number = roomNumber.trim();
        for (int i = 0; i < roomList.getNumberOfEntries(); i++) { // ADT method call: getNumberOfEntries()
            Room r = roomList.getEntry(i); // ADT method call: getEntry()
            if (r.getRoomNumber().equalsIgnoreCase(number)) {
                return r;
            }
        }
        return null;
    }

    // Rooms available now: vacant AND not reserved for today (linear scan).
    public Room[] getAvailableRooms(String roomTypeFilter) {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        String type = (roomTypeFilter == null) ? "ALL" : roomTypeFilter.trim();
        int total = roomList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()

        Room[] temp = new Room[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Room r = roomList.getEntry(i); // ADT method call: getEntry()
            boolean typeOk = type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(r.getRoomType());
            if (typeOk && r.isAvailable() && !isRoomTakenInRange(r, today, tomorrow)) {
                temp[count++] = r;
            }
        }
        return trim(temp, count);
    }

    // ================= Date-range availability (schedule overlap) =================

    // Rooms of a type with no CONFIRMED/ACTIVE booking overlapping [checkIn, checkOut).
    public Room[] getAvailableRoomsForRange(LocalDate checkIn, LocalDate checkOut, String roomTypeFilter) {
        String type = (roomTypeFilter == null || roomTypeFilter.trim().isEmpty()) ? "ALL" : roomTypeFilter.trim();
        int totalRooms = roomList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        Room[] temp = new Room[totalRooms];
        int count = 0;
        for (int i = 0; i < totalRooms; i++) {
            Room r = roomList.getEntry(i); // ADT method call: getEntry()
            boolean typeOk = type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(r.getRoomType());
            if (typeOk && !isRoomTakenInRange(r, checkIn, checkOut)) {
                temp[count++] = r;
            }
        }
        return trim(temp, count);
    }

    // True if at least one room of the type is free for the night of date.
    public boolean hasAvailabilityOn(LocalDate date, String roomTypeFilter) {
        String type = (roomTypeFilter == null || roomTypeFilter.trim().isEmpty()) ? "ALL" : roomTypeFilter.trim();
        LocalDate next = date.plusDays(1);
        int totalRooms = roomList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        for (int i = 0; i < totalRooms; i++) {
            Room r = roomList.getEntry(i); // ADT method call: getEntry()
            boolean typeOk = type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(r.getRoomType());
            if (typeOk && !isRoomTakenInRange(r, date, next)) {
                return true;
            }
        }
        return false;
    }

    // Total rooms of a given type (or "ALL"), for "N of M free" text.
    public int countRoomsByType(String roomTypeFilter) {
        String type = (roomTypeFilter == null || roomTypeFilter.trim().isEmpty()) ? "ALL" : roomTypeFilter.trim();
        int total = 0;
        for (int i = 0; i < roomList.getNumberOfEntries(); i++) { // ADT method call: getNumberOfEntries()
            Room r = roomList.getEntry(i); // ADT method call: getEntry()
            if (type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(r.getRoomType())) {
                total++;
            }
        }
        return total;
    }

    // True if a room is blocked for [checkIn, checkOut); delegates to the shared
    // schedule rule (with the 1-day housekeeping buffer) so all modules agree.
    private boolean isRoomTakenInRange(Room room, LocalDate checkIn, LocalDate checkOut) {
        return !com.tarumt.resorts.util.RoomScheduleAvailability.isAvailable(
                bookingList, room, checkIn, checkOut);
    }

    // ================= Report 1: Booking / Occupancy =================

    // Filter bookings by status + room type (linear scan).
    public Booking[] filterByStatusAndType(String statusFilter, String roomTypeFilter) {
        String status = (statusFilter == null) ? "ALL" : statusFilter.trim();
        String type = (roomTypeFilter == null) ? "ALL" : roomTypeFilter.trim();
        int total = bookingList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()

        Booking[] temp = new Booking[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Booking b = bookingList.getEntry(i); // ADT method call: getEntry()
            boolean statusOk = status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(b.getStatus());
            boolean typeOk = type.equalsIgnoreCase("ALL")
                    || type.equalsIgnoreCase(b.getRoom().getRoomType());
            if (statusOk && typeOk) {
                temp[count++] = b;
            }
        }
        return trim(temp, count);
    }

    // Insertion sort by check-in time ascending; null times sort to the end.
    public void sortByCheckInTime(Booking[] bookings) {
        for (int i = 1; i < bookings.length; i++) {
            Booking key = bookings[i];
            int j = i - 1;
            while (j >= 0 && compareCheckInTime(bookings[j], key) > 0) {
                bookings[j + 1] = bookings[j];
                j--;
            }
            bookings[j + 1] = key;
        }
    }

    // Null-safe check-in time comparison (null = "later than" any real time).
    private int compareCheckInTime(Booking a, Booking b) {
        String timeA = a.getCheckInTime();
        String timeB = b.getCheckInTime();
        if (timeA == null && timeB == null) {
            return 0;
        }
        if (timeA == null) {
            return 1;
        }
        if (timeB == null) {
            return -1;
        }
        return timeA.compareTo(timeB);
    }

    // ================= Report 2: Billing Summary =================

    // Filter bookings by payment + room type; CANCELLED excluded (not billable).
    public Booking[] filterByPayment(String paymentFilter, String roomTypeFilter) {
        String pay = (paymentFilter == null) ? "ALL" : paymentFilter.trim();
        String type = (roomTypeFilter == null) ? "ALL" : roomTypeFilter.trim();
        int total = bookingList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()

        Booking[] temp = new Booking[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Booking b = bookingList.getEntry(i); // ADT method call: getEntry()
            if ("CANCELLED".equalsIgnoreCase(b.getStatus())) {
                continue; // cancelled bookings are not billable
            }
            String status = b.getPaymentStatus();
            boolean payOk = pay.equalsIgnoreCase("ALL") || pay.equalsIgnoreCase(status);
            boolean typeOk = type.equalsIgnoreCase("ALL")
                    || type.equalsIgnoreCase(b.getRoom().getRoomType());
            if (payOk && typeOk) {
                temp[count++] = b;
            }
        }
        return trim(temp, count);
    }

    // Selection sort by amount payable (after discount), highest first.
    public void sortByAmountDescending(Booking[] bookings) {
        for (int i = 0; i < bookings.length - 1; i++) {
            int best = i;
            for (int j = i + 1; j < bookings.length; j++) {
                if (bookings[j].getFinalAmount() > bookings[best].getFinalAmount()) {
                    best = j;
                }
            }
            if (best != i) {
                Booking tmp = bookings[i];
                bookings[i] = bookings[best];
                bookings[best] = tmp;
            }
        }
    }

    // Sum the amount payable across the given bookings (report total).
    public double totalAmount(Booking[] bookings) {
        double total = 0.0;
        for (Booking b : bookings) {
            total += b.getFinalAmount();
        }
        return total;
    }

    // ================= Check-in (CONFIRMED -> ACTIVE) =================

    // Check a guest in: CONFIRMED -> ACTIVE, mark PAID, occupy the room.
    // Rejects early arrival and a room that is not Housekeeping-READY.
    public boolean checkInBooking(String confirmationNumber, String checkInTime) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        // No early check-in: the arrival date must not precede the schedule.
        if (isBeforeScheduledCheckIn(booking, parseDatePart(checkInTime))) {
            return false;
        }
        // The room must still be Housekeeping-ready at the moment of arrival.
        if (!isRoomReadyForCheckIn(booking.getRoom())) {
            return false;
        }
        booking.setCheckInTime(checkInTime);
        booking.setStatus("ACTIVE");
        booking.setPaymentStatus("PAID"); // guest settles the bill on check-in
        Room room = booking.getRoom();
        if (room != null) {
            room.setAvailable(false); // guest now occupies the room
        }
        return true;
    }

    // True if the guest would arrive before the scheduled check-in date.
    public boolean isBeforeScheduledCheckIn(Booking booking, LocalDate arrivalDate) {
        return booking != null
                && booking.getScheduledCheckInDate() != null
                && arrivalDate != null
                && arrivalDate.isBefore(booking.getScheduledCheckInDate());
    }

    // True when a room is READY, or never logged (null/"UNKNOWN").
    private boolean isRoomReadyForCheckIn(Room room) {
        if (room == null) {
            return false;
        }
        String cleaningStatus = room.getCleaningStatus();
        return cleaningStatus == null
                || cleaningStatus.equalsIgnoreCase("READY")
                || cleaningStatus.equalsIgnoreCase("UNKNOWN");
    }

    // Public wrapper so the UI can show a specific "room not ready" message.
    public boolean isBookingRoomReadyForCheckIn(Booking booking) {
        return booking != null && isRoomReadyForCheckIn(booking.getRoom());
    }

    // Extract the date part from a "yyyy-MM-dd HH:mm" string; null if bad.
    private LocalDate parseDatePart(String dateTime) {
        if (dateTime == null || dateTime.trim().length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(dateTime.trim().substring(0, 10));
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    // ================= Cancel booking (CONFIRMED only, soft-cancel) =================

    // Cancel a CONFIRMED booking: mark CANCELLED, keep the record, leave occupancy.
    public boolean cancelBooking(String confirmationNumber) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        booking.setStatus("CANCELLED");          // soft-cancel: retained, not deleted
        return true;                              // schedule auto-frees; occupancy untouched
    }

    // ================= Free-text search over bookings =================

    // Return bookings matching EVERY space-separated keyword (any field). Blank = all.
    public Booking[] search(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        String[] keywords = q.isEmpty() ? new String[0] : q.split("\\s+");

        int total = bookingList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        Booking[] temp = new Booking[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Booking b = bookingList.getEntry(i); // ADT method call: getEntry()
            boolean allKeywordsMatch = true;
            for (String keyword : keywords) {
                if (!matchesAnyField(b, keyword)) {
                    allKeywordsMatch = false;
                    break;
                }
            }
            if (allKeywordsMatch) {
                temp[count++] = b;
            }
        }
        return trim(temp, count);
    }

    // True if the query appears in any searchable field of the booking.
    private boolean matchesAnyField(Booking b, String query) {
        Guest g = b.getGuest();
        Room r = b.getRoom();
        LoyaltyAccount la = getLoyaltyAccountFor(b);
        return fieldContains(b.getConfirmationNumber(), query)
                || (g != null && (fieldContains(g.getName(), query)
                        || fieldContains(g.getGuestId(), query)
                        || fieldContains(String.valueOf(g.getMembershipTier()), query)))
                || (r != null && (fieldContains(r.getRoomNumber(), query)
                        || fieldContains(r.getRoomType(), query)))
                || (la != null && (fieldContains(la.getLoyaltyId(), query)
                        || fieldContains(String.valueOf(la.getPointsBalance()), query)))
                || fieldContains(b.getStatus(), query)
                || fieldContains(b.getPaymentStatus(), query)
                || fieldContains(b.getCheckInTime(), query)
                || fieldContains(b.getCheckOutTime(), query)
                || fieldContains(String.format("%.2f", b.getAmount()), query);
    }

    // Null-safe, case-insensitive substring test.
    private boolean fieldContains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    // ================= Accessors used by the UI =================

    // All bookings as an array for the UI to display.
    public Booking[] getAllBookings() {
        int total = bookingList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        Booking[] all = new Booking[total];
        for (int i = 0; i < total; i++) {
            all[i] = bookingList.getEntry(i); // ADT method call: getEntry()
        }
        return all;
    }

    public int getBookingCount() {
        return bookingList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
    }

    // Every guest from the shared collection.
    public Guest[] getAllGuests() {
        int total = guestList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        Guest[] all = new Guest[total];
        for (int i = 0; i < total; i++) {
            all[i] = guestList.getEntry(i); // ADT method call: getEntry()
        }
        return all;
    }

    // Copy the first count elements into a right-sized array.
    private Booking[] trim(Booking[] source, int count) {
        Booking[] result = new Booking[count];
        for (int i = 0; i < count; i++) {
            result[i] = source[i];
        }
        return result;
    }

    // Copy the first count elements into a right-sized array.
    private Room[] trim(Room[] source, int count) {
        Room[] result = new Room[count];
        for (int i = 0; i < count; i++) {
            result[i] = source[i];
        }
        return result;
    }
}
