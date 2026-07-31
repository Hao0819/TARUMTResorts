package com.tarumt.resorts.dao;

import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;

/**
 * RoomStatusLogDAO.java
 * Returns a collection pre-filled with 20 hard-coded sample RoomStatusLog
 * entities, covering 8 rooms across all three room types (Standard,
 * Deluxe, Suite). Every room's history starts at DIRTY and only moves
 * forward through DIRTY -> CLEANING -> INSPECTED -> READY, so every
 * record is a legal transition under HousekeepingControl.isValidNextStatus().
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

    public DoublyLinkedListQueue<RoomStatusLog> getAllLogs() {
        DoublyLinkedListQueue<RoomStatusLog> logs = new DoublyLinkedListQueue<>();

        // Room 101 (Standard) — full cycle, ends READY
        logs.enqueue(new RoomStatusLog("101", "DIRTY",     "2026-07-19 07:00"));
        // Room 102 (Standard) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("102", "DIRTY",     "2026-07-19 07:10"));
        // Room 103 (Standard) — ends CLEANING
        logs.enqueue(new RoomStatusLog("103", "DIRTY",     "2026-07-19 07:15"));
        logs.enqueue(new RoomStatusLog("101", "CLEANING",  "2026-07-19 07:20"));
        // Room 104 (Standard) — ends DIRTY (single record)
        logs.enqueue(new RoomStatusLog("104", "DIRTY",     "2026-07-19 07:30"));
        logs.enqueue(new RoomStatusLog("102", "CLEANING",  "2026-07-19 07:35"));
        logs.enqueue(new RoomStatusLog("103", "CLEANING",  "2026-07-19 07:45"));
        logs.enqueue(new RoomStatusLog("101", "INSPECTED", "2026-07-19 07:50"));
        logs.enqueue(new RoomStatusLog("102", "INSPECTED", "2026-07-19 08:00"));
        logs.enqueue(new RoomStatusLog("101", "READY",     "2026-07-19 08:05"));
        // Room 201 (Deluxe) — full cycle, ends READY
        logs.enqueue(new RoomStatusLog("201", "DIRTY",     "2026-07-19 08:10"));
        // Room 202 (Deluxe) — ends INSPECTED
        logs.enqueue(new RoomStatusLog("202", "DIRTY",     "2026-07-19 08:20"));
        logs.enqueue(new RoomStatusLog("201", "CLEANING",  "2026-07-19 08:40"));
        logs.enqueue(new RoomStatusLog("202", "CLEANING",  "2026-07-19 08:55"));
        // Room 301 (Suite) — ends CLEANING
        logs.enqueue(new RoomStatusLog("301", "DIRTY",     "2026-07-19 09:00"));
        logs.enqueue(new RoomStatusLog("201", "INSPECTED", "2026-07-19 09:10"));
        // Room 302 (Suite) — ends DIRTY (single record)
        logs.enqueue(new RoomStatusLog("302", "DIRTY",     "2026-07-19 09:15"));
        logs.enqueue(new RoomStatusLog("202", "INSPECTED", "2026-07-19 09:25"));
        logs.enqueue(new RoomStatusLog("201", "READY",     "2026-07-19 09:30"));
        logs.enqueue(new RoomStatusLog("301", "CLEANING",  "2026-07-19 09:40"));

        return logs;
    }
}