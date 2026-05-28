package com.hci.scholarship.repository;

import com.hci.scholarship.db.Database;
import com.hci.scholarship.model.ScholarshipApplication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationRepository {
    public void save(ScholarshipApplication app) throws SQLException {
        String sql = """
                INSERT INTO scholarship_applications
                (student_name, index_number, faculty, study_program, study_year, average_grade, family_income,
                 scholarship_type, email, phone, municipality, ects_credits, household_members, special_category,
                 scholarship_cycle, motivation, document_summary, status, gender, documents_confirmed, ai_recommendation, ai_score)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;