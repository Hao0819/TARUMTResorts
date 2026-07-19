package com.tarumt.resorts.dao;

import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.adt.Queue;

/**
 * RoomStatusLogDAO.java
 * Returns a collection pre-filled with hard-coded sample RoomStatusLog
 * entities, representing existing cleaning-status history for a few rooms.
 * Per tutor clarification: no file/database I/O, just sample data.
 *
 * @author YourName
 */
public class RoomStatusLogDAO {
    public Queue<RoomStatusLog> getAllLogs() {
        Queue<RoomStatusLog> logs = new Queue<>();
        logs.enqueue(new RoomStatusLog("101", "DIRTY", "2026-07-19 08:00"));
        logs.enqueue(new RoomStatusLog("101", "CLEANING", "2026-07-19 08:15"));
        logs.enqueue(new RoomStatusLog("102", "DIRTY", "2026-07-19 08:05"));
        logs.enqueue(new RoomStatusLog("201", "INSPECTED", "2026-07-19 09:00"));
        logs.enqueue(new RoomStatusLog("301", "READY", "2026-07-19 07:30"));
        return logs;
    }
}