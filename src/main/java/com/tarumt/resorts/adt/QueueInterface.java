package com.tarumt.resorts.adt;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

/**
 * QueueInterface.java
 * ADT Specification: A queue is a linear collection of entries of a type T
 * which follows First-In-First-Out (FIFO) order. An entry may only be added
 * to the rear of the queue and removed from the front of the queue.
 *
 * This interface also includes indexed-access and containment-check
 * operations to support list-style traversal and duplicate prevention,
 * since the collection may be reused by multiple modules with different
 * access requirements.
 *
 * Team Component - Shared Collection ADT.
 * Base skeleton authored by: Lim Jun Hao
 *
 * @param <T> the type of element stored in the queue
 */
public interface QueueInterface <T> {
    
     /**
     * Adds newEntry to the rear of the queue.
     * @param newEntry the entry to be added
     * @return true if the entry was added successfully, false if the
     *         queue is full
     */
    boolean enqueue(T newEntry);

    /**
     * Removes and returns the entry at the front of the queue.
     * @return the entry that was removed, or null if the queue is empty
     */
    T dequeue();

    /**
     * Returns the entry at the front of the queue without removing it.
     * @return the entry at the front, or null if the queue is empty
     */
    T peek();

    /**
     * Returns the entry at a given position in the queue, where position 0
     * is the front of the queue. Supports list-style indexed access for
     * traversal and reporting.
     * @param position the position of the entry to retrieve
     * @return the entry at the given position
     */
    T getEntry(int position);

    /**
     * Checks whether a given entry already exists in the queue.
     * Supports set-style duplicate prevention.
     * @param anEntry the entry to check for
     * @return true if the entry exists in the queue, false otherwise
     */
    boolean contains(T anEntry);

    /**
     * Returns the number of entries currently in the queue.
     * @return the number of entries
     */
    int getNumberOfEntries();

    /**
     * Checks whether the queue is empty.
     * @return true if the queue contains no entries
     */
    boolean isEmpty();

    /**
     * Checks whether the queue is full.
     * @return true if the queue cannot accept any more entries
     */
    boolean isFull();

    /**
     * Removes all entries from the queue, leaving it empty.
     */
    void clear();

    // =====================================================================
    // The methods below are placeholders for module-specific additions.
    // Each module owner should replace the placeholder with their actual
    // method signature and implementation, and update the Javadoc comment
    // with their name.
    // =====================================================================

    // --- Added by: [VIP Priority Allocation module owner] ---
    // boolean insertByPriority(T newEntry, int priorityScore);

    // --- Added by: Housekeeping module owner ---
    /**
    * Removes and returns the entry most recently added to the rear of the
    * queue. Supports undo/rollback of the last status change logged.
    * @return the removed entry, or null if the queue is empty
    */
    T removeLast();

    /**
    * Returns (without removing) the entry most recently added to the rear
    * of the queue. Used to preview the current value before rolling back.
    * @return the entry at the rear, or null if the queue is empty
    */
    T peekLast();

    // --- Added by: Front-Desk Service module owner ---
    /**
     * Performs a key-based linear search over the collection and returns
     * the first entry whose key equals the given key (case-insensitive).
     * The caller supplies a KeyExtractor so this generic collection knows
     * which field of an entry to treat as its key.
     * @param key the key value to search for
     * @param extractor knows how to read the key from an entry
     * @return the first matching entry, or null if no entry matches
     */
    T searchByKey(String key, KeyExtractor<T> extractor);

    // --- Added by: [Loyalty & Rewards module owner] ---
    // QueueInterface<T> getFilteredEntries(...);

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
         * @param element the element to read the key from
         * @return the element's key
         */
        String getKey(E element);
    }
}
