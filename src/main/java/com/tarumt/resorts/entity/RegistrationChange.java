/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tarumt.resorts.entity;

import java.time.LocalDateTime;

/**
 * Records one change made to a Walk-In booking request.
 *
 * @author LimJunHao
 */
public class RegistrationChange {

    private final String fieldName;
    private final String oldValue;
    private final String newValue;
    private final LocalDateTime changedAt;

    public RegistrationChange(
            String fieldName,
            String oldValue,
            String newValue) {

        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = LocalDateTime.now();
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}