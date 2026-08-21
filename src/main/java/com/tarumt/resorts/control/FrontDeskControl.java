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
 * Front-Desk logic: find bookings by confirmation no, check room
 * availability and do check-in / check-out / cancel. Front-Desk does not
 * create bookings - Walk-In / VIP do that.
 *
 * @author Tan Keng Ting
 */
public class FrontDeskControl {

    private ListQueueInterface<Booking> bookingList; // ADT collection declaration
    private ListQueueInterface<Guest> guestList; // ADT collection declaration
    private ListQueueInterface<Room> roomList; // ADT collection declaration

    // set by Main - lets a check-out also log the room as DIRTY in housekeeping
    private HousekeepingControl housekeeping;

    // set by Main - lets us show / search a guest's loyalty info
    private ListQueueInterface<LoyaltyAccount> loyaltyAccounts; // ADT collection declaration

    // standalone mode: load the sample data so this module can run on its own
    public FrontDeskControl() {
        this.guestList = new GuestDAO().getAllGuests();
        this.roomList = new RoomDAO().getAllRooms();
        this.bookingList = new BookingDAO().getAllBookings(guestList, roomList);
    }

    // used by Main: share the same collections as the rest of the system
    public FrontDeskControl(
            ListQueueInterface<Booking> sharedBookings, // ADT collection declaration
            ListQueueInterface<Guest> sharedGuests, // ADT collection declaration
            ListQueueInterface<Room> sharedRooms) { // ADT collection declaration
        bookingList = sharedBookings;
        guestList = sharedGuests;
        roomList = sharedRooms;
    }

    // plug in housekeeping (optional)
    public void setHousekeepingControl(HousekeepingControl housekeeping) {
        this.housekeeping = housekeeping;
    }

    // plug in the loyalty accounts (optional)
    public void setLoyaltyAccounts(ListQueueInterface<LoyaltyAccount> loyaltyAccounts) { // ADT collection declaration
        this.loyaltyAccounts = loyaltyAccounts;
    }

    // find this guest's loyalty account - just loop and match on guest id
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

    // loyalty id for the receipt, "-" if the guest has none
    public String getLoyaltyIdFor(Booking booking) {
        LoyaltyAccount account = getLoyaltyAccountFor(booking);
        return (account == null || account.getLoyaltyId() == null) ? "-" : account.getLoyaltyId();
    }

    // current points for the receipt, "-" if no account
    public String getLoyaltyPointsFor(Booking booking) {
        LoyaltyAccount account = getLoyaltyAccountFor(booking);
        return account == null ? "-" : String.valueOf(account.getPointsBalance());
    }

    //look up a booking by its 8-digit confirmation no
    // find one booking by conf no
    public Booking findByConfirmationNumber(String confirmationNumber) {
        if (confirmationNumber == null || confirmationNumber.trim().isEmpty()) {
            return null;
        }
        String key = confirmationNumber.trim();
        return bookingList.searchByKey(key, b -> b.getConfirmationNumber()); // ADT method call: searchByKey()
    }

    // a conf no must be exactly 8 digits
    public boolean isValidConfirmationNumber(String confirmationNumber) {
        return confirmationNumber != null
                && confirmationNumber.trim().matches("^[0-9]{8}$");
    }

    // is this conf no already taken?
    public boolean confirmationNumberExists(String confirmationNumber) {
        return findByConfirmationNumber(confirmationNumber) != null;
    }

    //check-out
    // check out: only an ACTIVE booking -> CHECKED_OUT, free the room + mark dirty
    public boolean checkOutBooking(String confirmationNumber, String checkOutTime) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"ACTIVE".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        booking.setStatus("CHECKED_OUT");
        booking.setCheckOutTime(checkOutTime);
        Room room = booking.getRoom();
        if (room != null) {
            room.setAvailable(true);            // room is empty again
            room.setCleaningStatus("DIRTY");    // pass it to housekeeping
            if (housekeeping != null) {
                housekeeping.logStatusChange(room.getRoomNumber(), "DIRTY", checkOutTime);
            }
        }
        return true;
    }

    //room availability
    // find a room by its number
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

    // rooms free right now = vacant AND not booked for today
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
            // vacant + no clash + cleaned (READY), so the list matches what check-in will accept
            if (typeOk && r.isAvailable() && !isRoomTakenInRange(r, today, tomorrow)
                    && isRoomReadyForCheckIn(r)) {
                temp[count++] = r;
            }
        }
        return trim(temp, count);
    }

    // date-range availability (for the calendar)
    // rooms of a type with no booking clashing [checkIn, checkOut)
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

    // is any room of this type free that night? (one day cell of the calendar)
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

    // total rooms of a type, so the UI can say "N of M free"
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

    // does this room clash the range? use the shared rule so every module agrees
    private boolean isRoomTakenInRange(Room room, LocalDate checkIn, LocalDate checkOut) {
        return !com.tarumt.resorts.util.RoomScheduleAvailability.isAvailable(
                bookingList, room, checkIn, checkOut);
    }

    // report 1: booking / occupancy
    // filter bookings by status + room type
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

    // insertion sort by check-in time; bookings not checked in yet go to the end
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

    // compare check-in times; a null time counts as "latest"
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

    // report 2: billing summary
    // filter by payment + room type; skip cancelled ones (not real money owed)
    public Booking[] filterByPayment(String paymentFilter, String roomTypeFilter) {
        String pay = (paymentFilter == null) ? "ALL" : paymentFilter.trim();
        String type = (roomTypeFilter == null) ? "ALL" : roomTypeFilter.trim();
        int total = bookingList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()

        Booking[] temp = new Booking[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Booking b = bookingList.getEntry(i); // ADT method call: getEntry()
            if ("CANCELLED".equalsIgnoreCase(b.getStatus())) {
                continue; // cancelled = nothing to bill
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

    // selection sort, biggest payable amount first
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

    // add up the payable amounts for the report total
    public double totalAmount(Booking[] bookings) {
        double total = 0.0;
        for (Booking b : bookings) {
            total += b.getFinalAmount();
        }
        return total;
    }

    // check-in (CONFIRMED -> ACTIVE)
    // check in: CONFIRMED -> ACTIVE + PAID, occupy the room.
    // rejects arriving early or a room that isn't cleaned yet.
    public boolean checkInBooking(String confirmationNumber, String checkInTime) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        // can't arrive before the booked date
        if (isBeforeScheduledCheckIn(booking, parseDatePart(checkInTime))) {
            return false;
        }
        // room has to be READY right now
        if (!isRoomReadyForCheckIn(booking.getRoom())) {
            return false;
        }
        booking.setCheckInTime(checkInTime);
        booking.setStatus("ACTIVE");
        booking.setPaymentStatus("PAID"); // guest pays on arrival
        Room room = booking.getRoom();
        if (room != null) {
            room.setAvailable(false); // room is now occupied
        }
        return true;
    }

    // is the guest trying to arrive before their booked date?
    public boolean isBeforeScheduledCheckIn(Booking booking, LocalDate arrivalDate) {
        return booking != null
                && booking.getScheduledCheckInDate() != null
                && arrivalDate != null
                && arrivalDate.isBefore(booking.getScheduledCheckInDate());
    }

    // room is fine for check-in when it's READY, or was never logged
    private boolean isRoomReadyForCheckIn(Room room) {
        if (room == null) {
            return false;
        }
        String cleaningStatus = room.getCleaningStatus();
        return cleaningStatus == null
                || cleaningStatus.equalsIgnoreCase("READY")
                || cleaningStatus.equalsIgnoreCase("UNKNOWN");
    }

    // public version so the UI can pop a "room not ready" message
    public boolean isBookingRoomReadyForCheckIn(Booking booking) {
        return booking != null && isRoomReadyForCheckIn(booking.getRoom());
    }

    // grab the yyyy-MM-dd part from a "yyyy-MM-dd HH:mm" string
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

    // cancel (CONFIRMED only, soft-cancel)
    // cancel a CONFIRMED booking - just flag it CANCELLED, keep the record
    public boolean cancelBooking(String confirmationNumber) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        booking.setStatus("CANCELLED");          // keep it for the record
        return true;                              // room frees up on its own
    }

    // keyword search 
    // search: a booking is kept only if it matches every keyword somewhere
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

    // does the keyword appear in any field of this booking?
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

    // null-safe contains, ignore case
    private boolean fieldContains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    // accessors for the UI 
    // all bookings as an array
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

    // all guests as an array
    public Guest[] getAllGuests() {
        int total = guestList.getNumberOfEntries(); // ADT method call: getNumberOfEntries()
        Guest[] all = new Guest[total];
        for (int i = 0; i < total; i++) {
            all[i] = guestList.getEntry(i); // ADT method call: getEntry()
        }
        return all;
    }

    // trim an over-sized array down to what we actually filled
    private Booking[] trim(Booking[] source, int count) {
        Booking[] result = new Booking[count];
        for (int i = 0; i < count; i++) {
            result[i] = source[i];
        }
        return result;
    }

    // same, for rooms
    private Room[] trim(Room[] source, int count) {
        Room[] result = new Room[count];
        for (int i = 0; i < count; i++) {
            result[i] = source[i];
        }
        return result;
    }
}
