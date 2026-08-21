package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.VIPAllocationControl;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.VIPAllocationRequest;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Console interface for the VIP & Loyalty Tier Priority Room Allocation
 * module.
 *
 * @author brian
 */
public class VIPAllocationUI {

    private VIPAllocationControl control;
    private Scanner sc;

    public VIPAllocationUI() {
        this(new VIPAllocationControl(), new Scanner(System.in));
    }

    public VIPAllocationUI(VIPAllocationControl sharedControl) {
        this(sharedControl, new Scanner(System.in));
    }

    public VIPAllocationUI(VIPAllocationControl sharedControl, Scanner sharedScanner) {
        control = sharedControl;
        sc = sharedScanner;
    }

    public void showMenu() {
        int choice;
        do {
            int menuWidth = 64;
            String menuBorder = "+" + "-".repeat(menuWidth + 2) + "+";
            String menuTitle = "VIP PRIORITY ALLOCATION MODULE";
            int leftPadding = (menuWidth - menuTitle.length()) / 2;
            int rightPadding = menuWidth - menuTitle.length() - leftPadding;

            System.out.println();
            System.out.println(menuBorder);
            System.out.println("| " + " ".repeat(leftPadding) + menuTitle
                    + " ".repeat(rightPadding) + " |");
            System.out.println(menuBorder);
            System.out.printf("| %-64s |%n", "1. Register New VIP Allocation Request");
            System.out.printf("| %-64s |%n", "2. Allocate Next VIP Guest");
            System.out.printf("| %-64s |%n", "3. View VIP Priority Queue");
            System.out.printf("| %-64s |%n", "4. Update Waiting VIP Request");
            System.out.printf("| %-64s |%n", "5. Cancel Waiting VIP Request");
            System.out.printf("| %-64s |%n", "6. VIP Priority Queue Report");
            System.out.printf("| %-64s |%n", "7. VIP Allocation History Report");
            System.out.printf("| %-64s |%n", "8. Search Request ID and Guest ID");
            System.out.printf("| %-64s |%n", "0. Back To Main Menu");
            System.out.println(menuBorder);
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                choice = -1;
                continue;
            }

            switch (choice) {
                case 1 -> registerRequest();
                case 2 -> allocateNextGuest();
                case 3 -> displayPriorityQueue();
                case 4 -> updateWaitingRequest();
                case 5 -> cancelWaitingRequest();
                case 6 -> displayPriorityQueueReport();
                case 7 -> displayAllocationHistoryReport();
                case 8 -> searchVIPRequests();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }

            if (choice != 0) {
                System.out.print("\nPress Enter to return to the VIP Allocation menu...");
                sc.nextLine();
            }
        } while (choice != 0);
    }

    // =====================================================================
    // Shared prompt helpers (Register flow). Return null on "0" (back).
    // =====================================================================

    private String promptRoomTypeCode() {
        while (true) {
            System.out.print("Enter room type (D = Deluxe, S = Standard, SU = Suite, 0 = back): ");
            String code = sc.nextLine().trim().toUpperCase();
            if (code.equals("0")) {
                return null;
            }
            String roomType;
            switch (code) {
                case "D" -> { roomType = "Deluxe"; }
                case "S" -> { roomType = "Standard"; }
                case "SU" -> { roomType = "Suite"; }
                default -> { roomType = null; }
            }
            if (roomType != null) {
                return roomType;
            }
            System.out.println("Invalid room type code. Please try again.");
        }
    }

    private Integer promptStayDuration() {
        while (true) {
            System.out.print("Enter stay duration in nights (1-30, 0 = back): ");
            String input = sc.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            try {
                int nights = Integer.parseInt(input);
                if (nights >= 1 && nights <= 30) {
                    return nights;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Stay duration must be between 1 and 30 nights.");
        }
    }

    // =====================================================================
    // Shared prompt helpers (Update flow). "na" keeps current value.
    // =====================================================================

    private String promptRoomTypeCodeOrKeep(String currentRoomType) {
        while (true) {
            System.out.print("Enter room type (D = Deluxe, S = Standard, SU = Suite, "
                    + "na = keep current [" + currentRoomType + "], 0 = back): ");
            String code = sc.nextLine().trim();
            if (code.equals("0")) {
                return null;
            }
            if (code.equalsIgnoreCase("na")) {
                return currentRoomType;
            }
            String roomType;
            switch (code.toUpperCase()) {
                case "D" -> { roomType = "Deluxe"; }
                case "S" -> { roomType = "Standard"; }
                case "SU" -> { roomType = "Suite"; }
                default -> { roomType = null; }
            }
            if (roomType != null) {
                return roomType;
            }
            System.out.println("Invalid room type code. Please try again.");
        }
    }

    private Integer promptStayDurationOrKeep(int currentStayDurationDays) {
        while (true) {
            System.out.print("Enter stay duration in nights (1-30, na = keep current ["
                    + currentStayDurationDays + "], 0 = back): ");
            String input = sc.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            if (input.equalsIgnoreCase("na")) {
                return currentStayDurationDays;
            }
            try {
                int nights = Integer.parseInt(input);
                if (nights >= 1 && nights <= 30) {
                    return nights;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Stay duration must be between 1 and 30 nights.");
        }
    }


    private LocalDate promptCalendarCheckInDateOrKeep(
            String roomType, int stayDurationDays, LocalDate currentCheckInDate) {

        while (true) {
            YearMonth selectedMonth;
            while (true) {
                System.out.print("Enter booking month (YYYY-MM, na = keep current, 0 = back): ");
                String input = sc.nextLine().trim();
                if (input.equals("0")) {
                    return null;
                }
                if (input.equalsIgnoreCase("na")) {
                    return currentCheckInDate;
                }
                try {
                    selectedMonth = YearMonth.parse(input);
                    if (selectedMonth.isBefore(YearMonth.now())) {
                        System.out.println("Booking month cannot be in the past.");
                        continue;
                    }
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid month. Example: 2026-09");
                }
            }

            displayBookingCalendar(selectedMonth, roomType);

            while (true) {
                System.out.print("Enter check-in day (1-" + selectedMonth.lengthOfMonth()
                        + "), M to change month, or 0 to cancel: ");
                String dayInput = sc.nextLine().trim();

                if (dayInput.equals("0")) {
                    return null;
                }
                if (dayInput.equalsIgnoreCase("M")) {
                    break;
                }

                int selectedDay;
                try {
                    selectedDay = Integer.parseInt(dayInput);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid day. Enter a number shown on the calendar.");
                    continue;
                }

                if (selectedDay < 1 || selectedDay > selectedMonth.lengthOfMonth()) {
                    System.out.println("Invalid day for " + selectedMonth.getMonth()
                            + " " + selectedMonth.getYear() + ".");
                    continue;
                }

                LocalDate selectedDate = selectedMonth.atDay(selectedDay);
                if (selectedDate.isBefore(LocalDate.now())) {
                    System.out.println("Check-in date cannot be in the past.");
                    continue;
                }

                boolean dateAvailable = control.hasAvailableRoomForSchedule(
                        roomType, selectedDate, stayDurationDays);

                if (!dateAvailable) {
                    LocalDate checkOutPreview = selectedDate.plusDays(stayDurationDays);
                    System.out.println("No single " + roomType
                            + " room is available for the complete stay from "
                            + selectedDate + " to " + checkOutPreview + ".");
                    System.out.println("Please choose another check-in day or enter M to change month.");
                    continue;
                }

                return selectedDate;
            }
        }
    }

    // =====================================================================
    // Calendar-based check-in date selection (Register flow).
    // =====================================================================

    private void displayBookingCalendar(YearMonth selectedMonth, String roomType) {
        String border = "+------+------+------+------+------+------+------+";

        String title = selectedMonth.getMonth() + " " + selectedMonth.getYear()
                + " - " + roomType.toUpperCase() + " NIGHTLY AVAILABILITY";
        int contentWidth = 48;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.printf("|%s%s%s|%n", " ".repeat(leftPadding), title, " ".repeat(rightPadding));
        System.out.println(border);
        System.out.println("| Sun  | Mon  | Tue  | Wed  | Thu  | Fri  | Sat  |");
        System.out.println(border);

        LocalDate firstDate = selectedMonth.atDay(1);
        int currentColumn = firstDate.getDayOfWeek().getValue() % 7;

        for (int column = 0; column < currentColumn; column++) {
            System.out.print("|      ");
        }

        int numberOfDays = selectedMonth.lengthOfMonth();
        for (int day = 1; day <= numberOfDays; day++) {
            LocalDate calendarDate = selectedMonth.atDay(day);
            String displayedValue;

            if (calendarDate.isBefore(LocalDate.now())) {
                displayedValue = "-";
            } else {
                boolean dateAvailable = control.hasAvailableRoomForSchedule(roomType, calendarDate, 1);
                displayedValue = dateAvailable ? String.valueOf(day) : "X";
            }

            System.out.printf("| %4s ", displayedValue);
            currentColumn++;
            if (currentColumn == 7) {
                System.out.println("|");
                currentColumn = 0;
            }
        }

        if (currentColumn != 0) {
            while (currentColumn < 7) {
                System.out.print("|      ");
                currentColumn++;
            }
            System.out.println("|");
        }

        System.out.println(border);
        System.out.println("- = Past date; X = No " + roomType + " room available that night (fully booked).");
    }

    private LocalDate promptCalendarCheckInDate(String roomType, int stayDurationDays) {
        while (true) {
            YearMonth selectedMonth;
            while (true) {
                System.out.print("Enter booking month (YYYY-MM, 0 = back): ");
                String input = sc.nextLine().trim();
                if (input.equals("0")) {
                    return null;
                }
                try {
                    selectedMonth = YearMonth.parse(input);
                    if (selectedMonth.isBefore(YearMonth.now())) {
                        System.out.println("Booking month cannot be in the past.");
                        continue;
                    }
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid month. Example: 2026-09");
                }
            }

            displayBookingCalendar(selectedMonth, roomType);

            while (true) {
                System.out.print("Enter check-in day (1-" + selectedMonth.lengthOfMonth()
                        + "), M to change month, or 0 to cancel: ");
                String dayInput = sc.nextLine().trim();

                if (dayInput.equals("0")) {
                    return null;
                }
                if (dayInput.equalsIgnoreCase("M")) {
                    break;
                }

                int selectedDay;
                try {
                    selectedDay = Integer.parseInt(dayInput);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid day. Enter a number shown on the calendar.");
                    continue;
                }

                if (selectedDay < 1 || selectedDay > selectedMonth.lengthOfMonth()) {
                    System.out.println("Invalid day for " + selectedMonth.getMonth()
                            + " " + selectedMonth.getYear() + ".");
                    continue;
                }

                LocalDate selectedDate = selectedMonth.atDay(selectedDay);
                if (selectedDate.isBefore(LocalDate.now())) {
                    System.out.println("Check-in date cannot be in the past.");
                    continue;
                }

                boolean dateAvailable = control.hasAvailableRoomForSchedule(
                        roomType, selectedDate, stayDurationDays);

                if (!dateAvailable) {
                    LocalDate checkOutPreview = selectedDate.plusDays(stayDurationDays);
                    System.out.println("No single " + roomType
                            + " room is available for the complete stay from "
                            + selectedDate + " to " + checkOutPreview + ".");
                    System.out.println("Please choose another check-in day or enter M to change month.");
                    continue;
                }

                return selectedDate;
            }
        }
    }

    // =====================================================================
    // Priority-tier guest directory.
    // =====================================================================

    private void displayPriorityGuestDirectory() {
        Guest[] guests = control.getPriorityTierGuests();

        String border = "+----------+----------------------+---------------+------------+";
        String title = "PRIORITY-TIER GUEST DIRECTORY";
        int contentWidth = border.length() - 4;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title + " ".repeat(rightPadding) + " |");
        System.out.println(border);

        if (guests.length == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No priority-tier guests found.");
            System.out.println(border);
            return;
        }

        System.out.printf("| %-8s | %-20s | %-13s | %-10s |%n",
                "Guest ID", "Guest Name", "Contact", "Tier");
        System.out.println(border);
        for (Guest g : guests) {
            System.out.printf("| %-8.8s | %-20.20s | %-13.13s | %-10.10s |%n",
                    g.getGuestId(), g.getName(), g.getContactNumber(), g.getMembershipTier());
        }
        System.out.println(border);
        System.out.println("Total priority-tier guests: " + guests.length);
    }

    // =====================================================================
    // Register a new VIP request.
    // =====================================================================

    private void registerRequest() {
        displayPriorityGuestDirectory();

        System.out.print("\nEnter Guest ID (0 = back): ");
        String guestId = sc.nextLine().trim();
        if (guestId.equals("0")) {
            System.out.println("Registration cancelled.");
            return;
        }

        String roomType = promptRoomTypeCode();
        if (roomType == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        Integer stayDurationDays = promptStayDuration();
        if (stayDurationDays == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        LocalDate checkInDate = promptCalendarCheckInDate(roomType, stayDurationDays);
        if (checkInDate == null) {
            System.out.println("Registration cancelled.");
            return;
        }

        VIPAllocationRequest request = control.registerVIPRequest(
                guestId, roomType, checkInDate, stayDurationDays);

        if (request == null) {
            System.out.println(
                    "Unable to register request. The Guest ID may not exist, "
                    + "may not hold a priority membership tier, "
                    + "or the requested room type / schedule may be invalid.");
            return;
        }

        printRequestDetailsTable(request, "VIP REQUEST REGISTERED");
    }

    // =====================================================================
    // Update / cancel a WAITING request.
    // =====================================================================

    private void updateWaitingRequest() {
        if (control.getWaitingCount() == 0) {
            System.out.println("No VIP requests are waiting.");
            return;
        }

        displayPriorityQueue();

        System.out.print("\nEnter Request ID to update (0 = back): ");
        String requestId = sc.nextLine().trim();
        if (requestId.equals("0")) {
            System.out.println("Update cancelled.");
            return;
        }

        System.out.print("Enter Guest ID for verification (0 = back): ");
        String guestId = sc.nextLine().trim();
        if (guestId.equals("0")) {
            System.out.println("Update cancelled.");
            return;
        }

        if (requestId.isEmpty() || guestId.isEmpty()) {
            System.out.println("Request ID and Guest ID cannot be blank.");
            return;
        }

        VIPAllocationRequest selected = control.findWaitingRequestById(requestId, guestId);
        if (selected == null) {
            System.out.println(
                    "No active WAITING VIP request matches the entered Request ID and Guest ID.");
            return;
        }

        printRequestDetailsTable(selected, "CURRENT REQUEST DETAILS");
        System.out.println("Enter 'na' for any field you want to leave unchanged.");

        String newRoomType = promptRoomTypeCodeOrKeep(selected.getRequestedRoomType());
        if (newRoomType == null) {
            System.out.println("Update cancelled.");
            return;
        }

        Integer newStayDurationDays = promptStayDurationOrKeep(selected.getStayDurationDays());
        if (newStayDurationDays == null) {
            System.out.println("Update cancelled.");
            return;
        }

        LocalDate newCheckInDate = promptCalendarCheckInDateOrKeep(
                newRoomType, newStayDurationDays, selected.getRequestedCheckInDate());
        if (newCheckInDate == null) {
            System.out.println("Update cancelled.");
            return;
        }

        boolean updated = control.updateVIPRequest(
                requestId, guestId, newRoomType, newCheckInDate, newStayDurationDays);

        if (updated) {
            System.out.println("VIP request updated successfully.");
            printRequestDetailsTable(
                    control.findRequestById(requestId), "UPDATED REQUEST DETAILS");
        } else {
            System.out.println("Unable to update the VIP request.");
        }
    }

    private void cancelWaitingRequest() {
        if (control.getWaitingCount() == 0) {
            System.out.println("No VIP requests are waiting.");
            return;
        }

        displayPriorityQueue();

        System.out.print("\nEnter Request ID to cancel (0 = back): ");
        String requestId = sc.nextLine().trim();
        if (requestId.equals("0")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        System.out.print("Enter Guest ID for verification (0 = back): ");
        String guestId = sc.nextLine().trim();
        if (guestId.equals("0")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        if (requestId.isEmpty() || guestId.isEmpty()) {
            System.out.println("Request ID and Guest ID cannot be blank.");
            return;
        }

        VIPAllocationRequest selected = control.findWaitingRequestById(requestId, guestId);
        if (selected == null) {
            System.out.println(
                    "No active WAITING VIP request matches the entered Request ID and Guest ID.");
            return;
        }

        printRequestDetailsTable(selected, "REQUEST TO BE CANCELLED");

        System.out.print("Confirm cancellation (Y/N): ");
        String confirmation = sc.nextLine().trim();
        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        boolean cancelled = control.cancelVIPRequest(requestId, guestId);
        if (cancelled) {
            System.out.println("VIP request cancelled successfully.");
        } else {
            System.out.println("Cancellation failed.");
        }
    }

    // =====================================================================
    // Search by Request ID or Guest ID.
    // =====================================================================

    private void displaySearchDirectory() {
        VIPAllocationRequest[] all = control.getAllRequestHistory();

        String border = "+----------+----------------------+----------+----------+";
        String title = "ALL VIP REQUESTS (choose a Request ID or Guest ID)";
        int contentWidth = border.length() - 4;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title + " ".repeat(rightPadding) + " |");
        System.out.println(border);
        System.out.printf("| %-8s | %-20s | %-8s | %-8s |%n",
                "Req ID", "Guest Name", "Tier", "Guest ID");
        System.out.println(border);

        if (all.length == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No VIP requests found.");
        } else {
            for (VIPAllocationRequest r : all) {
                System.out.printf("| %-8.8s | %-20.20s | %-8.8s | %-8.8s |%n",
                        r.getRequestId(),
                        r.getGuest().getName(),
                        r.getGuest().getMembershipTier(),
                        r.getGuest().getGuestId());
            }
        }
        System.out.println(border);
        System.out.println("Total records: " + all.length);
    }

    private void searchVIPRequests() {
        displaySearchDirectory();

        int searchMode;
        while (true) {
            System.out.println("\nSearch By:");
            System.out.println("1. Request ID");
            System.out.println("2. Guest ID");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            try {
                searchMode = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            if (searchMode == 0 || searchMode == 1 || searchMode == 2) {
                break;
            }
            System.out.println("Invalid choice. Please try again.");
        }

        if (searchMode == 0) {
            System.out.println("Search cancelled.");
            return;
        }

        if (searchMode == 1) {
            System.out.print("\nEnter Request ID to search (0 = back): ");
            String requestId = sc.nextLine().trim();
            if (requestId.equals("0") || requestId.isEmpty()) {
                System.out.println("Search cancelled.");
                return;
            }

            VIPAllocationRequest request = control.findRequestById(requestId);
            if (request == null) {
                System.out.println("No VIP request found with Request ID: " + requestId);
                return;
            }

            printRequestDetailsTable(request, "VIP REQUEST DETAILS");
            return;
        }

        System.out.print("\nEnter Guest ID to search (0 = back): ");
        String guestId = sc.nextLine().trim();
        if (guestId.equals("0") || guestId.isEmpty()) {
            System.out.println("Search cancelled.");
            return;
        }

        VIPAllocationRequest[] guestRequests = control.getRequestsByGuestId(guestId);
        if (guestRequests.length == 0) {
            System.out.println("No VIP requests found for Guest ID: " + guestId);
            return;
        }

        printGuestRequestsTable(guestId, guestRequests);
    }

    private void printGuestRequestsTable(String guestId, VIPAllocationRequest[] requests) {
        String border = "+----------+----------------------+----------+----------+------------+"
                + "------------------+------------+------------+-----------+";
        String title = "VIP REQUESTS FOR GUEST " + guestId.toUpperCase();
        int contentWidth = border.length() - 4;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title + " ".repeat(rightPadding) + " |");
        System.out.println(border);
        System.out.printf("| %-8s | %-20s | %-8s | %-8s | %-10s | %-16s | %-10s | %-10s | %-9s |%n",
                "Req ID", "Guest Name", "Tier", "Guest ID", "Room Type",
                "Request Time", "Check-In", "Check-Out", "Status");
        System.out.println(border);

        for (VIPAllocationRequest r : requests) {
            System.out.printf(
                    "| %-8.8s | %-20.20s | %-8.8s | %-8.8s | %-10.10s | %-16.16s | %-10s | %-10s | %-9.9s |%n",
                    r.getRequestId(),
                    r.getGuest().getName(),
                    r.getGuest().getMembershipTier(),
                    r.getGuest().getGuestId(),
                    r.getRequestedRoomType(),
                    r.getRequestTime(),
                    r.getRequestedCheckInDate(),
                    r.getRequestedCheckOutDate(),
                    r.getStatus());
        }
        System.out.println(border);
        System.out.println("Total requests for this guest: " + requests.length);
    }

    // =====================================================================
    // Allocate next guest.
    // =====================================================================

    private void allocateNextGuest() {
        if (control.getWaitingCount() == 0) {
            System.out.println("The VIP priority queue is empty.");
            return;
        }

        Booking booking = control.allocateNextVIPGuest();

        if (booking == null) {
            System.out.println(
                    "No matching available room for the front VIP guest's requested dates. "
                    + "The guest remains at the front of the priority queue.");
            return;
        }

        printBookingDetailsTable(booking, "VIP BOOKING CREATED");
    }

    private void printBookingDetailsTable(Booking booking, String title) {
        String border = "+----------------------+--------------------------------+";
        int contentWidth = 55;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.printf("|%s%s%s|%n", " ".repeat(leftPadding), title, " ".repeat(rightPadding));
        System.out.println(border);
        System.out.printf("| %-20s | %-30s |%n", "Confirmation No.", booking.getConfirmationNumber());
        System.out.printf("| %-20s | %-30s |%n", "Guest ID", booking.getGuest().getGuestId());
        System.out.printf("| %-20s | %-30.30s |%n", "Guest Name", booking.getGuest().getName());
        System.out.printf("| %-20s | %-30s |%n", "Membership Tier", booking.getGuest().getMembershipTier());
        System.out.printf("| %-20s | %-30s |%n", "Room Number", booking.getRoom().getRoomNumber());
        System.out.printf("| %-20s | %-30s |%n", "Room Type", booking.getRoom().getRoomType());
        System.out.printf("| %-20s | %-30s |%n", "Scheduled Check-In", booking.getScheduledCheckInDate());
        System.out.printf("| %-20s | %-30s |%n", "Scheduled Check-Out", booking.getScheduledCheckOutDate());
        System.out.printf("| %-20s | %-30s |%n", "Stay Duration",
                booking.getStayDurationDays() + (booking.getStayDurationDays() == 1 ? " night" : " nights"));
        System.out.printf("| %-20s | %-30s |%n", "Gross Amount", String.format("RM %,.2f", booking.getAmount()));
        System.out.printf("| %-20s | %-30s |%n", "Discount Rate",
                String.format("%.0f%%", booking.getDiscountRate() * 100));
        System.out.printf("| %-20s | %-30s |%n", "Discount Amount", String.format("RM %,.2f", booking.getDiscountAmount()));
        System.out.printf("| %-20s | %-30s |%n", "Final Amount", String.format("RM %,.2f", booking.getFinalAmount()));
        System.out.printf("| %-20s | %-30s |%n", "Payment Status", booking.getPaymentStatus());
        System.out.printf("| %-20s | %-30s |%n", "Booking Status", booking.getStatus());
        System.out.printf("| %-20s | %-30s |%n", "Booking Time", booking.getBookingCreatedTime());
        System.out.println(border);
    }

    // =====================================================================
    // Detail table (used by Register, Update, Cancel-preview, and Search).
    // =====================================================================

    private void printRequestDetailsTable(VIPAllocationRequest request, String title) {
        String border = "+----------------------+--------------------------------+";
        int contentWidth = 55;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.printf("|%s%s%s|%n", " ".repeat(leftPadding), title, " ".repeat(rightPadding));
        System.out.println(border);
        System.out.printf("| %-20s | %-30s |%n", "Request ID", request.getRequestId());
        System.out.printf("| %-20s | %-30s |%n", "Guest ID", request.getGuest().getGuestId());
        System.out.printf("| %-20s | %-30.30s |%n", "Guest Name", request.getGuest().getName());
        System.out.printf("| %-20s | %-30s |%n", "Membership Tier", request.getGuest().getMembershipTier());
        System.out.printf("| %-20s | %-30s |%n", "Requested Room Type", request.getRequestedRoomType());
        System.out.printf("| %-20s | %-30s |%n", "Requested Check-In", request.getRequestedCheckInDate());
        System.out.printf("| %-20s | %-30s |%n", "Requested Check-Out", request.getRequestedCheckOutDate());
        System.out.printf("| %-20s | %-30s |%n", "Stay Duration",
                request.getStayDurationDays() + (request.getStayDurationDays() == 1 ? " night" : " nights"));
        System.out.printf("| %-20s | %-30s |%n", "Request Time", request.getRequestTime());
        System.out.printf("| %-20s | %-30s |%n", "Status", request.getStatus());
        System.out.println(border);
    }

    // =====================================================================
    // View / display the priority list.
    // =====================================================================

    private void displayPriorityQueue() {
        VIPAllocationRequest[] list = control.getPriorityListInOrder();
        printPriorityTable(list, "VIP PRIORITY ALLOCATION QUEUE");
    }

    private void printPriorityTable(VIPAllocationRequest[] list, String title) {
        String border = "+-----+----------+----------+----------------------+----------+"
                + "------------+------------------+------------+------------+";
        int contentWidth = border.length() - 4;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title
                + " ".repeat(rightPadding) + " |");
        System.out.println(border);

        if (list.length == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No VIP guests match the current view.");
            System.out.println(border);
            return;
        }

        System.out.printf("| %-3s | %-8s | %-8s | %-20s | %-8s | %-10s | %-16s | %-10s | %-10s |%n",
                "Pos", "Req ID", "Guest ID", "Guest Name", "Tier", "Room Type", "Request Time",
                "Check-In", "Check-Out");
        System.out.println(border);

        for (int i = 0; i < list.length; i++) {
            VIPAllocationRequest r = list[i];
            System.out.printf(
                    "| %-3d | %-8.8s | %-8.8s | %-20.20s | %-8.8s | %-10.10s | %-16.16s | %-10s | %-10s |%n",
                    i + 1,
                    r.getRequestId(),
                    r.getGuest().getGuestId(),
                    r.getGuest().getName(),
                    r.getGuest().getMembershipTier(),
                    r.getRequestedRoomType(),
                    r.getRequestTime(),
                    r.getRequestedCheckInDate(),
                    r.getRequestedCheckOutDate());
        }

        System.out.println(border);
        System.out.println("Total records: " + list.length);
    }

    // =====================================================================
    // Report 1: VIP Priority Queue Report (filter by tier).
    // =====================================================================

    private void displayPriorityQueueReport() {
        String tierFilter = readTierFilter();
        if (tierFilter == null) {
            System.out.println("Report cancelled.");
            return;
        }

        VIPAllocationRequest[] fullQueue = control.getPriorityListInOrder();

        int matchCount = 0;
        for (VIPAllocationRequest r : fullQueue) {
            if (tierFilter.equalsIgnoreCase("ALL")
                    || r.getGuest().getMembershipTier().toString().equalsIgnoreCase(tierFilter)) {
                matchCount++;
            }
        }

        VIPAllocationRequest[] filtered = new VIPAllocationRequest[matchCount];
        int index = 0;
        for (VIPAllocationRequest r : fullQueue) {
            if (tierFilter.equalsIgnoreCase("ALL")
                    || r.getGuest().getMembershipTier().toString().equalsIgnoreCase(tierFilter)) {
                filtered[index++] = r;
            }
        }

        printPriorityTable(filtered, "VIP PRIORITY QUEUE REPORT (Filter: " + tierFilter + ")");
        printTierBarChart(filtered, "Waiting Requests by Tier");
    }

    // =====================================================================
    // Report 2: VIP Allocation History Report (filter tier + status, sorted).
    // =====================================================================

    private void displayAllocationHistoryReport() {
        String tierFilter = readTierFilter();
        if (tierFilter == null) {
            System.out.println("Report cancelled.");
            return;
        }

        String statusFilter = readStatusFilter();
        if (statusFilter == null) {
            System.out.println("Report cancelled.");
            return;
        }

        VIPAllocationRequest[] results = control.filterRequestHistory(tierFilter, statusFilter);
        control.sortByRequestTime(results);

        String border = "+----------+----------------------+----------+----------+------------+"
                + "------------------+------------+------------+-----------+";
        String title = "VIP ALLOCATION HISTORY REPORT";
        int contentWidth = border.length() - 4;
        int leftPadding = (contentWidth - title.length()) / 2;
        int rightPadding = contentWidth - title.length() - leftPadding;

        System.out.println();
        System.out.println(border);
        System.out.println("| " + " ".repeat(leftPadding) + title
                + " ".repeat(rightPadding) + " |");
        System.out.println(border);
        System.out.printf("| %-" + contentWidth + "s |%n",
                "Filters: Tier = " + tierFilter + " | Status = " + statusFilter);
        System.out.printf("| %-" + contentWidth + "s |%n", "Sorted by: Request Time (Ascending)");
        System.out.println(border);
        System.out.printf("| %-8s | %-20s | %-8s | %-8s | %-10s | %-16s | %-10s | %-10s | %-9s |%n",
                "Req ID", "Guest Name", "Tier", "Guest ID", "Room Type",
                "Request Time", "Check-In", "Check-Out", "Status");
        System.out.println(border);

        if (results.length == 0) {
            System.out.printf("| %-" + contentWidth + "s |%n", "No records match the selected filters.");
        } else {
            for (VIPAllocationRequest r : results) {
                System.out.printf(
                        "| %-8.8s | %-20.20s | %-8.8s | %-8.8s | %-10.10s | %-16.16s | %-10s | %-10s | %-9.9s |%n",
                        r.getRequestId(),
                        r.getGuest().getName(),
                        r.getGuest().getMembershipTier(),
                        r.getGuest().getGuestId(),
                        r.getRequestedRoomType(),
                        r.getRequestTime(),
                        r.getRequestedCheckInDate(),
                        r.getRequestedCheckOutDate(),
                        r.getStatus());
            }
        }
        System.out.println(border);
        System.out.println("Total matching records: " + results.length);

        printTierBarChart(results, "Matching Records by Tier");
    }

    // =====================================================================
    // Bar chart helper - shared by both reports.
    // =====================================================================

    private void printTierBarChart(VIPAllocationRequest[] requests, String chartTitle) {
        int platinumCount = 0;
        int diamondCount = 0;
        int eliteCount = 0;

        for (VIPAllocationRequest r : requests) {
            switch (r.getGuest().getMembershipTier()) {
                case PLATINUM -> platinumCount++;
                case DIAMOND -> diamondCount++;
                case ELITE -> eliteCount++;
                default -> { }
            }
        }

        System.out.println();
        System.out.println(chartTitle);

        int maxCount = Math.max(platinumCount, Math.max(diamondCount, eliteCount));
        if (maxCount == 0) {
            System.out.println("(No data to chart.)");
            return;
        }

        printBarRow("PLATINUM", platinumCount, maxCount);
        printBarRow("DIAMOND ", diamondCount, maxCount);
        printBarRow("ELITE   ", eliteCount, maxCount);
    }

    private void printBarRow(String label, int count, int maxCount) {
        int maxBarLength = 30;
        int barLength = maxCount == 0 ? 0 : (int) Math.round((double) count / maxCount * maxBarLength);
        System.out.printf("%-9s| %-" + maxBarLength + "s (%d)%n", label, "*".repeat(barLength), count);
    }

    // =====================================================================
    // Filter prompts.
    // =====================================================================

    private String readTierFilter() {
        while (true) {
            System.out.println("\nMembership Tier Filter");
            System.out.println("1. All Priority Tiers");
            System.out.println("2. PLATINUM");
            System.out.println("3. DIAMOND");
            System.out.println("4. ELITE");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "ALL";
                case "2": return "PLATINUM";
                case "3": return "DIAMOND";
                case "4": return "ELITE";
                case "0": return null;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private String readStatusFilter() {
        while (true) {
            System.out.println("\nRequest Status Filter");
            System.out.println("1. All Statuses");
            System.out.println("2. Waiting");
            System.out.println("3. Assigned");
            System.out.println("4. Cancelled");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");
            switch (sc.nextLine().trim()) {
                case "1": return "ALL";
                case "2": return "WAITING";
                case "3": return "ASSIGNED";
                case "4": return "CANCELLED";
                case "0": return null;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}