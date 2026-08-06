package com.tarumt.resorts.boundary;


import java.util.Scanner;

/**
 * Displays the system main menu and routes the user
 * to the selected module boundary.
 *
 * @author JunHao
 */
public class MainMenuUI {

    private final WalkInRegistrationUI walkInUI;
    private final HouseKeepingUI housekeepingUI;
    private final FrontDeskUI frontDeskUI;
    private final LoyaltyRewardsUI loyaltyUI;
    private final VIPAllocationUI vipUI;
    private final Scanner scanner;

    public MainMenuUI(
            WalkInRegistrationUI walkInUI,
            HouseKeepingUI housekeepingUI,
            FrontDeskUI frontDeskUI,
            LoyaltyRewardsUI loyaltyUI,
            VIPAllocationUI vipUI,
            Scanner scanner) {

        this.walkInUI = walkInUI;
        this.housekeepingUI = housekeepingUI;
        this.frontDeskUI = frontDeskUI;
        this.loyaltyUI = loyaltyUI;
        this.vipUI = vipUI;
        this.scanner = scanner;
    }

    public void showMenu() {

        int choice;

        do {
            System.out.println();
            System.out.println(
                    "+------------------------------------------------------+");
            System.out.println(
                    "|              TARUMT RESORTS MAIN MENU                |");
            System.out.println(
                    "+------------------------------------------------------+");

            System.out.printf(
                    "| %-52s |%n",
                    "1. Walk-In Booking Requests & Standard Allocation");

            System.out.printf(
                    "| %-52s |%n",
                    "2. Housekeeping & Task Log");

            System.out.printf(
                    "| %-52s |%n",
                    "3. Front-Desk Service");

            System.out.printf(
                    "| %-52s |%n",
                    "4. Loyalty & Rewards Service");

            System.out.printf(
                    "| %-52s |%n",
                    "5. VIP & Loyalty Tier Priority Allocation");

            System.out.printf(
                    "| %-52s |%n",
                    "0. Exit");

            System.out.println(
                    "+------------------------------------------------------+");

            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(
                        scanner.nextLine().trim());

            } catch (NumberFormatException exception) {
                System.out.println(
                        "Invalid input. Please enter a number.");

                choice = -1;
                continue;
            }

            switch (choice) {
                case 1 -> walkInUI.showMenu();
                case 2 -> housekeepingUI.showMenu();
                case 3 -> frontDeskUI.showMenu();
                case 4 -> loyaltyUI.showMenu();
                case 5 -> vipUI.showMenu();

                case 0 -> System.out.println(
                        "Thank you for using TARUMT Resorts.");

                default -> System.out.println(
                        "Invalid choice. Please try again.");
            }

        } while (choice != 0);
    }
}