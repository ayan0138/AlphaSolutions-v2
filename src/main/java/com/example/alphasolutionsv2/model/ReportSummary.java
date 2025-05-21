package com.example.alphasolutionsv2.model;

import java.util.Map;

/**
 * Report summary with calculated totals
 */
public class ReportSummary {
    private final double totalEstimatedHours;
    private final double totalCost;
    private final int totalTasks;
    private final Map<String, Integer> tasksByStatus;

    public ReportSummary(double totalEstimatedHours, double totalCost,
                         int totalTasks, Map<String, Integer> tasksByStatus) {
        this.totalEstimatedHours = totalEstimatedHours;
        this.totalCost = totalCost;
        this.totalTasks = totalTasks;
        this.tasksByStatus = tasksByStatus;
    }

    // Getters
    public double getTotalEstimatedHours() { return totalEstimatedHours; }
    public double getTotalCost() { return totalCost; }
    public int getTotalTasks() { return totalTasks; }
    public Map<String, Integer> getTasksByStatus() { return tasksByStatus; }

    // Hjælpe metoder
    public String getFormattedTotalCost() {
        return String.format("%.2f DKK", totalCost);
    }

    public String getFormattedTotalHours() {
        return String.format("%.1f timer", totalEstimatedHours);
    }
}