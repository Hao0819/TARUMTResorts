package com.tarumt.resorts.entity;

import java.time.LocalDate;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Booking.java
 * Links a Guest to a Room via a unique confirmation number. Created once
 * Room Allocation assigns a room (from either the Walk-In or VIP path),
 * and is the shared record that Front-Desk looks up and Loyalty updates
 * on checkout.
 *
 * Per ECB rules, this entity only references other entities (Guest, Room),
 * never a Control or Boundary class.
 *
 * @author LimJunHao
 */
public class Booking {
    private String confirmationNumber;
    private Guest guest;
    private Room room;
    private String bookingCreatedTime;
    // Planned stay dates used for room schedule availability.
    private LocalDate scheduledCheckInDate;
    private LocalDate scheduledCheckOutDate;
    private int stayDurationDays;
    private String checkInTime;
    private String checkOutTime; // set by Front-Desk on check-out
    private String status; // "ACTIVE", "CHECKED_OUT"
    // Gross room charge before membership discount.
    private double amount;

    // Membership pricing values locked when the Booking is created.
    private double discountRate;
    private double discountAmount;
    private double finalAmount;

    private String paymentStatus; // "PAID", "UNPAID"

    public Booking() {
    }

    public Booking(String confirmationNumber, Guest guest, Room room, String checkInTime) {
        this.confirmationNumber = confirmationNumber;
        this.guest = guest;
        this.room = room;
        // Temporary compatibility for existing DAO records.
        this.bookingCreatedTime = checkInTime;
        this.checkInTime = checkInTime;
        this.status = "ACTIVE";
        this.amount = 0.0;
        this.paymentStatus = "UNPAID";
    }

    /**
     * Creates a booking with its planned stay schedule and calculated amount.
     */
    public Booking(
            String confirmationNumber,
            Guest guest,
            Room room,
            String bookingCreatedTime,
            String checkInTime,
            LocalDate scheduledCheckInDate,
            int stayDurationDays) {

        this(
                confirmationNumber,
                guest,
                room,
                bookingCreatedTime,
                checkInTime);

        if (scheduledCheckInDate == null) {
            throw new IllegalArgumentException(
                    "Scheduled check-in date cannot be null.");
        }

        if (stayDurationDays <= 0) {
            throw new IllegalArgumentException(
                    "Stay duration must be greater than zero.");
        }

        setSchedule(
                scheduledCheckInDate,
                stayDurationDays);
    }

    public Booking(
            String confirmationNumber,
            Guest guest,
            Room room,
            String bookingCreatedTime,
            String checkInTime) {

        this(
                confirmationNumber,
                guest,
                room,
                checkInTime);

        this.bookingCreatedTime = bookingCreatedTime;
        // No check-in time means the room is booked but the guest has not checked in.
        this.status = checkInTime == null ? "CONFIRMED" : "ACTIVE";
    }

    public LocalDate getScheduledCheckInDate() {
        return scheduledCheckInDate;
    }

    public LocalDate getScheduledCheckOutDate() {
        return scheduledCheckOutDate;
    }

    /**
     * Updates the planned stay dates and recalculates the booking amount.
     * The Control must check room availability before calling this method.
     */
    public void setSchedule(
            LocalDate scheduledCheckInDate,
            int stayDurationDays) {

        if (scheduledCheckInDate == null) {
            throw new IllegalArgumentException(
                    "Scheduled check-in date cannot be null.");
        }

        if (stayDurationDays <= 0) {
            throw new IllegalArgumentException(
                    "Stay duration must be greater than zero.");
        }

        this.scheduledCheckInDate = scheduledCheckInDate;

        this.stayDurationDays = stayDurationDays;

        this.scheduledCheckOutDate = scheduledCheckInDate.plusDays(
                stayDurationDays);

        // Recalculate the total whenever the schedule changes.
        this.amount = room == null
                ? 0.0
                : room.getDailyRate() * stayDurationDays;
        // Recalculate membership pricing after the gross amount changes.
        applyMembershipDiscount();
    }

    public int getStayDurationDays() {
        return stayDurationDays;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getBookingCreatedTime() {
        return bookingCreatedTime;
    }

    public void setBookingCreatedTime(String bookingCreatedTime) {
        this.bookingCreatedTime = bookingCreatedTime;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    /**
     * Loads the verified gross amount of a historical booking.
     * Normal new bookings continue to calculate this value from room rate and
     * stay duration through {@link #setSchedule(LocalDate, int)}.
     */
    public void setHistoricalAmount(double amount) {
        if (amount < 0.0) {
            throw new IllegalArgumentException(
                    "Historical booking amount cannot be negative.");
        }

        this.amount = amount;
        applyMembershipDiscount();
    }

    /**
     * Calculates and stores the membership pricing for this Booking.
     * The gross amount remains unchanged for Loyalty points calculation.
     */
    private void applyMembershipDiscount() {

        MembershipTier membershipTier = guest == null
                || guest.getMembershipTier() == null
                        ? MembershipTier.NONE
                        : guest.getMembershipTier();

        discountRate = membershipTier.getRoomDiscountRate();

        discountAmount = roundMoney(amount * discountRate);

        finalAmount = roundMoney(amount - discountAmount);
    }

    /**
     * Rounds a monetary value to two decimal places.
     */
    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-20s %-8s %-16s %-12s",
                confirmationNumber, guest.getName(), room.getRoomNumber(), checkInTime, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Booking))
            return false;
        Booking other = (Booking) obj;
        return confirmationNumber != null && confirmationNumber.equals(other.confirmationNumber);
    }

    @Override
    public int hashCode() {
        return confirmationNumber != null ? confirmationNumber.hashCode() : 0;
    }
}
