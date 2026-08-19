package com.tarumt.resorts.dao;

import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
/**
 * RoomStatusLogDAO.java
 * Returns a collection pre-filled with hard-coded sample RoomStatusLog
 * entities, covering ALL 40 rooms now defined in RoomDAO (16 Standard:
 * 101-116, 14 Deluxe: 201-214, 10 Suite: 301-310) — expanded from the
 * original 8-room sample set to match RoomDAO's full room list, so no
 * room in the system is left showing UNKNOWN purely because it was
 * never included in this sample data.
 *
 * Each room's history starts at DIRTY and only moves forward through
 * DIRTY -> CLEANING -> INSPECTED -> READY, so every record is a legal
 * transition under HousekeepingControl.isValidNextStatus(). Rooms are
 * given a deliberate mix of how far their cycle got (ends at DIRTY /
 * CLEANING / INSPECTED / READY), split roughly evenly across the 40
 * rooms, so Report 1, Report 2, and the Summary Report all have
 * realistic variety to display instead of one repeated pattern.
 *
 * IMPORTANT — kept consistent with RoomDAO's isAvailable flag: a room
 * only ever becomes occupied (isAvailable = false in RoomDAO) through
 * Walk-In/VIP allocation, and allocation only accepts a room whose
 * cleaning status is READY or UNKNOWN (see
 * WalkInRegistrationControl.isReadyForAllocation() and
 * VIPAllocationControl.isReadyForAllocation() — both private methods with
 * identical logic; FrontDeskControl.isRoomReadyForCheckIn() re-applies the
 * same rule again at actual check-in time, in case the room's status
 * changed after allocation but before the guest arrived). So every room
 * RoomDAO marks as
 * occupied (103, 106, 111, 115, 202, 205, 209, 213, 302, 303, 307) MUST
 * end its history here at READY — it was clean when the guest checked
 * in, and check-in never changes the housekeeping status. A room ending
 * at DIRTY/CLEANING/INSPECTED while RoomDAO marks it occupied would be
 * impossible under the real allocation flow (this was previously the
 * case for several rooms — a room showing "still READY" while a guest
 * is inside is in fact correct once you see the Occupied column; the
 * bug was rooms marked occupied while mid-cleaning, which could never
 * really happen). Every other room (isAvailable = true in RoomDAO) is
 * vacant and may legitimately end at any stage.
 *
 * Records are enqueued in strict chronological (global) timestamp order,
 * so the rear of the queue always represents the most recently logged
 * change, which keeps rollback / previewLastChange() meaningful.
 *
 * Per tutor clarification: no file/database I/O, just sample data.
 *
 * @author KohJun
 */
public class RoomStatusLogDAO {

    public ListQueueInterface<RoomStatusLog> getAllLogs() {
        ListQueueInterface<RoomStatusLog> logs = new DoublyLinkedListQueue<>();

        // Room 101 (Standard) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("101", "DIRTY", "2026-07-19 07:00"));
        // Room 102 (Standard) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("102", "DIRTY", "2026-07-19 07:10"));
        logs.enqueue(new RoomStatusLog("102", "CLEANING", "2026-07-19 07:20"));
        logs.enqueue(new RoomStatusLog("102", "INSPECTED", "2026-07-19 07:30"));
        // Room 103 (Standard) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("103", "DIRTY", "2026-07-19 07:40"));
        logs.enqueue(new RoomStatusLog("103", "CLEANING", "2026-07-19 07:50"));
        logs.enqueue(new RoomStatusLog("103", "INSPECTED", "2026-07-19 08:00"));
        logs.enqueue(new RoomStatusLog("103", "READY", "2026-07-19 08:10"));
        // Room 104 (Standard) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("104", "DIRTY", "2026-07-19 08:20"));
        // Room 105 (Standard) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("105", "DIRTY", "2026-07-19 08:30"));
        // Room 106 (Standard) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("106", "DIRTY", "2026-07-19 08:40"));
        logs.enqueue(new RoomStatusLog("106", "CLEANING", "2026-07-19 08:50"));
        logs.enqueue(new RoomStatusLog("106", "INSPECTED", "2026-07-19 09:00"));
        logs.enqueue(new RoomStatusLog("106", "READY", "2026-07-19 09:10"));
        // Room 107 (Standard) — ends CLEANING
        logs.enqueue(new RoomStatusLog("107", "DIRTY", "2026-07-19 09:20"));
        logs.enqueue(new RoomStatusLog("107", "CLEANING", "2026-07-19 09:30"));
        // Room 108 (Standard) — ends DIRTY
        logs.enqueue(new RoomStatusLog("108", "DIRTY", "2026-07-19 09:40"));
        // Room 109 (Standard) — ends READY
        logs.enqueue(new RoomStatusLog("109", "DIRTY", "2026-07-19 09:50"));
        logs.enqueue(new RoomStatusLog("109", "CLEANING", "2026-07-19 10:00"));
        logs.enqueue(new RoomStatusLog("109", "INSPECTED", "2026-07-19 10:10"));
        logs.enqueue(new RoomStatusLog("109", "READY", "2026-07-19 10:20"));
        // Room 110 (Standard) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("110", "DIRTY", "2026-07-19 10:30"));
        logs.enqueue(new RoomStatusLog("110", "CLEANING", "2026-07-19 10:40"));
        logs.enqueue(new RoomStatusLog("110", "INSPECTED", "2026-07-19 10:50"));
        // Room 111 (Standard) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("111", "DIRTY", "2026-07-19 11:00"));
        logs.enqueue(new RoomStatusLog("111", "CLEANING", "2026-07-19 11:10"));
        logs.enqueue(new RoomStatusLog("111", "INSPECTED", "2026-07-19 11:20"));
        logs.enqueue(new RoomStatusLog("111", "READY", "2026-07-19 11:30"));
        // Room 112 (Standard) — ends DIRTY
        logs.enqueue(new RoomStatusLog("112", "DIRTY", "2026-07-19 11:40"));
        // Room 113 (Standard) — ends READY
        logs.enqueue(new RoomStatusLog("113", "DIRTY", "2026-07-19 11:50"));
        logs.enqueue(new RoomStatusLog("113", "CLEANING", "2026-07-19 12:00"));
        logs.enqueue(new RoomStatusLog("113", "INSPECTED", "2026-07-19 12:10"));
        logs.enqueue(new RoomStatusLog("113", "READY", "2026-07-19 12:20"));
        // Room 114 (Standard) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("114", "DIRTY", "2026-07-19 12:30"));
        logs.enqueue(new RoomStatusLog("114", "CLEANING", "2026-07-19 12:40"));
        logs.enqueue(new RoomStatusLog("114", "INSPECTED", "2026-07-19 12:50"));
        // Room 115 (Standard) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("115", "DIRTY", "2026-07-19 13:00"));
        logs.enqueue(new RoomStatusLog("115", "CLEANING", "2026-07-19 13:10"));
        logs.enqueue(new RoomStatusLog("115", "INSPECTED", "2026-07-19 13:20"));
        logs.enqueue(new RoomStatusLog("115", "READY", "2026-07-19 13:30"));
        // Room 116 (Standard) — ends DIRTY
        logs.enqueue(new RoomStatusLog("116", "DIRTY", "2026-07-19 13:40"));
        // Room 201 (Deluxe) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("201", "DIRTY", "2026-07-19 13:50"));
        // Room 202 (Deluxe) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("202", "DIRTY", "2026-07-19 14:00"));
        logs.enqueue(new RoomStatusLog("202", "CLEANING", "2026-07-19 14:10"));
        logs.enqueue(new RoomStatusLog("202", "INSPECTED", "2026-07-19 14:20"));
        logs.enqueue(new RoomStatusLog("202", "READY", "2026-07-19 14:30"));
        // Room 203 (Deluxe) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("203", "DIRTY", "2026-07-19 14:40"));
        // Room 204 (Deluxe) — ends DIRTY
        logs.enqueue(new RoomStatusLog("204", "DIRTY", "2026-07-19 14:50"));
        // Room 205 (Deluxe) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("205", "DIRTY", "2026-07-19 15:00"));
        logs.enqueue(new RoomStatusLog("205", "CLEANING", "2026-07-19 15:10"));
        logs.enqueue(new RoomStatusLog("205", "INSPECTED", "2026-07-19 15:20"));
        logs.enqueue(new RoomStatusLog("205", "READY", "2026-07-19 15:30"));
        // Room 206 (Deluxe) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("206", "DIRTY", "2026-07-19 15:40"));
        logs.enqueue(new RoomStatusLog("206", "CLEANING", "2026-07-19 15:50"));
        logs.enqueue(new RoomStatusLog("206", "INSPECTED", "2026-07-19 16:00"));
        // Room 207 (Deluxe) — ends CLEANING
        logs.enqueue(new RoomStatusLog("207", "DIRTY", "2026-07-19 16:10"));
        logs.enqueue(new RoomStatusLog("207", "CLEANING", "2026-07-19 16:20"));
        // Room 208 (Deluxe) — ends DIRTY
        logs.enqueue(new RoomStatusLog("208", "DIRTY", "2026-07-19 16:30"));
        // Room 209 (Deluxe) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("209", "DIRTY", "2026-07-19 16:40"));
        logs.enqueue(new RoomStatusLog("209", "CLEANING", "2026-07-19 16:50"));
        logs.enqueue(new RoomStatusLog("209", "INSPECTED", "2026-07-19 17:00"));
        logs.enqueue(new RoomStatusLog("209", "READY", "2026-07-19 17:10"));
        // Room 210 (Deluxe) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("210", "DIRTY", "2026-07-19 17:20"));
        logs.enqueue(new RoomStatusLog("210", "CLEANING", "2026-07-19 17:30"));
        logs.enqueue(new RoomStatusLog("210", "INSPECTED", "2026-07-19 17:40"));
        // Room 211 (Deluxe) — ends CLEANING
        logs.enqueue(new RoomStatusLog("211", "DIRTY", "2026-07-19 17:50"));
        logs.enqueue(new RoomStatusLog("211", "CLEANING", "2026-07-19 18:00"));
        // Room 212 (Deluxe) — ends DIRTY
        logs.enqueue(new RoomStatusLog("212", "DIRTY", "2026-07-19 18:10"));
        // Room 213 (Deluxe) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("213", "DIRTY", "2026-07-19 18:20"));
        logs.enqueue(new RoomStatusLog("213", "CLEANING", "2026-07-19 18:30"));
        logs.enqueue(new RoomStatusLog("213", "INSPECTED", "2026-07-19 18:40"));
        logs.enqueue(new RoomStatusLog("213", "READY", "2026-07-19 18:50"));
        // Room 214 (Deluxe) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("214", "DIRTY", "2026-07-19 19:00"));
        logs.enqueue(new RoomStatusLog("214", "CLEANING", "2026-07-19 19:10"));
        logs.enqueue(new RoomStatusLog("214", "INSPECTED", "2026-07-19 19:20"));
        // Room 301 (Suite) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("301", "DIRTY", "2026-07-19 19:30"));
        // Room 302 (Suite) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("302", "DIRTY", "2026-07-19 19:40"));
        logs.enqueue(new RoomStatusLog("302", "CLEANING", "2026-07-19 19:50"));
        logs.enqueue(new RoomStatusLog("302", "INSPECTED", "2026-07-19 20:00"));
        logs.enqueue(new RoomStatusLog("302", "READY", "2026-07-19 20:10"));
        // Room 303 (Suite) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("303", "DIRTY", "2026-07-19 20:20"));
        logs.enqueue(new RoomStatusLog("303", "CLEANING", "2026-07-19 20:30"));
        logs.enqueue(new RoomStatusLog("303", "INSPECTED", "2026-07-19 20:40"));
        logs.enqueue(new RoomStatusLog("303", "READY", "2026-07-19 20:50"));
        // Room 304 (Suite) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("304", "DIRTY", "2026-07-19 21:00"));
        // Room 305 (Suite) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("305", "DIRTY", "2026-07-19 21:10"));
        // Room 306 (Suite) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("306", "DIRTY", "2026-07-19 21:20"));
        logs.enqueue(new RoomStatusLog("306", "CLEANING", "2026-07-19 21:30"));
        logs.enqueue(new RoomStatusLog("306", "INSPECTED", "2026-07-19 21:40"));
        // Room 307 (Suite) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("307", "DIRTY", "2026-07-19 21:50"));
        logs.enqueue(new RoomStatusLog("307", "CLEANING", "2026-07-19 22:00"));
        logs.enqueue(new RoomStatusLog("307", "INSPECTED", "2026-07-19 22:10"));
        logs.enqueue(new RoomStatusLog("307", "READY", "2026-07-19 22:20"));
        // Room 308 (Suite) — ends DIRTY
        logs.enqueue(new RoomStatusLog("308", "DIRTY", "2026-07-19 22:30"));
        // Room 309 (Suite) — ends READY
        logs.enqueue(new RoomStatusLog("309", "DIRTY", "2026-07-19 22:40"));
        logs.enqueue(new RoomStatusLog("309", "CLEANING", "2026-07-19 22:50"));
        logs.enqueue(new RoomStatusLog("309", "INSPECTED", "2026-07-19 23:00"));
        logs.enqueue(new RoomStatusLog("309", "READY", "2026-07-19 23:10"));
        // Room 310 (Suite) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("310", "DIRTY", "2026-07-19 23:20"));
        logs.enqueue(new RoomStatusLog("310", "CLEANING", "2026-07-19 23:30"));
        logs.enqueue(new RoomStatusLog("310", "INSPECTED", "2026-07-19 23:40"));
        return logs;
    }
}