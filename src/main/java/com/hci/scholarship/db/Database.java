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



