/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.dao;

import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.WalkInRegistration;
import java.time.LocalDate;

// Hard-coded Walk-In registration records for reporting, reusing Guest objects from the shared Guest Queue.
// @author LimJunHao
public class WalkInRegistrationDAO {

        public ListQueueInterface<WalkInRegistration> getAllRegistrations(
                        ListQueueInterface<Guest> sharedGuests) {

                // Interface reference with a doubly linked implementation.
                ListQueueInterface<WalkInRegistration> registrationHistory = new DoublyLinkedListQueue<>();

                String[][] sampleData = {
                                // Reg ID, Guest ID, Registration Time, Room Type, Status,
                                // Requested Check-In, Stay Duration, Booking Confirmation

                                { "WR0001", "G001", "2026-08-10 07:50", "Standard",
                                                "ASSIGNED", "2026-08-20", "7", "20260001" },

                                { "WR0002", "G003", "2026-08-10 08:15", "Deluxe",
                                                "ASSIGNED", "2026-08-20", "7", "20260003" },

                                { "WR0003", "G008", "2026-08-10 09:00", "Suite",
                                                "CANCELLED", "2026-08-22", "2", "" },

                                { "WR0004", "G013", "2026-08-10 10:30", "Standard",
                                                "ASSIGNED", "2026-08-24", "3", "20260016" },

                                { "WR0005", "G016", "2026-08-10 12:00", "Deluxe",
                                                "CANCELLED", "2026-08-24", "3", "" },

                                { "WR0006", "G020", "2026-08-10 14:00", "Suite",
                                                "ASSIGNED", "2026-08-27", "2", "20260034" },

                                { "WR0007", "G007", "2026-08-11 08:10", "Standard",
                                                "ASSIGNED", "2026-08-20", "7", "20260035" },

                                { "WR0008", "G009", "2026-08-11 09:20", "Deluxe",
                                                "ASSIGNED", "2026-08-20", "7", "20260037" },

                                { "WR0009", "G014", "2026-08-11 10:40", "Suite",
                                                "CANCELLED", "2026-08-24", "1", "" },

                                { "WR0010", "G015", "2026-08-11 12:15", "Standard",
                                                "ASSIGNED", "2026-08-24", "3", "20260018" },

                                { "WR0011", "G018", "2026-08-11 14:30", "Deluxe",
                                                "ASSIGNED", "2026-08-24", "3", "20260022" },

                                { "WR0012", "G019", "2026-08-11 16:00", "Suite",
                                                "CANCELLED", "2026-08-25", "3", "" },

                                { "WR0013", "G002", "2026-08-12 08:00", "Suite",
                                                "WAITING", "2026-08-25", "1", "" },

                                { "WR0014", "G003", "2026-08-12 08:30", "Deluxe",
                                                "WAITING", "2026-08-28", "3", "" },

                                { "WR0015", "G008", "2026-08-12 09:00", "Suite",
                                                "WAITING", "2026-08-29", "2", "" },

                                { "WR0016", "G013", "2026-08-12 09:30", "Standard",
                                                "WAITING", "2026-08-30", "1", "" },

                                { "WR0017", "G016", "2026-08-12 10:00", "Deluxe",
                                                "WAITING", "2026-08-31", "2", "" },

                                { "WR0018", "G017", "2026-08-12 10:30", "Suite",
                                                "WAITING", "2026-09-01", "3", "" },

                                { "WR0019", "G019", "2026-08-12 11:00", "Standard",
                                                "WAITING", "2026-09-02", "2", "" },

                                { "WR0020", "G020", "2026-08-12 11:30", "Deluxe",
                                                "WAITING", "2026-09-03", "1", "" }
                };

                for (int i = 0; i < sampleData.length; i++) {
                        String registrationId = sampleData[i][0];
                        String guestId = sampleData[i][1];
                        String registrationTime = sampleData[i][2];
                        String roomType = sampleData[i][3];
                        String status = sampleData[i][4];
                        String bookingConfirmationNumber = sampleData[i][7];

                        Guest guest = findGuestById(sharedGuests, guestId);

                        if (guest == null) {
                                throw new IllegalStateException("Sample Guest not found : " + guestId);
                        }

                        // Priority-tier guests must be handled by the VIP allocation module.
                        if (guest.getMembershipTier().isPriorityTier()) {
                                throw new IllegalStateException(
                                                "Priority-tier Guest cannot enter Standard history: "
                                                                + guestId
                                                                + " ("
                                                                + guest.getMembershipTier()
                                                                + ")");
                        }

                        LocalDate requestedCheckInDate = LocalDate.parse(sampleData[i][5]);

                        int stayDurationDays = Integer.parseInt(sampleData[i][6]);

                        WalkInRegistration registrationRecord = new WalkInRegistration(
                                        registrationId,
                                        guest,
                                        registrationTime,
                                        roomType,
                                        requestedCheckInDate,
                                        stayDurationDays);

                        registrationRecord.setStatus(status);

                        // Only ASSIGNED registrations have created Bookings.
                        if (!bookingConfirmationNumber.isEmpty()) {
                                registrationRecord.setBookingConfirmationNumber(
                                                bookingConfirmationNumber);
                        }

                        registrationHistory.enqueue(registrationRecord);

                }

                return registrationHistory;

        }

        // Finds a shared Guest by exact Guest ID, so each sample registration links to a real Guest.
        private Guest findGuestById(
                        ListQueueInterface<Guest> guests,
                        String guestId) {

                return guests.searchByKey(
                                guestId,
                                guest -> guest.getGuestId());
        }

}
