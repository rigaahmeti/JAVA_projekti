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
    public List<ScholarshipApplication> findAll() throws SQLException {
        return search("");
    }

    public List<ScholarshipApplication> topPending(int limit) throws SQLException {
        List<ScholarshipApplication> list = new ArrayList<>();
        String sql = "SELECT * FROM scholarship_applications WHERE status='Pending' ORDER BY ai_score DESC, family_income ASC LIMIT ?";
        try (Connection connection = Database.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }
    public List<ScholarshipApplication> search(String keyword) throws SQLException {
        List<ScholarshipApplication> list = new ArrayList<>();
        String sql = """
                SELECT * FROM scholarship_applications
                WHERE student_name LIKE ? OR index_number LIKE ? OR faculty LIKE ? OR study_program LIKE ?
                   OR scholarship_type LIKE ? OR scholarship_cycle LIKE ? OR special_category LIKE ?
                   OR municipality LIKE ? OR status LIKE ?
                ORDER BY
                    CASE status WHEN 'Pending' THEN 0 WHEN 'Approved' THEN 1 ELSE 2 END,
                    ai_score DESC,
                    id DESC
                """;
        try (Connection connection = Database.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            String query = "%" + keyword + "%";
            for (int i = 1; i <= 9; i++) ps.setString(i, query);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }
    public int countByStatus(String status) throws SQLException {
        try (Connection connection = Database.getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM scholarship_applications WHERE status=?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    public int countAll() throws SQLException {
        try (Connection connection = Database.getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM scholarship_applications")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double averageScore() throws SQLException {
        try (Connection connection = Database.getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT AVG(ai_score) FROM scholarship_applications")) {
            return rs.next() ? Math.round(rs.getDouble(1) * 100.0) / 100.0 : 0;
        }
    }
    public List<String[]> countByFaculty() throws SQLException {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT faculty, COUNT(*) AS total FROM scholarship_applications GROUP BY faculty";
        try (Connection c = Database.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) result.add(new String[]{rs.getString("faculty"), String.valueOf(rs.getInt("total"))});
        }
        return result;
    }
    private ScholarshipApplication map(ResultSet rs) throws SQLException {
        return new ScholarshipApplication(
                rs.getInt("id"),
                rs.getString("student_name"),
                rs.getString("index_number"),
                rs.getString("faculty"),
                rs.getString("study_program"),
                rs.getInt("study_year"),
                rs.getDouble("average_grade"),
                rs.getDouble("family_income"),
                rs.getString("scholarship_type"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("municipality"),
                rs.getInt("ects_credits"),
                rs.getInt("household_members"),
                rs.getString("special_category"),
                rs.getString("scholarship_cycle"),
                rs.getString("motivation"),
                rs.getString("document_summary"),
                rs.getString("status"),
                rs.getString("gender"),
                rs.getInt("documents_confirmed") == 1,
                rs.getString("ai_recommendation"),
                rs.getDouble("ai_score"),
                rs.getString("created_at")
        );
    }
}
