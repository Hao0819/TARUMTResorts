/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.WalkInRegistrationControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.WalkInRegistration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Scanner;

/**
 * WalkInRegistrationUI.java
 * Console interface for the Walk-In Registrations module.
 *
 * @author Junhao
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

        /**
         * Displays a monthly booking calendar for one room type.
         * An unavailable starting date is shown as X.
         */
        private void displayBookingCalendar(
                        YearMonth selectedMonth,
                        String roomType) {

                String border = "+------+------+------+------+------+------+------+";

                String title = selectedMonth.getMonth()
                                + " "
                                + selectedMonth.getYear()
                                + " - "
                                + roomType.toUpperCase()
                                + " NIGHTLY AVAILABILITY";
                int contentWidth = 48;
                int leftPadding = (contentWidth - title.length()) / 2;
                int rightPadding = contentWidth
                                - title.length()
                                - leftPadding;

                System.out.println();
                System.out.println(border);

                System.out.printf(
                                "|%s%s%s|%n",
                                " ".repeat(leftPadding),
                                title,
                                " ".repeat(rightPadding));

                System.out.println(border);
                System.out.println(
                                "| Sun  | Mon  | Tue  | Wed  | Thu  | Fri  | Sat  |");
                System.out.println(border);

                LocalDate firstDate = selectedMonth.atDay(1);

                int currentColumn = firstDate.getDayOfWeek()
                                .getValue() % 7;

                // Print empty cells before the first day of the month.
                for (int column = 0; column < currentColumn; column++) {

                        System.out.print("|      ");
                }

                int numberOfDays = selectedMonth.lengthOfMonth();

                for (int day = 1; day <= numberOfDays; day++) {

                        LocalDate calendarDate = selectedMonth.atDay(day);

                        boolean dateAvailable = control.hasAvailableRoomForSchedule(
                                        roomType,
                                        calendarDate,
                                        1);

                        String displayedValue = dateAvailable
                                        ? String.valueOf(day)
                                        : "X";

                        System.out.printf(
                                        "| %4s ",
                                        displayedValue);

                        currentColumn++;

                        if (currentColumn == 7) {
                                System.out.println("|");
                                currentColumn = 0;
                        }
                }

                // Complete the final row when the month does not end on Saturday.
                if (currentColumn != 0) {
                        while (currentColumn < 7) {
                                System.out.print("|      ");
                                currentColumn++;
                        }

                        System.out.println("|");
                }

                System.out.println(border);
                System.out.println(
                                "X = All rooms of this type are booked for that night.");
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

                int stayDurationDays;

                while (true) {
                        System.out.print(
                                        "Enter stay duration in nights (1-30): ");

                        try {
                                stayDurationDays = Integer.parseInt(
                                                sc.nextLine().trim());

                                if (stayDurationDays >= 1
                                                && stayDurationDays <= 30) {

                                        break;
                                }

                        } catch (NumberFormatException exception) {
                                // The validation message below handles invalid input.
                        }

                        System.out.println(
                                        "Stay duration must be between 1 and 30 nights.");
                }

                LocalDate requestedCheckInDate = null;

                while (requestedCheckInDate == null) {
                        YearMonth selectedMonth;

                        while (true) {
                                System.out.print(
                                                "Enter booking month (YYYY-MM): ");

                                try {
                                        selectedMonth = YearMonth.parse(
                                                        sc.nextLine().trim());

                                        if (selectedMonth.isBefore(
                                                        YearMonth.now())) {

                                                System.out.println(
                                                                "Booking month cannot be in the past.");
                                                continue;
                                        }

                                        break;

                                } catch (java.time.format.DateTimeParseException exception) {

                                        System.out.println(
                                                        "Invalid month. Example: 2026-08");
                                }
                        }

                        displayBookingCalendar(
                                        selectedMonth,
                                        roomType);

                        while (true) {
                                System.out.print(
                                                "Enter check-in date (YYYY-MM-DD)"
                                                                + " or M to change month: ");

                                String dateInput = sc.nextLine().trim();

                                if (dateInput.equalsIgnoreCase("M")) {
                                        break;
                                }

                                LocalDate selectedDate;

                                try {
                                        selectedDate = LocalDate.parse(dateInput);

                                } catch (java.time.format.DateTimeParseException exception) {

                                        System.out.println(
                                                        "Invalid date. Example: 2026-08-12");
                                        continue;
                                }

                                if (!YearMonth.from(selectedDate)
                                                .equals(selectedMonth)) {

                                        System.out.println(
                                                        "Please select a date from the displayed month.");
                                        continue;
                                }

                                boolean dateAvailable = control.hasAvailableRoomForSchedule(
                                                roomType,
                                                selectedDate,
                                                stayDurationDays);

                                if (!dateAvailable) {
                                        LocalDate requestedCheckOutDate = selectedDate.plusDays(
                                                        stayDurationDays);

                                        System.out.println(
                                                        "No single "
                                                                        + roomType
                                                                        + " room is available for the complete stay from "
                                                                        + selectedDate
                                                                        + " to "
                                                                        + requestedCheckOutDate
                                                                        + ".");

                                        System.out.println(
                                                        "Please choose another check-in date "
                                                                        + "or enter M to change month.");

                                        continue;
                                }

                                requestedCheckInDate = selectedDate;

                                break;
                        }
                }

                String registrationTime = java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                Guest guest = control.registerGuest(
                                name,
                                contact,
                                email,
                                registrationTime,
                                roomType,
                                requestedCheckInDate,
                                stayDurationDays);

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

                String border = "+----------------------+--------------------------------+";

                String title = "WALK-IN REGISTRATION RESULT";

                int contentWidth = 55;
                int leftPadding = (contentWidth - title.length()) / 2;
                int rightPadding = contentWidth
                                - title.length()
                                - leftPadding;

                String durationText = registration.getStayDurationDays()
                                + (registration.getStayDurationDays() == 1
                                                ? " night"
                                                : " nights");

                System.out.println();
                System.out.println(border);

                System.out.printf(
                                "|%s%s%s|%n",
                                " ".repeat(leftPadding),
                                title,
                                " ".repeat(rightPadding));

                System.out.println(border);

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Registration ID",
                                registration.getRegistrationId());

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Guest ID",
                                registration.getGuest().getGuestId());

                System.out.printf(
                                "| %-20s | %-30.30s |%n",
                                "Guest Name",
                                registration.getGuest().getName());

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Contact Number",
                                registration.getGuest().getContactNumber());

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Room Type",
                                registration.getRequestedRoomType());

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Requested Check-In",
                                registration.getRequestedCheckInDate());

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Requested Check-Out",
                                registration.getRequestedCheckOutDate());

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Stay Duration",
                                durationText);

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Registered Time",
                                registration.getRegistrationTime());

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Status",
                                registration.getStatus());

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
                String border = "+----------------------+--------------------------------------+";

                String title = "BOOKING ALLOCATION RESULT";

                int contentWidth = 61;
                int leftPadding = (contentWidth - title.length()) / 2;
                int rightPadding = contentWidth
                                - title.length()
                                - leftPadding;

                String durationText = booking.getStayDurationDays()
                                + (booking.getStayDurationDays() == 1
                                                ? " night"
                                                : " nights");

                String dailyRateText = String.format(
                                "RM %,.2f",
                                booking.getRoom().getDailyRate());

                String totalAmountText = String.format(
                                "RM %,.2f",
                                booking.getAmount());

                System.out.println();
                System.out.println(border);

                System.out.printf(
                                "|%s%s%s|%n",
                                " ".repeat(leftPadding),
                                title,
                                " ".repeat(rightPadding));

                System.out.println(border);

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Confirmation No.",
                                booking.getConfirmationNumber());

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Guest ID",
                                booking.getGuest().getGuestId());

                System.out.printf(
                                "| %-20s | %-36.36s |%n",
                                "Guest Name",
                                booking.getGuest().getName());

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Room Number",
                                booking.getRoom().getRoomNumber());

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Room Type",
                                booking.getRoom().getRoomType());

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Daily Rate",
                                dailyRateText);

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Scheduled Check-In",
                                booking.getScheduledCheckInDate());

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Scheduled Check-Out",
                                booking.getScheduledCheckOutDate());

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Stay Duration",
                                durationText);

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Total Amount",
                                totalAmountText);

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Booking Created",
                                booking.getBookingCreatedTime());

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Booking Status",
                                booking.getStatus());

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Payment Status",
                                booking.getPaymentStatus());

                System.out.println(border);
                System.out.println(
                                "Booking created successfully.");
        }

        private void displayWaitingQueue() {
                WalkInRegistration[] waiting = control.getAllWaitingRegistrations();

                String border = "+-----+----------+----------+------------+"
                                + "------------+------------+--------+------------------+";

                String title = "ACTIVE WALK-IN WAITING QUEUE";

                int contentWidth = 94;
                int leftPadding = (contentWidth - title.length()) / 2;
                int rightPadding = contentWidth
                                - title.length()
                                - leftPadding;

                System.out.println();
                System.out.println(border);

                System.out.printf(
                                "|%s%s%s|%n",
                                " ".repeat(leftPadding),
                                title,
                                " ".repeat(rightPadding));

                System.out.println(border);

                if (waiting.length == 0) {
                        System.out.printf(
                                        "| %-92s |%n",
                                        "No guests are currently waiting.");

                        System.out.println(border);
                        return;
                }

                System.out.printf(
                                "| %-3s | %-8s | %-8s | %-10s | "
                                                + "%-10s | %-10s | %-6s | %-16s |%n",
                                "Pos",
                                "Reg ID",
                                "Guest ID",
                                "Room Type",
                                "Check-In",
                                "Check-Out",
                                "Nights",
                                "Registered Time");

                System.out.println(border);

                // Array order is the same as the active FIFO queue order.
                for (int position = 0; position < waiting.length; position++) {

                        WalkInRegistration registration = waiting[position];

                        System.out.printf(
                                        "| %-3d | %-8.8s | %-8.8s | %-10.10s | "
                                                        + "%-10s | %-10s | %-6d | %-16.16s |%n",
                                        position + 1,
                                        registration.getRegistrationId(),
                                        registration.getGuest().getGuestId(),
                                        registration.getRequestedRoomType(),
                                        registration.getRequestedCheckInDate(),
                                        registration.getRequestedCheckOutDate(),
                                        registration.getStayDurationDays(),
                                        registration.getRegistrationTime());
                }

                System.out.println(border);
                System.out.println(
                                "Total waiting guests: "
                                                + waiting.length);
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

                // Display available Reg ID and Guest ID values for staff selection.
                displayWaitingQueue();

                System.out.print("\nEnter Registration ID to cancel: ");
                String registrationId = sc.nextLine().trim();

                System.out.print("Enter Guest ID for verification: ");
                String guestId = sc.nextLine().trim();

                if (registrationId.isEmpty() || guestId.isEmpty()) {
                        System.out.println(
                                        "Registration ID and Guest ID cannot be blank.");
                        return;
                }

                System.out.println();
                System.out.println(
                                "+--------------------------------------------------+");
                System.out.println(
                                "|              CANCELLATION CONFIRMATION           |");
                System.out.println(
                                "+--------------------------------------------------+");

                System.out.printf(
                                "| %-18s | %-27s |%n",
                                "Registration ID",
                                registrationId.toUpperCase());

                System.out.printf(
                                "| %-18s | %-27s |%n",
                                "Guest ID",
                                guestId.toUpperCase());

                System.out.println(
                                "+--------------------------------------------------+");

                System.out.print("Confirm cancellation (Y/N): ");
                String confirmation = sc.nextLine().trim();

                if (!confirmation.equalsIgnoreCase("Y")) {
                        System.out.println("Cancellation aborted.");
                        return;
                }

                boolean registrationCancelled = control.cancelWaitingRegistration(
                                registrationId,
                                guestId);

                if (registrationCancelled) {
                        System.out.println(
                                        "Waiting registration cancelled successfully.");
                } else {
                        System.out.println(
                                        "Cancellation failed. The Registration ID and Guest ID "
                                                        + "do not match an active WAITING registration.");
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