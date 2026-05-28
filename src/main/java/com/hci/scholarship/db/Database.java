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





