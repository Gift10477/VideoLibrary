package com.videolibrary.rmi;

import com.videolibrary.models.Movie;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the VLS Remote Service using a live XAMPP MySQL Database.
 */
public class VlsServiceImpl extends UnicastRemoteObject implements VlsService {

    // Connection string targeting the XAMPP MySQL instance on localhost
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vls_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Default XAMPP password is empty

    public VlsServiceImpl() throws RemoteException {
        super();
        try {
            // Test connection on startup
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("[Database] Successfully connected to XAMPP MySQL database.");
            }
        } catch (Exception e) {
            System.err.println("[Database Error] Initialization failed: " + e.getMessage());
        }
    }

    @Override
    public synchronized boolean registerMovie(String title, String genre) throws RemoteException {
        String findGenreSql = "SELECT id FROM Genres WHERE genre = ? AND isactive = 1";
        String insertMovieSql = "INSERT INTO Movies (genre_id, Title, isactive) VALUES (?, ?, 1)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            int genreId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(findGenreSql)) {
                stmt.setString(1, genre);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        genreId = rs.getInt("id");
                    }
                }
            }

            if (genreId == -1) return false;

            try (PreparedStatement stmt = conn.prepareStatement(insertMovieSql)) {
                stmt.setInt(1, genreId);
                stmt.setString(2, title);
                stmt.executeUpdate();
                System.out.println("[Server] SQL Insert: Movie '" + title + "' registered successfully.");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized boolean registerCustomer(String name, String phone, String email) throws RemoteException {
        String sql = "INSERT INTO Clients (Fullname, isactive) VALUES (?, 1)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
            System.out.println("[Server] SQL Insert: Customer '" + name + "' registered successfully.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> getGenres() throws RemoteException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT genre FROM Genres WHERE isactive = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("genre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Movie> getMoviesByGenre(String genre) throws RemoteException {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT m.id, m.Title FROM Movies m JOIN Genres g ON m.genre_id = g.id " +
                "WHERE g.genre = ? AND m.isactive = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, genre);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Movie(rs.getInt("id"), rs.getString("Title"), genre));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<String> getCustomers() throws RemoteException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT Fullname FROM Clients WHERE isactive = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("Fullname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public synchronized boolean removeGenre(String genreName) throws RemoteException {
        // Soft delete: set isactive to 0 so it's hidden from lists
        String sql = "UPDATE Genres SET isactive = 0 WHERE genre = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, genreName);
            int rowsUpdated = stmt.executeUpdate();
            System.out.println("[Server] SQL Soft Delete: Genre '" + genreName + "' deactivated.");
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized boolean removeMovie(int movieId) throws RemoteException {
        // Soft delete: set isactive to 0 for the selected movie id
        String sql = "UPDATE Movies SET isactive = 0 WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            int rowsUpdated = stmt.executeUpdate();
            System.out.println("[Server] SQL Soft Delete: Movie ID " + movieId + " deactivated.");
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized boolean rentMovie(String customerName, int movieId) throws RemoteException {
        String findClientSql = "SELECT id FROM Clients WHERE Fullname = ? AND isactive = 1";
        String insertRentalSql = "INSERT INTO Rentals (client_id, movie_id, Returned) VALUES (?, ?, 0)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            int clientId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(findClientSql)) {
                stmt.setString(1, customerName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) clientId = rs.getInt("id");
                }
            }
            if (clientId == -1) return false;

            try (PreparedStatement stmt = conn.prepareStatement(insertRentalSql)) {
                stmt.setInt(1, clientId);
                stmt.setInt(2, movieId);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized boolean returnMovie(String customerName, int movieId) throws RemoteException {
        String sql = "UPDATE Rentals r JOIN Clients c ON r.client_id = c.id " +
                "SET r.Returned = 1 WHERE c.Fullname = ? AND r.movie_id = ? AND r.Returned = 0";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerName);
            stmt.setInt(2, movieId);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public synchronized boolean registerGenre(String genreName) throws RemoteException {
        String sql = "INSERT INTO Genres (genre, isactive) VALUES (?, 1)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, genreName);
            stmt.executeUpdate();
            System.out.println("[Server] SQL Insert: Genre '" + genreName + "' saved.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Movie> getBorrowedMovies(String customerName) throws RemoteException {
        return getMoviesByRentalStatus(customerName, 0);
    }

    @Override
    public List<Movie> getReturnedMovies(String customerName) throws RemoteException {
        return getMoviesByRentalStatus(customerName, 1);
    }

    private List<Movie> getMoviesByRentalStatus(String customerName, int status) {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT m.id, m.Title, g.genre FROM Rentals r " +
                "JOIN Clients c ON r.client_id = c.id " +
                "JOIN Movies m ON r.movie_id = m.id " +
                "JOIN Genres g ON m.genre_id = g.id " +
                "WHERE c.Fullname = ? AND r.Returned = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerName);
            stmt.setInt(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Movie(rs.getInt("id"), rs.getString("Title"), rs.getString("genre")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}