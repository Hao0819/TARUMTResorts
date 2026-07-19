package com.tarumt.resorts.control;

import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.WalkInRegistration;
import com.tarumt.resorts.adt.Queue;
import com.tarumt.resorts.dao.GuestDAO;
import com.tarumt.resorts.dao.RoomDAO;

/**
 * WalkInRegistrationControl.java
 * Handles the business logic for the Walk-In Registration module.
 *
 * @author junha
 */
public class WalkInRegistrationControl {

    private Queue<WalkInRegistration> registrationQueue;
    private Queue<Room> roomList;
    private int confirmationCounter;
    private int guestCounter;
    private Queue<Booking> bookingList;
    private Queue<Guest> guestList ;

    public WalkInRegistrationControl() {
        registrationQueue = new Queue<>();
        roomList = new RoomDAO().getAllRooms();
        bookingList = new Queue<>();
        guestList = new GuestDAO().getAllGuests();
        confirmationCounter = 1;
        // guestCounter continue AFTER the guests GuestDAO already
        guestCounter = guestList.getNumberOfEntries()+1;
    }

      /**
     * Looks up an existing guest by guestId in guestList (linear search).
     * If found, returns that SAME existing Guest object — so a returning
     * guest is correctly recognized as one person, not duplicated.
     * If not found, this is a genuinely new guest: create a new Guest,
     * add it to guestList so it's recognized next time, and return it.
     */
    private Guest findOrCreateGuest( String name, String contactNumber, String email){
        int totalGuest = guestList.getNumberOfEntries();
        for (int i = 0; i < totalGuest ; i++){
            Guest existing = guestList.getEntry(i);
            if(existing.getContactNumber().equalsIgnoreCase(contactNumber)){
                return existing; // return guest , reuse their real record
            }
        }
        //auto generate guest id 
        String newGuestId = String.format("G%03d", guestCounter++);
        //Not found -genuinely new guest
        Guest newGuest = new Guest(newGuestId, name, contactNumber, email, "None");
        guestList.enqueue(newGuest);
        return newGuest;
    }

    //Input validation helpers (called by Boundary before registering)
    
    /**validates contact number :digit only, 9 to 11 digit long
     * reject symbols, letters and invalid lengths.
    **/
    public boolean isValidContact(String contact){
        return contact != null && contact.matches("^[0-9]{9,11}$");
    }

    //Validates emauk addess against a standard email pattern
    public boolean isValidEmail(String email){
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

        /**
     * Converts a short staff-entered room type code into the full room
     * type string used by RoomDAO's sample data ("Standard", "Deluxe",
     * "Suite"). Returns null if the code isn't recognized, so the
     * Boundary layer knows to reject it and ask again.
     */
    public String parseRoomTypeCode(String code){
        if (code == null){
            return null;
        }
        switch (code.trim().toUpperCase()) {
            case "D":
                return "Deluxe";
            case "S":
                return "Standard";
            case "SU":
                return "Suite";
            default:
                return null;
        }
    }


    /**
     * Registers a new walk-in guest.
     * First resolves the Guest via findOrCreateGuest() (so returning guests
     * are recognized correctly rather than duplicated), then prevents
     * duplicate WAITING registrations for that same guest.
     *
     * We deliberately do NOT use registrationQueue.contains() for the
     * duplicate check, because WalkInRegistration.equals() compares
     * registrationId — and since each registration attempt gets a freshly
     * generated registrationId, contains() would never actually catch the
     * same guest trying to register twice.
     *
     * @return true if registered successfully, false if this guest is
     *         already waiting
     */
     public Guest registerGuest(String registrationId, 
            String name, String contactNumber, String email,
            String registrationTime, String requestedRoomType) {
 
        Guest guest = findOrCreateGuest( name, contactNumber, email);
 
        // Search the queue for an existing WAITING registration with the
        // same guestId — same linear-search shape used elsewhere.
        int totalWaiting = registrationQueue.getNumberOfEntries();
        for (int i = 0; i < totalWaiting; i++) {
            WalkInRegistration existing = registrationQueue.getEntry(i);
            if (existing.getGuest().getGuestId().equalsIgnoreCase(guest.getGuestId())
                    && existing.getStatus().equals("WAITING")) {
                return null; // this guest is already in line
            }
        }
 
        WalkInRegistration registration = new WalkInRegistration(
                registrationId, guest, registrationTime, requestedRoomType);
 
        registrationQueue.enqueue(registration);
        return guest;
    }

    /**
     * Processes the guest currently at the front of the queue:
     * 1. peek() at them first WITHOUT removing them — if no room is
     *    available for their requested room type, they stay in the queue.
     * 2. Search roomList for the first room that is both available AND
     *    matches the requested room type (linear search, same pattern as
     *    searchByGuestId will use later).
     * 3. If no matching room is found, return null — the guest was never
     *    dequeued, so they remain at the front of the line for next time.
     * 4. If a room IS found: NOW dequeue() the guest for real, generate a
     *    confirmation number, build the Booking, mark the room unavailable,
     *    update the registration's status, and return the Booking.
     *
     * @return the created Booking, or null if no matching room was available
     */
    public Booking processNextGuest(){
        //Step 1 : look at front guest without remove them 
        WalkInRegistration nextGuest = registrationQueue.peek();
        if(nextGuest == null){
            return null; // queue is empty , nobody to process 
        }

        //Step 2: linear search through roomList for a matching, available room
        //find the room is matching the condition 
        Room assignedRoom = null;
        int totalRoom = roomList.getNumberOfEntries();
        for (int i = 0; i < totalRoom; i++){
            Room candidate = roomList.getEntry(i); //doesn't crteate a new Room, copies the memory address  into candidate
            if(candidate.isAvailable() && candidate.getRoomType().equalsIgnoreCase(nextGuest.getRequestedRoomType())) {// equalsIgnoreCase use to ignore upper or lower case
                assignedRoom = candidate; // assigned room number 
                break;
            }
        }

        //Step 3: if no matching room found , guest stays in queue
        //if not match the assignedRoom would still null
        if (assignedRoom == null){
            return null;
        }
          //Step 4 : a room was found , remove the guest from the queue
          /**the guest data already stored in nextGuest in Step 1 ,
           * not need to capture value into a valuable**/
        registrationQueue.dequeue();

        //Generate a simple confirmation number
        String confirmationNumber = String.format("CNF%04d",confirmationCounter );
        confirmationCounter++;
        
        //Build the Booking linking guest + room + confirmation number
        Booking booking = new Booking(confirmationNumber, nextGuest.getGuest(),assignedRoom, nextGuest.getRegistrationTime());

        //Mark the room as no longer available so isn't assiged twice
        assignedRoom.setAvailable(false);

        //Relect the change on the registration record too
        //status change from "Waiting" to "ASSIGNED"
        nextGuest.setStatus("ASSIGNED");

        // Keep a permanent record of this booking so it can be looked up
        bookingList.enqueue(booking);

        return booking;
    }

    // search the queue for a registration by guestId, using a self-implemented linear search
    public WalkInRegistration searchByGuestId(String guestId){
        int totalWaiting = registrationQueue.getNumberOfEntries();
        for (int i = 0; i <totalWaiting; i ++){
            WalkInRegistration reg = registrationQueue.getEntry(i);
            if (reg.getGuest().getGuestId().equalsIgnoreCase(guestId)){
                return reg;
            }
        }
        return null;

    }

      
     // Returns how many guests are currently waiting in the queue.
    public int getWaitingCount() {
        return registrationQueue.getNumberOfEntries();
    }

    /**exposes the full list of booking created. 
     * Other modules cam use this to display or search all completed bookings**/
    public Queue<Booking> getBookingList(){
        return bookingList;
    }

}