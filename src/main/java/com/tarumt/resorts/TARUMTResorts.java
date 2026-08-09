/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.tarumt.resorts;

/**
*
* @author Junhao
*/

import com.tarumt.resorts.adt.ListQueueInterface;
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
import com.tarumt.resorts.dao.BookingDAO;
import com.tarumt.resorts.entity.Booking;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.Room;
import com.tarumt.resorts.entity.RoomStatusLog;
import com.tarumt.resorts.entity.WalkInRegistration;

import com.tarumt.resorts.boundary.VIPAllocationUI;
import com.tarumt.resorts.control.VIPAllocationControl;
import com.tarumt.resorts.dao.VIPAllocationDAO;
import com.tarumt.resorts.entity.VIPAllocationRequest;
import com.tarumt.resorts.boundary.MainMenuUI;
import com.tarumt.resorts.boundary.LoyaltyRewardsUI;
import com.tarumt.resorts.control.LoyaltyRewardsControl;
import com.tarumt.resorts.dao.LoyaltyInitializerData;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.LoyaltyTransaction;

import java.util.Scanner;

public class TARUMTResorts {

        public static void main(String[] args) {
                // Load all initial DAO data only once.
                ListQueueInterface<Room> sharedRooms = new RoomDAO().getAllRooms();

                ListQueueInterface<Guest> sharedGuests = new GuestDAO().getAllGuests();

                // Load the hard-coded registration history once.
                // Store the DAO result using the shared ADT interface.
                ListQueueInterface<WalkInRegistration> sharedRegistrationHistory = new WalkInRegistrationDAO()
                                .getAllRegistrations(sharedGuests);

                // Seed the shared booking collection ONCE from BookingDAO,
                // against the same Guest and Room objects above, so Walk-In and
                // Front-Desk read and write the exact same Booking/Room state.
                ListQueueInterface<Booking> sharedBookings = new BookingDAO()
                                .getAllBookings(sharedGuests, sharedRooms);

                ListQueueInterface<RoomStatusLog> sharedStatusLogs = new RoomStatusLogDAO().getAllLogs();

                // Initialize Loyalty data using the same shared Guest collection.
               LoyaltyInitializerData loyaltyInitializer = new LoyaltyInitializerData(
                                sharedGuests,
                                sharedBookings);

                // Retrieve the initialized loyalty accounts.
                ListQueueInterface<LoyaltyAccount> sharedLoyaltyAccounts = loyaltyInitializer.getLoyaltyAccounts();

                // Retrieve the initialized loyalty transaction history.
                ListQueueInterface<LoyaltyTransaction> sharedLoyaltyTransactions = loyaltyInitializer
                                .getLoyaltyTransactions();
                // Both Controls receive the same shared Room Queue reference.
                WalkInRegistrationControl walkInControl = new WalkInRegistrationControl(
                                sharedRooms,
                                sharedGuests,
                                sharedBookings,
                                sharedRegistrationHistory);

                HousekeepingControl housekeepingControl = new HousekeepingControl(
                                sharedRooms,
                                sharedStatusLogs);

                // Front-Desk receives the SAME shared references, so it can see
                // bookings created at runtime by Walk-In, and its check-out
                // updates the same Room objects Walk-In/Housekeeping query.
                FrontDeskControl frontDeskControl = new FrontDeskControl(
                                sharedBookings,
                                sharedGuests,
                                sharedRooms);

                // Let a Front-Desk check-out also log the freed room as DIRTY
                // in Housekeeping's status log, keeping both modules in sync.
                frontDeskControl.setHousekeepingControl(housekeepingControl);

                // Create the Loyalty control using the shared system data.
                LoyaltyRewardsControl loyaltyControl =
                                new LoyaltyRewardsControl(
                                sharedLoyaltyAccounts,
                                sharedLoyaltyTransactions,
                                sharedGuests,
                                sharedBookings);

                // Load the hard-coded VIP allocation request history.
                ListQueueInterface<VIPAllocationRequest> sharedVipRequestHistory = new VIPAllocationDAO()
                                .getAllRequests(sharedGuests);

                VIPAllocationControl vipControl = new VIPAllocationControl(
                                sharedRooms,
                                sharedGuests,
                                sharedBookings,
                                sharedVipRequestHistory);

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

                // Create the Loyalty UI using the Loyalty control and shared Scanner.
                LoyaltyRewardsUI loyaltyUI = new LoyaltyRewardsUI(
                                loyaltyControl,
                                scanner);

                VIPAllocationUI vipUI = new VIPAllocationUI(
                                vipControl,
                                scanner);

                // Main only wires the module boundaries together.
                // Menu display and input handling belong to the Boundary layer.
                MainMenuUI mainMenuUI = new MainMenuUI(
                                walkInUI,
                                housekeepingUI,
                                frontDeskUI,
                                loyaltyUI,
                                vipUI,
                                scanner);

                mainMenuUI.showMenu();

                // Close System.in only after the entire application exits.
                scanner.close();

        }

}
