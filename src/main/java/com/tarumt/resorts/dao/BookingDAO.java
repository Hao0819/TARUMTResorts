package com.tarumt.resorts.dao;

import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;

/**
 * BookingDAO.java
 * Returns a collection pre-filled with hard-coded sample Booking entities
 * for the Front-Desk Service module to search and report on.
 *
 * Per tutor clarification: no file/database I/O, just hard-coded sample
 * data. Bookings reuse the Guest and Room sample data from GuestDAO and
 * RoomDAO so the confirmation numbers point at guests/rooms that actually
 * exist elsewhere in the system.
 *
 * Confirmation numbers are 8-digit as required by the Front-Desk
 * specification.
 *
 * @author Keng Ting
 */
public class BookingDAO {

    private ListQueueInterface<Guest> guests;
    private ListQueueInterface<Room> rooms;

    /**
     * Builds the sample bookings against the SAME shared Guest and Room
     * collections used by the rest of the system, so a Booking's Room is the
     * very same object queried by the availability feature. This prevents the
     * "stale room copy" problem where a check-out frees one copy of a room but
     * the availability view still reads a different copy.
     */
    public ListQueueInterface<Booking> getAllBookings(ListQueueInterface<Guest> sharedGuests, ListQueueInterface<Room> sharedRooms) {
        this.guests = sharedGuests;
        this.rooms = sharedRooms;
        ListQueueInterface<Booking> bookings = new DoublyLinkedListQueue<>();

        // ACTIVE bookings - guests currently occupying the rooms marked
        // unavailable in RoomDAO (103, 106, 202, 205, 302, 303). These guests
        // are all ASSIGNED in WalkInRegistrationDAO, never WAITING, so nobody
        // is queuing for a room while already occupying one. Amount and payment
        // status give the Billing Summary Report real data.
        bookings.enqueue(new Booking("20260001", findGuest("G001"), findRoom("103"), "2026-07-15 13:20", 450.00, "UNPAID"));
        bookings.enqueue(new Booking("20260002", findGuest("G003"), findRoom("106"), "2026-07-16 15:45", 420.00, "PAID"));
        bookings.enqueue(new Booking("20260003", findGuest("G010"), findRoom("202"), "2026-07-17 11:10", 980.00, "PARTIAL"));
        bookings.enqueue(new Booking("20260004", findGuest("G012"), findRoom("205"), "2026-07-17 18:30", 1150.00, "UNPAID"));
        bookings.enqueue(new Booking("20260005", findGuest("G016"), findRoom("302"), "2026-07-18 09:05", 2100.00, "UNPAID"));
        bookings.enqueue(new Booking("20260006", findGuest("G019"), findRoom("303"), "2026-07-19 20:15", 1875.00, "PARTIAL"));

        // CHECKED_OUT bookings - completed past stays in rooms that are free
        // again. A guest who checked out earlier may legitimately be WAITING in
        // today's walk-in queue (a returning guest), so these may reuse guests
        // from the waiting list.
        bookings.enqueue(markCheckedOut(new Booking("20260007", findGuest("G005"), findRoom("101"), "2026-07-10 12:00", 380.00, "PAID"), "2026-07-12 11:30"));
        bookings.enqueue(markCheckedOut(new Booking("20260008", findGuest("G007"), findRoom("201"), "2026-07-11 14:00", 890.00, "PAID"), "2026-07-14 10:15"));
        bookings.enqueue(markCheckedOut(new Booking("20260009", findGuest("G014"), findRoom("301"), "2026-07-12 16:30", 1650.00, "PAID"), "2026-07-15 12:00"));
        bookings.enqueue(markCheckedOut(new Booking("20260010", findGuest("G002"), findRoom("104"), "2026-07-13 10:45", 300.00, "UNPAID"), "2026-07-14 09:40"));
        bookings.enqueue(markCheckedOut(new Booking("20260011", findGuest("G009"), findRoom("105"), "2026-07-08 15:20", 520.00, "PAID"), "2026-07-10 11:00"));
        bookings.enqueue(markCheckedOut(new Booking("20260012", findGuest("G013"), findRoom("203"), "2026-07-09 13:10", 1020.00, "PARTIAL"), "2026-07-11 10:30"));
        bookings.enqueue(markCheckedOut(new Booking("20260013", findGuest("G017"), findRoom("304"), "2026-07-06 16:00", 1980.00, "PAID"), "2026-07-09 12:15"));
        bookings.enqueue(markCheckedOut(new Booking("20260014", findGuest("G020"), findRoom("305"), "2026-07-07 09:45", 2250.00, "UNPAID"), "2026-07-10 10:05"));

        // CONFIRMED bookings (approved, guest NOT yet checked in). These let
        // the Front-Desk demonstrate Check-in and Cancel on their own, without
        // first running the Walk-In module. The 5-argument constructor is used
        // with a null check-in time, so each one is CONFIRMED. Confirmation
        // numbers continue after the records above (20260015-20260034); Walk-In's
        // counter (bookingList size + 1) resumes after these, so runtime bookings
        // never collide. Rooms are ones NOT occupied by the ACTIVE bookings above.
        bookings.enqueue(withBilling(new Booking("20260015", findGuest("G001"), findRoom("101"), "2026-07-20 10:00", null),  450.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260016", findGuest("G002"), findRoom("102"), "2026-07-21 11:30", null),  480.00, "PARTIAL"));
        bookings.enqueue(withBilling(new Booking("20260017", findGuest("G003"), findRoom("104"), "2026-07-21 14:05", null),  450.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260018", findGuest("G004"), findRoom("105"), "2026-07-22 09:15", null),  500.00, "PAID"));
        bookings.enqueue(withBilling(new Booking("20260019", findGuest("G005"), findRoom("107"), "2026-07-22 16:40", null),  460.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260020", findGuest("G006"), findRoom("108"), "2026-07-23 10:00", null),  450.00, "PARTIAL"));
        bookings.enqueue(withBilling(new Booking("20260021", findGuest("G007"), findRoom("201"), "2026-07-23 13:20", null),  950.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260022", findGuest("G008"), findRoom("203"), "2026-07-24 12:00", null),  980.00, "PAID"));
        bookings.enqueue(withBilling(new Booking("20260023", findGuest("G009"), findRoom("204"), "2026-07-24 18:30", null),  950.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260024", findGuest("G010"), findRoom("206"), "2026-07-25 08:45", null), 1020.00, "PARTIAL"));
        bookings.enqueue(withBilling(new Booking("20260025", findGuest("G011"), findRoom("207"), "2026-07-25 15:10", null),  950.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260026", findGuest("G012"), findRoom("301"), "2026-07-26 11:00", null), 1850.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260027", findGuest("G013"), findRoom("304"), "2026-07-26 17:25", null), 1900.00, "PARTIAL"));
        bookings.enqueue(withBilling(new Booking("20260028", findGuest("G014"), findRoom("305"), "2026-07-27 10:30", null), 1850.00, "PAID"));
        bookings.enqueue(withBilling(new Booking("20260029", findGuest("G015"), findRoom("102"), "2026-07-27 14:50", null),  470.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260030", findGuest("G016"), findRoom("104"), "2026-07-28 09:05", null),  450.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260031", findGuest("G017"), findRoom("105"), "2026-07-28 16:15", null),  490.00, "PARTIAL"));
        bookings.enqueue(withBilling(new Booking("20260032", findGuest("G018"), findRoom("201"), "2026-07-29 12:40", null),  950.00, "UNPAID"));
        bookings.enqueue(withBilling(new Booking("20260033", findGuest("G019"), findRoom("203"), "2026-07-29 19:00", null),  970.00, "PAID"));
        bookings.enqueue(withBilling(new Booking("20260034", findGuest("G020"), findRoom("301"), "2026-07-30 10:20", null), 1880.00, "UNPAID"));

        return bookings;
    }

    /** Sets amount + payment on a freshly built CONFIRMED booking before storing. */
    private Booking withBilling(Booking booking, double amount, String paymentStatus) {
        booking.setAmount(amount);
        booking.setPaymentStatus(paymentStatus);
        return booking;
    }

    /** Linear lookup of a sample guest by id, so a Booking links a real Guest. */
    private Guest findGuest(String guestId) {
        for (int i = 0; i < guests.getNumberOfEntries(); i++) {
            Guest g = guests.getEntry(i);
            if (g.getGuestId().equalsIgnoreCase(guestId)) {
                return g;
            }
        }
        return null;
    }

    /** Linear lookup of a sample room by room number, so a Booking links a real Room. */
    private Room findRoom(String roomNumber) {
        for (int i = 0; i < rooms.getNumberOfEntries(); i++) {
            Room r = rooms.getEntry(i);
            if (r.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return r;
            }
        }
        return null;
    }

    /** Helper to flip a freshly built booking to CHECKED_OUT before storing. */
    private Booking markCheckedOut(Booking booking, String checkOutTime) {
        booking.setStatus("CHECKED_OUT");
        booking.setCheckOutTime(checkOutTime);
        return booking;
    }
}
