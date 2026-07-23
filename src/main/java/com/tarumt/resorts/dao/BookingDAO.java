package com.tarumt.resorts.dao;

import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;

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

    private DoublyLinkedListQueue<Guest> guests;
    private DoublyLinkedListQueue<Room> rooms;

    /**
     * Builds the sample bookings against the SAME shared Guest and Room
     * collections used by the rest of the system, so a Booking's Room is the
     * very same object queried by the availability feature. This prevents the
     * "stale room copy" problem where a check-out frees one copy of a room but
     * the availability view still reads a different copy.
     */
    public DoublyLinkedListQueue<Booking> getAllBookings(DoublyLinkedListQueue<Guest> sharedGuests, DoublyLinkedListQueue<Room> sharedRooms) {
        this.guests = sharedGuests;
        this.rooms = sharedRooms;
        DoublyLinkedListQueue<Booking> bookings = new DoublyLinkedListQueue<>();

        // ACTIVE bookings - guests currently occupying rooms marked
        // unavailable in RoomDAO (103, 106, 202, 205, 302, 303).
        // Amount and payment status give the Billing Summary Report real data.
        bookings.enqueue(new Booking("20260001", findGuest("G001"), findRoom("103"), "2026-07-15 13:20", 450.00, "UNPAID"));
        bookings.enqueue(new Booking("20260002", findGuest("G004"), findRoom("202"), "2026-07-16 15:45", 980.00, "PARTIAL"));
        bookings.enqueue(new Booking("20260003", findGuest("G002"), findRoom("106"), "2026-07-17 11:10", 420.00, "PAID"));
        bookings.enqueue(new Booking("20260004", findGuest("G008"), findRoom("302"), "2026-07-17 18:30", 2100.00, "UNPAID"));
        bookings.enqueue(new Booking("20260005", findGuest("G011"), findRoom("205"), "2026-07-18 09:05", 1150.00, "UNPAID"));
        bookings.enqueue(new Booking("20260006", findGuest("G014"), findRoom("303"), "2026-07-19 20:15", 1875.00, "PARTIAL"));

        // CHECKED_OUT bookings - past stays kept so reports have a mix of
        // statuses to filter and sort on.
        bookings.enqueue(markCheckedOut(new Booking("20260007", findGuest("G003"), findRoom("101"), "2026-07-10 12:00", 380.00, "PAID"), "2026-07-12 11:30"));
        bookings.enqueue(markCheckedOut(new Booking("20260008", findGuest("G005"), findRoom("201"), "2026-07-11 14:00", 890.00, "PAID"), "2026-07-14 10:15"));
        bookings.enqueue(markCheckedOut(new Booking("20260009", findGuest("G007"), findRoom("301"), "2026-07-12 16:30", 1650.00, "PAID"), "2026-07-15 12:00"));
        bookings.enqueue(markCheckedOut(new Booking("20260010", findGuest("G010"), findRoom("104"), "2026-07-13 10:45", 300.00, "UNPAID"), "2026-07-14 09:40"));

        return bookings;
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
