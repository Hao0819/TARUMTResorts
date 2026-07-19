/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.WalkInRegistrationControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;

import java.util.Scanner;

/**
 * WalkInRegistrationUI.java
 * Console interface for the Walk-In Registrations module.
 *
 * @author junha
 */
public class WalkInRegistrationUI {

    private WalkInRegistrationControl control;
    private Scanner sc;

    public WalkInRegistrationUI() {
        control = new WalkInRegistrationControl();
        sc = new Scanner(System.in);
    }

    public void showMenu() {
        int choice;
        do {
            System.out.println("\n=== Walk-In Registration Module ===");
            System.out.println("1. Register new walk-in guest");
            System.out.println("2. Process next guest");
            System.out.println("3. View waiting count");
            System.out.println("4. Report 1: Waiting list (not built yet)");
            System.out.println("5. Report 2: Room-type demand (not built yet)");
            System.out.println("6. Search guest by ID (not built yet)");
            System.out.println("0. Back to main menu");
            System.out.print("Enter choice: ");
            
            //prevent input letters, symbols, blank
            try{
                choice = Integer.parseInt(sc.nextLine());
            }catch(NumberFormatException e){
                System.out.println("Invalid input. Please enter a number.");
                choice = -1;// sentinel: not 0, so the do-while loop continues
                continue;
            }

            switch (choice) {
                case 1 -> registerGuest();
                case 2 -> processNextGuest();
                case 3 -> System.out.println("Guests waiting: " + control.getWaitingCount());
                case 4, 5 -> System.out.println("Not implemented yet.");
                case 6 -> searchGuest();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private void registerGuest() {
        System.out.print("Enter guest name: ");
        String name = sc.nextLine();
 
        String contact;
        while (true) {
            System.out.print("Enter contact number (digits only, 9-11 digits): ");
            contact = sc.nextLine();
            if (control.isValidContact(contact)) {
                break;
            }
            System.out.println("Invalid contact number. Please try again.");
        }
 
        String email;
        while (true) {
            System.out.print("Enter email: ");
            email = sc.nextLine();
            if (control.isValidEmail(email)) {
                break;
            }
            System.out.println("Invalid email format. Please try again.");
        }
 
        String roomType;
        while (true) {
            System.out.print("Enter room type (D = Deluxe, S = Standard, SU = Suite): ");
            String code = sc.nextLine();
            roomType = control.parseRoomTypeCode(code);
            if (roomType != null) {
                break;
            }
            System.out.println("Invalid room type code. Please try again.");
        }
 
        String registrationId = "R" + System.currentTimeMillis() % 100000;
        String registrationTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
 
        Guest guest = control.registerGuest(registrationId, name, contact, email, registrationTime, roomType);
 
        if (guest != null) {
            System.out.println("Guest registered successfully. Guest ID: " + guest.getGuestId());
        } else {
            System.out.println("This guest is already in the waiting queue.");
        }
    }
 
    private void processNextGuest() {
        Booking booking = control.processNextGuest();
        if (booking == null) {
            System.out.println("No guest processed — either no one is waiting, or no matching room is available.");
        } else {
            System.out.println("Booking created!");
            System.out.println(booking);
        }
    }
 
    private void searchGuest() {
        System.out.print("Enter guest ID to search: ");
        String id = sc.nextLine();
        var result = control.searchByGuestId(id);
        System.out.println(result != null ? "Found: " + result : "Guest not found in waiting list.");
    }
}