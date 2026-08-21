package com.tarumt.resorts.dao;

import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;

/**
 * RoomDAO.java
 * Returns a collection pre-filled with hard-coded sample Room entities.
 * Provides the shared Room inventory used by Walk-In, VIP Allocation,
 * Housekeeping and Front-Desk modules.
 *
 * @author LimJunHao
 */
public class RoomDAO {

        // Fixed nightly rates shared by all rooms of the same type.
        private static final double STANDARD_DAILY_RATE = 200.00;
        private static final double DELUXE_DAILY_RATE = 350.00;
        private static final double SUITE_DAILY_RATE = 500.00;

        public ListQueueInterface<Room> getAllRooms() {
                ListQueueInterface<Room> rooms = new DoublyLinkedListQueue<>();
                rooms.enqueue(new Room(
                                "101", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "102", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "103", "Standard", false, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "104", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "105", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "106", "Standard", false, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "107", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "108", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "109", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "110", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "111", "Standard", false, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "112", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "113", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "114", "Standard", true, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "115", "Standard", false, STANDARD_DAILY_RATE));
                rooms.enqueue(new Room(
                                "116", "Standard", true, STANDARD_DAILY_RATE));

                rooms.enqueue(new Room(
                                "201", "Deluxe", true, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "202", "Deluxe", false, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "203", "Deluxe", true, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "204", "Deluxe", true, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "205", "Deluxe", false, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "206", "Deluxe", true, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "207", "Deluxe", true, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "208", "Deluxe", true, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "209", "Deluxe", false, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "210", "Deluxe", true, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "211", "Deluxe", true, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "212", "Deluxe", true, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "213", "Deluxe", false, DELUXE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "214", "Deluxe", true, DELUXE_DAILY_RATE));

                rooms.enqueue(new Room(
                                "301", "Suite", true, SUITE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "302", "Suite", false, SUITE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "303", "Suite", false, SUITE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "304", "Suite", true, SUITE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "305", "Suite", true, SUITE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "306", "Suite", true, SUITE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "307", "Suite", false, SUITE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "308", "Suite", true, SUITE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "309", "Suite", true, SUITE_DAILY_RATE));
                rooms.enqueue(new Room(
                                "310", "Suite", true, SUITE_DAILY_RATE));
                return rooms;
        }
}