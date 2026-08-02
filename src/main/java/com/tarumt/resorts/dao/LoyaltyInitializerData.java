/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.dao;

import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;
import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.LoyaltyAccount;
import com.tarumt.resorts.entity.MembershipTier;
import com.tarumt.resorts.entity.LoyaltyTransaction;
import com.tarumt.resorts.entity.LoyaltyTransaction.TransactionType;
import java.time.LocalDate;
import com.tarumt.resorts.entity.Guest;

/**
 * LoyaltyInitializerData.java
 *
 * Creates and initializes sample loyalty-account data using
 * the team's shared generic ListQueue ADT.
 *
 * No Java Collection Framework classes are used.
 *
 * @author YourName
 */
public class LoyaltyInitializerData {

    private ListQueueInterface<LoyaltyAccount> loyaltyAccounts;
    private ListQueueInterface<LoyaltyTransaction> loyaltyTransactions;
    private ListQueueInterface<Guest> guests;

    /**
     * Creates the custom ADT collection and loads sample data.
     */
    public LoyaltyInitializerData(
        ListQueueInterface<Guest> guests) {

    this.guests = guests;

    loyaltyAccounts = new DoublyLinkedListQueue<>();
    loyaltyTransactions = new DoublyLinkedListQueue<>();

    initializeLoyaltyAccounts();
    initializeLoyaltyTransactions();
}
    
    /**
 * Finds a Guest from the shared Guest collection.
 *
 * @param guestId guest ID to search
 * @return matching Guest, or null if not found
 */
    private Guest findGuestById(String guestId) {

    if (guests == null
            || guestId == null
            || guestId.trim().isEmpty()) {

        return null;
    }

    return guests.searchByKey(
            guestId.trim(),
            guest -> guest.getGuestId()
    );
}

    /**
 * Creates loyalty accounts using the same Guest objects
 * shared by Registration, Booking and Front-Desk modules.
 */
    private void initializeLoyaltyAccounts() {

    addInitialAccount("L001", "G001", 2500, true);
    addInitialAccount("L002", "G002", 6800, true);
    addInitialAccount("L003", "G003", 850, true);
    addInitialAccount("L004", "G004", 12000, true);
    addInitialAccount("L005", "G005", 17500, true);
    addInitialAccount("L006", "G006", 23000, true);
    addInitialAccount("L007", "G007", 3200, false);
    addInitialAccount("L008", "G008", 7500, true);
}
    
    /**
 * Adds one initial loyalty account when the Guest exists.
 */
private void addInitialAccount(
        String loyaltyId,
        String guestId,
        int points,
        boolean active) {

    Guest guest = findGuestById(guestId);

    if (guest == null) {
        System.out.println(
                "Warning: Guest "
                + guestId
                + " was not found. Loyalty account "
                + loyaltyId
                + " was not loaded.");

        return;
    }

    LoyaltyAccount account =
            new LoyaltyAccount(
                    loyaltyId,
                    guest,
                    points,
                    active
            );

    loyaltyAccounts.enqueue(account);
}
    
    /**
 * Inserts hard-coded loyalty transactions into the custom ADT.
 */
    private void initializeLoyaltyTransactions() {

        LoyaltyTransaction transaction1 =
                new LoyaltyTransaction(
                     "T001",
                     "L001",
                      "B001",
                      TransactionType.EARN,
                       2500,
                       LocalDate.of(2026, 1, 15),
                       LocalDate.of(2027, 1, 15)
                );

        LoyaltyTransaction transaction2 =
                new LoyaltyTransaction(
                    "T002",
                    "L002",
                    "B002",
                    TransactionType.EARN,
                    7000,
                    LocalDate.of(2026, 2, 10),
                    LocalDate.of(2027, 2, 10)
                );

        LoyaltyTransaction transaction3 =
                new LoyaltyTransaction(
                    "T003",
                    "L002",
                    null,
                    TransactionType.REDEEM,
                    200,
                    LocalDate.of(2026, 3, 5),
                    null
                );

        LoyaltyTransaction transaction4 =
                new LoyaltyTransaction(
                    "T004",
                    "L003",
                    "B003",
                    TransactionType.EARN,
                    850,
                    LocalDate.of(2025, 9, 20),
                    LocalDate.of(2026, 9, 20)
                );

        LoyaltyTransaction transaction5 =
                new LoyaltyTransaction(
                    "T005",
                    "L004",
                    "B004",
                    TransactionType.EARN,
                    12000,
                    LocalDate.of(2026, 4, 2),
                    LocalDate.of(2027, 4, 2)
                );

        LoyaltyTransaction transaction6 =
                new LoyaltyTransaction(
                    "T006",
                    "L005",
                    "B005",
                    TransactionType.EARN,
                    17500,
                    LocalDate.of(2025, 8, 15),
                    LocalDate.of(2026, 8, 15)
                );

        LoyaltyTransaction transaction7 =
                new LoyaltyTransaction(
                    "T007",
                    "L006",
                    "B006",
                    TransactionType.EARN,
                    23000,
                    LocalDate.of(2026, 5, 12),
                    LocalDate.of(2027, 5, 12)
                );

        LoyaltyTransaction transaction8 =
                new LoyaltyTransaction(
                    "T008",
                    "L008",
                    "B008",
                    TransactionType.EARN,
                    7500,
                    LocalDate.of(2025, 8, 25),
                    LocalDate.of(2026, 8, 25)
                );
        
        // L002 earned 7,000 points but already redeemed 200 points
        transaction2.deductRemainingPoints(200);

        loyaltyTransactions.enqueue(transaction1);
        loyaltyTransactions.enqueue(transaction2);
        loyaltyTransactions.enqueue(transaction3);
        loyaltyTransactions.enqueue(transaction4);
        loyaltyTransactions.enqueue(transaction5);
        loyaltyTransactions.enqueue(transaction6);
        loyaltyTransactions.enqueue(transaction7);
        loyaltyTransactions.enqueue(transaction8);
    }

    /**
     * Returns the initialized loyalty-account collection.
     *
     * @return custom ADT containing loyalty accounts
     */
    public ListQueueInterface<LoyaltyAccount> getLoyaltyAccounts() {
        return loyaltyAccounts;
    }
    
    /**
    * Returns the initialized loyalty-transaction collection.
    *
    * @return custom ADT containing loyalty transactions
    */
    public ListQueueInterface<LoyaltyTransaction>
            getLoyaltyTransactions() {

        return loyaltyTransactions;
    }
}
