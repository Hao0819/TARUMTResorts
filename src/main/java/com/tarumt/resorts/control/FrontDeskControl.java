package com.tarumt.resorts.control;

import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.adt.ListQueueInterface;
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
 * @author Keng Ting
 */
public class FrontDeskControl {

    private ListQueueInterface<Booking> bookingList;
    private ListQueueInterface<Guest> guestList;
    private ListQueueInterface<Room> roomList;

    // Optional collaborator (Housekeeping module). When wired in by Main, a
    // check-out also writes a DIRTY entry into Housekeeping's status log so
    // their View/reports reflect the handover — not just the Room field.
    private HousekeepingControl housekeeping;

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

    /**
     * Mirrors the allocation readiness rule used by Walk-In/VIP: a vacant room
     * can only be given to a new guest once Housekeeping has it READY (or it
     * has never been logged, i.e. UNKNOWN).
     */
    public boolean isBookable(Room room) {
        if (room == null || !room.isAvailable()) {
            return false;
        }
        String cleaning = room.getCleaningStatus();
        return cleaning == null
                || cleaning.equalsIgnoreCase("READY")
                || cleaning.equalsIgnoreCase("UNKNOWN");
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
     * Returns the rooms currently available, optionally filtered by room type
     * ("ALL" for every type). Self-implemented linear scan.
     */
    public Room[] getAvailableRooms(String roomTypeFilter) {
        String type = (roomTypeFilter == null) ? "ALL" : roomTypeFilter.trim();
        int total = roomList.getNumberOfEntries();

        Room[] temp = new Room[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Room r = roomList.getEntry(i);
            boolean typeOk = type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(r.getRoomType());
            if (r.isAvailable() && typeOk) {
                temp[count++] = r;
            }
        }
        return trim(temp, count);
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
     * Filters bookings by two criteria: payment status and room type.
     * The special payment filter "OUTSTANDING" matches UNPAID or PARTIAL.
     * Self-implemented linear scan.
     */
    public Booking[] filterByPayment(String paymentFilter, String roomTypeFilter) {
        String pay = (paymentFilter == null) ? "ALL" : paymentFilter.trim();
        String type = (roomTypeFilter == null) ? "ALL" : roomTypeFilter.trim();
        int total = bookingList.getNumberOfEntries();

        Booking[] temp = new Booking[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Booking b = bookingList.getEntry(i);
            String status = b.getPaymentStatus();
            boolean payOk;
            if (pay.equalsIgnoreCase("ALL")) {
                payOk = true;
            } else if (pay.equalsIgnoreCase("OUTSTANDING")) {
                payOk = "UNPAID".equalsIgnoreCase(status) || "PARTIAL".equalsIgnoreCase(status);
            } else {
                payOk = pay.equalsIgnoreCase(status);
            }
            boolean typeOk = type.equalsIgnoreCase("ALL")
                    || type.equalsIgnoreCase(b.getRoom().getRoomType());
            if (payOk && typeOk) {
                temp[count++] = b;
            }
        }
        return trim(temp, count);
    }

    /**
     * Selection sort ordering bookings by amount owed, highest first.
     */
    public void sortByAmountDescending(Booking[] bookings) {
        for (int i = 0; i < bookings.length - 1; i++) {
            int best = i;
            for (int j = i + 1; j < bookings.length; j++) {
                if (bookings[j].getAmount() > bookings[best].getAmount()) {
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

    /** Sums the amount across the given bookings (used for report totals). */
    public double totalAmount(Booking[] bookings) {
        double total = 0.0;
        for (Booking b : bookings) {
            total += b.getAmount();
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
     * bookings can be checked in.
     *
     * @return false if the booking is missing or not in CONFIRMED state
     */
    public boolean checkInBooking(String confirmationNumber, String checkInTime) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        booking.setCheckInTime(checkInTime);
        booking.setStatus("ACTIVE");
        return true;
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
     * Cancels a CONFIRMED booking: marks it CANCELLED (kept in the collection)
     * and releases its reserved room.
     *
     * @return false if the booking is missing or not in CONFIRMED state
     */
    public boolean cancelBooking(String confirmationNumber) {
        Booking booking = findByConfirmationNumber(confirmationNumber);
        if (booking == null || !"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }
        booking.setStatus("CANCELLED");          // soft-cancel: retained, not deleted
        Room room = booking.getRoom();
        if (room != null) {
            room.setAvailable(true);             // reserved room released, bookable again
        }
        return true;
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
        return fieldContains(b.getConfirmationNumber(), query)
                || (g != null && (fieldContains(g.getName(), query)
                        || fieldContains(g.getGuestId(), query)
                        || fieldContains(String.valueOf(g.getMembershipTier()), query)))
                || (r != null && (fieldContains(r.getRoomNumber(), query)
                        || fieldContains(r.getRoomType(), query)))
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
    // Structured multi-field FILTER over bookings. Every criterion is
    // optional: a blank text field (or "ALL" for the enumerated ones) is
    // ignored, and all supplied criteria must match (AND). This narrows by
    // specific fields, so the user does not have to eyeball a long keyword
    // result. Distinct from the free-text SEARCH above. Self-implemented O(n).
    //
    // Fields: confirmation number, guest name, guest ID, membership tier,
    // room type, and check-in date. To add another field, add one optional
    // parameter and one more AND condition below.
    // =====================================================================

    /**
     * @param confirmationNumber partial match on the confirmation number (blank = any)
     * @param guestName          partial, case-insensitive match on the guest name (blank = any)
     * @param guestId            partial match on the guest ID (blank = any)
     * @param membershipTier     exact match on the guest tier, e.g. GOLD (blank/"ALL" = any)
     * @param roomType           exact match on the room type, e.g. Deluxe (blank/"ALL" = any)
     * @param checkInDate        substring match on the check-in time, e.g. "2026-07-17" (blank = any)
     * @return every booking matching ALL supplied criteria
     */
    public Booking[] filterBookings(String confirmationNumber, String guestName, String guestId,
            String membershipTier, String roomType, String checkInDate) {
        String conf = norm(confirmationNumber);
        String name = norm(guestName);
        String gid = norm(guestId);
        String tier = normFilter(membershipTier);
        String type = normFilter(roomType);
        String date = norm(checkInDate);

        int total = bookingList.getNumberOfEntries();
        Booking[] temp = new Booking[total];
        int count = 0;
        for (int i = 0; i < total; i++) {
            Booking b = bookingList.getEntry(i);
            Guest g = b.getGuest();
            Room r = b.getRoom();

            boolean confOk = conf.isEmpty()
                    || (b.getConfirmationNumber() != null
                        && b.getConfirmationNumber().toLowerCase().contains(conf));
            boolean nameOk = name.isEmpty()
                    || (g != null && g.getName() != null
                        && g.getName().toLowerCase().contains(name));
            boolean idOk = gid.isEmpty()
                    || (g != null && g.getGuestId() != null
                        && g.getGuestId().toLowerCase().contains(gid));
            boolean tierOk = tier.equalsIgnoreCase("ALL")
                    || (g != null && g.getMembershipTier() != null
                        && String.valueOf(g.getMembershipTier()).equalsIgnoreCase(tier));
            boolean typeOk = type.equalsIgnoreCase("ALL")
                    || (r != null && r.getRoomType() != null
                        && r.getRoomType().equalsIgnoreCase(type));
            boolean dateOk = date.isEmpty()
                    || (b.getCheckInTime() != null && b.getCheckInTime().toLowerCase().contains(date));

            if (confOk && nameOk && idOk && tierOk && typeOk && dateOk) {
                temp[count++] = b;
            }
        }
        return trim(temp, count);
    }

    /** Trims to lower-case for a "contains" text criterion; null becomes "" (ignored). */
    private String norm(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    /** Normalizes an enumerated filter; null/blank becomes "ALL" (ignored). */
    private String normFilter(String value) {
        return (value == null || value.trim().isEmpty()) ? "ALL" : value.trim();
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
