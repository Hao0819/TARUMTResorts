/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.dao;

import com.tarumt.resorts.adt.Queue;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.WalkInRegistration;

/**
 * Provides hard-coded Walk-In registration records for reporting.
 * The registrations reuse Guest objects from the shared Guest Queue.
 * 
 * @author junha
 */
public class WalkInRegistrationDAO {

    public Queue<WalkInRegistration> getAllRegistrations(Queue<Guest> sharedGuests) {

        Queue<WalkInRegistration> registrationHistory = new Queue<>();

        String[][] sampleData = { 
                // Reg ID, Guest ID, Registration Time, Room Type, Status
                { "WR0001", "G001", "2026-07-20 07:50", "Standard", "ASSIGNED" },
                { "WR0002", "G002", "2026-07-20 08:15", "Standard", "WAITING" },
                { "WR0003", "G003", "2026-07-20 08:45", "Standard", "ASSIGNED" },
                { "WR0004", "G004", "2026-07-20 09:05", "Standard", "WAITING" },
                { "WR0005", "G005", "2026-07-20 09:30", "Standard", "ASSIGNED" },
                { "WR0006", "G006", "2026-07-20 10:20", "Standard", "WAITING" },
                { "WR0007", "G007", "2026-07-20 10:55", "Standard", "ASSIGNED" },
                { "WR0008", "G008", "2026-07-20 11:10", "Standard", "WAITING" },

                { "WR0009", "G009", "2026-07-20 11:45", "Deluxe", "WAITING" },
                { "WR0010", "G010", "2026-07-20 12:10", "Deluxe", "ASSIGNED" },
                { "WR0011", "G011", "2026-07-20 12:30", "Deluxe", "WAITING" },
                { "WR0012", "G012", "2026-07-20 13:05", "Deluxe", "ASSIGNED" },
                { "WR0013", "G013", "2026-07-20 13:40", "Deluxe", "WAITING" },
                { "WR0014", "G014", "2026-07-20 14:10", "Deluxe", "ASSIGNED" },
                { "WR0015", "G015", "2026-07-20 14:50", "Deluxe", "WAITING" },

                { "WR0016", "G016", "2026-07-20 15:25", "Suite", "ASSIGNED" },
                { "WR0017", "G017", "2026-07-20 15:35", "Suite", "WAITING" },
                { "WR0018", "G018", "2026-07-20 16:05", "Suite", "WAITING" },
                { "WR0019", "G019", "2026-07-20 16:40", "Suite", "ASSIGNED" },
                { "WR0020", "G020", "2026-07-20 17:15", "Suite", "WAITING" }
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

            WalkInRegistration registrationRecord = new WalkInRegistration(registrationId, guest, registrationTime,
                    roomType);

            // The constructor defaults to WAITING, so update sample history
            // records that have already completed allocation.
            registrationRecord.setStatus(status);
            registrationHistory.enqueue(registrationRecord);

        }

        return registrationHistory;

    }

    private Guest findGuestById(Queue<Guest> guests, String guestId) {
        int total = guests.getNumberOfEntries();

        for (int i = 0; i < total; i++) {
            Guest guest = guests.getEntry(i);

            if (guest.getGuestId().equalsIgnoreCase(guestId)) {
                return guest;
            }
        }
        return null;

    }

}
