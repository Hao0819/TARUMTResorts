package com.tarumt.resorts.dao;

import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.VIPAllocationRequest;

import java.time.LocalDate;

/**
 * VIPAllocationDAO.java
 * Provides hard-coded VIP Allocation request history for the VIP &
 * Loyalty Tier Priority Room Allocation module. Reuses Guest objects
 * from the shared Guest Queue so IDs stay consistent across modules.
 *
 * @author brian
 */
public class VIPAllocationDAO {

    public ListQueueInterface<VIPAllocationRequest> getAllRequests(
            ListQueueInterface<Guest> sharedGuests) {

        ListQueueInterface<VIPAllocationRequest> requestHistory =
                new DoublyLinkedListQueue<>();

        String[][] sampleData = {
            // Req ID,   Guest ID, Request Time,        Room Type,  Status,      Check-In,     Nights
            { "VR0001", "G006", "2026-07-20 08:05", "Suite",    "ASSIGNED",  "2026-07-22", "2" },
            { "VR0002", "G011", "2026-07-20 08:20", "Deluxe",   "ASSIGNED",  "2026-07-23", "3" },
            { "VR0003", "G004", "2026-07-20 09:00", "Standard", "CANCELLED", "2026-07-25", "2" },
            { "VR0004", "G012", "2026-07-20 09:30", "Suite",    "WAITING",   "2026-08-18", "3" },
            { "VR0005", "G005", "2026-07-20 09:45", "Deluxe",   "WAITING",   "2026-08-19", "2" },
            { "VR0006", "G010", "2026-07-20 10:00", "Standard", "WAITING",   "2026-08-20", "2" },
            { "VR0007", "G004", "2026-07-20 10:15", "Deluxe",   "WAITING",   "2026-08-21", "1" }
        };

        for (int i = 0; i < sampleData.length; i++) {
            String requestId = sampleData[i][0];
            String guestId = sampleData[i][1];
            String requestTime = sampleData[i][2];
            String roomType = sampleData[i][3];
            String status = sampleData[i][4];
            LocalDate checkInDate = LocalDate.parse(sampleData[i][5]);
            int stayDurationDays = Integer.parseInt(sampleData[i][6]);

            Guest guest = findGuestById(sharedGuests, guestId);
            if (guest == null) {
                throw new IllegalStateException(
                        "Sample Guest not found: " + guestId);
            }
            if (!guest.getMembershipTier().isPriorityTier()) {
                throw new IllegalStateException(
                        "Non-priority Guest cannot enter VIP history: "
                        + guestId + " (" + guest.getMembershipTier() + ")");
            }

            VIPAllocationRequest request = new VIPAllocationRequest(
                    requestId, guest, requestTime, roomType,
                    checkInDate, stayDurationDays);

            // Constructor defaults to WAITING, so override for
            // sample history records that already completed/cancelled.
            request.setStatus(status);
            requestHistory.enqueue(request);
        }
        return requestHistory;
    }

    private Guest findGuestById(
            ListQueueInterface<Guest> guests,
            String guestId) {
        return guests.searchByKey(
                guestId,
                guest -> guest.getGuestId());
    }
}