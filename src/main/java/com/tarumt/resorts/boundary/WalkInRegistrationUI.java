/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.boundary;

import com.tarumt.resorts.control.WalkInRegistrationControl;
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
            System.out.println("\n=== Walk-In Registration Module ===");
            System.out.println("1. Register new walk-in guest");
            System.out.println("2. Process next guest");
            System.out.println("3. View waiting count");
            System.out.println("4. Report 1: Waiting list (sorted by time, optional filter)");
            System.out.println("5. Report 2: Room-type demand summary");
            System.out.println("6. Search guest by ID");
            System.out.println("0. Back to main menu");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> registerGuest();
                case 2 -> processNextGuest();
                case 3 -> System.out.println("Guests waiting: " + control.getWaitingCount());
                case 4 -> printReport1();
                case 5 -> printReport2();
                case 6 -> searchGuest();
                case 0 -> System.out.println("Returning to main menu...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private void registerGuest() {
        System.out.print("Enter guest ID: ");
        String id = sc.nextLine();
        System.out.print("Enter guest name: ");
        String name = sc.nextLine();
        System.out.print("Enter requested room type: ");
        String roomType = sc.nextLine();

        Guest guest = new Guest(id, name, "", "", "None");
        String regId = "R" + System.currentTimeMillis() % 100000;
        String formattedTime = java.time.LocalDateTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        WalkInRegistration reg = new WalkInRegistration(regId, guest,
        formattedTime, roomType);

        boolean success = control.registerGuest(reg);
        System.out.println(success ? "Guest registered successfully." : "Guest already registered.");
    }

    private void processNextGuest() {
        WalkInRegistration reg = control.processNextGuest();
        if (reg == null) {
            System.out.println("No guests waiting.");
        } else {
            System.out.println("Now serving: " + reg.getGuest().getName()
                    + " | Room type: " + reg.getRequestedRoomType());
        }
    }

    private void printReport1() {
        System.out.print("Filter by room type (leave blank for all): ");
        String filter = sc.nextLine();

        WalkInRegistration[] sorted = control.getSortedWaitingList(filter);

        System.out.println("=====================================================");
        System.out.println("  TARUMT Resorts - Walk-In Waiting List Report");
        System.out.println("  Generated at: " + java.time.LocalDateTime.now());
        System.out.println("=====================================================");
        System.out.printf("%-10s %-20s %-12s %-16s %-10s%n",
        "RegID", "Guest Name", "Room Type", "Time", "Status");
        System.out.println("-----------------------------------------------------");
        for (WalkInRegistration reg : sorted) {
            System.out.println(reg);
        }
        System.out.println("-----------------------------------------------------");
        System.out.println("Total records displayed: " + sorted.length);
        System.out.println("=====================================================");
    }

    private void printReport2() {
        String[] summary = control.getRoomTypeDemandSummary();

        System.out.println("=====================================================");
        System.out.println("  TARUMT Resorts - Room-Type Demand Summary Report");
        System.out.println("  Generated at: " + java.time.LocalDateTime.now());
        System.out.println("=====================================================");
        System.out.printf("%-20s %-10s%n", "Room Type", "Demand Count");
        System.out.println("-----------------------------------------------------");
        for (String line : summary) {
            System.out.println(line);
        }
        System.out.println("-----------------------------------------------------");
        System.out.println("Total room types displayed: " + summary.length);
        System.out.println("=====================================================");
    }

    private void searchGuest() {
        System.out.print("Enter guest ID to search: ");
        String id = sc.nextLine();
        WalkInRegistration result = control.searchByGuestId(id);
        System.out.println(result != null ? "Found: " + result : "Guest not found in waiting list.");
    }
}
