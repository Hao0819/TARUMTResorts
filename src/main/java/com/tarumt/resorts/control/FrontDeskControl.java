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
 * Business logic for the Front-Desk Service module. Front-desk agents look up
 * existing bookings by their unique 8-digit confirmation number, inspect the
 * linked guest/room details, check room availability, and update the statuses
 * that belong to Front-Desk (payment and check-out).
 *
 * Per the team's single booking-creation decision, Front-Desk does NOT create
 * bookings (Walk-In / VIP do); it searches and manages the resulting Booking
 * from the shared collection.
 *
 * @author Tan Keng Ting
 */
public class FrontDeskControl {

    private ListQueueInterface<Booking> bookingList;
    private ListQueueInterface<Guest> guestList;
    private ListQueueInterface<Room> roomList;

    // Optional collaborator (Housekeeping module). When wired in by Main, a
    // check-out also writes a DIRTY entry into Housekeeping's status log so
    // their View/reports reflect the handover — not just the Room field.
    private HousekeepingControl housekeeping;

    // Optional shared Loyalty accounts (owned by the Loyalty module). When wired
    // in by Main, Front-Desk can show and search each booking guest's loyalty ID
    // and current points. Left null in standalone mode.
    private ListQueueInterface<LoyaltyAccount> loyaltyAccounts;

    /**
     * Standalone constructor - loads hard-coded sample data so the module
     * can be demonstrated on its own without the Walk-In module running.
     */
    public FrontDeskControl() {
        // Create ONE shared Guest and Room collection, then build the sample
        // bookings against those same objects so every feature (availability,
        // check-out, reports) reads and writes the same Room instances.
        this.guestList = new GuestDAO().getAllGuests();
        this.roomList = new RoomDAO().getAllRooms();
        this.bookingList = new BookingDAO().getAllBookings(guestList, roomList);
    }

    /**
     * Integrated constructor - receives the same shared collection references
     * used by the rest of the system, so Front-Desk sees the bookings created
     * at runtime by the Walk-In / VIP modules and the current room state.
     */
    public FrontDeskControl(
            ListQueueInterface<Booking> sharedBookings,
            ListQueueInterface<Guest> sharedGuests,
            ListQueueInterface<Room> sharedRooms) {
        bookingList = sharedBookings;
        guestList = sharedGuests;
        roomList = sharedRooms;
    }

    /**
     * Wires in the shared Housekeeping control so that checking a guest out
     * also logs the room as DIRTY in Housekeeping's status log. Optional:
     * when left null (standalone mode), check-out still updates the shared
     * Room object's cleaningStatus field directly.
     */
    public void setHousekeepingControl(HousekeepingControl housekeeping) {
        this.housekeeping = housekeeping;
    }

    /**
     * Wires in the shared Loyalty accounts so Front-Desk can display and search
     * a booking guest's loyalty ID and current points. Optional: when left null
     * (standalone mode), loyalty lookups return null and the UI shows "-".
     */
    public void setLoyaltyAccounts(ListQueueInterface<LoyaltyAccount> loyaltyAccounts) {
        this.loyaltyAccounts = loyaltyAccounts;
    }

    /**
     * Finds the loyalty account belonging to a booking's guest (matched by guest
     * ID), or null if loyalty data isn't wired in or the guest has no account.
     * Self-implemented linear scan.
     */
    public LoyaltyAccount getLoyaltyAccountFor(Booking booking) {
        if (loyaltyAccounts == null || booking == null || booking.getGuest() == null) {
            return null;
        }
        String guestId = booking.getGuest().getGuestId();
        if (guestId == null) {
            return null;
        }
        for (int i = 0; i < loyaltyAccounts.getNumberOfEntries(); i++) {
            LoyaltyAccount account = loyaltyAccounts.getEntry(i);
            if (guestId.equalsIgnoreCase(account.getGuestId())) {
                return account;
            }
        }
        return null;
    }

    /** The booking guest's loyalty ID, or "-" when there is no linked account. */
    public String getLoyaltyIdFor(Booking booking) {
        LoyaltyAccount account = getLoyaltyAccountFor(booking);
        return (account == null || account.getLoyaltyId() == null) ? "-" : account.getLoyaltyId();
    }

    /** The booking guest's current points as text, or "-" when there is no account. */
    public String getLoyaltyPointsFor(Booking booking) {
        LoyaltyAccount account = getLoyaltyAccountFor(booking);
        return account == null ? "-" : String.valueOf(account.getPointsBalance());
    }

    // =====================================================================
    // Core feature: look up a booking by its 8-digit confirmation number.
    // =====================================================================

    /**
     * Retrieves the full booking record for a confirmation number by
     * delegating to the shared ADT's key-based linear search. The lambda tells
     * the generic collection that a Booking's key is its confirmation number.
     *
     * Time complexity: O(n) - searchByKey scans the booking list from front to
     * rear in the worst case (target at the rear or absent); best case O(1).
     *
     * @return the matching Booking, or null if none exists
     */
    public Booking findByConfirmationNumber(String confirmationNumber) {
        if (confirmationNumber == null || confirmationNumber.trim().isEmpty()) {
            return null;
        }
        String key = confirmationNumber.trim();
        return bookingList.searchByKey(key, b -> b.getConfirmationNumber());
    }

    /** A confirmation number must be exactly 8 digits. */
    public boolean isValidConfirmationNumber(String confirmationNumber) {
        return confirmationNumber != null
                && confirmationNumber.trim().matches("^[0-9]{8}$");
    }

    /** True if a booking already uses this confirmation number (uniqueness check). */
    public boolean confirmationNumberExists(String confirmationNumber) {
        return findByConfirmationNumber(confirmationNumber) != null;
    }

    // =====================================================================
    // Front-Desk write operations: payment update and check-out.
    // =====================================================================

    /**
     * Checks a guest out: marks the booking CHECKED_OUT, records the check-out
     * time, and hands the room over to Housekeeping on the shared Room object.
     * The bill was already settled at check-in (ACTIVE bookings are PAID), so
     * check-out does not touch the payment status.
     *
     * The room becomes vacant (available) but is flagged DIRTY, so it is NOT
     * immediately allocatable: Walk-In/VIP allocation only accepts rooms whose
     * cleaning status is READY (or never logged). Housekeeping must run the
     * DIRTY -> CLEANING -> INSPECTED -> READY cycle before the room can be
     * given to the next guest.
     *
     * @return false if the booking is missing or already checked out
     */
    public boolean checkOutBooking(String confirmationNumber, String checkOutTime) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        // Only an ACTIVE (checked-in) booking can be checked out. A CONFIRMED
        // booking must be checked in first; CANCELLED/CHECKED_OUT are terminal.
        if (booking == null || !"ACTIVE".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        booking.setStatus("CHECKED_OUT");
        booking.setCheckOutTime(checkOutTime);
        Room room = booking.getRoom();
        if (room != null) {
            room.setAvailable(true);            // no longer occupied
            room.setCleaningStatus("DIRTY");    // hand over to Housekeeping (Room field)
            // Also record the handover in Housekeeping's status log, so their
            // View current status / reports show the room as DIRTY. Non-fatal:
            // if Housekeeping isn't wired in (standalone) or its transition rule
            // rejects the entry, the check-out itself still succeeds.
            if (housekeeping != null) {
                housekeeping.logStatusChange(room.getRoomNumber(), "DIRTY", checkOutTime);
            }
        }
        return true;
    }

    // =====================================================================
    // Availability query over the shared room collection.
    // =====================================================================

    /** Linear lookup of a room by room number (to inspect a specific room). */
    public Room findRoomByNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return null;
        }
        String number = roomNumber.trim();
        for (int i = 0; i < roomList.getNumberOfEntries(); i++) {
            Room r = roomList.getEntry(i);
            if (r.getRoomNumber().equalsIgnoreCase(number)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Returns the rooms available RIGHT NOW, optionally filtered by room type
     * ("ALL" for every type). A room qualifies when both hold:
     *   1. it is vacant (occupancy flag available),
     *   2. it has no CONFIRMED/ACTIVE booking overlapping today (not reserved
     *      for a guest arriving today).
     * Housekeeping cleaning status is intentionally NOT considered here - room
     * readiness/cleaning is the Housekeeping module's concern, so Front-Desk
     * reports physical availability only. Self-implemented linear scan.
     */
    public Room[] getAvailableRooms(String roomTypeFilter) {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        String type = (roomTypeFilter == null) ? "ALL" : roomTypeFilter.trim();
        int total = roomList.getNumberOfEntries();

        Room[] temp = new Room[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Room r = roomList.getEntry(i);
            boolean typeOk = type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(r.getRoomType());
            if (typeOk && r.isAvailable() && !isRoomTakenInRange(r, today, tomorrow)) {
                temp[count++] = r;
            }
        }
        return trim(temp, count);
    }

    // =====================================================================
    // Date-range availability. Answers "for a given date range and room type,
    // which rooms are free, and is that type full?" A room is taken for the
    // range if it has a CONFIRMED or ACTIVE booking whose scheduled stay
    // overlaps [checkIn, checkOut). CHECKED_OUT / CANCELLED bookings, and
    // bookings without a schedule, do not block. Uses the shared Booking
    // scheduled dates provided by Walk-In/VIP. Self-implemented, O(rooms*bookings).
    // =====================================================================

    /**
     * Returns rooms of the given type (or "ALL") that have NO CONFIRMED/ACTIVE
     * booking overlapping the requested [checkIn, checkOut) date range.
     */
    public Room[] getAvailableRoomsForRange(LocalDate checkIn, LocalDate checkOut, String roomTypeFilter) {
        String type = (roomTypeFilter == null || roomTypeFilter.trim().isEmpty()) ? "ALL" : roomTypeFilter.trim();
        int totalRooms = roomList.getNumberOfEntries();
        Room[] temp = new Room[totalRooms];
        int count = 0;
        for (int i = 0; i < totalRooms; i++) {
            Room r = roomList.getEntry(i);
            boolean typeOk = type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(r.getRoomType());
            if (typeOk && !isRoomTakenInRange(r, checkIn, checkOut)) {
                temp[count++] = r;
            }
        }
        return trim(temp, count);
    }

    /**
     * True if at least one room of the given type (or "ALL") is free for a
     * one-night stay starting on {@code date} - i.e. has no CONFIRMED/ACTIVE
     * booking overlapping [date, date+1). Backs each day cell of the
     * availability calendar; short-circuits on the first free room so drawing a
     * month never allocates a result array.
     */
    public boolean hasAvailabilityOn(LocalDate date, String roomTypeFilter) {
        String type = (roomTypeFilter == null || roomTypeFilter.trim().isEmpty()) ? "ALL" : roomTypeFilter.trim();
        LocalDate next = date.plusDays(1);
        int totalRooms = roomList.getNumberOfEntries();
        for (int i = 0; i < totalRooms; i++) {
            Room r = roomList.getEntry(i);
            boolean typeOk = type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(r.getRoomType());
            if (typeOk && !isRoomTakenInRange(r, date, next)) {
                return true;
            }
        }
        return false;
    }

    /** Total rooms of a given type (or "ALL"), so the UI can report "N of M free". */
    public int countRoomsByType(String roomTypeFilter) {
        String type = (roomTypeFilter == null || roomTypeFilter.trim().isEmpty()) ? "ALL" : roomTypeFilter.trim();
        int total = 0;
        for (int i = 0; i < roomList.getNumberOfEntries(); i++) {
            Room r = roomList.getEntry(i);
            if (type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(r.getRoomType())) {
                total++;
            }
        }
        return total;
    }

    /**
     * True if the room cannot be scheduled for [checkIn, checkOut) because a
     * CONFIRMED or ACTIVE booking's scheduled stay overlaps it. Delegates to the
     * shared {@link com.tarumt.resorts.util.RoomScheduleAvailability} rule so
     * Walk-In, VIP, and Front-Desk all agree on the same room/date decisions
     * (and share its 1-day Housekeeping turnaround buffer, which reserves the
     * night after each check-out for cleaning). This is simply the negation of
     * that utility's "is available" answer.
     */
    private boolean isRoomTakenInRange(Room room, LocalDate checkIn, LocalDate checkOut) {
        return !com.tarumt.resorts.util.RoomScheduleAvailability.isAvailable(
                bookingList, room, checkIn, checkOut);
    }

    // =====================================================================
    // Report 1 support: Booking / Occupancy (filter status + type, sort time).
    // =====================================================================

    /**
     * Filters bookings by two criteria: booking status (ALL/ACTIVE/CHECKED_OUT)
     * and room type (ALL/Standard/Deluxe/Suite). Self-implemented linear scan.
     */
    public Booking[] filterByStatusAndType(String statusFilter, String roomTypeFilter) {
        String status = (statusFilter == null) ? "ALL" : statusFilter.trim();
        String type = (roomTypeFilter == null) ? "ALL" : roomTypeFilter.trim();
        int total = bookingList.getNumberOfEntries();

        Booking[] temp = new Booking[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Booking b = bookingList.getEntry(i);
            boolean statusOk = status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(b.getStatus());
            boolean typeOk = type.equalsIgnoreCase("ALL")
                    || type.equalsIgnoreCase(b.getRoom().getRoomType());
            if (statusOk && typeOk) {
                temp[count++] = b;
            }
        }
        return trim(temp, count);
    }

    /**
     * Insertion sort ordering bookings by check-in time ascending. The time
     * strings use "yyyy-MM-dd HH:mm", so lexicographic comparison is already
     * chronological.
     *
     * A booking created by Walk-In/VIP that has been allocated a room but not
     * yet checked in has a null check-in time (status CONFIRMED). Those are
     * ordered AFTER every checked-in booking rather than dereferenced, so the
     * report never crashes on a not-yet-checked-in booking.
     */
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

    /**
     * Null-safe check-in time comparison. A null check-in time (booked but not
     * yet checked in) is treated as "later than" any real time, so such
     * bookings sort to the end of the ascending list.
     */
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

    // =====================================================================
    // Report 2 support: Billing Summary (filter payment + type, sort amount).
    // =====================================================================

    /**
     * Filters bookings by two criteria: payment status and room type, for the
     * Billing Summary. CANCELLED bookings are excluded: they are kept on record
     * (visible via the Occupancy report's status filter) but are not billable
     * revenue, so counting their UNPAID amount as an outstanding balance would
     * overstate what guests actually owe. Payment is a two-state flow - UNPAID
     * (booking made) or PAID (settled at check-in). Self-implemented linear scan.
     */
    public Booking[] filterByPayment(String paymentFilter, String roomTypeFilter) {
        String pay = (paymentFilter == null) ? "ALL" : paymentFilter.trim();
        String type = (roomTypeFilter == null) ? "ALL" : roomTypeFilter.trim();
        int total = bookingList.getNumberOfEntries();

        Booking[] temp = new Booking[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Booking b = bookingList.getEntry(i);
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

    /**
     * Selection sort ordering bookings by the amount payable (after any
     * membership discount), highest first.
     */
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

    /**
     * Sums the amount payable (after membership discount) across the given
     * bookings, matching the per-booking figure shown in the Billing report and
     * the detail card's "Amount Payable". Used for the report total.
     */
    public double totalAmount(Booking[] bookings) {
        double total = 0.0;
        for (Booking b : bookings) {
            total += b.getFinalAmount();
        }
        return total;
    }

    // =====================================================================
    // Check-in (CONFIRMED -> ACTIVE). Walk-In/VIP create a booking as
    // CONFIRMED (room allocated, guest not yet arrived); the front desk
    // performs the actual check-in when the guest turns up.
    // =====================================================================

    /**
     * Checks a guest in: a CONFIRMED booking (booked but not yet arrived)
     * becomes ACTIVE and gets its check-in time recorded. Only CONFIRMED
     * bookings can be checked in, and never BEFORE the scheduled check-in date
     * (a guest cannot arrive early). On success the room is marked occupied
     * (not available) and, since the bill is settled on arrival, the payment
     * status is marked PAID (CONFIRMED/UNPAID -> ACTIVE/PAID).
     *
     * Added: also re-checks Housekeeping readiness at the moment of actual
     * arrival, not just at allocation time. Walk-In/VIP only validate a
     * room's cleaning status when the booking is created AND only for a
     * same-day request (see WalkInRegistrationControl.isReadyForAllocation()
     * / VIPAllocationControl.isReadyForAllocation()) - a room booked well in
     * advance is never re-checked before the guest actually shows up days or
     * weeks later. Without this check, a room that Housekeeping re-logged as
     * DIRTY/CLEANING/INSPECTED after allocation (e.g. a supervisor's manual
     * "d" jump-to-DIRTY) could still be checked into. Re-validating here, on
     * the shared Room object's own cleaningStatus field, closes that gap
     * without requiring Front-Desk to depend on the optional Housekeeping
     * control reference.
     *
     * @return false if the booking is missing, not CONFIRMED, the arrival
     *         date is before the scheduled check-in date, or the room is not
     *         currently Housekeeping-READY (nor UNKNOWN/never logged)
     */
    public boolean checkInBooking(String confirmationNumber, String checkInTime) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        // No early check-in: the arrival date must not precede the schedule.
        if (isBeforeScheduledCheckIn(booking, parseDatePart(checkInTime))) {
            return false;
        }
        // Added: the room must still be Housekeeping-ready right now - not
        // just at the moment the booking was originally allocated.
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

    /**
     * True if the guest would be checking in BEFORE the booking's scheduled
     * check-in date. Bookings without a schedule (null) never count as early.
     * Exposed so the UI can show a specific "too early" message.
     */
    public boolean isBeforeScheduledCheckIn(Booking booking, LocalDate arrivalDate) {
        return booking != null
                && booking.getScheduledCheckInDate() != null
                && arrivalDate != null
                && arrivalDate.isBefore(booking.getScheduledCheckInDate());
    }

    /**
     * Added: true when a room is Housekeeping-READY, or has never been
     * logged at all (cleaningStatus null/"UNKNOWN"). Mirrors
     * WalkInRegistrationControl.isReadyForAllocation() /
     * VIPAllocationControl.isReadyForAllocation() exactly, so all three
     * modules agree on what "ready for a guest" means. Reads the shared
     * Room object's own field directly rather than calling into
     * HousekeepingControl, so this works even when Housekeeping isn't wired
     * in (standalone Front-Desk mode) - it simply falls back to whatever the
     * Room's own cleaningStatus already says (default "UNKNOWN").
     */
    private boolean isRoomReadyForCheckIn(Room room) {
        if (room == null) {
            return false;
        }
        String cleaningStatus = room.getCleaningStatus();
        return cleaningStatus == null
                || cleaningStatus.equalsIgnoreCase("READY")
                || cleaningStatus.equalsIgnoreCase("UNKNOWN");
    }

    /**
     * Public, null-safe wrapper around {@link #isRoomReadyForCheckIn(Room)}
     * so the boundary layer can show a specific "room not ready" message
     * instead of a generic check-in failure, the same way
     * {@link #isBeforeScheduledCheckIn(Booking, LocalDate)} is exposed for
     * the early-arrival case.
     */
    public boolean isBookingRoomReadyForCheckIn(Booking booking) {
        return booking != null && isRoomReadyForCheckIn(booking.getRoom());
    }

    /** Extracts the date (yyyy-MM-dd) from a "yyyy-MM-dd HH:mm" string; null if unparseable. */
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

    // =====================================================================
    // Cancel booking (Type A - confirmed bookings). Team rules:
    //  - Only a CONFIRMED booking (room reserved, guest not yet checked in)
    //    may be cancelled here. An ACTIVE (in-house) booking must be checked
    //    out / early-checked-out instead, not cancelled.
    //  - The record is RETAINED in the shared collection with status
    //    CANCELLED (soft-cancel), never deleted, so it stays auditable.
    //  - Cancelling a pending (pre-approval) registration by Reg ID + Guest ID
    //    is a separate operation that belongs to the Walk-In module.
    // =====================================================================

    /**
     * Cancels a CONFIRMED booking: marks it CANCELLED (kept in the collection).
     *
     * The room's occupancy flag is deliberately left untouched. A CONFIRMED
     * booking may be for a FUTURE date on a room that is currently occupied by
     * another ACTIVE guest; forcing the room to "available" here would wrongly
     * free an in-house room. Because date-range availability ignores CANCELLED
     * bookings, cancelling automatically frees the room for that period without
     * changing the current occupancy flag.
     *
     * @return false if the booking is missing or not in CONFIRMED state
     */
    public boolean cancelBooking(String confirmationNumber) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        booking.setStatus("CANCELLED");          // soft-cancel: retained, not deleted
        return true;                              // schedule auto-frees; occupancy untouched
    }

    // =====================================================================
    // Free-text search over bookings. The user types one or more keywords
    // (space-separated); a booking is returned only if EVERY keyword appears
    // in SOME field (confirmation number, guest name/ID, membership tier,
    // room number/type, booking status, payment status, check-in/out time,
    // or amount). Each keyword is OR-matched across all fields; the keywords
    // are AND-combined so extra words narrow the results. Case-insensitive
    // substring. Self-implemented O(n * keywords).
    //
    // Example: "siti deluxe" returns bookings that contain BOTH "siti" and
    // "deluxe" somewhere — i.e. Siti's Deluxe booking — without having to
    // scan every "siti" record by hand.
    //
    // This is a SEARCH (free-text keywords, match anywhere), distinct from the
    // report FILTERS which narrow by structured per-field criteria.
    // =====================================================================

    /**
     * @param query one or more space-separated keywords; a booking is returned
     *              only if every keyword appears in some field. Blank returns
     *              all bookings.
     * @return every booking matching all of the keywords
     */
    public Booking[] search(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        String[] keywords = q.isEmpty() ? new String[0] : q.split("\\s+");

        int total = bookingList.getNumberOfEntries();
        Booking[] temp = new Booking[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Booking b = bookingList.getEntry(i);
            boolean allKeywordsMatch = true;
            for (String keyword : keywords) {
                if (!matchesAnyField(b, keyword)) { // this keyword found in no field
                    allKeywordsMatch = false;
                    break;
                }
            }
            if (allKeywordsMatch) { // also true when there are no keywords (blank query)
                temp[count++] = b;
            }
        }
        return trim(temp, count);
    }

    /** True if the (already lower-cased) query appears in any searchable field of the booking. */
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

    /** Null-safe, case-insensitive substring test for one field against the query. */
    private boolean fieldContains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    // =====================================================================
    // Accessors used by the boundary/UI for listing.
    // =====================================================================

    /** Returns all bookings as an array so the UI can display them. */
    public Booking[] getAllBookings() {
        int total = bookingList.getNumberOfEntries();
        Booking[] all = new Booking[total];
        for (int i = 0; i < total; i++) {
            all[i] = bookingList.getEntry(i);
        }
        return all;
    }

    public int getBookingCount() {
        return bookingList.getNumberOfEntries();
    }

    /** Returns every guest from the shared collection (for guest-directory lookups). */
    public Guest[] getAllGuests() {
        int total = guestList.getNumberOfEntries();
        Guest[] all = new Guest[total];
        for (int i = 0; i < total; i++) {
            all[i] = guestList.getEntry(i);
        }
        return all;
    }

    /** Copies the first count elements of source into a right-sized array. */
    private Booking[] trim(Booking[] source, int count) {
        Booking[] result = new Booking[count];
        for (int i = 0; i < count; i++) {
            result[i] = source[i];
        }
        return result;
    }

    /** Copies the first count elements of source into a right-sized array. */
    private Room[] trim(Room[] source, int count) {
        Room[] result = new Room[count];
        for (int i = 0; i < count; i++) {
            result[i] = source[i];
        }
        return result;
    }
}