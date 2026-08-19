package com.tarumt.resorts.util;

import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Room;

import java.time.LocalDate;
import java.util.Iterator;

/**
 * Single shared source for the "is this room free for this date range"
 * schedule-overlap rule, used by Walk-In, VIP, and Front-Desk so all three
 * modules agree on the same room/date combinations.
 *
 * A room is blocked for a requested [checkIn, checkOut) range when it has a
 * CONFIRMED or ACTIVE booking whose own [scheduledCheckIn, scheduledCheckOut)
 * range overlaps it. The blocked range additionally extends
 * HOUSEKEEPING_TURNAROUND_DAYS past each existing booking's scheduled
 * check-out date, reserving guaranteed time for Housekeeping to complete the
 * DIRTY -> CLEANING -> INSPECTED -> READY cycle before the next guest is due.
 *
 * This does not replace each module's own same-day operational check (Room
 * isAvailable() + cleaningStatus == READY/UNKNOWN) - that still applies
 * separately for immediate/today check-ins. This utility only decides which
 * room/date combinations may be scheduled at all.
 */
public final class RoomScheduleAvailability {

    public static final int HOUSEKEEPING_TURNAROUND_DAYS = 1;

    private RoomScheduleAvailability() {
    }

    /**
     * True if the room has no CONFIRMED/ACTIVE booking overlapping the
     * requested [requestedCheckInDate, requestedCheckOutDate) range, once
     * the housekeeping turnaround buffer is applied.
     */
    public static boolean isAvailable(
            ListQueueInterface<Booking> bookingList,
            Room room,
            LocalDate requestedCheckInDate,
            LocalDate requestedCheckOutDate) {

        if (bookingList == null
                || room == null
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
                            .equalsIgnoreCase(room.getRoomNumber());

            boolean blocksSchedule = "CONFIRMED".equalsIgnoreCase(existingBooking.getStatus())
                    || "ACTIVE".equalsIgnoreCase(existingBooking.getStatus());

            LocalDate existingCheckInDate = existingBooking.getScheduledCheckInDate();
            LocalDate existingCheckOutDate = existingBooking.getScheduledCheckOutDate();

            if (sameRoom
                    && blocksSchedule
                    && existingCheckInDate != null
                    && existingCheckOutDate != null) {

                LocalDate blockedUntilDate = existingCheckOutDate.plusDays(
                        HOUSEKEEPING_TURNAROUND_DAYS);

                boolean datesOverlap = requestedCheckInDate.isBefore(blockedUntilDate)
                        && requestedCheckOutDate.isAfter(existingCheckInDate);

                if (datesOverlap) {
                    return false;
                }
            }
        }

        return true;
    }
}
