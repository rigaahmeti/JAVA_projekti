package com.hci.scholarship.service;

public class ScoringService {
    public static final double MINIMUM_AVERAGE = 8.0;

    public double calculateScore(double averageGrade, double familyIncome, int studyYear) {
        double gradeScore = Math.min(averageGrade / 10.0, 1.0) * 45.0;
        double incomeScore = financialNeedPoints(familyIncome);
        double yearScore = Math.min(studyYear / 5.0, 1.0) * 10.0;
        return Math.round((gradeScore + incomeScore + yearScore) * 100.0) / 100.0;
    }

    public String recommendation(double score, double familyIncome) {
        if (score >= 82) return "High Priority - " + financialNeedLevel(familyIncome);
        if (score >= 68) return "Recommended - " + financialNeedLevel(familyIncome);
        return "Committee Review - " + financialNeedLevel(familyIncome);
    }

    public String financialNeedLevel(double familyIncome) {
        if (familyIncome <= 250) return "critical financial need";
        if (familyIncome <= 500) return "high financial need";
        if (familyIncome <= 800) return "medium financial need";
        return "lower financial priority";
    }

    private double financialNeedPoints(double familyIncome) {
        if (familyIncome <= 250) return 45.0;
        if (familyIncome <= 500) return 34.0;
        if (familyIncome <= 800) return 22.0;
        return 10.0;
    }
}

