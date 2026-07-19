/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.WalkInRegistrationControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.WalkInRegistration;

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
            int menuWidth = 64;
            // Include the spaces placed on both sides of the 64-character content.
            String menuBorder = "+" + "-".repeat(menuWidth + 2) + "+";
            String menuTitle = "WALK-IN REGISTRATION MODULE";

            int leftPadding = (menuWidth - menuTitle.length()) / 2;

            int rightPadding = menuWidth - menuTitle.length() - leftPadding;

            System.out.println();
            System.out.println(menuBorder);

            // Centre the module title within the menu border.
            System.out.println(
                    "| "
                            + " ".repeat(leftPadding)
                            + menuTitle
                            + " ".repeat(rightPadding)
                            + " |");

            System.out.println(menuBorder);

            System.out.printf(
                    "| %-64s |%n",
                    "1. Register new walk-in guest");

            System.out.printf(
                    "| %-64s |%n",
                    "2. Process next guest");

            System.out.printf(
                    "| %-64s |%n",
                    "3. View active waiting queue");

            System.out.printf(
                    "| %-64s |%n",
                    "4. Walk-In Registration Analysis Report (not implemented)");

            System.out.printf(
                    "| %-64s |%n",
                    "5. Room-Type Demand and Availability Report (not implemented)");

            System.out.printf(
                    "| %-64s |%n",
                    "6. Search waiting registration by Guest ID");

            System.out.printf(
                    "| %-64s |%n",
                    "0. Back to main menu");

            System.out.println(menuBorder);
            System.out.print("Enter choice: ");

            // prevent input letters, symbols, blank
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                choice = -1;// sentinel: not 0, so the do-while loop continues
                continue;
            }

            switch (choice) {
                case 1 -> registerGuest();
                case 2 -> processNextGuest();
                case 3 -> displayWaitingQueue();
                case 4, 5 -> System.out.println("Not implemented yet.");
                case 6 -> searchGuest();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private void registerGuest() {
        String name;

        while (true) {
            System.out.print("Enter guest name: ");
            name = sc.nextLine().trim();

            if (control.isValidName(name)) {
                break;
            }
            System.out.println("Guest name cannot be blank!");
        }

        String contact;
        while (true) {
            System.out.print("Enter Malaysian mobile number (10-11 digits, starts with 01): ");
            contact = sc.nextLine();
            if (control.isValidContact(contact)) {
                break;
            }
            System.out.println("Invalid mobile number. Use 10-11 digits starting with 01.");
        }

        String email;
        while (true) {
            System.out.print("Enter email: ");
            email = sc.nextLine().trim();
            if (control.isValidEmail(email)) {
                break;
            }
            System.out.println("Invalid email. Example: guest@example.com");
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

        String registrationTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        Guest guest = control.registerGuest(name, contact, email, registrationTime, roomType);

        if (guest != null) {
            System.out.println("Guest registered successfully. Guest ID: " + guest.getGuestId());
        } else {
            System.out.println("This guest is already in the waiting queue.");
        }
    }

    private void processNextGuest() {
        // compares the number of waiting registrations with 0
        if (control.getWaitingCount() == 0) {
            System.out.println("The waiting queue is empty.");
            return;
        }
        // Peeks at the front registration and searches for a room.
        // Returns a new Booking or null.
        Booking booking = control.processNextGuest();
        if (booking == null) { // no matching available room was found
            System.out.println(
                    "No matching available room for the front guest. "
                            + "The guest remains at the front of the queue.");
        } else {
            displayBookingResult(booking);
        }
    }

    private void displayBookingResult(Booking booking) {
        String border = "+------------+----------+----------------------+"
                + "----------+------------+------------------+----------+";

        String title = "BOOKING ALLOCATION RESULT";
        int contentWidth = 98;

        int leftPadding = (contentWidth - title.length()) / 2;

        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);

        // Centre the title within the booking-result table.
        System.out.println(
                "| "
                        + " ".repeat(leftPadding)
                        + title
                        + " ".repeat(rightPadding)
                        + " |");

        System.out.println(border);

        System.out.printf(
                "| %-10s | %-8s | %-20s | %-8s | %-10s | "
                        + "%-16s | %-8s |%n",
                "Confirm ID",
                "Guest ID",
                "Guest Name",
                "Room No.",
                "Room Type",
                "Allocation Time",
                "Status");

        System.out.println(border);

        System.out.printf(
                "| %-10.10s | %-8.8s | %-20.20s | %-8.8s | "
                        + "%-10.10s | %-16.16s | %-8.8s |%n",
                booking.getConfirmationNumber(),
                booking.getGuest().getGuestId(),
                booking.getGuest().getName(),
                booking.getRoom().getRoomNumber(),
                booking.getRoom().getRoomType(),
                booking.getCheckInTime(),
                booking.getStatus());

        System.out.println(border);
        System.out.println("Booking created successfully.");
    }

    private void displayWaitingQueue() {
        WalkInRegistration[] waiting = control.getAllWaitingRegistrations();

        String border = "+-----+----------+----------+------------+"
                + "------------------+";

        String title = "ACTIVE WALK-IN WAITING QUEUE";
        int contentWidth = 57;

        int leftPadding = (contentWidth - title.length()) / 2;

        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);

        // Centre the title within the waiting-queue table.
        System.out.println(
                "| "
                        + " ".repeat(leftPadding)
                        + title
                        + " ".repeat(rightPadding)
                        + " |");

        System.out.println(border);

        if (waiting.length == 0) {
            System.out.printf(
                    "| %-57s |%n",
                    "No guests are currently waiting.");
            System.out.println(border);
            return;
        }

        System.out.printf(
                "| %-3s | %-8s | %-8s | %-10s | %-16s |%n",
                "Pos",
                "Reg ID",
                "Guest ID",
                "Room Type",
                "Registered Time");

        System.out.println(border);

        // Preserve the original FIFO order when displaying the queue.
        for (int i = 0; i < waiting.length; i++) {
            WalkInRegistration registration = waiting[i];

            System.out.printf(
                    "| %-3d | %-8.8s | %-8.8s | %-10.10s | %-16.16s |%n",
                    i + 1,
                    registration.getRegistrationId(),
                    registration.getGuest().getGuestId(),
                    registration.getRequestedRoomType(),
                    registration.getRegistrationTime());
        }

        System.out.println(border);
        System.out.println(
                "Total waiting guests: " + waiting.length);
    }

    private void searchGuest() {
        if (control.getWaitingCount() == 0) {
            System.out.println(
                    "\nNo waiting registrations are available to search.");
            return;
        }

        // Show the FIFO queue so staff can see the available Guest IDs.
        displayWaitingQueue();

        System.out.print("\nEnter Guest ID to search: ");
        String guestId = sc.nextLine().trim();

        WalkInRegistration result = control.searchByGuestId(guestId);

        if (result == null) {
            System.out.println(
                    "No active waiting registration found for Guest ID: "
                            + guestId);
            return;
        }

        String border = "+----------+----------+----------------------+"
                + "------------+------------------+----------+";

        String title = "WAITING REGISTRATION SEARCH RESULT";
        int contentWidth = 85;

        int leftPadding = (contentWidth - title.length()) / 2;

        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);

        // Centre the search-result title inside the table.
        System.out.println(
                "| "
                        + " ".repeat(leftPadding)
                        + title
                        + " ".repeat(rightPadding)
                        + " |");

        System.out.println(border);

        System.out.printf(
                "| %-8s | %-8s | %-20s | %-10s | %-16s | %-8s |%n",
                "Reg ID",
                "Guest ID",
                "Guest Name",
                "Room Type",
                "Registered Time",
                "Status");

        System.out.println(border);

        System.out.printf(
                "| %-8.8s | %-8.8s | %-20.20s | %-10.10s | "
                        + "%-16.16s | %-8.8s |%n",
                result.getRegistrationId(),
                result.getGuest().getGuestId(),
                result.getGuest().getName(),
                result.getRequestedRoomType(),
                result.getRegistrationTime(),
                result.getStatus());

        System.out.println(border);
    }
}