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

/**
 * Provides hard-coded Walk-In registration records for reporting.
 * The registrations reuse Guest objects from the shared Guest Queue.
 * 
 * @author JunHao
 */
public class WalkInRegistrationDAO {

    public ListQueueInterface<WalkInRegistration> getAllRegistrations(
            ListQueueInterface<Guest> sharedGuests) {

        // Interface reference with a doubly linked implementation.
        ListQueueInterface<WalkInRegistration> registrationHistory = new DoublyLinkedListQueue<>();

        String[][] sampleData = {
                // Reg ID, Guest ID, Registration Time, Room Type, Status,
                // Requested Check-In Date, Stay Duration

                { "WR0001", "G001", "2026-07-18 07:50", "Standard",
                        "ASSIGNED", "2026-07-18", "2" },
                { "WR0002", "G003", "2026-07-18 08:15", "Deluxe",
                        "ASSIGNED", "2026-07-19", "3" },
                { "WR0003", "G008", "2026-07-18 09:00", "Suite",
                        "CANCELLED", "2026-07-20", "2" },
                { "WR0004", "G013", "2026-07-18 10:30", "Standard",
                        "ASSIGNED", "2026-07-20", "1" },
                { "WR0005", "G016", "2026-07-18 12:00", "Deluxe",
                        "CANCELLED", "2026-07-21", "3" },
                { "WR0006", "G020", "2026-07-18 14:00", "Suite",
                        "ASSIGNED", "2026-07-21", "2" },

                { "WR0007", "G007", "2026-07-19 08:10", "Standard",
                        "ASSIGNED", "2026-07-22", "2" },
                { "WR0008", "G009", "2026-07-19 09:20", "Deluxe",
                        "ASSIGNED", "2026-07-22", "3" },
                { "WR0009", "G014", "2026-07-19 10:40", "Suite",
                        "CANCELLED", "2026-07-23", "1" },
                { "WR0010", "G015", "2026-07-19 12:15", "Standard",
                        "ASSIGNED", "2026-07-23", "2" },
                { "WR0011", "G018", "2026-07-19 14:30", "Deluxe",
                        "ASSIGNED", "2026-07-24", "2" },
                { "WR0012", "G019", "2026-07-19 16:00", "Suite",
                        "CANCELLED", "2026-07-24", "3" },

                { "WR0013", "G002", "2026-07-20 08:00", "Standard",
                        "WAITING", "2026-08-13", "2" },
                { "WR0014", "G003", "2026-07-20 08:30", "Deluxe",
                        "WAITING", "2026-08-11", "3" },
                { "WR0015", "G008", "2026-07-20 09:00", "Suite",
                        "WAITING", "2026-08-12", "2" },
                { "WR0016", "G013", "2026-07-20 09:30", "Standard",
                        "WAITING", "2026-08-13", "1" },
                { "WR0017", "G016", "2026-07-20 10:00", "Deluxe",
                        "WAITING", "2026-08-14", "2" },
                { "WR0018", "G017", "2026-07-20 10:30", "Suite",
                        "WAITING", "2026-08-15", "3" },
                { "WR0019", "G019", "2026-07-20 11:00", "Standard",
                        "WAITING", "2026-08-16", "2" },
                { "WR0020", "G020", "2026-07-20 11:30", "Deluxe",
                        "WAITING", "2026-08-17", "1" }
        };

        for (int i = 0; i < sampleData.length; i++) {
            String registrationId = sampleData[i][0];
            String guestId = sampleData[i][1];
            String registrationTime = sampleData[i][2];
            String roomType = sampleData[i][3];
            String status = sampleData[i][4];

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

            // The constructor defaults to WAITING, so update sample history
            // records that have already completed allocation.
            registrationRecord.setStatus(status);
            registrationHistory.enqueue(registrationRecord);

        }

        return registrationHistory;

    }

    private Guest findGuestById(
            ListQueueInterface<Guest> guests,
            String guestId) {

        // Search the linked entries directly using Guest ID as the key.
        return guests.searchByKey(
                guestId,
                guest -> guest.getGuestId());
    }

}
