/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paola.dal;
import com.paola.Models.NewsFeed;
import java.util.List;
import java.util.Set;


public interface NewsRepositoryInterface {
   
    void insertNews(NewsFeed news);

    boolean existsByGuid(String guid);

    Set<String> getAllGuids();

    List<NewsFeed> findAll();

   
    default void printAllTitles(List<NewsFeed> newsList) {
        newsList.forEach(news -> System.out.println(news.getTitle()));
    }
}
