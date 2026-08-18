/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.WalkInRegistrationControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.WalkInRegistration;
import com.tarumt.resorts.entity.RegistrationChange;

import java.time.format.DateTimeFormatter;
import java.util.Iterator;
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
                                        "1. Submit standard room booking request");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "2. Process next standard booking request");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "3. View standard booking request queue");
                        System.out.printf(
                                        "| %-64s |%n",
                                        "4. Update waiting booking request");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "5. Cancel waiting booking request");
                        System.out.printf(
                                        "| %-64s |%n",
                                        "6. Search standard booking requests");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "7. Walk-In Registration Analysis Report");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "8. Room-Type Demand and Availability Report");

                        System.out.printf(
                                        "| %-64s |%n",
                                        "0. Back to main menu");

                        System.out.println(menuBorder);

                        System.out.println(
                                        "Navigation: Within a function, enter 0 "
                                                        + "to cancel and return to this menu.");

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
                                case 4 -> updateWaitingBookingRequest();
                                case 5 -> cancelWaitingRegistration();
                                case 6 -> searchRegistrationHistory();
                                case 7 -> displayRegistrationAnalysisReport();
                                case 8 -> displayRoomTypeDemandReport();
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
                        String roomType,
                        int stayDurationDays) {

                String border = "+------+------+------+------+------+------+------+";

                String durationText = stayDurationDays == 1
                                ? "1 NIGHT"
                                : stayDurationDays + " NIGHTS";

                String title = selectedMonth.getMonth()
                                + " "
                                + selectedMonth.getYear()
                                + " - "
                                + roomType.toUpperCase()
                                + " - "
                                + durationText;
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

                        String displayedValue;

                        if (calendarDate.isBefore(LocalDate.now())) {
                                // Past dates cannot be selected, but they are not necessarily fully booked.
                                displayedValue = "-";

                        } else {
                                boolean dateAvailable = control.hasAvailableRoomForSchedule(
                                                roomType,
                                                calendarDate,
                                                stayDurationDays);

                                displayedValue = dateAvailable
                                                ? String.valueOf(day)
                                                : "X";
                        }

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
                System.out.println("- = Past date");
                System.out.println("Day number = Available check-in date");
                System.out.println("X = Unavailable for the complete stay");
        }

        private void registerGuest() {
                String contact;

                while (true) {
                        System.out.print(
                                        "Enter Malaysian mobile number "
                                                        + "(10-11 digits, starts with 01): ");

                        contact = sc.nextLine().trim();

                        if (contact.equals("0")) {
                                System.out.println("Booking request cancelled.");
                                return;
                        }

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
                                                        + "A new standard booking request will be created.");
                } else {
                        // Only request personal details when this is a new Guest.
                        while (true) {
                                System.out.print(
                                                "Enter guest name : ");

                                name = sc.nextLine().trim();

                                if (name.equals("0")) {
                                        System.out.println("Booking request cancelled.");
                                        return;
                                }

                                if (control.isValidName(name)) {
                                        break;
                                }

                                System.out.println(
                                                "Guest name cannot be blank!");
                        }

                        while (true) {
                                System.out.print(
                                                "Enter email : ");

                                email = sc.nextLine().trim();

                                if (email.equals("0")) {
                                        System.out.println("Booking request cancelled.");
                                        return;
                                }

                                if (control.isValidEmail(email)) {
                                        break;
                                }

                                System.out.println(
                                                "Invalid email. "
                                                                + "Example: guest@example.com");
                        }
                }

                String roomType;

                // Display the shared room-type choices and current nightly rates.
                displayAvailableRoomTypes();

                while (true) {
                        System.out.print("Enter room type code: ");

                        String roomTypeCode = sc.nextLine().trim();

                        if (roomTypeCode.equals("0")) {
                                System.out.println(
                                                "Booking request cancelled.");
                                return;
                        }

                        // Convert the entered code into the complete room-type name.
                        roomType = control.parseRoomTypeCode(
                                        roomTypeCode);

                        if (roomType != null) {
                                break;
                        }

                        System.out.println(
                                        "Invalid room type code. Please try again.");
                }

                int stayDurationDays;

                while (true) {
                        System.out.print(
                                        "Enter stay duration in nights "
                                                        + "(1-30): ");

                        String stayDurationInput = sc.nextLine().trim();

                        if (stayDurationInput.equals("0")) {
                                System.out.println(
                                                "Booking request cancelled.");
                                return;
                        }

                        try {
                                stayDurationDays = Integer.parseInt(stayDurationInput);

                                if (stayDurationDays >= 1
                                                && stayDurationDays <= 30) {

                                        break;
                                }

                        } catch (NumberFormatException exception) {
                                // The validation message below handles invalid input.
                        }

                        System.out.println(
                                        "Stay duration must be between "
                                                        + "1 and 30 nights.");
                }

                LocalDate requestedCheckInDate = null;

                while (requestedCheckInDate == null) {
                        YearMonth selectedMonth;

                        while (true) {
                                System.out.print(
                                                "Enter booking month "
                                                                + "(YYYY-MM): ");

                                String bookingMonthInput = sc.nextLine().trim();

                                if (bookingMonthInput.equals("0")) {
                                        System.out.println(
                                                        "Booking request cancelled.");
                                        return;
                                }

                                try {
                                        selectedMonth = YearMonth.parse(
                                                        bookingMonthInput);

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
                                        roomType,
                                        stayDurationDays);

                        while (true) {
                                System.out.print(
                                                "Enter check-in day (1-"
                                                                + selectedMonth.lengthOfMonth()
                                                                + ", M = Change Month): ");

                                String dayInput = sc.nextLine().trim();

                                if (dayInput.equals("0")) {
                                        System.out.println(
                                                        "Booking request cancelled.");
                                        return;
                                }
                                if (dayInput.equalsIgnoreCase("M")) {
                                        break;
                                }

                                int selectedDay;

                                try {
                                        selectedDay = Integer.parseInt(dayInput);
                                } catch (NumberFormatException exception) {
                                        System.out.println(
                                                        "Invalid day. Enter a number shown on the calendar.");
                                        continue;
                                }

                                if (selectedDay < 1
                                                || selectedDay > selectedMonth.lengthOfMonth()) {

                                        System.out.println(
                                                        "Invalid day for "
                                                                        + selectedMonth.getMonth()
                                                                        + " "
                                                                        + selectedMonth.getYear()
                                                                        + ".");
                                        continue;
                                }

                                // Combine the selected month with the entered day.
                                LocalDate selectedDate = selectedMonth.atDay(selectedDay);

                                if (selectedDate.isBefore(LocalDate.now())) {
                                        System.out.println(
                                                        "Check-in date cannot be in the past.");
                                        continue;
                                }

                                boolean dateAvailable = control.hasAvailableRoomForSchedule(
                                                roomType,
                                                selectedDate,
                                                stayDurationDays);

                                if (!dateAvailable) {
                                        LocalDate requestedCheckOutDate = selectedDate.plusDays(stayDurationDays);

                                        System.out.println(
                                                        "No single "
                                                                        + roomType
                                                                        + " room is available for the complete stay from "
                                                                        + selectedDate
                                                                        + " to "
                                                                        + requestedCheckOutDate
                                                                        + ".");

                                        System.out.println(
                                                        "Please choose another check-in day "
                                                                        + "or enter M to change month.");

                                        continue;
                                }

                                requestedCheckInDate = selectedDate;
                                break;
                        }
                }

                String registrationTime = java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                Guest guest;

                try {
                        guest = control.registerGuest(
                                        name,
                                        contact,
                                        email,
                                        registrationTime,
                                        roomType,
                                        requestedCheckInDate,
                                        stayDurationDays);

                } catch (IllegalArgumentException exception) {
                        System.out.println(
                                        "Unable to submit booking request: "
                                                        + exception.getMessage());

                        return;
                }

                // Display the request that was just added to the rear of the FIFO queue.
                WalkInRegistration registration = control.findLatestWaitingRegistrationByGuestId(
                                guest.getGuestId());

                displayRegistrationResult(registration);
        }

        /**
         * Displays the room types and current nightly rates obtained from the
         * shared Room collection.
         */
        private void displayAvailableRoomTypes() {

                // Read the latest room rates instead of hard-coding prices in the UI.
                double standardDailyRate = control.getDailyRateByRoomType("Standard");

                double deluxeDailyRate = control.getDailyRateByRoomType("Deluxe");

                double suiteDailyRate = control.getDailyRateByRoomType("Suite");

                System.out.println();
                System.out.println(
                                "+--------+------------+------------------+");
                System.out.println(
                                "|          AVAILABLE ROOM TYPES          |");
                System.out.println(
                                "+--------+------------+------------------+");

                System.out.printf(
                                "| %-6s | %-10s | %-16s |%n",
                                "Code",
                                "Room Type",
                                "Rate Per Night");

                System.out.println(
                                "+--------+------------+------------------+");

                System.out.printf(
                                "| %-6s | %-10s | RM %13.2f |%n",
                                "S",
                                "Standard",
                                standardDailyRate);

                System.out.printf(
                                "| %-6s | %-10s | RM %13.2f |%n",
                                "D",
                                "Deluxe",
                                deluxeDailyRate);

                System.out.printf(
                                "| %-6s | %-10s | RM %13.2f |%n",
                                "SU",
                                "Suite",
                                suiteDailyRate);

                System.out.println(
                                "+--------+------------+------------------+");
        }

        private void displayRegistrationResult(
                        WalkInRegistration registration) {

                String border = "+----------------------+--------------------------------+";

                String title = "STANDARD BOOKING REQUEST RESULT";

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
                                "Standard booking request submitted successfully.");
        }

        private void processNextGuest() {
                // compares the number of waiting registrations with 0
                if (control.getWaitingCount() == 0) {
                        System.out.println("No standard booking requests are waiting.");
                        return;
                }
                if (control.isFrontWaitingRequestExpired()) {
                        System.out.println(
                                        "The front booking request has an expired check-in date.");

                        System.out.println(
                                        "The request remains at the front of the FIFO queue.");

                        System.out.println(
                                        "Cancel the expired request before processing "
                                                        + "the next waiting request.");

                        return;
                }
                // Peeks at the front registration and searches for a room.
                // Returns a new Booking or null.
                Booking booking = control.processNextGuest();
                if (booking == null) { // no matching available room was found
                        System.out.println(
                                        "No matching available room for the front booking request. "
                                                        + "The request remains at the front of the FIFO queue.");
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

                String grossAmountText = String.format(
                                "RM %,.2f",
                                booking.getAmount());

                String discountRateText = String.format(
                                "%.0f%%",
                                booking.getDiscountRate() * 100);

                String discountAmountText = String.format(
                                "RM %,.2f",
                                booking.getDiscountAmount());

                String finalAmountText = String.format(
                                "RM %,.2f",
                                booking.getFinalAmount());

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
                                "Membership Tier",
                                booking.getGuest().getMembershipTier());

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
                                "Gross Amount",
                                grossAmountText);

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Discount Rate",
                                discountRateText);

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Discount Amount",
                                discountAmountText);

                System.out.printf(
                                "| %-20s | %-36s |%n",
                                "Final Amount",
                                finalAmountText);

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
                                "Booking confirmed successfully. "
                                                + "Payment will be collected during check-in.");
        }

        private void displayWaitingQueue() {
                WalkInRegistration[] waiting = control.getAllWaitingRegistrations();

                String border = "+-----+----------+----------+----------------------+"
                                + "------------+------------+------------+--------+------------------+";

                String title = "STANDARD BOOKING REQUEST WAITING QUEUE";

                int contentWidth = 117;
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
                                        "| %-115s |%n",
                                        "No booking requests are currently waiting.");

                        System.out.println(border);
                        return;
                }

                System.out.printf(
                                "| %-3s | %-8s | %-8s | %-20s | %-10s | "
                                                + "%-10s | %-10s | %-6s | %-16s |%n",
                                "Pos",
                                "Reg ID",
                                "Guest ID",
                                "Guest Name",
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
                                        "| %-3d | %-8.8s | %-8.8s | %-20.20s | %-10.10s | "
                                                        + "%-10s | %-10s | %-6d | %-16.16s |%n",
                                        position + 1,
                                        registration.getRegistrationId(),
                                        registration.getGuest().getGuestId(),
                                        registration.getGuest().getName(),
                                        registration.getRequestedRoomType(),
                                        registration.getRequestedCheckInDate(),
                                        registration.getRequestedCheckOutDate(),
                                        registration.getStayDurationDays(),
                                        registration.getRegistrationTime());
                }

                System.out.println(border);
                System.out.println(
                                "Total waiting requests: "
                                                + waiting.length);
        }

        private void displayRegistrationAnalysisReport() {
                String roomTypeFilter;

                while (true) {
                        String filterBorder = "+--------+----------------------+";

                        System.out.println();
                        System.out.println(filterBorder);
                        System.out.println(
                                        "|       ROOM TYPE FILTER        |");
                        System.out.println(filterBorder);

                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "Option",
                                        "Selection");

                        System.out.println(filterBorder);
                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "1",
                                        "All Room Types");
                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "2",
                                        "Standard");
                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "3",
                                        "Deluxe");
                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "4",
                                        "Suite");
                        System.out.println(filterBorder);

                        System.out.print("Enter filter option: ");

                        String choice = sc.nextLine().trim();

                        switch (choice) {
                                case "1" -> roomTypeFilter = "ALL";
                                case "2" -> roomTypeFilter = "Standard";
                                case "3" -> roomTypeFilter = "Deluxe";
                                case "4" -> roomTypeFilter = "Suite";

                                case "0" -> {
                                        System.out.println(
                                                        "Report generation cancelled.");
                                        return;
                                }

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
                        String statusFilterBorder = "+--------+----------------------+";

                        System.out.println();
                        System.out.println(statusFilterBorder);
                        System.out.println(
                                        "|  REGISTRATION STATUS FILTER   |");
                        System.out.println(statusFilterBorder);

                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "Option",
                                        "Selection");

                        System.out.println(statusFilterBorder);
                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "1",
                                        "All Statuses");
                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "2",
                                        "Waiting");
                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "3",
                                        "Assigned");
                        System.out.printf(
                                        "| %-6s | %-20s |%n",
                                        "4",
                                        "Cancelled");
                        System.out.println(statusFilterBorder);

                        System.out.print("Enter filter option: ");
                        System.out.print("Enter choice: ");

                        String choice = sc.nextLine().trim();

                        switch (choice) {
                                case "1" -> statusFilter = "ALL";
                                case "2" -> statusFilter = "WAITING";
                                case "3" -> statusFilter = "ASSIGNED";
                                case "4" -> statusFilter = "CANCELLED";

                                case "0" -> {
                                        System.out.println(
                                                        "Report generation cancelled.");
                                        return;
                                }

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

                String border = "+----------+----------+----------------------+------------+"
                                + "------------+------------+--------+"
                                + "------------------+------------+-----------+";

                String title = "WALK-IN REGISTRATION ANALYSIS REPORT";
                int contentWidth = 134;

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
                                "| %-134s |%n",
                                "Generated at: " + generatedTime);

                System.out.printf(
                                "| %-134s |%n",
                                "Room Type Filter: " + roomTypeFilter);

                System.out.printf(
                                "| %-134s |%n",
                                "Status Filter: " + statusFilter);

                System.out.printf(
                                "| %-134s |%n",
                                "Sorted by: Registration Time (Ascending)");

                System.out.println(border);

                System.out.printf(
                                "| %-8s | %-8s | %-20s | %-10s | %-10s | "
                                                + "%-10s | %-6s | %-16s | %-10s | %-9s |%n",
                                "Reg ID",
                                "Guest ID",
                                "Guest Name",
                                "Room Type",
                                "Check-In",
                                "Check-Out",
                                "Nights",
                                "Registered Time",
                                "Confirm No",
                                "Status");

                System.out.println(border);

                if (reportRecords.length == 0) {
                        System.out.printf(
                                        "| %-134s |%n",
                                        "No registration records match the selected filters.");
                } else {
                        for (int i = 0; i < reportRecords.length; i++) {

                                WalkInRegistration registration = reportRecords[i];

                                String confirmationDisplay = registration.getBookingConfirmationNumber() == null
                                                ? "-"
                                                : registration.getBookingConfirmationNumber();

                                System.out.printf(
                                                "| %-8.8s | %-8.8s | %-20.20s | %-10.10s | "
                                                                + "%-10s | %-10s | %-6d | %-16.16s | "
                                                                + "%-10.10s | %-9.9s |%n",
                                                registration.getRegistrationId(),
                                                registration.getGuest().getGuestId(),
                                                registration.getGuest().getName(),
                                                registration.getRequestedRoomType(),
                                                registration.getRequestedCheckInDate(),
                                                registration.getRequestedCheckOutDate(),
                                                registration.getStayDurationDays(),
                                                registration.getRegistrationTime(),
                                                confirmationDisplay,
                                                registration.getStatus());
                        }
                }

                System.out.println(border);

                System.out.printf(
                                "| %-134s |%n",
                                "Total matching records: " + reportRecords.length);

                System.out.println(border);
                if (reportRecords.length > 0) {

                        String[] statusLabels = {
                                        "Waiting",
                                        "Assigned",
                                        "Cancelled"
                        };

                        int[] statusCounts = {
                                        0,
                                        0,
                                        0
                        };

                        String[] roomTypeLabels = {
                                        "Standard",
                                        "Deluxe",
                                        "Suite"
                        };

                        int[] roomTypeCounts = {
                                        0,
                                        0,
                                        0
                        };

                        // Summarise the filtered records by status and room type.
                        for (int recordIndex = 0; recordIndex < reportRecords.length; recordIndex++) {

                                WalkInRegistration registration = reportRecords[recordIndex];

                                if (registration.getStatus()
                                                .equalsIgnoreCase("WAITING")) {

                                        statusCounts[0]++;

                                } else if (registration.getStatus()
                                                .equalsIgnoreCase("ASSIGNED")) {

                                        statusCounts[1]++;

                                } else if (registration.getStatus()
                                                .equalsIgnoreCase("CANCELLED")) {

                                        statusCounts[2]++;
                                }

                                if (registration.getRequestedRoomType()
                                                .equalsIgnoreCase("Standard")) {

                                        roomTypeCounts[0]++;

                                } else if (registration.getRequestedRoomType()
                                                .equalsIgnoreCase("Deluxe")) {

                                        roomTypeCounts[1]++;

                                } else if (registration.getRequestedRoomType()
                                                .equalsIgnoreCase("Suite")) {

                                        roomTypeCounts[2]++;
                                }
                        }

                        displaySideBySideBarCharts(
                                        "REGISTRATIONS BY STATUS",
                                        statusLabels,
                                        statusCounts,
                                        "REGISTRATIONS BY ROOM TYPE",
                                        roomTypeLabels,
                                        roomTypeCounts);

                        int highestStatusCount = statusCounts[0];

                        for (int statusIndex = 1; statusIndex < statusCounts.length; statusIndex++) {

                                if (statusCounts[statusIndex] > highestStatusCount) {
                                        highestStatusCount = statusCounts[statusIndex];
                                }
                        }

                        String largestStatusGroups = "";

                        for (int statusIndex = 0; statusIndex < statusLabels.length; statusIndex++) {

                                if (statusCounts[statusIndex] == highestStatusCount) {

                                        if (!largestStatusGroups.isEmpty()) {
                                                largestStatusGroups += " and ";
                                        }

                                        largestStatusGroups += statusLabels[statusIndex];
                                }
                        }

                        int highestRoomTypeCount = roomTypeCounts[0];

                        for (int roomTypeIndex = 1; roomTypeIndex < roomTypeCounts.length; roomTypeIndex++) {

                                if (roomTypeCounts[roomTypeIndex] > highestRoomTypeCount) {

                                        highestRoomTypeCount = roomTypeCounts[roomTypeIndex];
                                }
                        }

                        String mostRequestedRoomTypes = "";

                        for (int roomTypeIndex = 0; roomTypeIndex < roomTypeLabels.length; roomTypeIndex++) {

                                if (roomTypeCounts[roomTypeIndex] == highestRoomTypeCount) {

                                        if (!mostRequestedRoomTypes.isEmpty()) {
                                                mostRequestedRoomTypes += " and ";
                                        }

                                        mostRequestedRoomTypes += roomTypeLabels[roomTypeIndex];
                                }
                        }

                        System.out.println();

                        System.out.println(
                                        "Largest status group: "
                                                        + largestStatusGroups
                                                        + " ("
                                                        + highestStatusCount
                                                        + " records)");

                        System.out.println(
                                        "Most requested room type: "
                                                        + mostRequestedRoomTypes
                                                        + " ("
                                                        + highestRoomTypeCount
                                                        + " requests)");

                        String endLabel = "END OF REPORT";

                        int endLeftPadding = (contentWidth - endLabel.length()) / 2;

                        int endRightPadding = contentWidth
                                        - endLabel.length()
                                        - endLeftPadding;

                        System.out.println();
                        System.out.println(border);

                        System.out.printf(
                                        "| %s%s%s |%n",
                                        " ".repeat(endLeftPadding),
                                        endLabel,
                                        " ".repeat(endRightPadding));

                        System.out.println(border);
                }
        }

        private void displayRoomTypeDemandReport() {

                String[] roomTypes = {
                                "Standard", "Deluxe", "Suite"
                };

                int[] requestCounts = new int[roomTypes.length];
                int[] roomsAvailableTonightCounts = new int[roomTypes.length];

                int totalRegistrationRequests = control.getAllRegistrationHistory().length;

                int totalRooms = 0;

                String border = "+------------+----------+----------+---------+-----------+"
                                + "---------+-------------+----------+";

                String title = "ROOM-TYPE DEMAND SUMMARY REPORT";
                int contentWidth = 91;

                int leftPadding = (contentWidth - title.length()) / 2;

                int rightPadding = contentWidth - title.length() - leftPadding;

                String generatedTime = java.time.LocalDateTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern(
                                                "yyyy-MM-dd HH:mm:ss"));

                System.out.println();
                System.out.println(border);

                System.out.printf(
                                "|%s%s%s|%n",
                                " ".repeat(leftPadding),
                                title,
                                " ".repeat(rightPadding));

                System.out.println(border);

                System.out.printf(
                                "| %-89s |%n",
                                "Generated At: " + generatedTime);

                System.out.println(border);

                System.out.printf(
                                "| %-10s | %-8s | %-8s | %-7s | %-9s | "
                                                + "%-7s | %-11s | %-8s |%n",
                                "Room Type",
                                "All Req.",
                                "Assigned",
                                "Waiting",
                                "Cancelled",
                                "Rooms",
                                "Tonight",
                                "Req. %");

                System.out.println(border);

                for (int roomTypeIndex = 0; roomTypeIndex < roomTypes.length; roomTypeIndex++) {

                        String roomType = roomTypes[roomTypeIndex];

                        int requestCount = control.filterRegistrationHistory(
                                        roomType,
                                        "ALL").length;

                        int assignedCount = control.filterRegistrationHistory(
                                        roomType,
                                        "ASSIGNED").length;

                        int waitingCount = control.filterRegistrationHistory(
                                        roomType,
                                        "WAITING").length;

                        int cancelledCount = control.filterRegistrationHistory(
                                        roomType,
                                        "CANCELLED").length;

                        int roomCount = control.countTotalRoomsByType(roomType);

                        int roomsAvailableTonight = control.countAvailableRoomsByType(roomType);

                        double demandPercentage = totalRegistrationRequests == 0
                                        ? 0.0
                                        : requestCount * 100.0
                                                        / totalRegistrationRequests;

                        requestCounts[roomTypeIndex] = requestCount;
                        roomsAvailableTonightCounts[roomTypeIndex] = roomsAvailableTonight;

                        totalRooms += roomCount;

                        System.out.printf(
                                        "| %-10s | %-8d | %-8d | %-7d | %-9d | "
                                                        + "%-7d | %-11d | %7.2f%% |%n",
                                        roomType,
                                        requestCount,
                                        assignedCount,
                                        waitingCount,
                                        cancelledCount,
                                        roomCount,
                                        roomsAvailableTonight,
                                        demandPercentage);
                }

                System.out.println(border);

                System.out.printf(
                                "| %-89s |%n",
                                "Total registration requests: "
                                                + totalRegistrationRequests);

                System.out.printf(
                                "| %-89s |%n",
                                "Total rooms: " + totalRooms);

                System.out.println(border);

                displaySideBySideBarCharts(
                                "TOTAL REQUESTS BY ROOM TYPE",
                                roomTypes,
                                requestCounts,
                                "ROOMS AVAILABLE TONIGHT",
                                roomTypes,
                                roomsAvailableTonightCounts);

                int highestDemand = requestCounts[0];

                for (int roomTypeIndex = 1; roomTypeIndex < requestCounts.length; roomTypeIndex++) {

                        if (requestCounts[roomTypeIndex] > highestDemand) {
                                highestDemand = requestCounts[roomTypeIndex];
                        }
                }

                String mostRequestedRoomTypes = "";

                for (int roomTypeIndex = 0; roomTypeIndex < roomTypes.length; roomTypeIndex++) {

                        if (requestCounts[roomTypeIndex] == highestDemand) {

                                if (!mostRequestedRoomTypes.isEmpty()) {
                                        mostRequestedRoomTypes += " and ";
                                }

                                mostRequestedRoomTypes += roomTypes[roomTypeIndex];
                        }
                }

                System.out.println();
                System.out.println(
                                "Most requested room type: "
                                                + mostRequestedRoomTypes
                                                + " ("
                                                + highestDemand
                                                + " requests)");

                // Find the room type with the lowest request demand.
                int lowestDemand = requestCounts[0];

                for (int roomTypeIndex = 1; roomTypeIndex < requestCounts.length; roomTypeIndex++) {

                        if (requestCounts[roomTypeIndex] < lowestDemand) {
                                lowestDemand = requestCounts[roomTypeIndex];
                        }
                }

                // Include every room type tied for the lowest demand.
                String leastRequestedRoomTypes = "";

                for (int roomTypeIndex = 0; roomTypeIndex < roomTypes.length; roomTypeIndex++) {

                        if (requestCounts[roomTypeIndex] == lowestDemand) {

                                if (!leastRequestedRoomTypes.isEmpty()) {
                                        leastRequestedRoomTypes += " and ";
                                }

                                leastRequestedRoomTypes += roomTypes[roomTypeIndex];
                        }
                }

                // Find the room type with the most rooms available tonight.
                int highestAvailability = roomsAvailableTonightCounts[0];

                for (int roomTypeIndex = 1; roomTypeIndex < roomsAvailableTonightCounts.length; roomTypeIndex++) {

                        if (roomsAvailableTonightCounts[roomTypeIndex] > highestAvailability) {

                                highestAvailability = roomsAvailableTonightCounts[roomTypeIndex];
                        }
                }

                // Include every room type tied for the highest availability.
                String mostAvailableRoomTypes = "";

                for (int roomTypeIndex = 0; roomTypeIndex < roomTypes.length; roomTypeIndex++) {

                        if (roomsAvailableTonightCounts[roomTypeIndex] == highestAvailability) {

                                if (!mostAvailableRoomTypes.isEmpty()) {
                                        mostAvailableRoomTypes += " and ";
                                }

                                mostAvailableRoomTypes += roomTypes[roomTypeIndex];
                        }
                }

                System.out.println(
                                "Least requested room type: "
                                                + leastRequestedRoomTypes
                                                + " ("
                                                + lowestDemand
                                                + " requests)");

                System.out.println(
                                "Most rooms available tonight: "
                                                + mostAvailableRoomTypes
                                                + " ("
                                                + highestAvailability
                                                + " rooms)");
                String endLabel = "END OF REPORT";

                int endLeftPadding = (contentWidth - endLabel.length()) / 2;

                int endRightPadding = contentWidth
                                - endLabel.length()
                                - endLeftPadding;

                System.out.println(border);

                System.out.printf(
                                "|%s%s%s|%n",
                                " ".repeat(endLeftPadding),
                                endLabel,
                                " ".repeat(endRightPadding));

                System.out.println(border);
        }

        /**
         * Displays two vertical bar charts side by side.
         */
        private void displaySideBySideBarCharts(
                        String leftChartTitle,
                        String[] leftCategoryLabels,
                        int[] leftValues,
                        String rightChartTitle,
                        String[] rightCategoryLabels,
                        int[] rightValues) {

                int leftHighestValue = 0;
                int rightHighestValue = 0;

                for (int index = 0; index < leftValues.length; index++) {

                        if (leftValues[index] > leftHighestValue) {
                                leftHighestValue = leftValues[index];
                        }
                }

                for (int index = 0; index < rightValues.length; index++) {

                        if (rightValues[index] > rightHighestValue) {
                                rightHighestValue = rightValues[index];
                        }
                }

                int highestGraphLevel = Math.max(
                                leftHighestValue,
                                rightHighestValue);

                int chartWidth = 42;

                System.out.println();
                System.out.println(
                                "GRAPHICAL REPRESENTATION OF WALK-IN MODULE");
                System.out.println();

                System.out.printf(
                                "%-42s | %-42s%n",
                                leftChartTitle,
                                rightChartTitle);

                System.out.printf(
                                "%-42s | %-42s%n",
                                "^",
                                "^");

                for (int graphLevel = highestGraphLevel; graphLevel >= 1; graphLevel--) {

                        String leftGraphRow;

                        if (graphLevel <= leftHighestValue) {
                                leftGraphRow = String.format("%2d |", graphLevel);
                        } else {
                                leftGraphRow = "   |";
                        }

                        for (int valueIndex = 0; valueIndex < leftValues.length; valueIndex++) {

                                String graphBar = leftValues[valueIndex] >= graphLevel
                                                ? "####"
                                                : "";

                                leftGraphRow += String.format(
                                                " %-10s",
                                                graphBar);
                        }

                        String rightGraphRow;

                        if (graphLevel <= rightHighestValue) {
                                rightGraphRow = String.format("%2d |", graphLevel);
                        } else {
                                rightGraphRow = "   |";
                        }

                        for (int valueIndex = 0; valueIndex < rightValues.length; valueIndex++) {

                                String graphBar = rightValues[valueIndex] >= graphLevel
                                                ? "####"
                                                : "";

                                rightGraphRow += String.format(
                                                " %-10s",
                                                graphBar);
                        }

                        System.out.printf(
                                        "%-" + chartWidth + "s | %-"
                                                        + chartWidth + "s%n",
                                        leftGraphRow,
                                        rightGraphRow);
                }

                String leftAxis = " 0 +"
                                + "-----------".repeat(
                                                leftCategoryLabels.length);

                String rightAxis = " 0 +"
                                + "-----------".repeat(
                                                rightCategoryLabels.length);

                System.out.printf(
                                "%-42s | %-42s%n",
                                leftAxis,
                                rightAxis);

                String leftLabelRow = "    ";
                String rightLabelRow = "    ";

                for (int index = 0; index < leftCategoryLabels.length; index++) {

                        leftLabelRow += String.format(
                                        " %-10.10s",
                                        leftCategoryLabels[index]);
                }

                for (int index = 0; index < rightCategoryLabels.length; index++) {

                        rightLabelRow += String.format(
                                        " %-10.10s",
                                        rightCategoryLabels[index]);
                }

                System.out.printf(
                                "%-42s | %-42s%n",
                                leftLabelRow,
                                rightLabelRow);

                String leftValueRow = "    ";
                String rightValueRow = "    ";

                for (int index = 0; index < leftValues.length; index++) {

                        leftValueRow += String.format(
                                        " %-10d",
                                        leftValues[index]);
                }

                for (int index = 0; index < rightValues.length; index++) {

                        rightValueRow += String.format(
                                        " %-10d",
                                        rightValues[index]);
                }

                System.out.printf(
                                "%-42s | %-42s%n",
                                leftValueRow,
                                rightValueRow);
        }

        /**
         * Displays one booking request using a consistent table format.
         */
        private void displayBookingRequestDetails(
                        String tableTitle,
                        WalkInRegistration registration) {

                String border = "+----------------------+--------------------------------+";

                int titleAreaWidth = 53;
                int leftPadding = (titleAreaWidth - tableTitle.length()) / 2;
                int rightPadding = titleAreaWidth - tableTitle.length() - leftPadding;

                String stayDurationText = registration.getStayDurationDays()
                                + (registration.getStayDurationDays() == 1
                                                ? " night"
                                                : " nights");

                System.out.println();
                System.out.println(border);

                System.out.println(
                                "| "
                                                + " ".repeat(leftPadding)
                                                + tableTitle
                                                + " ".repeat(rightPadding)
                                                + " |");

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
                                stayDurationText);

                System.out.printf(
                                "| %-20s | %-30s |%n",
                                "Status",
                                registration.getStatus());

                System.out.println(border);
        }

        /**
         * Displays all changes recorded for one booking request.
         */
        private void displayRegistrationChangeHistory(
                        WalkInRegistration registration) {

                if (registration.getNumberOfChanges() == 0) {
                        System.out.println(
                                        "\nNo changes have been recorded "
                                                        + "for this booking request.");
                        return;
                }

                String border = "+------------------------+------------------+"
                                + "------------------+------------------+";

                String title = "REGISTRATION CHANGE HISTORY";

                int titleWidth = 79;
                int leftPadding = (titleWidth - title.length()) / 2;
                int rightPadding = titleWidth - title.length() - leftPadding;

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm");

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
                                "| %-22s | %-16s | %-16s | %-16s |%n",
                                "Field Changed",
                                "Old Value",
                                "New Value",
                                "Changed At");

                System.out.println(border);

                Iterator<RegistrationChange> changeIterator = registration.getChangeHistoryIterator();

                while (changeIterator.hasNext()) {
                        RegistrationChange change = changeIterator.next();

                        System.out.printf(
                                        "| %-22.22s | %-16.16s | %-16.16s | %-16s |%n",
                                        change.getFieldName(),
                                        change.getOldValue(),
                                        change.getNewValue(),
                                        change.getChangedAt().format(
                                                        timeFormatter));
                }

                System.out.println(border);

                System.out.println(
                                "Total recorded changes: "
                                                + registration.getNumberOfChanges());
        }

        private void updateWaitingBookingRequest() {

                if (control.getWaitingCount() == 0) {
                        System.out.println(
                                        "No standard booking requests are waiting.");
                        return;
                }

                displayWaitingQueue();

                System.out.print(
                                "\nEnter Registration ID to update : ");

                String registrationId = sc.nextLine().trim();

                if (registrationId.equals("0")) {
                        System.out.println("Update cancelled.");
                        return;
                }

                System.out.print(
                                "Enter Guest ID for verification : ");

                String guestId = sc.nextLine().trim();

                if (guestId.equals("0")) {
                        System.out.println("Update cancelled.");
                        return;
                }

                WalkInRegistration selectedRegistration = control.findWaitingRegistration(
                                registrationId,
                                guestId);

                if (selectedRegistration == null) {
                        System.out.println(
                                        "No active WAITING booking request matches "
                                                        + "the entered Registration ID and Guest ID.");
                        return;
                }

                displayBookingRequestDetails(
                                "CURRENT BOOKING REQUEST",
                                selectedRegistration);

                System.out.println();
                System.out.println(
                                "Update instructions: Press Enter to keep the current value.");
                System.out.println(
                                "Enter 0 at any input prompt to cancel and return to the Walk-In menu.");

                // Show valid room-type codes and current prices before staff update the
                // request.
                displayAvailableRoomTypes();

                String updatedRoomType = selectedRegistration.getRequestedRoomType();

                LocalDate updatedCheckInDate = selectedRegistration.getRequestedCheckInDate();

                int updatedStayDurationDays = selectedRegistration.getStayDurationDays();

                // Step 1: Select the final room type.
                while (true) {
                        System.out.print("Enter new room type code: ");

                        String roomTypeInput = sc.nextLine().trim();

                        if (roomTypeInput.equals("0")) {
                                System.out.println("Update cancelled.");
                                return;
                        }

                        if (roomTypeInput.isEmpty()) {
                                break;
                        }

                        String parsedRoomType = control.parseRoomTypeCode(roomTypeInput);

                        if (parsedRoomType != null) {
                                updatedRoomType = parsedRoomType;
                                break;
                        }

                        System.out.println(
                                        "Invalid room type code. Please try again.");
                }

                // Step 2: Select the final stay duration.
                while (true) {
                        System.out.print(
                                        "Enter new stay duration "
                                                        + "(1-30 nights) : ");

                        String durationInput = sc.nextLine().trim();

                        if (durationInput.equals("0")) {
                                System.out.println("Update cancelled.");
                                return;
                        }

                        if (durationInput.isEmpty()) {
                                break;
                        }

                        try {
                                int parsedStayDurationDays = Integer.parseInt(durationInput);

                                if (parsedStayDurationDays < 1
                                                || parsedStayDurationDays > 30) {

                                        System.out.println(
                                                        "Stay duration must be between "
                                                                        + "1 and 30 nights.");
                                        continue;
                                }

                                updatedStayDurationDays = parsedStayDurationDays;

                                break;

                        } catch (NumberFormatException exception) {
                                System.out.println(
                                                "Enter a whole number between 1 and 30.");
                        }
                }

                // Step 3: Display the calendar using the final room type
                // and stay duration before selecting the check-in date.
                YearMonth selectedMonth = YearMonth.from(updatedCheckInDate);

                calendarSelection: while (true) {
                        System.out.print(
                                        "Enter booking month "
                                                        + "(YYYY-MM) : ");

                        String monthInput = sc.nextLine().trim();

                        if (monthInput.equals("0")) {
                                System.out.println("Update cancelled.");
                                return;
                        }

                        if (!monthInput.isEmpty()) {
                                try {
                                        YearMonth parsedMonth = YearMonth.parse(monthInput);

                                        if (parsedMonth.isBefore(YearMonth.now())) {
                                                System.out.println(
                                                                "Booking month cannot be in the past.");
                                                continue;
                                        }

                                        selectedMonth = parsedMonth;

                                } catch (java.time.format.DateTimeParseException exception) {
                                        System.out.println(
                                                        "Invalid month. Example: 2026-08");
                                        continue;
                                }
                        }

                        displayBookingCalendar(
                                        selectedMonth,
                                        updatedRoomType,
                                        updatedStayDurationDays);

                        while (true) {
                                System.out.print(
                                                "Enter new check-in day (1-"
                                                                + selectedMonth.lengthOfMonth()
                                                                + ", "
                                                                + "M = Change month) : ");

                                String dayInput = sc.nextLine().trim();

                                if (dayInput.equals("0")) {
                                        System.out.println("Update cancelled.");
                                        return;
                                }

                                if (dayInput.isEmpty()) {
                                        break calendarSelection;
                                }

                                if (dayInput.equalsIgnoreCase("M")) {
                                        break;
                                }

                                try {
                                        int selectedDay = Integer.parseInt(dayInput);

                                        if (selectedDay < 1
                                                        || selectedDay > selectedMonth.lengthOfMonth()) {

                                                System.out.println(
                                                                "Invalid day. Enter a day shown "
                                                                                + "on the calendar.");
                                                continue;
                                        }

                                        LocalDate parsedCheckInDate = selectedMonth.atDay(selectedDay);

                                        if (parsedCheckInDate.isBefore(LocalDate.now())) {
                                                System.out.println(
                                                                "The check-in date cannot be in the past.");
                                                continue;
                                        }

                                        boolean roomAvailable = control.hasAvailableRoomForSchedule(
                                                        updatedRoomType,
                                                        parsedCheckInDate,
                                                        updatedStayDurationDays);

                                        if (!roomAvailable) {
                                                System.out.println(
                                                                "That day is marked X. No single "
                                                                                + updatedRoomType
                                                                                + " room is available for the "
                                                                                + "complete "
                                                                                + updatedStayDurationDays
                                                                                + "-night stay.");

                                                continue;
                                        }

                                        updatedCheckInDate = parsedCheckInDate;
                                        break calendarSelection;

                                } catch (NumberFormatException exception) {
                                        System.out.println(
                                                        "Enter a valid day number, M, or 0.");
                                }
                        }
                }
                boolean noValueChanged = updatedRoomType.equalsIgnoreCase(
                                selectedRegistration.getRequestedRoomType())
                                && updatedCheckInDate.equals(
                                                selectedRegistration.getRequestedCheckInDate())
                                && updatedStayDurationDays == selectedRegistration.getStayDurationDays();

                if (noValueChanged) {
                        System.out.println(
                                        "No changes were entered. "
                                                        + "The booking request remains unchanged.");
                        return;
                }

                boolean requestUpdated = control.updateWaitingBookingRequest(
                                registrationId,
                                guestId,
                                updatedRoomType,
                                updatedCheckInDate,
                                updatedStayDurationDays);

                if (!requestUpdated) {
                        System.out.println(
                                        "The update cannot be completed because "
                                                        + "no single room is available for the "
                                                        + "complete updated stay.");

                        System.out.println(
                                        "The original booking request remains unchanged.");
                        return;
                }

                System.out.println(
                                "Booking request updated successfully.");

                displayBookingRequestDetails(
                                "UPDATED BOOKING REQUEST",
                                selectedRegistration);
        }

        private void cancelWaitingRegistration() {
                if (control.getWaitingCount() == 0) {
                        System.out.println("No standard booking requests are waiting.");
                        return;
                }

                // Display available Reg ID and Guest ID values for staff selection.
                displayWaitingQueue();

                System.out.print(
                                "\nEnter Registration ID to cancel : ");

                String registrationId = sc.nextLine().trim();

                if (registrationId.equals("0")) {
                        System.out.println("Cancellation aborted.");
                        return;
                }

                System.out.print(
                                "Enter Guest ID for verification : ");

                String guestId = sc.nextLine().trim();

                if (guestId.equals("0")) {
                        System.out.println("Cancellation aborted.");
                        return;
                }

                if (registrationId.isEmpty() || guestId.isEmpty()) {
                        System.out.println(
                                        "Registration ID and Guest ID cannot be blank.");
                        return;
                }

                WalkInRegistration selectedRegistration = control.findWaitingRegistration(
                                registrationId,
                                guestId);

                if (selectedRegistration == null) {
                        System.out.println(
                                        "No active WAITING booking request matches "
                                                        + "the entered Registration ID and Guest ID.");
                        return;
                }

                // Display the selected request before asking for confirmation.
                displayBookingRequestDetails(
                                "CANCELLATION CONFIRMATION",
                                selectedRegistration);

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
                                        "Waiting booking request cancelled successfully.");
                        // Display the request again to confirm its updated status.
                        displayBookingRequestDetails(
                                        "CANCELLED BOOKING REQUEST",
                                        selectedRegistration);
                } else {
                        System.out.println(
                                        "Cancellation failed. The Registration ID and Guest ID "
                                                        + "do not match an active WAITING booking request.");
                }
        }

        private void displayGuestDirectory() {
                // Only show Guests who have Walk-In registration history.
                Guest[] guests = control.getGuestsWithRegistrationHistory();

                String border = "+----------+----------------------+"
                                + "---------------+------------+";

                String title = "WALK-IN HISTORY GUEST DIRECTORY";
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
                                "Total guests with Walk-In history: "
                                                + guests.length);
        }

        /**
         * Displays the available search methods for registration history.
         */
        private void searchRegistrationHistory() {

                while (true) {
                        System.out.println();
                        System.out.println(
                                        "+------------------------------------------------+");
                        System.out.println(
                                        "|        SEARCH STANDARD BOOKING REQUESTS        |");
                        System.out.println(
                                        "+------------------------------------------------+");
                        System.out.printf(
                                        "| %-46s |%n",
                                        "1. Search by Registration ID");
                        System.out.printf(
                                        "| %-46s |%n",
                                        "2. Search all requests by Guest ID");
                        System.out.printf(
                                        "| %-46s |%n",
                                        "0. Return to Walk-In menu");
                        System.out.println(
                                        "+------------------------------------------------+");

                        System.out.print("Enter search option: ");
                        String searchOption = sc.nextLine().trim();

                        switch (searchOption) {
                                case "1" -> {
                                        searchRegistrationById();
                                        return;
                                }

                                case "2" -> {
                                        searchRegistrationHistoryByGuestId();
                                        return;
                                }

                                case "0" -> {
                                        return;
                                }

                                default -> System.out.println(
                                                "Invalid search option. Please try again.");
                        }
                }
        }

        /**
         * Displays all Registration IDs available in the registration history.
         */
        private void displayRegistrationIdDirectory() {

                WalkInRegistration[] registrationRecords = control.getAllRegistrationHistory();

                String border = "+----------+----------+----------------------+------------+"
                                + "------------+------------+";

                String directoryTitle = "REGISTRATION ID DIRECTORY";
                int directoryContentWidth = 81;

                int directoryLeftPadding = (directoryContentWidth - directoryTitle.length()) / 2;

                int directoryRightPadding = directoryContentWidth
                                - directoryTitle.length()
                                - directoryLeftPadding;

                System.out.println();
                System.out.println(border);

                System.out.printf(
                                "| %s%s%s |%n",
                                " ".repeat(directoryLeftPadding),
                                directoryTitle,
                                " ".repeat(directoryRightPadding));

                System.out.println(border);

                System.out.printf(
                                "| %-8s | %-8s | %-20s | %-10s | %-10s | %-10s |%n",
                                "Reg ID",
                                "Guest ID",
                                "Guest Name",
                                "Room Type",
                                "Confirm No",
                                "Status");

                System.out.println(border);

                for (int recordIndex = 0; recordIndex < registrationRecords.length; recordIndex++) {

                        WalkInRegistration registration = registrationRecords[recordIndex];

                        String confirmationDisplay = registration.getBookingConfirmationNumber() == null
                                        ? "-"
                                        : registration.getBookingConfirmationNumber();

                        System.out.printf(
                                        "| %-8.8s | %-8.8s | %-20.20s | %-10.10s | "
                                                        + "%-10.10s | %-10.10s |%n",
                                        registration.getRegistrationId(),
                                        registration.getGuest().getGuestId(),
                                        registration.getGuest().getName(),
                                        registration.getRequestedRoomType(),
                                        confirmationDisplay,
                                        registration.getStatus());
                }

                System.out.println(border);

                System.out.println(
                                "Total registration records: "
                                                + registrationRecords.length);
        }

        /**
         * Searches the complete registration history using one Registration ID.
         */
        private void searchRegistrationById() {

                displayRegistrationIdDirectory();

                System.out.print(
                                "\nEnter Registration ID to search : ");

                String registrationId = sc.nextLine().trim();

                if (registrationId.equals("0")) {
                        System.out.println("Search cancelled.");
                        return;
                }

                if (registrationId.isEmpty()) {
                        System.out.println(
                                        "Registration ID cannot be blank.");
                        return;
                }

                WalkInRegistration registration = control.findRegistrationById(
                                registrationId);

                if (registration == null) {
                        System.out.println(
                                        "No standard booking request found for Registration ID: "
                                                        + registrationId);
                        return;
                }

                Guest guest = registration.getGuest();

                String bookingConfirmationDisplay = registration.getBookingConfirmationNumber() == null
                                ? "Not created"
                                : registration.getBookingConfirmationNumber();

                String border = "+------------------------+----------------------------------+";

                System.out.println();
                System.out.println(border);
                System.out.println(
                                "|             STANDARD BOOKING REQUEST DETAILS              |");
                System.out.println(border);

                System.out.printf(
                                "| %-22s | %-32.32s |%n",
                                "Registration ID",
                                registration.getRegistrationId());

                System.out.printf(
                                "| %-22s | %-32.32s |%n",
                                "Guest ID",
                                guest.getGuestId());

                System.out.printf(
                                "| %-22s | %-32.32s |%n",
                                "Guest Name",
                                guest.getName());

                System.out.printf(
                                "| %-22s | %-32.32s |%n",
                                "Contact Number",
                                guest.getContactNumber());

                System.out.printf(
                                "| %-22s | %-32.32s |%n",
                                "Membership Tier",
                                guest.getMembershipTier());

                System.out.printf(
                                "| %-22s | %-32.32s |%n",
                                "Requested Room Type",
                                registration.getRequestedRoomType());

                System.out.printf(
                                "| %-22s | %-32s |%n",
                                "Requested Check-In",
                                registration.getRequestedCheckInDate());

                System.out.printf(
                                "| %-22s | %-32s |%n",
                                "Requested Check-Out",
                                registration.getRequestedCheckOutDate());

                System.out.printf(
                                "| %-22s | %-32d |%n",
                                "Stay Duration (Nights)",
                                registration.getStayDurationDays());

                System.out.printf(
                                "| %-22s | %-32.32s |%n",
                                "Registered Time",
                                registration.getRegistrationTime());

                System.out.printf(
                                "| %-22s | %-32.32s |%n",
                                "Status",
                                registration.getStatus());

                System.out.printf(
                                "| %-22s | %-32.32s |%n",
                                "Booking Confirmation",
                                bookingConfirmationDisplay);

                System.out.println(border);

                // Display changes stored in this registration Entity's ADT.
                displayRegistrationChangeHistory(
                                registration);
        }

        private void searchRegistrationHistoryByGuestId() {
                // Show available Guest IDs before asking staff to choose one.
                displayGuestDirectory();
                System.out.print(
                                "\nEnter Guest ID to search registration history : ");

                String guestId = sc.nextLine().trim();

                if (guestId.equals("0")) {
                        System.out.println("Search cancelled.");
                        return;
                }

                if (guestId.isEmpty()) {
                        System.out.println(
                                        "Guest ID cannot be blank.");
                        return;
                }

                Guest existingGuest = control.findGuestById(guestId);

                if (existingGuest == null) {
                        System.out.println(
                                        "Guest ID does not exist: "
                                                        + guestId);
                        return;
                }

                WalkInRegistration[] registrations = control.searchRegistrationHistoryByGuestId(
                                guestId);

                if (registrations.length == 0) {
                        System.out.println(
                                        "Guest found, but no standard booking request "
                                                        + "history exists for Guest ID: "
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

                String historyBorder = "+----------+------------+------------+------------+"
                                + "--------+------------+-----------+";

                String historyTitle = "STANDARD BOOKING REQUEST HISTORY";
                int historyContentWidth = 81;

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
                                "| %-8s | %-10s | %-10s | %-10s | "
                                                + "%-6s | %-10s | %-9s |%n",
                                "Reg ID",
                                "Room Type",
                                "Check-In",
                                "Check-Out",
                                "Nights",
                                "Confirm No",
                                "Status");

                System.out.println(historyBorder);

                for (int i = 0; i < registrations.length; i++) {

                        WalkInRegistration registration = registrations[i];

                        String confirmationDisplay = registration.getBookingConfirmationNumber() == null
                                        ? "-"
                                        : registration.getBookingConfirmationNumber();

                        System.out.printf(
                                        "| %-8.8s | %-10.10s | %-10s | %-10s | "
                                                        + "%-6d | %-10.10s | %-9.9s |%n",
                                        registration.getRegistrationId(),
                                        registration.getRequestedRoomType(),
                                        registration.getRequestedCheckInDate(),
                                        registration.getRequestedCheckOutDate(),
                                        registration.getStayDurationDays(),
                                        confirmationDisplay,
                                        registration.getStatus());
                }

                System.out.println(historyBorder);
                System.out.println(
                                "Total registration records: "
                                                + registrations.length);
        }

}
