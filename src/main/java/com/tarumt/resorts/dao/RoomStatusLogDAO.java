package com.tarumt.resorts.dao;

import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
/**
 * RoomStatusLogDAO.java
 * Returns a collection pre-filled with hard-coded sample RoomStatusLog
 * entities for all 40 rooms (16 Standard, 14 Deluxe, 10 Suite), each
 * following a legal DIRTY -> CLEANING -> INSPECTED -> READY sequence.
 * Occupied rooms (per RoomDAO) always end at READY, since a room can
 * only be allocated to a guest once it is clean.
 *
 * @author GanKohJun
 */
public class RoomStatusLogDAO {

    public ListQueueInterface<RoomStatusLog> getAllLogs() {
        ListQueueInterface<RoomStatusLog> logs = new DoublyLinkedListQueue<>(); // ADT collection declaration

        // Room 101 (Standard) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("101", "DIRTY", "2026-07-19 07:00")); // ADT method call: enqueue()
        // Room 102 (Standard) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("102", "DIRTY", "2026-07-19 07:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("102", "CLEANING", "2026-07-19 07:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("102", "INSPECTED", "2026-07-19 07:30")); // ADT method call: enqueue()
        // Room 103 (Standard) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("103", "DIRTY", "2026-07-19 07:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("103", "CLEANING", "2026-07-19 07:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("103", "INSPECTED", "2026-07-19 08:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("103", "READY", "2026-07-19 08:10")); // ADT method call: enqueue()
        // Room 104 (Standard) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("104", "DIRTY", "2026-07-19 08:20")); // ADT method call: enqueue()
        // Room 105 (Standard) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("105", "DIRTY", "2026-07-19 08:30")); // ADT method call: enqueue()
        // Room 106 (Standard) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("106", "DIRTY", "2026-07-19 08:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("106", "CLEANING", "2026-07-19 08:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("106", "INSPECTED", "2026-07-19 09:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("106", "READY", "2026-07-19 09:10")); // ADT method call: enqueue()
        // Room 107 (Standard) — ends CLEANING
        logs.enqueue(new RoomStatusLog("107", "DIRTY", "2026-07-19 09:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("107", "CLEANING", "2026-07-19 09:30")); // ADT method call: enqueue()
        // Room 108 (Standard) — ends DIRTY
        logs.enqueue(new RoomStatusLog("108", "DIRTY", "2026-07-19 09:40")); // ADT method call: enqueue()
        // Room 109 (Standard) — ends READY
        logs.enqueue(new RoomStatusLog("109", "DIRTY", "2026-07-19 09:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("109", "CLEANING", "2026-07-19 10:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("109", "INSPECTED", "2026-07-19 10:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("109", "READY", "2026-07-19 10:20")); // ADT method call: enqueue()
        // Room 110 (Standard) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("110", "DIRTY", "2026-07-19 10:30")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("110", "CLEANING", "2026-07-19 10:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("110", "INSPECTED", "2026-07-19 10:50")); // ADT method call: enqueue()
        // Room 111 (Standard) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("111", "DIRTY", "2026-07-19 11:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("111", "CLEANING", "2026-07-19 11:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("111", "INSPECTED", "2026-07-19 11:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("111", "READY", "2026-07-19 11:30")); // ADT method call: enqueue()
        // Room 112 (Standard) — ends DIRTY
        logs.enqueue(new RoomStatusLog("112", "DIRTY", "2026-07-19 11:40")); // ADT method call: enqueue()
        // Room 113 (Standard) — ends READY
        logs.enqueue(new RoomStatusLog("113", "DIRTY", "2026-07-19 11:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("113", "CLEANING", "2026-07-19 12:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("113", "INSPECTED", "2026-07-19 12:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("113", "READY", "2026-07-19 12:20")); // ADT method call: enqueue()
        // Room 114 (Standard) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("114", "DIRTY", "2026-07-19 12:30")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("114", "CLEANING", "2026-07-19 12:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("114", "INSPECTED", "2026-07-19 12:50")); // ADT method call: enqueue()
        // Room 115 (Standard) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("115", "DIRTY", "2026-07-19 13:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("115", "CLEANING", "2026-07-19 13:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("115", "INSPECTED", "2026-07-19 13:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("115", "READY", "2026-07-19 13:30")); // ADT method call: enqueue()
        // Room 116 (Standard) — ends DIRTY
        logs.enqueue(new RoomStatusLog("116", "DIRTY", "2026-07-19 13:40")); // ADT method call: enqueue()
        // Room 201 (Deluxe) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("201", "DIRTY", "2026-07-19 13:50")); // ADT method call: enqueue()
        // Room 202 (Deluxe) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("202", "DIRTY", "2026-07-19 14:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("202", "CLEANING", "2026-07-19 14:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("202", "INSPECTED", "2026-07-19 14:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("202", "READY", "2026-07-19 14:30")); // ADT method call: enqueue()
        // Room 203 (Deluxe) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("203", "DIRTY", "2026-07-19 14:40")); // ADT method call: enqueue()
        // Room 204 (Deluxe) — ends DIRTY
        logs.enqueue(new RoomStatusLog("204", "DIRTY", "2026-07-19 14:50")); // ADT method call: enqueue()
        // Room 205 (Deluxe) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("205", "DIRTY", "2026-07-19 15:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("205", "CLEANING", "2026-07-19 15:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("205", "INSPECTED", "2026-07-19 15:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("205", "READY", "2026-07-19 15:30")); // ADT method call: enqueue()
        // Room 206 (Deluxe) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("206", "DIRTY", "2026-07-19 15:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("206", "CLEANING", "2026-07-19 15:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("206", "INSPECTED", "2026-07-19 16:00")); // ADT method call: enqueue()
        // Room 207 (Deluxe) — ends CLEANING
        logs.enqueue(new RoomStatusLog("207", "DIRTY", "2026-07-19 16:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("207", "CLEANING", "2026-07-19 16:20")); // ADT method call: enqueue()
        // Room 208 (Deluxe) — ends DIRTY
        logs.enqueue(new RoomStatusLog("208", "DIRTY", "2026-07-19 16:30")); // ADT method call: enqueue()
        // Room 209 (Deluxe) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("209", "DIRTY", "2026-07-19 16:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("209", "CLEANING", "2026-07-19 16:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("209", "INSPECTED", "2026-07-19 17:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("209", "READY", "2026-07-19 17:10")); // ADT method call: enqueue()
        // Room 210 (Deluxe) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("210", "DIRTY", "2026-07-19 17:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("210", "CLEANING", "2026-07-19 17:30")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("210", "INSPECTED", "2026-07-19 17:40")); // ADT method call: enqueue()
        // Room 211 (Deluxe) — ends CLEANING
        logs.enqueue(new RoomStatusLog("211", "DIRTY", "2026-07-19 17:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("211", "CLEANING", "2026-07-19 18:00")); // ADT method call: enqueue()
        // Room 212 (Deluxe) — ends DIRTY
        logs.enqueue(new RoomStatusLog("212", "DIRTY", "2026-07-19 18:10")); // ADT method call: enqueue()
        // Room 213 (Deluxe) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("213", "DIRTY", "2026-07-19 18:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("213", "CLEANING", "2026-07-19 18:30")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("213", "INSPECTED", "2026-07-19 18:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("213", "READY", "2026-07-19 18:50")); // ADT method call: enqueue()
        // Room 214 (Deluxe) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("214", "DIRTY", "2026-07-19 19:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("214", "CLEANING", "2026-07-19 19:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("214", "INSPECTED", "2026-07-19 19:20")); // ADT method call: enqueue()
        // Room 301 (Suite) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("301", "DIRTY", "2026-07-19 19:30")); // ADT method call: enqueue()
        // Room 302 (Suite) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("302", "DIRTY", "2026-07-19 19:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("302", "CLEANING", "2026-07-19 19:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("302", "INSPECTED", "2026-07-19 20:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("302", "READY", "2026-07-19 20:10")); // ADT method call: enqueue()
        // Room 303 (Suite) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("303", "DIRTY", "2026-07-19 20:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("303", "CLEANING", "2026-07-19 20:30")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("303", "INSPECTED", "2026-07-19 20:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("303", "READY", "2026-07-19 20:50")); // ADT method call: enqueue()
        // Room 304 (Suite) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("304", "DIRTY", "2026-07-19 21:00")); // ADT method call: enqueue()
        // Room 305 (Suite) — ends DIRTY (has a CHECKED_OUT booking in
        // BookingDAO; nobody has cleaned it yet since the guest left)
        logs.enqueue(new RoomStatusLog("305", "DIRTY", "2026-07-19 21:10")); // ADT method call: enqueue()
        // Room 306 (Suite) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("306", "DIRTY", "2026-07-19 21:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("306", "CLEANING", "2026-07-19 21:30")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("306", "INSPECTED", "2026-07-19 21:40")); // ADT method call: enqueue()
        // Room 307 (Suite) — ends READY (OCCUPIED - guest checked into this room while it was READY)
        logs.enqueue(new RoomStatusLog("307", "DIRTY", "2026-07-19 21:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("307", "CLEANING", "2026-07-19 22:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("307", "INSPECTED", "2026-07-19 22:10")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("307", "READY", "2026-07-19 22:20")); // ADT method call: enqueue()
        // Room 308 (Suite) — ends DIRTY
        logs.enqueue(new RoomStatusLog("308", "DIRTY", "2026-07-19 22:30")); // ADT method call: enqueue()
        // Room 309 (Suite) — ends READY
        logs.enqueue(new RoomStatusLog("309", "DIRTY", "2026-07-19 22:40")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("309", "CLEANING", "2026-07-19 22:50")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("309", "INSPECTED", "2026-07-19 23:00")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("309", "READY", "2026-07-19 23:10")); // ADT method call: enqueue()
        // Room 310 (Suite) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("310", "DIRTY", "2026-07-19 23:20")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("310", "CLEANING", "2026-07-19 23:30")); // ADT method call: enqueue()
        logs.enqueue(new RoomStatusLog("310", "INSPECTED", "2026-07-19 23:40")); // ADT method call: enqueue()
        return logs;
    }
}