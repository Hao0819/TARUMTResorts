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
        rooms.enqueue(new Room("103", "Standard", true));
        rooms.enqueue(new Room("104", "Standard", true));
        rooms.enqueue(new Room("105", "Standard", true));
        rooms.enqueue(new Room("106", "Standard", true));
        rooms.enqueue(new Room("107", "Standard", true));
        rooms.enqueue(new Room("108", "Standard", true));

        rooms.enqueue(new Room("201", "Deluxe", true));
        rooms.enqueue(new Room("202", "Deluxe", false));
        rooms.enqueue(new Room("203", "Deluxe", true));
        rooms.enqueue(new Room("204", "Deluxe", true));
        rooms.enqueue(new Room("205", "Deluxe", true));
        rooms.enqueue(new Room("206", "Deluxe", true));
        rooms.enqueue(new Room("207", "Deluxe", true));

        rooms.enqueue(new Room("301", "Suite", true));
        rooms.enqueue(new Room("302", "Suite", true));
        rooms.enqueue(new Room("303", "Suite", true));
        rooms.enqueue(new Room("304", "Suite", true));
        rooms.enqueue(new Room("305", "Suite", true));
        return rooms;
    }
}