package com.tarumt.resorts.dao;


import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.adt.Queue;

/**
 * GuestDAO.java
 * Returns a collection pre-filled with hard-coded sample Guest entities.
 * Per tutor clarification: no file/database I/O, just sample data.
 *
 * @author junha
 */
public class GuestDAO {

    public Queue<Guest> getAllGuests() {
        Queue<Guest> guests = new Queue<>();
        guests.enqueue(new Guest("G001", "Amy Tan", "012-3456789", "amy@mail.com", "T1"));
        guests.enqueue(new Guest("G002", "Ben Lee", "013-4567890", "ben@mail.com", "T2"));
        guests.enqueue(new Guest("G003", "Cara Wong", "014-5678901", "cara@mail.com", "T1"));
        guests.enqueue(new Guest("G004", "David Ho", "016-6789012", "david@mail.com", "T3"));
        guests.enqueue(new Guest("G005", "Ella Ng", "017-7890123", "ella@mail.com", "T2"));
        return guests;
    }
}