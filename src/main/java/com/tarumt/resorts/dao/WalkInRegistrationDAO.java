/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.dao;

import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.WalkInRegistration;

/**
 * Provides hard-coded Walk-In registration records for reporting.
 * The registrations reuse Guest objects from the shared Guest Queue.
 * 
 * @author junha
 */
public class WalkInRegistrationDAO {

    public DoublyLinkedListQueue<WalkInRegistration> getAllRegistrations(DoublyLinkedListQueue<Guest> sharedGuests) {

        DoublyLinkedListQueue<WalkInRegistration> registrationHistory = new DoublyLinkedListQueue<>();

        String[][] sampleData = {
                // Reg ID, Guest ID, Registration Time, Room Type, Status

                // Historical records from 18 July.
                { "WR0001", "G001", "2026-07-18 07:50", "Standard", "ASSIGNED" },
                { "WR0002", "G003", "2026-07-18 08:15", "Deluxe", "ASSIGNED" },
                { "WR0003", "G008", "2026-07-18 09:00", "Suite", "CANCELLED" },
                { "WR0004", "G013", "2026-07-18 10:30", "Standard", "ASSIGNED" },
                { "WR0005", "G016", "2026-07-18 12:00", "Deluxe", "CANCELLED" },
                { "WR0006", "G020", "2026-07-18 14:00", "Suite", "ASSIGNED" },

                // Historical records from 19 July.
                { "WR0007", "G007", "2026-07-19 08:10", "Standard", "ASSIGNED" },
                { "WR0008", "G009", "2026-07-19 09:20", "Deluxe", "ASSIGNED" },
                { "WR0009", "G014", "2026-07-19 10:40", "Suite", "CANCELLED" },
                { "WR0010", "G015", "2026-07-19 12:15", "Standard", "ASSIGNED" },
                { "WR0011", "G018", "2026-07-19 14:30", "Deluxe", "ASSIGNED" },
                { "WR0012", "G019", "2026-07-19 16:00", "Suite", "CANCELLED" },

                // Current active FIFO waiting records from 20 July.
                { "WR0013", "G002", "2026-07-20 08:00", "Standard", "WAITING" },
                { "WR0014", "G003", "2026-07-20 08:30", "Deluxe", "WAITING" },
                { "WR0015", "G008", "2026-07-20 09:00", "Suite", "WAITING" },
                { "WR0016", "G013", "2026-07-20 09:30", "Standard", "WAITING" },
                { "WR0017", "G016", "2026-07-20 10:00", "Deluxe", "WAITING" },
                { "WR0018", "G017", "2026-07-20 10:30", "Suite", "WAITING" },
                { "WR0019", "G019", "2026-07-20 11:00", "Standard", "WAITING" },
                { "WR0020", "G020", "2026-07-20 11:30", "Deluxe", "WAITING" }
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

            WalkInRegistration registrationRecord = new WalkInRegistration(registrationId, guest, registrationTime,
                    roomType);

            // The constructor defaults to WAITING, so update sample history
            // records that have already completed allocation.
            registrationRecord.setStatus(status);
            registrationHistory.enqueue(registrationRecord);

        }

        return registrationHistory;

    }

    private Guest findGuestById(
            DoublyLinkedListQueue<Guest> guests,
            String guestId) {

        // Search the linked nodes directly using Guest ID as the key.
        return guests.searchByKey(
                guestId,
                guest -> guest.getGuestId());
    }

}
