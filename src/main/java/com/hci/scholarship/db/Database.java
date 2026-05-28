package com.hci.scholarship.db;

import com.hci.scholarship.repository.UserRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:sqlite:scholarships.db";

    public static String url() {
        return URL;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    public static void initialize() {
        String sql = """
                CREATE TABLE IF NOT EXISTS scholarship_applications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_name TEXT NOT NULL,
                    index_number TEXT NOT NULL UNIQUE,
                    faculty TEXT NOT NULL,
                    study_program TEXT NOT NULL,
                    study_year INTEGER NOT NULL,
                    average_grade REAL NOT NULL,
                    family_income REAL NOT NULL,
                    scholarship_type TEXT NOT NULL,
                    email TEXT NOT NULL DEFAULT '',
                    phone TEXT NOT NULL DEFAULT '',
                    municipality TEXT NOT NULL DEFAULT '',
                    ects_credits INTEGER NOT NULL DEFAULT 0,
                    household_members INTEGER NOT NULL DEFAULT 1,
                    special_category TEXT NOT NULL DEFAULT 'General',
                    scholarship_cycle TEXT NOT NULL DEFAULT 'Annual',
                    motivation TEXT NOT NULL DEFAULT '',
                    document_summary TEXT NOT NULL DEFAULT '',
                    status TEXT NOT NULL,
                    gender TEXT NOT NULL,
                    documents_confirmed INTEGER NOT NULL,
                    ai_recommendation TEXT NOT NULL,
                    ai_score REAL NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
                """;
        String usersSql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('STUDENT', 'ADMIN')),
                    active INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
                """;
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
            statement.execute(usersSql);
            ensureProfessionalColumns(statement);
            seedUsers(connection);
            seedDemoApplications(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }
    private static void seedUsers(Connection connection) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO users (username, password_hash, full_name, role)
                VALUES (?, ?, ?, ?)
                """;
        try (var ps = connection.prepareStatement(sql)) {
            addUser(ps, "student", "student123", "Student Demo", "STUDENT");
            addUser(ps, "admin", "admin123", "Zyrtari i Bursave", "ADMIN");
        }
    }

    private static void addUser(java.sql.PreparedStatement ps, String username, String password, String fullName, String role)
            throws SQLException {
        ps.setString(1, username);
        ps.setString(2, UserRepository.hash(password));
        ps.setString(3, fullName);
        ps.setString(4, role);
        ps.executeUpdate();
    }
    private static void ensureProfessionalColumns(Statement statement) {
        List<String> columns = List.of(
                "ALTER TABLE scholarship_applications ADD COLUMN email TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE scholarship_applications ADD COLUMN phone TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE scholarship_applications ADD COLUMN municipality TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE scholarship_applications ADD COLUMN ects_credits INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE scholarship_applications ADD COLUMN household_members INTEGER NOT NULL DEFAULT 1",
                "ALTER TABLE scholarship_applications ADD COLUMN special_category TEXT NOT NULL DEFAULT 'General'",
                "ALTER TABLE scholarship_applications ADD COLUMN scholarship_cycle TEXT NOT NULL DEFAULT 'Annual'",
                "ALTER TABLE scholarship_applications ADD COLUMN motivation TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE scholarship_applications ADD COLUMN document_summary TEXT NOT NULL DEFAULT ''"
        );
        for (String alter : columns) {
            try {
                statement.executeUpdate(alter);
            } catch (SQLException ignored) {
                // SQLite reports duplicate column names after the first migration.
            }
        }
    }

    private static void seedDemoApplications(Statement statement) throws SQLException {
        statement.executeUpdate("""
         INSERT OR IGNORE INTO scholarship_applications
                (student_name, index_number, faculty, study_program, study_year, average_grade, family_income,
                 scholarship_type, email, phone, municipality, ects_credits, household_members, special_category,
                 scholarship_cycle, motivation, document_summary, status, gender, documents_confirmed, ai_recommendation, ai_score)
                VALUES
                 ('Arta Krasniqi', '2023001', 'FIEK', 'Inxhinieri Kompjuterike', 3, 9.40, 220,
                 'Excellence and Need', 'arta@student.uni', '+38344111222', 'Prishtine', 120, 5, 'Low-income household',
                 'Annual 2026/27', 'Strong academic record and financial need.', 'ID, transcript, income proof',
                 'Approved', 'Female', 1, 'High Priority - critical financial need', 93.30),
                  ('Dion Berisha', '2022018', 'Fakulteti Ekonomik', 'Menaxhment', 4, 8.20, 340,
                 'Social Support', 'dion@student.uni', '+38349123456', 'Peje', 168, 6, 'First-generation student',
                 'Annual 2026/27', 'Needs support to complete the final study year.', 'ID, transcript, income proof',
                 'Pending', 'Male', 1, 'Recommended - high financial need', 78.90),
                ('Elira Gashi', '2024042', 'Fakulteti i Edukimit', 'Edukim Fillor', 2, 8.05, 910,
                 'Research Project', 'elira@student.uni', '+38345101010', 'Gjilan', 60, 3, 'General',
                 'Semester Spring 2027', 'Requests project support for educational research.', 'ID, transcript',
                 'Rejected', 'Female', 1, 'Committee Review - lower financial priority', 50.23)
                """);
        statement.executeUpdate("""
                UPDATE scholarship_applications
                SET ai_score=93.30, ai_recommendation='High Priority - critical financial need'
                WHERE index_number='2023001' AND student_name='Arta Krasniqi'
                """);
        statement.executeUpdate("""
                UPDATE scholarship_applications
                SET ai_score=78.90, ai_recommendation='Recommended - high financial need'
                WHERE index_number='2022018' AND student_name='Dion Berisha'
                """);
        statement.executeUpdate("""
                UPDATE scholarship_applications
                SET average_grade=8.05, family_income=910, ai_score=50.23,
                    ai_recommendation='Committee Review - lower financial priority'
                WHERE index_number='2024042' AND student_name='Elira Gashi'
                """);
    }
}
