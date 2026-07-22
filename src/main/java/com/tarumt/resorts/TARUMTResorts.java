/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.tarumt.resorts;

/**
*
* @author junhao
*/

import com.tarumt.resorts.adt.Queue;
import com.tarumt.resorts.boundary.HouseKeepingUI;
import com.tarumt.resorts.boundary.WalkInRegistrationUI;
import com.tarumt.resorts.boundary.FrontDeskUI;
import com.tarumt.resorts.control.HousekeepingControl;
import com.tarumt.resorts.control.WalkInRegistrationControl;
import com.tarumt.resorts.control.FrontDeskControl;
import com.tarumt.resorts.dao.GuestDAO;
import com.tarumt.resorts.dao.RoomDAO;
import com.tarumt.resorts.dao.WalkInRegistrationDAO;
import com.tarumt.resorts.dao.RoomStatusLogDAO;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.entity.WalkInRegistration;

import java.util.Scanner;

public class TARUMTResorts {

        public static void main(String[] args) {
                // Load all initial DAO data only once.
                Queue<Room> sharedRooms = new RoomDAO().getAllRooms();

                Queue<Guest> sharedGuests = new GuestDAO().getAllGuests();

                // Load the hard-coded registration history once.
                Queue<WalkInRegistration> sharedRegistrationHistory = new WalkInRegistrationDAO()
                                .getAllRegistrations(sharedGuests);
 
                Queue<Booking> sharedBookings = new Queue<>();

                Queue<RoomStatusLog> sharedStatusLogs = new RoomStatusLogDAO().getAllLogs();

                // Both Controls receive the same shared Room Queue reference.
                WalkInRegistrationControl walkInControl = new WalkInRegistrationControl(
                                sharedRooms,
                                sharedGuests,
                                sharedBookings,
                                sharedRegistrationHistory );

                HousekeepingControl housekeepingControl = new HousekeepingControl(
                                sharedRooms,
                                sharedStatusLogs);

                // Front-Desk runs on its own hard-coded sample bookings so it
                // can be demonstrated independently of the Walk-In workflow.
                FrontDeskControl frontDeskControl = new FrontDeskControl();

                // All menus read input through the same Scanner object.
                Scanner scanner = new Scanner(System.in);

                WalkInRegistrationUI walkInUI = new WalkInRegistrationUI(
                                walkInControl,
                                scanner);

                HouseKeepingUI housekeepingUI = new HouseKeepingUI(
                                housekeepingControl,
                                scanner);

                FrontDeskUI frontDeskUI = new FrontDeskUI(
                                frontDeskControl,
                                scanner);

                int choice;

                do {
                        System.out.println();
                        System.out.println(
                                        "+------------------------------------------------+");
                        System.out.println(
                                        "|            TARUMT RESORTS MAIN MENU            |");
                        System.out.println(
                                        "+------------------------------------------------+");
                        System.out.printf(
                                        "| %-46s |%n",
                                        "1. Walk-In Registration & Standard Booking");
                        System.out.printf(
                                        "| %-46s |%n",
                                        "2. Housekeeping & Task Log");
                        System.out.printf(
                                        "| %-46s |%n",
                                        "3. Front-Desk Service");
                        System.out.printf(
                                        "| %-46s |%n",
                                        "0. Exit");
                        System.out.println(
                                        "+------------------------------------------------+");
                        System.out.print("Enter choice: ");

                        try {
                                choice = Integer.parseInt(
                                                scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                                System.out.println(
                                                "Invalid input. Please enter a number.");
                                choice = -1;
                                continue;
                        }

                        switch (choice) {
                                case 1 -> walkInUI.showMenu();
                                case 2 -> housekeepingUI.showMenu();
                                case 3 -> frontDeskUI.showMenu();
                                case 0 -> System.out.println(
                                                "Thank you for using TARUMT Resorts.");
                                default -> System.out.println(
                                                "Invalid choice. Please try again.");
                        }

                } while (choice != 0);

                // Close System.in only when the entire application exits.
                scanner.close();
        }
}