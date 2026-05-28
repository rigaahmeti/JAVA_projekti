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
        try (Connection connection = Database.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, app.getStudentName());
            ps.setString(2, app.getIndexNumber());
            ps.setString(3, app.getFaculty());
            ps.setString(4, app.getStudyProgram());
            ps.setInt(5, app.getStudyYear());
            ps.setDouble(6, app.getAverageGrade());
            ps.setDouble(7, app.getFamilyIncome());
            ps.setString(8, app.getScholarshipType());
            ps.setString(9, app.getEmail());
            ps.setString(10, app.getPhone());
            ps.setString(11, app.getMunicipality());
            ps.setInt(12, app.getEctsCredits());
            ps.setInt(13, app.getHouseholdMembers());
            ps.setString(14, app.getSpecialCategory());
            ps.setString(15, app.getScholarshipCycle());
            ps.setString(16, app.getMotivation());
            ps.setString(17, app.getDocumentSummary());
            ps.setString(18, app.getStatus());
            ps.setString(19, app.getGender());
            ps.setInt(20, app.isDocumentsConfirmed() ? 1 : 0);
            ps.setString(21, app.getAiRecommendation());
            ps.setDouble(22, app.getAiScore());
            ps.executeUpdate();
        }
    }
    public void updateStatus(int id, String status) throws SQLException {
        try (Connection connection = Database.getConnection(); PreparedStatement ps = connection.prepareStatement("UPDATE scholarship_applications SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
    public boolean existsByIndexNumber(String indexNumber) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM scholarship_applications WHERE index_number=? LIMIT 1")) {
            ps.setString(1, indexNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public void delete(int id) throws SQLException {
        try (Connection connection = Database.getConnection(); PreparedStatement ps = connection.prepareStatement("DELETE FROM scholarship_applications WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
