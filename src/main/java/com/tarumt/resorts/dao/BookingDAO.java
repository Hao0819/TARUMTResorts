package com.tarumt.resorts.dao;

import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

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

        private static final DateTimeFormatter BOOKING_TIME_FORMAT =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        private ListQueueInterface<Guest> guests;
        private ListQueueInterface<Room> rooms;

        /**
         * Applies hard-coded stay schedules to the sample bookings in the same
         * order in which they were added to the shared booking collection.
         */
        private void applySampleSchedules(
                        ListQueueInterface<Booking> bookings) {

                String[][] scheduleData = {
                                // Confirmation No., Scheduled Check-In, Stay Duration

                                // ACTIVE bookings: occupy 11 rooms from 20 to 27 August.
                                { "20260001", "2026-08-20", "7" },
                                { "20260002", "2026-08-20", "7" },
                                { "20260003", "2026-08-20", "7" },
                                { "20260004", "2026-08-20", "7" },
                                { "20260005", "2026-08-20", "7" },
                                { "20260006", "2026-08-20", "7" },

                                // CHECKED_OUT bookings
                                { "20260007", "2026-07-10", "2" },
                                { "20260008", "2026-07-11", "3" },
                                { "20260009", "2026-07-12", "3" },
                                { "20260010", "2026-07-13", "1" },
                                { "20260011", "2026-07-08", "2" },
                                { "20260012", "2026-07-09", "2" },
                                { "20260013", "2026-07-06", "3" },
                                { "20260014", "2026-07-07", "3" },

                                // CONFIRMED bookings occupying the remaining original rooms.
                                { "20260015", "2026-08-24", "3" },
                                { "20260016", "2026-08-24", "3" },
                                { "20260017", "2026-08-24", "3" },
                                { "20260018", "2026-08-24", "3" },
                                { "20260019", "2026-08-24", "3" },
                                { "20260020", "2026-08-24", "3" },
                                { "20260021", "2026-08-24", "3" },
                                { "20260022", "2026-08-24", "3" },
                                { "20260023", "2026-08-24", "3" },
                                { "20260024", "2026-08-24", "3" },
                                { "20260025", "2026-08-24", "3" },
                                { "20260026", "2026-08-24", "3" },
                                { "20260027", "2026-08-24", "3" },
                                { "20260028", "2026-08-24", "3" },

                                // Later bookings begin after the ACTIVE bookings check out.
                                { "20260029", "2026-08-27", "2" },
                                { "20260030", "2026-08-27", "2" },
                                { "20260031", "2026-08-27", "2" },
                                { "20260032", "2026-08-27", "2" },
                                { "20260033", "2026-08-27", "2" },
                                { "20260034", "2026-08-27", "2" },

                                // Additional ACTIVE bookings for the expanded room inventory.
                                { "20260035", "2026-08-20", "7" },
                                { "20260036", "2026-08-20", "7" },
                                { "20260037", "2026-08-20", "7" },
                                { "20260038", "2026-08-20", "7" },
                                { "20260039", "2026-08-20", "7" },

                                // CONFIRMED bookings occupying the expanded inventory.
                                { "20260040", "2026-08-24", "3" },
                                { "20260041", "2026-08-24", "3" },
                                { "20260042", "2026-08-24", "3" },
                                { "20260043", "2026-08-24", "3" },
                                { "20260044", "2026-08-24", "3" },
                                { "20260045", "2026-08-24", "3" },
                                { "20260046", "2026-08-24", "3" },
                                { "20260047", "2026-08-24", "3" },
                                { "20260048", "2026-08-24", "3" },
                                { "20260049", "2026-08-24", "3" },
                                { "20260050", "2026-08-24", "3" },
                                { "20260051", "2026-08-24", "3" },
                                { "20260052", "2026-08-24", "3" },
                                { "20260053", "2026-08-24", "3" },
                                { "20260054", "2026-08-24", "3" },

                                // CANCELLED bookings
                                { "20260055", "2026-08-10", "3" },
                                { "20260056", "2026-08-10", "3" },
                                { "20260057", "2026-08-10", "3" },
                                { "20260058", "2026-08-10", "3" },
                                { "20260059", "2026-08-10", "3" },
                                { "20260060", "2026-08-10", "3" }
                };

                if (bookings.getNumberOfEntries() != scheduleData.length) {

                        throw new IllegalStateException(
                                        "Booking and schedule sample counts do not match.");
                }

                Iterator<Booking> bookingIterator = bookings.getIterator();

                int scheduleIndex = 0;

                while (bookingIterator.hasNext()) {
                        Booking booking = bookingIterator.next();

                        String expectedConfirmationNumber = scheduleData[scheduleIndex][0];

                        if (!booking.getConfirmationNumber()
                                        .equals(expectedConfirmationNumber)) {

                                throw new IllegalStateException(
                                                "Booking schedule order mismatch: "
                                                                + expectedConfirmationNumber);
                        }

                        LocalDate scheduledCheckInDate = LocalDate.parse(
                                        scheduleData[scheduleIndex][1]);

                        int stayDurationDays = Integer.parseInt(
                                        scheduleData[scheduleIndex][2]);

                        booking.setSchedule(
                                        scheduledCheckInDate,
                                        stayDurationDays);

                        scheduleIndex++;
                }
        }

        /**
         * Builds the sample bookings against the SAME shared Guest and Room
         * collections used by the rest of the system, so a Booking's Room is the
         * very same object queried by the availability feature. This prevents the
         * "stale room copy" problem where a check-out frees one copy of a room but
         * the availability view still reads a different copy.
         */
        public ListQueueInterface<Booking> getAllBookings(ListQueueInterface<Guest> sharedGuests,
                        ListQueueInterface<Room> sharedRooms) {
                this.guests = sharedGuests;
                this.rooms = sharedRooms;
                ListQueueInterface<Booking> bookings = new DoublyLinkedListQueue<>();

                // ACTIVE bookings - guests currently occupying the rooms marked
                // unavailable in RoomDAO (103, 106, 202, 205, 302, 303). These guests
                // are all ASSIGNED in WalkInRegistrationDAO, never WAITING, so nobody
                // is queuing for a room while already occupying one. Amount and payment
                // status give the Billing Summary Report real data.
                // ACTIVE bookings for rooms currently marked unavailable in RoomDAO.
                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260001",
                                                findGuest("G001"),
                                                findRoom("103"),
                                                "2026-08-20 08:00"),
                                "PAID"));

                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260002",
                                                findGuest("G002"),
                                                findRoom("106"),
                                                "2026-08-20 08:15"),
                                "PAID"));

                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260003",
                                                findGuest("G003"),
                                                findRoom("202"),
                                                "2026-08-20 08:30"),
                                "PAID"));

                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260004",
                                                findGuest("G004"),
                                                findRoom("205"),
                                                "2026-08-20 08:45"),
                                "PAID"));

                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260005",
                                                findGuest("G005"),
                                                findRoom("302"),
                                                "2026-08-20 09:00"),
                                "PAID"));

                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260006",
                                                findGuest("G006"),
                                                findRoom("303"),
                                                "2026-08-20 09:15"),
                                "PAID"));

                // CHECKED_OUT bookings - completed past stays in rooms that are free
                // again. A guest who checked out earlier may legitimately be WAITING in
                // today's walk-in queue (a returning guest), so these may reuse guests
                // from the waiting list.
                bookings.enqueue(markCheckedOut(
                                withPaymentStatus(
                                                new Booking(
                                                                "20260007",
                                                                findGuest("G005"),
                                                                findRoom("101"),
                                                                "2026-07-10 12:00"),
                                                "PAID"),
                                "2026-07-12 11:30"));
                bookings.enqueue(markCheckedOut(
                                withPaymentStatus(
                                                new Booking(
                                                                "20260008",
                                                                findGuest("G007"),
                                                                findRoom("201"),
                                                                "2026-07-11 14:00"),
                                                "PAID"),
                                "2026-07-14 10:15"));
                bookings.enqueue(markCheckedOut(
                                withPaymentStatus(
                                                new Booking(
                                                                "20260009",
                                                                findGuest("G014"),
                                                                findRoom("301"),
                                                                "2026-07-12 16:30"),
                                                "PAID"),
                                "2026-07-15 12:00"));
                bookings.enqueue(markCheckedOut(
                                withPaymentStatus(
                                                new Booking(
                                                                "20260010",
                                                                findGuest("G002"),
                                                                findRoom("104"),
                                                                "2026-07-13 10:45"),
                                                "PAID"),
                                "2026-07-14 09:40"));
                bookings.enqueue(markCheckedOut(
                                withPaymentStatus(
                                                new Booking(
                                                                "20260011",
                                                                findGuest("G009"),
                                                                findRoom("105"),
                                                                "2026-07-08 15:20"),
                                                "PAID"),
                                "2026-07-10 11:00"));
                bookings.enqueue(markCheckedOut(
                                withPaymentStatus(
                                                new Booking(
                                                                "20260012",
                                                                findGuest("G013"),
                                                                findRoom("203"),
                                                                "2026-07-09 13:10"),
                                                "PAID"),
                                "2026-07-11 10:30"));
                bookings.enqueue(markCheckedOut(
                                withPaymentStatus(
                                                new Booking(
                                                                "20260013",
                                                                findGuest("G017"),
                                                                findRoom("304"),
                                                                "2026-07-06 16:00"),
                                                "PAID"),
                                "2026-07-09 12:15"));
                bookings.enqueue(markCheckedOut(
                                withPaymentStatus(
                                                new Booking(
                                                                "20260014",
                                                                findGuest("G020"),
                                                                findRoom("305"),
                                                                "2026-07-07 09:45"),
                                                "PAID"),
                                "2026-07-10 10:05"));

                // CONFIRMED bookings (approved, guest NOT yet checked in). These let
                // the Front-Desk demonstrate Check-in and Cancel on their own, without
                // first running the Walk-In module. The 5-argument constructor is used
                // with a null check-in time, so each one is CONFIRMED. Confirmation
                // numbers continue after the records above (20260015-20260034); Walk-In's
                // counter (bookingList size + 1) resumes after these, so runtime bookings
                // never collide. Rooms are ones NOT occupied by the ACTIVE bookings above.
                bookings.enqueue(withPaymentStatus(new Booking("20260015", findGuest("G012"), findRoom("101"),
                                "2026-07-20 10:00", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260016", findGuest("G013"), findRoom("102"),
                                "2026-08-18 10:35", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260017", findGuest("G014"), findRoom("104"),
                                "2026-07-21 14:05", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(
                                new Booking("20260018", findGuest("G015"), findRoom("105"), "2026-08-19 12:20", null),
                                "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260019", findGuest("G016"), findRoom("107"),
                                "2026-07-22 16:40", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260020", findGuest("G017"), findRoom("108"),
                                "2026-07-23 10:00", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260021", findGuest("G017"), findRoom("201"),
                                "2026-07-23 13:20", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(
                                new Booking("20260022", findGuest("G018"), findRoom("203"), "2026-08-19 14:35", null),
                                "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260023", findGuest("G019"), findRoom("204"),
                                "2026-07-24 18:30", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260024", findGuest("G021"), findRoom("206"),
                                "2026-07-25 08:45", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260025", findGuest("G022"), findRoom("207"),
                                "2026-07-25 15:10", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260026", findGuest("G023"), findRoom("301"),
                                "2026-07-26 11:00", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260027", findGuest("G024"), findRoom("304"),
                                "2026-07-26 17:25", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking("20260028", findGuest("G025"), findRoom("305"),
                                "2026-07-27 10:30", null), "UNPAID"));
                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260029",
                                findGuest("G015"),
                                findRoom("103"),
                                "2026-07-27 14:50",
                                null),
                                "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260030",
                                findGuest("G016"),
                                findRoom("106"),
                                "2026-07-28 09:05",
                                null),
                                "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260031",
                                findGuest("G017"),
                                findRoom("302"),
                                "2026-07-28 16:15",
                                null),
                                "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260032",
                                findGuest("G018"),
                                findRoom("202"),
                                "2026-07-29 12:40",
                                null),
                                "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260033",
                                findGuest("G019"),
                                findRoom("205"),
                                "2026-07-29 19:00",
                                null),
                                "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260034",
                                findGuest("G020"),
                                findRoom("303"),
                                "2026-08-18 14:05",
                                null),
                                "UNPAID"));
                // Additional ACTIVE bookings for the expanded room inventory.
                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260035",
                                                findGuest("G007"),
                                                findRoom("111"),
                                                "2026-08-20 09:30"),
                                "PAID"));

                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260036",
                                                findGuest("G008"),
                                                findRoom("115"),
                                                "2026-08-20 09:45"),
                                "PAID"));

                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260037",
                                                findGuest("G009"),
                                                findRoom("209"),
                                                "2026-08-20 10:00"),
                                "PAID"));

                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260038",
                                                findGuest("G010"),
                                                findRoom("213"),
                                                "2026-08-20 10:15"),
                                "PAID"));

                bookings.enqueue(withPaymentStatus(
                                new Booking(
                                                "20260039",
                                                findGuest("G011"),
                                                findRoom("307"),
                                                "2026-08-20 10:30"),
                                "PAID"));

                // CONFIRMED bookings for the additional available rooms.
                // These bookings complete the fully-booked period from 10-12 August.

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260040", findGuest("G026"), findRoom("109"),
                                "2026-08-01 08:00", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260041", findGuest("G027"), findRoom("110"),
                                "2026-08-01 08:15", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260042", findGuest("G028"), findRoom("112"),
                                "2026-08-01 08:30", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260043", findGuest("G029"), findRoom("113"),
                                "2026-08-01 08:45", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260044", findGuest("G030"), findRoom("114"),
                                "2026-08-01 09:00", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260045", findGuest("G031"), findRoom("116"),
                                "2026-08-01 09:15", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260046", findGuest("G032"), findRoom("208"),
                                "2026-08-01 09:30", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260047", findGuest("G033"), findRoom("210"),
                                "2026-08-01 09:45", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260048", findGuest("G034"), findRoom("211"),
                                "2026-08-01 10:00", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260049", findGuest("G035"), findRoom("212"),
                                "2026-08-01 10:15", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260050", findGuest("G036"), findRoom("214"),
                                "2026-08-01 10:30", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260051", findGuest("G037"), findRoom("306"),
                                "2026-08-01 10:45", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260052", findGuest("G038"), findRoom("308"),
                                "2026-08-01 11:00", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260053", findGuest("G039"), findRoom("309"),
                                "2026-08-01 11:15", null), "UNPAID"));

                bookings.enqueue(withPaymentStatus(new Booking(
                                "20260054", findGuest("G040"), findRoom("310"),
                                "2026-08-01 11:30", null), "UNPAID"));

                // CANCELLED bookings remain in history but do not block room schedules.
                bookings.enqueue(markCancelled(withPaymentStatus(new Booking(
                                "20260055", findGuest("G001"), findRoom("109"),
                                "2026-07-31 09:00", null), "UNPAID")));

                bookings.enqueue(markCancelled(withPaymentStatus(new Booking(
                                "20260056", findGuest("G002"), findRoom("208"),
                                "2026-07-31 09:15", null), "UNPAID")));

                bookings.enqueue(markCancelled(withPaymentStatus(new Booking(
                                "20260057", findGuest("G003"), findRoom("306"),
                                "2026-07-31 09:30", null), "UNPAID")));

                bookings.enqueue(markCancelled(withPaymentStatus(new Booking(
                                "20260058", findGuest("G004"), findRoom("110"),
                                "2026-07-31 09:45", null), "UNPAID")));

                bookings.enqueue(markCancelled(withPaymentStatus(new Booking(
                                "20260059", findGuest("G005"), findRoom("210"),
                                "2026-07-31 10:00", null), "UNPAID")));

                bookings.enqueue(markCancelled(withPaymentStatus(new Booking(
                                "20260060", findGuest("G006"), findRoom("308"),
                                "2026-07-31 10:15", null), "UNPAID")));

                // Add planned stay dates and recalculate each booking amount.
                applySampleSchedules(bookings);
                addHistoricalLoyaltyBookings(bookings);
                return bookings;
        }

        /**
         * Adds verified historical stays used by the Loyalty opening ledger.
         * Every point batch therefore references a real shared Booking rather
         * than an unlinked ADJUST transaction.
         */
        private void addHistoricalLoyaltyBookings(
                        ListQueueInterface<Booking> bookings) {

                int[] nextBookingNumber = {1001};

                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G001", 500, 36);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G016", 800, 34);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G002", 1800, 55);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G002", 3000, 20);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G003", 7500, 48);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G004", 12000, 42);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G006", 23000, 18);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G005", 15000, 33);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G010", 12000, 46);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G011", 17500, 29);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G012", 23000, 24);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G008", 1200, 53);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G008", 2000, 22);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G009", 3000, 51);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G009", 4100, 27);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G014", 700, 49);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G014", 1000, 21);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G017", 700, 47);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G017", 1000, 19);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G015", 3000, 54);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G015", 4500, 26);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G018", 3000, 52);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G018", 4500, 25);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G019", 1200, 45);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G019", 2000, 17);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G020", 2500, 50);
                addHistoricalEarnedPoints(bookings, nextBookingNumber,
                                "G020", 3500, 23);
        }

        /** Splits one verified total into several independently dated stays. */
        private void addHistoricalEarnedPoints(
                        ListQueueInterface<Booking> bookings,
                        int[] nextBookingNumber,
                        String guestId,
                        int totalPoints,
                        int latestStayDaysAgo) {

                if (totalPoints >= 2000) {
                        int first = totalPoints * 40 / 100;
                        int second = totalPoints * 30 / 100;
                        int third = totalPoints - first - second;
                        addHistoricalLoyaltyBooking(bookings, nextBookingNumber,
                                        guestId, first, latestStayDaysAgo + 28);
                        addHistoricalLoyaltyBooking(bookings, nextBookingNumber,
                                        guestId, second, latestStayDaysAgo + 14);
                        addHistoricalLoyaltyBooking(bookings, nextBookingNumber,
                                        guestId, third, latestStayDaysAgo);
                } else {
                        int first = totalPoints / 2;
                        int second = totalPoints - first;
                        addHistoricalLoyaltyBooking(bookings, nextBookingNumber,
                                        guestId, first, latestStayDaysAgo + 14);
                        addHistoricalLoyaltyBooking(bookings, nextBookingNumber,
                                        guestId, second, latestStayDaysAgo);
                }
        }

        /** Creates one real PAID and CHECKED_OUT historical Booking. */
        private void addHistoricalLoyaltyBooking(
                        ListQueueInterface<Booking> bookings,
                        int[] nextBookingNumber,
                        String guestId,
                        int points,
                        int stayDaysAgo) {

                LocalDateTime checkOutTime = LocalDateTime.now()
                                .minusDays(stayDaysAgo)
                                .withHour(11)
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0);
                LocalDate checkInDate = checkOutTime.toLocalDate().minusDays(1);
                String confirmationNumber = String.format(
                                "%04d%04d",
                                checkOutTime.getYear(),
                                nextBookingNumber[0]++);
                Booking booking = new Booking(
                                confirmationNumber,
                                findGuest(guestId),
                                findRoom("101"),
                                checkInDate.atTime(14, 0)
                                                .format(BOOKING_TIME_FORMAT));

                booking.setSchedule(checkInDate, 1);
                booking.setHistoricalAmount(points);
                booking.setPaymentStatus("PAID");
                booking.setStatus("CHECKED_OUT");
                booking.setCheckOutTime(
                                checkOutTime.format(BOOKING_TIME_FORMAT));
                bookings.enqueue(booking);
        }

        /**
         * Sets the payment status for a sample Booking.
         * The amount is calculated from the Room rate and stay duration.
         */
        private Booking withPaymentStatus(
                        Booking booking,
                        String paymentStatus) {

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

        /**
         * Linear lookup of a sample room by room number, so a Booking links a real
         * Room.
         */
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

        /**
         * Marks a sample booking as cancelled while retaining it in history.
         */
        private Booking markCancelled(Booking booking) {
                booking.setStatus("CANCELLED");
                return booking;
        }
}
