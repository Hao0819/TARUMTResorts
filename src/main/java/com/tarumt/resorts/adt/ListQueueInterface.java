package com.tarumt.resorts.adt;

import java.util.Iterator;
import java.util.Comparator;
import java.util.function.Predicate;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

/**
 * ListQueueInterface.java
 *
 * Defines the behaviours of the team's shared generic ListQueue ADT.
 * The ADT supports FIFO queue processing, list-style indexed access,
 * rear removal, iteration, key-based searching, filtering, and
 * comparator-based priority insertion.
 *
 * Different modules use the same interface according to their business
 * requirements. Standard Walk-In Registration uses FIFO operations,
 * VIP Allocation uses priority insertion, Housekeeping uses rear removal,
 * Front-Desk uses key-based searching, and Loyalty uses filtering.
 *
 * The implementation class determines how these behaviours are performed
 * using linked nodes.
 *
 * Team Component - Shared Collection ADT.
 * Base skeleton authored by: Lim Jun Hao
 *
 * @param <T> the type of entry stored in this collection
 */
public interface ListQueueInterface<T> {

    /**
     * Adds newEntry to the rear of the queue.
     * 
     * @param newEntry the entry to be added
     * @return true if the entry was added successfully, false if the
     *         queue is full
     */
    boolean enqueue(T newEntry);

    /**
     * Removes and returns the entry at the front of the queue.
     * 
     * @return the entry that was removed, or null if the queue is empty
     */
    T dequeue();

    /**
     * Returns the entry at the front of the queue without removing it.
     * 
     * @return the entry at the front, or null if the queue is empty
     */
    T peek();

    /**
     * Returns the entry at a given position in the queue, where position 0
     * is the front of the queue. Supports list-style indexed access for
     * traversal and reporting.
     * 
     * @param position the position of the entry to retrieve
     * @return the entry at the given position
     */
    T getEntry(int position);

    /**
     * Returns an iterator that traverses entries from front to rear.
     * A complete traversal runs in O(n) time.
     *
     * @return iterator positioned before the front entry
     */
    Iterator<T> getIterator();

    /**
     * Checks whether a given entry already exists in the queue.
     * Supports set-style duplicate prevention.
     * 
     * @param anEntry the entry to check for
     * @return true if the entry exists in the queue, false otherwise
     */
    boolean contains(T anEntry);

    /**
     * Returns the number of entries currently in the queue.
     * 
     * @return the number of entries
     */
    int getNumberOfEntries();

    /**
     * Checks whether the queue is empty.
     * 
     * @return true if the queue contains no entries
     */
    boolean isEmpty();

    /**
     * Checks whether the queue is full.
     * 
     * @return true if the queue cannot accept any more entries
     */
    boolean isFull();

    /**
     * Removes all entries from the queue, leaving it empty.
     */
    void clear();

    // --- Added for VIP Priority Allocation module ---

    /**
     * Inserts an entry according to the order defined by the comparator.
     * Entries with higher priority can be positioned before entries that
     * were inserted earlier, while equal entries retain insertion order.
     *
     * @param newEntry   the entry to insert
     * @param comparator defines the ordering between entries
     * @return true if the entry was inserted successfully
     */
    boolean priorityEnqueue(
            T newEntry,
            Comparator<T> comparator);

    // --- Added by: Housekeeping module owner ---
    /**
     * Removes and returns the entry most recently added to the rear of the
     * queue. Supports undo/rollback of the last status change logged.
     * 
     * @return the removed entry, or null if the queue is empty
     */
    T removeLast();

    /**
     * Returns (without removing) the entry most recently added to the rear
     * of the queue. Used to preview the current value before rolling back.
     * 
     * @return the entry at the rear, or null if the queue is empty
     */
    T peekLast();

    // --- Added by: Front-Desk Service module owner ---
    /**
     * Performs a key-based linear search over the collection and returns
     * the first entry whose key equals the given key (case-insensitive).
     * The caller supplies a KeyExtractor so this generic collection knows
     * which field of an entry to treat as its key.
     * 
     * @param key       the key value to search for
     * @param extractor knows how to read the key from an entry
     * @return the first matching entry, or null if no entry matches
     */
    T searchByKey(String key, KeyExtractor<T> extractor);

    // --- Added for Loyalty & Rewards and reporting modules ---

    /**
     * Creates a new collection containing entries that satisfy the condition.
     * The original collection and its entry order remain unchanged.
     *
     * @param condition determines whether an entry should be included
     * @return a new collection containing the matching entries
     */
    ListQueueInterface<T> filter(
            Predicate<T> condition);

    /**
     * KeyExtractor (nested helper for searchByKey)
     * A small functional interface that tells this generic collection how to
     * obtain the String search-key from an element of type T (e.g. a Booking
     * is keyed by its confirmation number). Kept nested here so the shared ADT
     * stays as a single interface plus a single implementation class.
     *
     * This is NOT part of the Java Collections Framework; it is a user-defined
     * interface, which is permitted by the assignment rules.
     *
     * Added by: Front-Desk Service module owner
     *
     * @param <E> the type of element the key is extracted from
     */
    interface KeyExtractor<E> {
        /**
         * Returns the String key that identifies the given element.
         * 
         * @param element the element to read the key from
         * @return the element's key
         */
        String getKey(E element);
    }
}
