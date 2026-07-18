package com.tarumt.resorts.dao;


import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.adt.Queue;

/**
 * RoomDAO.java
 * Returns a collection pre-filled with hard-coded sample Room entities.
 * Used by Walk-In Registration's own Room Allocation step, since no other
 * module currently owns Room Allocation.
 *
 * @author junha
 */
public class RoomDAO {

    public Queue<Room> getAllRooms() {
        Queue<Room> rooms = new Queue<>();
        rooms.enqueue(new Room("101", "Standard", true));
        rooms.enqueue(new Room("102", "Standard", true));
        rooms.enqueue(new Room("201", "Deluxe", true));
        rooms.enqueue(new Room("202", "Deluxe", false));
        rooms.enqueue(new Room("301", "Suite", true));
        return rooms;
    }
}