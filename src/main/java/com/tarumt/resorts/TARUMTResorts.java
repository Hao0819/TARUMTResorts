/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.tarumt.resorts;
/**import com.tarumt.resorts.boundary.WalkInRegistrationUI;
import com.tarumt.resorts.boundary.PriorityAllocationUI;
import com.tarumt.resorts.boundary.HousekeepingUI;
import com.tarumt.resorts.boundary.FrontDeskUI;
import com.tarumt.resorts.boundary.LoyaltyRewardsUI; **/

import java.util.Scanner;
/**
 *
 * @author junha
 */
public class TARUMTResorts {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;

          /** do {
            System.out.println("\n=== TARUMT Resorts System ===");
            System.out.println("1. Walk-In Registrations");
            System.out.println("2. VIP Priority Allocation");
            System.out.println("3. Housekeeping & Task Log");
            System.out.println("4. Front-Desk Service");
            System.out.println("5. Loyalty & Rewards Service");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

             switch (choice) {
                case 1 -> new WalkInRegistrationUI().showMenu();
                case 2 -> new PriorityAllocationUI().showMenu();
                case 3 -> new HousekeepingUI().showMenu();
                case 4 -> new FrontDeskUI().showMenu();
                case 5 -> new LoyaltyRewardsUI().showMenu();
                case 0 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);**/
    }
}
