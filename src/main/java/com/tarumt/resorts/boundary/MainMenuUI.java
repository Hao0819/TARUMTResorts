package com.tarumt.resorts.boundary;

import java.util.Scanner;

/**
 * Displays the system main menu and routes the user
 * to the selected module boundary.
 *
 * UI improvement: the home page now shows a "TARUMT RESORTS" banner
 * (name + tagline) above the menu table, instead of jumping straight
 * into the options table with no introduction.
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

    /**
     * Prints a title centered inside a "| ... |" row of the given width.
     * Shared by the menu table so it lines up cleanly.
     */
    private void printCentered(String text, int width) {
        if (text.length() >= width) {
            System.out.printf("| %-" + width + "s |%n", text.substring(0, width));
            return;
        }
        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        System.out.println("| " + " ".repeat(leftPadding) + text + " ".repeat(rightPadding) + " |");
    }

    /**
     * Added / restyled: home-page banner shown once above the menu table
     * every time the main menu is displayed.
     *
     * Fix (readability): the previous "block" font used only "_" and "|"
     * characters, which produced a maze-like, hard-to-read pattern with
     * lots of disconnected strokes (per your screenshot). Switched to a
     * classic thin-line ASCII font — same idea (big text made of
     * characters) but built from "/", "\", "_", and "|" in a way that
     * traces clean, continuous letter outlines, so it reads instantly
     * instead of looking like noise. Still plain default text color, no
     * ANSI codes, so it matches the rest of the console UI.
     */
    private void printHomeBanner() {
        System.out.println();
        System.out.println("     _____  _    ____  _   _ __  __ _____   ____  _____ ____   ___  ____ _____ ____");
        System.out.println("    |_   _|/ \\  |  _ \\| | | |  \\/  |_   _| |  _ \\| ____/ ___| / _ \\|  _ \\_   _/ ___|");
        System.out.println("      | | / _ \\ | |_) | | | | |\\/| | | |   | |_) |  _| \\___ \\| | | | |_) || | \\___ \\");
        System.out.println("      | |/ ___ \\|  _ <| |_| | |  | | | |   |  _ <| |___ ___) | |_| |  _ < | |  ___) |");
        System.out.println("      |_/_/   \\_\\_| \\_\\\\___/|_|  |_| |_|   |_| \\_\\_____|____/ \\___/|_| \\_\\|_| |____/");
        System.out.println();
        printCenteredPlain("Integrated Resort Management System", 90);
        System.out.println();
    }

    /** Centers plain text (no pipe borders) within the given width. */
    private void printCenteredPlain(String text, int width) {
        if (text.length() >= width) {
            System.out.println(text.substring(0, width));
            return;
        }
        int leftPadding = (width - text.length()) / 2;
        System.out.println(" ".repeat(leftPadding) + text);
    }

    public void showMenu() {

        int choice;

        do {
            // Banner is printed first, then the menu table follows below it.
            printHomeBanner();

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