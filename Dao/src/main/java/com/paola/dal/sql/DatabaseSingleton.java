/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paola.dal.sql;




import com.paola.Models.NewsCategory;
import com.paola.Models.NewsFeed;
import com.paola.Models.User;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseSingleton {
   
    private static final DatabaseSingleton instance = new DatabaseSingleton();

    public static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=Photo;encrypt=true;trustServerCertificate=true;user=photoUser;password=photo123!";
    
  
    private DatabaseSingleton() {}

    public static DatabaseSingleton getInstance() {
        return instance;
    }

    public static User authenticateUser(String username, String password) {
        // just for my testing to get hashed passwords
        String rawPassword = "admin123";
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        System.out.println("Hashed admin password: " + hashed);

        rawPassword = "user123";
        hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        System.out.println("Hashed user password: " + hashed);
                
        String sql = "SELECT UserId, username, passwordhash, isAdmin FROM NewsFeedUser WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("passwordhash");
                System.out.println("Hash from DB: " + storedPasswordHash); 
                
                
                if (BCrypt.checkpw(password, storedPasswordHash)) {
                    System.out.println("PASSWORD CHECKED");

                    int id = rs.getInt("UserId");
                    boolean isAdmin = rs.getBoolean("isAdmin");
                    return new User(id, username, isAdmin);
                }
                
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error during password hashing check: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Authentication failed
    }

    public static void deleteAllNewsFeeds() {
        String sql = "EXEC DeleteAllNewsFeeds";
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
            System.out.println("All NewsFeed records deleted.");

        } catch (SQLException e) {
            System.err.println("Failed to delete all news feeds.");
            e.printStackTrace();
        }
    }
	
    public static void insertNewsFeed(NewsFeed news) {
        String sql = "EXEC InsertNewsFeed ?, ?, ?, ?, ?, ?, ?, ?";
     
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL); 
             PreparedStatement stmt = conn.prepareStatement(sql)) { 
            stmt.setString(1, news.getTitle());
            stmt.setString(2, news.getDescription());
            stmt.setString(3, news.getLink());
            stmt.setString(4, news.getGuid());
            stmt.setString(5, news.getPubDate());
            stmt.setString(6, news.getImageUrl());
            stmt.setString(7, news.getLocalImagePath());


            String category = (news.getCategory() != null) ? news.getCategory().name() : NewsCategory.UNKNOWN.name();
            stmt.setString(8, news.getCategory().toString());

            stmt.executeUpdate(); 

        } catch (SQLException e) {
            System.err.println("Failed to insert news: " + news.getTitle());
            e.printStackTrace();
        }
    }


    public static boolean alreadyInDatabase(String guid) {
        String sql = "SELECT COUNT(*) FROM NewsFeed WHERE guid = ?";
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, guid);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; 
    }

    public static Set<String> getAllGuids1() {
        Set<String> guids = new HashSet<>();
        String sql = "SELECT guid FROM NewsFeed";
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                guids.add(rs.getString("guid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guids;
    }

   
    public static Set<String> getAllGuids() {
        Set<String> guids = new HashSet<>();
        String sql = "SELECT guid FROM NewsFeed"; 
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);

             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                guids.add(rs.getString("guid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guids;
    }
}
