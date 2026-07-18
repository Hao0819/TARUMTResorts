/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.control;

import com.tarumt.resorts.adt.QueueInterface;
import com.tarumt.resorts.dao.WalkInDAO;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.WalkInRegistration;

/**
 * WalkInRegistrationControl.java
 * Handles the business logic for walk-in guest registration and
 * standard booking processing.
 *
 * @author junha
 */
public class WalkInRegistrationControl {

    private QueueInterface<WalkInRegistration> registrationQueue;

    public WalkInRegistrationControl() {
        registrationQueue = WalkInDAO.initializeRegistrations();
    }

    public boolean registerGuest(WalkInRegistration registration) {
        if (registrationQueue.contains(registration)) {
            return false;
        }
        return registrationQueue.enqueue(registration);
    }

    public WalkInRegistration processNextGuest() {
        WalkInRegistration reg = registrationQueue.dequeue();
        if (reg != null) {
            reg.setStatus("ASSIGNED");
        }
        return reg;
    }

    public WalkInRegistration peekNextGuest() {
        return registrationQueue.peek();
    }

    public int getWaitingCount() {
        return registrationQueue.getNumberOfEntries();
    }

    public QueueInterface<WalkInRegistration> getRegistrationQueue() {
        return registrationQueue;
    }

    // ================= Report 1: Waiting list sorted by registration time =================

    /**
     * Returns all waiting registrations sorted by registration time using
     * a self-implemented insertion sort (no java.util.Collections.sort()).
     * Optionally filters by requested room type.
     *
     * @param roomTypeFilter room type to filter by, or null/"" for no filter
     */
    public WalkInRegistration[] getSortedWaitingList(String roomTypeFilter) {
        int n = registrationQueue.getNumberOfEntries();
        WalkInRegistration[] temp = new WalkInRegistration[n];

        // Copy entries out of the queue via getEntry (list-style access)
        int count = 0;
        for (int i = 0; i < n; i++) {
            WalkInRegistration reg = registrationQueue.getEntry(i);
            if (roomTypeFilter == null || roomTypeFilter.isEmpty()
                    || reg.getRequestedRoomType().equalsIgnoreCase(roomTypeFilter)) {
                temp[count] = reg;
                count++;
            }
        }

        WalkInRegistration[] result = new WalkInRegistration[count];
        System.arraycopy(temp, 0, result, 0, count);

        insertionSortByTime(result);
        return result;
    }

    /** Self-implemented insertion sort, ascending by registrationTime. */
    private void insertionSortByTime(WalkInRegistration[] arr) {
        for (int i = 1; i < arr.length; i++) {
            WalkInRegistration key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j].getRegistrationTime().compareTo(key.getRegistrationTime()) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // ================= Report 2: Room-type demand summary =================

    /**
     * Searches the queue for a guest by guestId.
     * Self-implemented linear search using list-style access.
     */
    public WalkInRegistration searchByGuestId(String guestId) {
        int n = registrationQueue.getNumberOfEntries();
        for (int i = 0; i < n; i++) {
            WalkInRegistration reg = registrationQueue.getEntry(i);
            if (reg.getGuest().getGuestId().equalsIgnoreCase(guestId)) {
                return reg;
            }
        }
        return null;
    }

    /**
     * Returns room types and their demand counts, sorted by demand
     * (highest first) using a self-implemented selection sort.
     */
    public String[] getRoomTypeDemandSummary() {
        int n = registrationQueue.getNumberOfEntries();
        String[] types = new String[n];
        int[] counts = new int[n];
        int distinctCount = 0;

        for (int i = 0; i < n; i++) {
            String type = registrationQueue.getEntry(i).getRequestedRoomType();
            int idx = -1;
            for (int j = 0; j < distinctCount; j++) {
                if (types[j].equalsIgnoreCase(type)) {
                    idx = j;
                    break;
                }
            }
            if (idx == -1) {
                types[distinctCount] = type;
                counts[distinctCount] = 1;
                distinctCount++;
            } else {
                counts[idx]++;
            }
        }

        // Selection sort descending by count
        for (int i = 0; i < distinctCount - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < distinctCount; j++) {
                if (counts[j] > counts[maxIdx]) {
                    maxIdx = j;
                }
            }
            int tempCount = counts[i]; counts[i] = counts[maxIdx]; counts[maxIdx] = tempCount;
            String tempType = types[i]; types[i] = types[maxIdx]; types[maxIdx] = tempType;
        }

        String[] result = new String[distinctCount];
        for (int i = 0; i < distinctCount; i++) {
            result[i] = types[i] + " (" + counts[i] + ")";
        }
        return result;
    }
}