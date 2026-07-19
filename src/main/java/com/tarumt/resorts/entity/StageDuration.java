package com.tarumt.resorts.entity;

/**
 * StageDuration.java
 * Represents the average time (in minutes) spent in a given cleaning
 * stage, used for the Housekeeping average-duration report.
 *
 * @author YourName
 */
public class StageDuration {
    private String stageName;
    private long averageMinutes;

    public StageDuration() {
    }

    public StageDuration(String stageName, long averageMinutes) {
        this.stageName = stageName;
        this.averageMinutes = averageMinutes;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public long getAverageMinutes() {
        return averageMinutes;
    }

    public void setAverageMinutes(long averageMinutes) {
        this.averageMinutes = averageMinutes;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-10d minutes", stageName, averageMinutes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StageDuration)) return false;
        StageDuration other = (StageDuration) obj;
        return stageName != null && stageName.equals(other.stageName);
    }

    @Override
    public int hashCode() {
        return stageName != null ? stageName.hashCode() : 0;
    }
}