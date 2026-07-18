/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.adt;
/**
 * Queue.java
 * Linked-list based implementation of QueueInterface. A dummy-free singly
 * linked list is used, with a front and rear reference for O(1) enqueue
 * and dequeue operations.
 *
 * Team Component - Shared Collection ADT.
 * Base skeleton authored by: Lim Jun Hao
 *
 * @param <T> the type of element stored in the queue
 */
public class Queue<T> implements QueueInterface<T> {
    private Node front;
    private Node rear;
    private int numberOfEntries;
    /**
     * Node.java (inner class)
     * Represents a single node in the linked queue.
     */
    private class Node {
        private T data;
        private Node next;
        private Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
    public Queue() {
        front = null;
        rear = null;
        numberOfEntries = 0;
    }
    @Override
    public boolean enqueue(T newEntry) {
        if (isFull()) {
            return false;
        }
        Node newNode = new Node(newEntry);
        if (isEmpty()) {
            front = newNode;
        } else {
            rear.next = newNode;
        }
        rear = newNode;
        numberOfEntries++;
        return true;
    }
    @Override
    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        numberOfEntries--;
        return data;
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
        if (position < 0 || position >= numberOfEntries) {
            throw new IndexOutOfBoundsException("Invalid position: " + position);
        }
        Node current = front;
        for (int i = 0; i < position; i++) {
            current = current.next;
        }
        return current.data;
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
}