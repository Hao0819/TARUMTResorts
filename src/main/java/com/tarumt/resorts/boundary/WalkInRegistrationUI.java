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

        // Temporary constructor for running this module independently.
        public WalkInRegistrationUI() {
                this(
                                new WalkInRegistrationControl(),
                                new Scanner(System.in));
        }

        // Constructor that accepts an initialized Control.
        public WalkInRegistrationUI(
                        WalkInRegistrationControl sharedControl) {

                this(sharedControl, new Scanner(System.in));
        }

        // Constructor used when Main provides the Control and Scanner.
        public WalkInRegistrationUI(
                        WalkInRegistrationControl sharedControl,
                        Scanner sharedScanner) {

                // Keep the same references provided by Main.
                control = sharedControl;
                sc = sharedScanner;
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
                                        "4. Walk-In Registration Analysis Report");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "5. Room-Type Demand and Availability Report");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "6. Search registration history by Guest ID");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "7. Update requested room type");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "8. Cancel waiting registration");

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
                                case 4 -> displayRegistrationAnalysisReport();
                                case 5 -> displayRoomTypeDemandReport();
                                case 6 -> searchRegistrationHistory();
                                case 7 -> updateRequestedRoomType();
                                case 8 -> cancelWaitingRegistration();
                                case 0 -> System.out.println("Returning to main menu...");
                                default -> System.out.println("Invalid choice.");
                        }
                        if (choice != 0) {
                                System.out.print(
                                                "\nPress Enter to return to the Walk-In menu...");
                                sc.nextLine();
                        }

                } while (choice != 0);
        }

        private void registerGuest() {
                String contact;

                while (true) {
                        System.out.print(
                                        "Enter Malaysian mobile number "
                                                        + "(10-11 digits, starts with 01): ");

                        contact = sc.nextLine();

                        if (control.isValidContact(contact)) {
                                break;
                        }

                        System.out.println(
                                        "Invalid mobile number. "
                                                        + "Use 10-11 digits starting with 01.");
                }

                // Check whether this contact belongs to an existing Guest.
                Guest existingGuest = control.findGuestByContact(contact);

                // High-tier members bypass the Standard FIFO queue.
                if (existingGuest != null
                                && existingGuest.getMembershipTier().isPriorityTier()) {

                        System.out.println(
                                        "This guest is a "
                                                        + existingGuest.getMembershipTier()
                                                        + " member and must use VIP Priority Allocation.");
                        return;
                }
                
                String name;
                String email;

                if (existingGuest != null) {
                        // Reuse the existing profile instead of creating a duplicate Guest.
                        name = existingGuest.getName();
                        email = existingGuest.getEmail();

                        System.out.println(
                                        "Existing guest found: "
                                                        + existingGuest.getGuestId()
                                                        + " - "
                                                        + existingGuest.getName());

                        System.out.println(
                                        "Using existing guest details. "
                                                        + "A new walk-in registration will be created.");
                } else {
                        // Only request personal details when this is a new Guest.
                        while (true) {
                                System.out.print("Enter guest name: ");
                                name = sc.nextLine().trim();

                                if (control.isValidName(name)) {
                                        break;
                                }

                                System.out.println(
                                                "Guest name cannot be blank!");
                        }

                        while (true) {
                                System.out.print("Enter email: ");
                                email = sc.nextLine().trim();

                                if (control.isValidEmail(email)) {
                                        break;
                                }

                                System.out.println(
                                                "Invalid email. "
                                                                + "Example: guest@example.com");
                        }
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
                        WalkInRegistration registration = control.searchByGuestId(guest.getGuestId());

                        displayRegistrationResult(registration);
                } else {
                        System.out.println(
                                        "This guest is already in the waiting queue.");
                }
        }

        private void displayRegistrationResult(
                        WalkInRegistration registration) {

                String border = "+----------+----------+----------------------+"
                                + "---------------+------------+------------------+";

                String title = "WALK-IN REGISTRATION RESULT";
                int contentWidth = 90;

                int leftPadding = (contentWidth - title.length()) / 2;

                int rightPadding = contentWidth - title.length() - leftPadding;

                System.out.println();
                System.out.println(border);

                System.out.println(
                                "| "
                                                + " ".repeat(leftPadding)
                                                + title
                                                + " ".repeat(rightPadding)
                                                + " |");

                System.out.println(border);

                System.out.printf(
                                "| %-8s | %-8s | %-20s | %-13s | %-10s | %-16s |%n",
                                "Reg ID",
                                "Guest ID",
                                "Guest Name",
                                "Contact",
                                "Room Type",
                                "Registered Time");

                System.out.println(border);

                System.out.printf(
                                "| %-8.8s | %-8.8s | %-20.20s | %-13.13s | "
                                                + "%-10.10s | %-16.16s |%n",
                                registration.getRegistrationId(),
                                registration.getGuest().getGuestId(),
                                registration.getGuest().getName(),
                                registration.getGuest().getContactNumber(),
                                registration.getRequestedRoomType(),
                                registration.getRegistrationTime());

                System.out.println(border);
                System.out.println(
                                "Walk-in registration created successfully.");
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
                                + "----------+------------+------------------+-----------+";

                String title = "BOOKING ALLOCATION RESULT";
                int contentWidth = 99;

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
                                                + "%-16s | %-9s |%n",
                                "Confirm ID",
                                "Guest ID",
                                "Guest Name",
                                "Room No.",
                                "Room Type",
                                "Booking Time",
                                "Status");

                System.out.println(border);

                System.out.printf(
                                "| %-10.10s | %-8.8s | %-20.20s | %-8.8s | "
                                                + "%-10.10s | %-16.16s | %-9.9s |%n",
                                booking.getConfirmationNumber(),
                                booking.getGuest().getGuestId(),
                                booking.getGuest().getName(),
                                booking.getRoom().getRoomNumber(),
                                booking.getRoom().getRoomType(),
                                booking.getBookingCreatedTime(),
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

        private void displayRegistrationAnalysisReport() {
                String roomTypeFilter;

                while (true) {
                        System.out.println("\nRoom Type Filter");
                        System.out.println("1. All Room Types");
                        System.out.println("2. Standard");
                        System.out.println("3. Deluxe");
                        System.out.println("4. Suite");
                        System.out.print("Enter choice: ");

                        String choice = sc.nextLine().trim();

                        switch (choice) {
                                case "1" -> roomTypeFilter = "ALL";
                                case "2" -> roomTypeFilter = "Standard";
                                case "3" -> roomTypeFilter = "Deluxe";
                                case "4" -> roomTypeFilter = "Suite";
                                default -> {
                                        System.out.println(
                                                        "Invalid room type filter. Please try again.");
                                        continue;
                                }
                        }

                        break;
                }

                String statusFilter;

                while (true) {
                        System.out.println("\nRegistration Status Filter");
                        System.out.println("1. All Statuses");
                        System.out.println("2. Waiting");
                        System.out.println("3. Assigned");
                        System.out.println("4. Cancelled");
                        System.out.print("Enter choice: ");

                        String choice = sc.nextLine().trim();

                        switch (choice) {
                                case "1" -> statusFilter = "ALL";
                                case "2" -> statusFilter = "WAITING";
                                case "3" -> statusFilter = "ASSIGNED";
                                case "4" -> statusFilter = "CANCELLED";
                                default -> {
                                        System.out.println(
                                                        "Invalid status filter. Please try again.");
                                        continue;
                                }
                        }

                        break;
                }

                // Filter history using both criteria.
                WalkInRegistration[] reportRecords = control.filterRegistrationHistory(
                                roomTypeFilter,
                                statusFilter);

                // Sort the filtered array by registration time ascending.
                control.sortByRegistrationTime(reportRecords);

                String border = "+----------+----------+----------------------+"
                                + "------------+------------------+-----------+";
                String title = "WALK-IN REGISTRATION ANALYSIS REPORT";
                int contentWidth = 86;

                int leftPadding = (contentWidth - title.length()) / 2;

                int rightPadding = contentWidth - title.length() - leftPadding;

                System.out.println();
                System.out.println(border);

                System.out.println(
                                "| "
                                                + " ".repeat(leftPadding)
                                                + title
                                                + " ".repeat(rightPadding)
                                                + " |");

                System.out.println(border);

                String generatedTime = java.time.LocalDateTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern(
                                                "yyyy-MM-dd HH:mm"));

                System.out.printf(
                                "| %-86s |%n",
                                "Generated at: " + generatedTime);

                System.out.printf(
                                "| %-86s |%n",
                                "Room Type Filter: " + roomTypeFilter);

                System.out.printf(
                                "| %-86s |%n",
                                "Status Filter: " + statusFilter);

                System.out.printf(
                                "| %-86s |%n",
                                "Sorted by: Registration Time (Ascending)");

                System.out.println(border);

                System.out.printf(
                                "| %-8s | %-8s | %-20s | %-10s | %-16s | %-9s |%n",
                                "Reg ID",
                                "Guest ID",
                                "Guest Name",
                                "Room Type",
                                "Registered Time",
                                "Status");

                System.out.println(border);

                if (reportRecords.length == 0) {
                        System.out.printf(
                                        "| %-86s |%n",
                                        "No registration records match the selected filters.");
                } else {
                        for (int i = 0; i < reportRecords.length; i++) {
                                WalkInRegistration registration = reportRecords[i];

                                System.out.printf(
                                                "| %-8.8s | %-8.8s | %-20.20s | "
                                                                + "%-10.10s | %-16.16s | %-9.9s |%n",
                                                registration.getRegistrationId(),
                                                registration.getGuest().getGuestId(),
                                                registration.getGuest().getName(),
                                                registration.getRequestedRoomType(),
                                                registration.getRegistrationTime(),
                                                registration.getStatus());
                        }
                }

                System.out.println(border);

                System.out.printf(
                                "| %-86s |%n",
                                "Total matching records: " + reportRecords.length);

                System.out.println(border);
        }

        private void displayRoomTypeDemandReport() {
                String[] roomTypes = {
                                "Standard", "Deluxe", "Suite"
                };

                String border = "+------------+----------------+-------------+"
                                + "-----------------+---------+------------+";

                String title = "ROOM-TYPE DEMAND AND AVAILABILITY REPORT";

                int contentWidth = 84;
                int leftPadding = (contentWidth - title.length()) / 2;
                int rightPadding = contentWidth - title.length() - leftPadding;

                System.out.println();
                System.out.println(border);

                System.out.printf(
                                "|%" + leftPadding + "s%s%"
                                                + rightPadding + "s|%n",
                                "", title, "");

                System.out.println(border);

                System.out.printf(
                                "| %-10s | %-14s | %-11s | %-15s | %-7s | %-10s |%n",
                                "Room Type",
                                "Waiting Demand",
                                "Total Rooms",
                                "Allocatable",
                                "Balance",
                                "Assessment");

                System.out.println(border);

                for (int i = 0; i < roomTypes.length; i++) {
                        String roomType = roomTypes[i];

                        int waitingDemand = control.countWaitingByRoomType(roomType);

                        int totalRooms = control.countTotalRoomsByType(roomType);

                        int availableRooms = control.countAvailableRoomsByType(roomType);

                        int balance = availableRooms - waitingDemand;

                        String assessment;

                        if (balance > 0) {
                                assessment = "SURPLUS";
                        } else if (balance == 0) {
                                assessment = "EXACT";
                        } else {
                                assessment = "SHORTAGE";
                        }

                        System.out.printf(
                                        "| %-10s | %-14d | %-11d | %-15d | %-7d | %-10s |%n",
                                        roomType,
                                        waitingDemand,
                                        totalRooms,
                                        availableRooms,
                                        balance,
                                        assessment);
                }

                System.out.println(border);
        }

        private void updateRequestedRoomType() {
                if (control.getWaitingCount() == 0) {
                        System.out.println("The waiting queue is empty.");
                        return;
                }

                displayWaitingQueue();

                System.out.print("\nEnter Guest ID to update: ");
                String guestId = sc.nextLine().trim();

                WalkInRegistration registration = control.searchByGuestId(guestId);

                if (registration == null) {
                        System.out.println("No active waiting registration found for Guest ID: " + guestId);
                        return;
                }

                System.out.println(
                                "Current requested room type: "
                                                + registration.getRequestedRoomType());

                String newRoomType;

                while (true) {
                        System.out.print(
                                        "Enter new room type "
                                                        + "(D = Deluxe, S = Standard, SU = Suite): ");

                        newRoomType = control.parseRoomTypeCode(sc.nextLine());

                        if (newRoomType != null) {
                                break;
                        }

                        System.out.println(
                                        "Invalid room type code. Please try again.");
                }

                if (control.updateRequestedRoomType(guestId, newRoomType)) {
                        System.out.println(
                                        "Requested room type updated successfully to "
                                                        + newRoomType + ".");
                } else {
                        System.out.println("Unable to update the registration.");
                }

        }

        private void cancelWaitingRegistration() {
                if (control.getWaitingCount() == 0) {
                        System.out.println("The waiting queue is empty.");
                        return;
                }

                displayWaitingQueue();

                System.out.print("\nEnter Guest ID to cancel: ");
                String guestId = sc.nextLine().trim();

                WalkInRegistration registration = control.searchByGuestId(guestId);

                if (registration == null) {
                        System.out.println(
                                        "No active waiting registration found for Guest ID: "
                                                        + guestId);
                        return;
                }

                String selectedBorder = "+----------+----------+----------------------+------------+";

                System.out.println();
                System.out.println(selectedBorder);
                System.out.println(
                                "|                 SELECTED REGISTRATION                 |");
                System.out.println(selectedBorder);

                System.out.printf(
                                "| %-8s | %-8s | %-20s | %-10s |%n",
                                "Reg ID",
                                "Guest ID",
                                "Guest Name",
                                "Room Type");

                System.out.println(selectedBorder);

                System.out.printf(
                                "| %-8.8s | %-8.8s | %-20.20s | %-10.10s |%n",
                                registration.getRegistrationId(),
                                registration.getGuest().getGuestId(),
                                registration.getGuest().getName(),
                                registration.getRequestedRoomType());

                System.out.println(selectedBorder);

                System.out.print(
                                "Confirm cancellation (Y/N): ");
                String confirmation = sc.nextLine().trim();

                if (!confirmation.equalsIgnoreCase("Y")) {
                        System.out.println("Cancellation aborted.");
                        return;
                }

                if (control.cancelWaitingRegistration(guestId)) {
                        System.out.println(
                                        "Waiting registration cancelled successfully.");
                } else {
                        System.out.println(
                                        "Unable to cancel the registration.");
                }
        }

        private void displayGuestDirectory() {
                Guest[] guests = control.getAllGuests();

                String border = "+----------+----------------------+"
                                + "---------------+------------+";

                String title = "GUEST ID DIRECTORY";
                int contentWidth = 60;

                int leftPadding = (contentWidth - title.length()) / 2;

                int rightPadding = contentWidth - title.length() - leftPadding;

                System.out.println();
                System.out.println(border);

                System.out.println(
                                "| "
                                                + " ".repeat(leftPadding)
                                                + title
                                                + " ".repeat(rightPadding)
                                                + " |");

                System.out.println(border);

                System.out.printf(
                                "| %-8s | %-20s | %-13s | %-10s |%n",
                                "Guest ID",
                                "Guest Name",
                                "Contact",
                                "Tier");

                System.out.println(border);

                for (int i = 0; i < guests.length; i++) {
                        Guest guest = guests[i];

                        System.out.printf(
                                        "| %-8.8s | %-20.20s | %-13.13s | %-10.10s |%n",
                                        guest.getGuestId(),
                                        guest.getName(),
                                        guest.getContactNumber(),
                                        guest.getMembershipTier());
                }

                System.out.println(border);
                System.out.println(
                                "Total guest profiles: " + guests.length);
        }

        private void searchRegistrationHistory() {
                // Show available Guest IDs before asking staff to choose one.
                displayGuestDirectory();
                System.out.print(
                                "\nEnter Guest ID to search registration history: ");

                String guestId = sc.nextLine().trim();

                WalkInRegistration[] registrations = control.searchRegistrationHistoryByGuestId(guestId);

                if (registrations.length == 0) {
                        System.out.println(
                                        "No registration history found for Guest ID: "
                                                        + guestId);
                        return;
                }

                // Ensure history is displayed chronologically.
                control.sortByRegistrationTime(registrations);

                Guest guest = registrations[0].getGuest();

                String profileBorder = "+----------------------+--------------------------------+";

                String profileTitle = "GUEST PROFILE";
                int profileContentWidth = 53;

                int profileLeftPadding = (profileContentWidth - profileTitle.length()) / 2;

                int profileRightPadding = profileContentWidth
                                - profileTitle.length()
                                - profileLeftPadding;

                System.out.println();
                System.out.println(profileBorder);

                System.out.println(
                                "| "
                                                + " ".repeat(profileLeftPadding)
                                                + profileTitle
                                                + " ".repeat(profileRightPadding)
                                                + " |");

                System.out.println(profileBorder);

                System.out.printf(
                                "| %-20s | %-30.30s |%n",
                                "Guest ID",
                                guest.getGuestId());

                System.out.printf(
                                "| %-20s | %-30.30s |%n",
                                "Guest Name",
                                guest.getName());

                System.out.printf(
                                "| %-20s | %-30.30s |%n",
                                "Contact Number",
                                guest.getContactNumber());

                System.out.printf(
                                "| %-20s | %-30.30s |%n",
                                "Email",
                                guest.getEmail());

                System.out.printf(
                                "| %-20s | %-30.30s |%n",
                                "Membership Tier",
                                guest.getMembershipTier());

                System.out.println(profileBorder);

                String historyBorder = "+----------+------------+------------------+-----------+";

                String historyTitle = "REGISTRATION HISTORY";
                int historyContentWidth = 52;

                int historyLeftPadding = (historyContentWidth - historyTitle.length()) / 2;

                int historyRightPadding = historyContentWidth
                                - historyTitle.length()
                                - historyLeftPadding;

                System.out.println();
                System.out.println(historyBorder);

                System.out.println(
                                "| "
                                                + " ".repeat(historyLeftPadding)
                                                + historyTitle
                                                + " ".repeat(historyRightPadding)
                                                + " |");

                System.out.println(historyBorder);

                System.out.printf(
                                "| %-8s | %-10s | %-16s | %-9s |%n",
                                "Reg ID",
                                "Room Type",
                                "Registered Time",
                                "Status");

                System.out.println(historyBorder);

                for (int i = 0; i < registrations.length; i++) {
                        WalkInRegistration registration = registrations[i];

                        System.out.printf(
                                        "| %-8.8s | %-10.10s | %-16.16s | %-9.9s |%n",
                                        registration.getRegistrationId(),
                                        registration.getRequestedRoomType(),
                                        registration.getRegistrationTime(),
                                        registration.getStatus());
                }

                System.out.println(historyBorder);
                System.out.println(
                                "Total registration records: "
                                                + registrations.length);
        }

}