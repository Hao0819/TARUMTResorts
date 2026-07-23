/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.adt;

import java.util.Iterator;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Generic collection ADT implemented using a doubly linked structure.
 * Supports FIFO queue operations, rear removal, indexed access,
 * iteration, containment checking, and key-based searching.
 *
 * The front and rear references allow efficient insertion and removal
 * at both ends of the linked structure.
 *
 * Team Component - Shared Collection ADT.
 * Base skeleton authored by: Lim Jun Hao
 *
 * @param <T> the type of entry stored in this collection
 */

public class DoublyLinkedListQueue<T> implements ListQueueInterface<T> {
    private Node front;
    private Node rear;
    private int numberOfEntries;

    private class Node {

        // Stores the generic entry held by this node
        private T data;
        // Points to the node before this node.
        private Node previous;
        // Points to the node after this node.
        private Node next;

        /**
         * Creates an unlinked node containing the given entry.
         *
         * @param data the entry stored in this node
         */
        private Node(T data) {
            this.data = data;
            // A newly created node is not connected yet.
            this.previous = null;
            this.next = null;
        }
    }

    public DoublyLinkedListQueue() {
        front = null;
        rear = null;
        numberOfEntries = 0;
    }

    @Override
    public boolean enqueue(T newEntry) {

        // A linked structure is full only when no more memory is available.
        if (isFull()) {
            return false;
        }

        // Create a new unlinked node for the entry.
        Node newNode = new Node(newEntry);

        if (isEmpty()) {
            // The first node becomes both the front and rear.
            front = newNode;
            rear = newNode;
        } else {
            // Connect the new node after the current rear.
            newNode.previous = rear;
            rear.next = newNode;

            // Move the rear reference to the newly added node.
            rear = newNode;
        }

        numberOfEntries++;
        return true;
    }

    @Override
    public T dequeue() {

        if (isEmpty()) {
            return null;
        }

        // Keep the current front node so it can be disconnected safely.
        Node removedNode = front;
        T removedData = removedNode.data;

        // Move the front reference to the second node.
        front = removedNode.next;

        if (front == null) {
            // The collection contained only one node and is now empty.
            rear = null;
        } else {
            // The new front must not point to the removed node.
            front.previous = null;
        }

        // Fully disconnect the removed node from the linked structure.
        removedNode.previous = null;
        removedNode.next = null;

        numberOfEntries--;
        return removedData;
    }

    @Override
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return front.data;
    }

    @Override
    public T getEntry(int position) {

        if (position < 0
                || position >= numberOfEntries) {

            throw new IndexOutOfBoundsException(
                    "Invalid position: " + position);
        }

        Node currentNode;

        if (position < numberOfEntries / 2) {
            // The position is nearer to the front.
            currentNode = front;

            for (int currentPosition = 0; currentPosition < position; currentPosition++) {

                currentNode = currentNode.next;
            }
        } else {
            // The position is nearer to the rear.
            currentNode = rear;

            for (int currentPosition = numberOfEntries - 1; currentPosition > position; currentPosition--) {

                currentNode = currentNode.previous;
            }
        }

        return currentNode.data;
    }

    @Override
    public boolean contains(T anEntry) {
        Node current = front;
        while (current != null) {
            if (current.data.equals(anEntry)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Returns an iterator that traverses the linked nodes from front to rear.
     * A complete traversal takes O(n) time and uses O(1) extra space.
     */
    @Override
    public Iterator<T> getIterator() {
        return new Iterator<T>() {
            private Node currentNode = front;

            @Override
            public boolean hasNext() {
                return currentNode != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException(
                            "No more queue entries.");
                }

                T currentData = currentNode.data;
                currentNode = currentNode.next;
                return currentData;
            }
        };
    }

    @Override
    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    @Override
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    @Override
    public boolean isFull() {
        // Linked implementation - queue is only "full" if memory runs out.
        // Returning false here; can be overridden with a capacity limit
        // if the team decides a bounded queue is more appropriate.
        return false;
    }

    @Override
    public void clear() {
        front = null;
        rear = null;
        numberOfEntries = 0;
    }

    // ===== Stack-like behaviour (added by: Housekeeping module owner) =====
    @Override
    public T removeLast() {

        if (isEmpty()) {
            return null;
        }

        // Keep the current rear node so it can be disconnected safely.
        Node removedNode = rear;
        T removedData = removedNode.data;

        // Move the rear reference to the previous node.
        rear = removedNode.previous;

        if (rear == null) {
            // The collection contained only one node and is now empty.
            front = null;
        } else {
            // The new rear must not point to the removed node.
            rear.next = null;
        }

        // Fully disconnect the removed node from the linked structure.
        removedNode.previous = null;
        removedNode.next = null;

        numberOfEntries--;
        return removedData;
    }

    @Override
    public T peekLast() {
        if (isEmpty()) {
            return null;
        }
        return rear.data;
    }

    // ===== Key-based search behaviour (added by: Front-Desk module owner) =====

    @Override
    public T searchByKey(String key, KeyExtractor<T> extractor) {
        if (key == null || extractor == null) {
            return null;
        }
        // Self-implemented linear search: walk the linked nodes from front
        // to rear, comparing each entry's key with the target key.
        // Time complexity O(n) - worst case visits every one of the n nodes
        // (target at the rear or absent); best case O(1) (target at front).
        Node current = front;
        while (current != null) {
            String currentKey = extractor.getKey(current.data);
            if (currentKey != null && currentKey.equalsIgnoreCase(key)) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }

    // ===== Filtering behaviour (used by Loyalty and reporting modules) =====

    /**
     * Creates a new collection containing entries that satisfy the condition.
     * The original collection and its entry order remain unchanged.
     *
     * @param condition determines whether an entry should be included
     * @return a new collection containing the matching entries
     */
    @Override
    public ListQueueInterface<T> filter(
            Predicate<T> condition) {

        DoublyLinkedListQueue<T> filteredEntries = new DoublyLinkedListQueue<>();

        if (condition == null) {
            return filteredEntries;
        }

        Node currentNode = front;

        // Traverse every linked node exactly once.
        while (currentNode != null) {

            if (condition.test(currentNode.data)) {
                // Store the same entry reference in the filtered collection.
                filteredEntries.enqueue(currentNode.data);
            }

            currentNode = currentNode.next;
        }

        return filteredEntries;
    }

    // ===== Priority insertion behaviour (used by VIP Allocation module) =====

    /**
     * Inserts an entry into the position determined by the comparator.
     * Entries that compare as equal retain their original insertion order.
     *
     * @param newEntry   the entry to insert
     * @param comparator defines the priority order between entries
     * @return true if the entry was inserted successfully
     */
    @Override
    public boolean priorityEnqueue(
            T newEntry,
            Comparator<T> comparator) {

        if (newEntry == null
                || comparator == null
                || isFull()) {

            return false;
        }

        if (isEmpty()) {
            // The normal enqueue method correctly handles an empty structure.
            return enqueue(newEntry);
        }

        Node currentNode = front;

        // Move forward while the new entry belongs after the current entry.
        // Equal entries are skipped to preserve their original insertion order.
        while (currentNode != null
                && comparator.compare(
                        newEntry,
                        currentNode.data) >= 0) {

            currentNode = currentNode.next;
        }

        if (currentNode == null) {
            // The new entry has the lowest priority and belongs at the rear.
            return enqueue(newEntry);
        }

        Node newNode = new Node(newEntry);
        Node previousNode = currentNode.previous;

        // Connect the new node between previousNode and currentNode.
        newNode.previous = previousNode;
        newNode.next = currentNode;
        currentNode.previous = newNode;

        if (previousNode == null) {
            // The new entry has the highest priority and becomes the front.
            front = newNode;
        } else {
            previousNode.next = newNode;
        }

        numberOfEntries++;
        return true;
    }
}