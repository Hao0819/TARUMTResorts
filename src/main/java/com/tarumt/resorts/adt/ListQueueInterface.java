/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.tarumt.resorts.adt;

import java.util.Iterator;
import java.util.Comparator;
import java.util.function.Predicate;

// Shared generic ListQueue ADT used across all modules - FIFO queue ops, indexed access,
// rear removal, iteration, key search, filtering, and priority insertion.
// Walk-In uses FIFO, VIP uses priorityEnqueue, Housekeeping uses removeLast/peekLast,
// Front-Desk uses searchByKey, Loyalty uses filter. DoublyLinkedListQueue is the implementation.
// Team Component - Shared Collection ADT. Base skeleton by: Lim Jun Hao
public interface ListQueueInterface<T> {

    // adds to the rear of the queue
    boolean enqueue(T newEntry);

    // removes and returns the front entry, null if empty
    T dequeue();

    // returns the front entry without removing it
    T peek();

    // zero-based indexed access, position 0 = front
    T getEntry(int position);

    // front-to-rear traversal, O(n) for a full pass
    Iterator<T> getIterator();

    boolean contains(T anEntry);

    int getNumberOfEntries();

    boolean isEmpty();

    boolean isFull();

    void clear();

    // --- Added by: Brian Lee Kit Mun ---

    // inserts using the comparator's ordering; equal entries keep their insertion order
    boolean priorityEnqueue(
            T newEntry,
            Comparator<T> comparator);

    // --- Added by: Gan Koh Jun ---

    // removes the rear entry - used for undo/rollback of the last logged change
    T removeLast();

    // rear entry without removing it, preview before rollback
    T peekLast();

    // --- Added by: Tan Keng Ting ---

    // linear search for the first entry whose key matches (case-insensitive); extractor tells us which field is the key
    T searchByKey(String key, KeyExtractor<T> extractor);

    // --- Added by: Gary Khor Wei Qi ---

    // builds a new collection with only the matching entries, original stays untouched
    ListQueueInterface<T> filter(
            Predicate<T> condition);

    // Small functional interface so this generic collection knows which field of T to use as a key
    // (e.g. Booking is keyed by confirmation number). Not part of java.util - user-defined, per assignment rules.
    // Added by: Front-Desk Service module owner
    interface KeyExtractor<E> {
        String getKey(E element);
    }
}
