package com.tarumt.resorts.entity;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Guest.java
 * Represents a hotel guest's core profile. Shared entity used across
 * multiple modules.
 *
 * @author junha
 */
public class Guest {

    private String guestId;
    private String name;
    private String contactNumber;
    private String email;
    private MembershipTier membershipTier = MembershipTier.NONE;

    public Guest() {
    }

    /**
     * Creates a Guest using the shared MembershipTier enum.
     */
    public Guest(
            String guestId,
            String name,
            String contactNumber,
            String email,
            MembershipTier membershipTier) {

        this.guestId = guestId;
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;

        this.membershipTier = membershipTier == null
                ? MembershipTier.NONE
                : membershipTier;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MembershipTier getMembershipTier() {
        return membershipTier;
    }

    public void setMembershipTier(
            MembershipTier membershipTier) {

        this.membershipTier = membershipTier == null
                ? MembershipTier.NONE
                : membershipTier;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-20s %-15s %-10s", guestId, name, contactNumber, membershipTier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Guest))
            return false;
        Guest other = (Guest) obj;
        return guestId != null && guestId.equals(other.guestId);
    }

    @Override
    public int hashCode() {
        return guestId != null ? guestId.hashCode() : 0;
    }
}
