/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paola.dal;



import com.paola.Models.NewsCategory;
import com.paola.Models.NewsFeed;
import com.paola.dal.sql.DatabaseSingleton;
//import Models.NewsFeed;
import java.sql.*;
import java.util.*;
import java.util.List;


public class NewsRepository implements NewsRepositoryInterface {
    
    private final DatabaseSingleton db = DatabaseSingleton.getInstance();

  
    @Override
    public void insertNews(NewsFeed news) {
        db.insertNewsFeed(news); 
    }

    
    @Override
    public boolean existsByGuid(String guid) {
        return db.alreadyInDatabase(guid); 
    }

    
    @Override
    public Set<String> getAllGuids() {
        Set<String> guids = new HashSet<>(); 
        String sql = "SELECT guid FROM NewsFeed"; 
        
        try (Connection conn = DriverManager.getConnection(DatabaseSingleton.CONNECTION_URL);
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

   
    @Override
    public List<NewsFeed> findAll() {
        List<NewsFeed> newsList = new ArrayList<>(); 
        String sql = "SELECT title, description, link, guid, pubDate, imageUrl, category, localImagePath FROM NewsFeed ORDER BY pubDate DESC";

        try (Connection conn = DriverManager.getConnection(DatabaseSingleton.CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) { 

            while (rs.next()) { 
                NewsFeed news = new NewsFeed(); 
                news.setTitle(rs.getString("title"));
                news.setDescription(rs.getString("description"));
                news.setLink(rs.getString("link"));
                news.setGuid(rs.getString("guid"));
                news.setPubDate(rs.getString("pubDate"));
                news.setImageUrl(rs.getString("imageUrl"));
                news.setCategory(NewsCategory.valueOf(rs.getString("category")));
                news.setLocalImagePath(rs.getString("localImagePath"));
                newsList.add(news); 
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }

        return newsList; 
    }
}
