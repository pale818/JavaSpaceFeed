/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paola.controller;

import com.paola.Models.NewsCategory;
import com.paola.Models.NewsFeed;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NewsController {
    private List<NewsFeed> allNews;
    private List<NewsFeed> filteredNews;
    private int currentIndex;

    public NewsController(List<NewsFeed> allNews) {
        this.allNews = new ArrayList<>(allNews);      
        this.filteredNews = new ArrayList<>(allNews); 
        this.currentIndex = 0;                        
    }

    public void filterByCategory(NewsCategory category) {
        System.out.println("filteredNews (before filter): " + filteredNews);

        if (category == null) {
            filteredNews = new ArrayList<>(allNews);
        } else {
            filteredNews = allNews.stream()
                    .filter(n -> n.getCategory() == category)
                    .collect(Collectors.toList());

            System.out.println("filteredNews (after filter): " + filteredNews);
        }
        currentIndex = 0;
    }

    public List<NewsFeed> getNewsList() {
        return new ArrayList<>(allNews); 
    }

   
    public Optional<NewsFeed> getOptionalCurrentNews() {
        return filteredNews.isEmpty() ? Optional.empty() : Optional.of(filteredNews.get(currentIndex));
    }

    public boolean hasNext() {
        return currentIndex < filteredNews.size() - 1;
    }

    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    
    public void nextNews() {
        if (hasNext()) currentIndex++;
    }

  
    public void previousNews() {
        if (hasPrevious()) currentIndex--;
    }

    
    public void setNewsList(List<NewsFeed> newsList) {
        this.allNews = new ArrayList<>(newsList);
        this.filteredNews = new ArrayList<>(newsList);
        currentIndex = 0;
    }
}
