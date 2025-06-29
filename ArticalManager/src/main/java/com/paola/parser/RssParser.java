/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paola.parser;


import com.paola.Log;
import com.paola.Models.NewsCategory;
import com.paola.Models.NewsFeed;
import com.paola.dal.NewsRepository;
import org.jsoup.Jsoup;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class RssParser {
     private static final String RSS_URL = "https://www.nasa.gov/news-release/feed/";

    

    public List<NewsFeed> parse(int count) {
        List<NewsFeed> rssFeed = new ArrayList<>();
        System.out.println("PARSE count " + count);

        ExecutorService executor = Executors.newCachedThreadPool();

        Map<String, String> failedDownloads = new HashMap<>();

        NewsRepository repository = new NewsRepository();
        Set<String> downloadedGuids = repository.getAllGuids();


        try {

            URL url = new URL(RSS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            System.out.println("HTTP response code: " + responseCode);

            if (responseCode != 200) {
                System.err.println("Failed to fetch RSS feed.");
                return rssFeed;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            StringBuilder rawXml = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                rawXml.append(line).append("\n");
            }

            System.out.println("--- RAW XML START ---");
            System.out.println(rawXml.substring(0, Math.min(1000, rawXml.length())));
            System.out.println("--- RAW XML END ---");

           
            InputStream inputStream = new ByteArrayInputStream(rawXml.toString().getBytes());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            NodeList items = doc.getElementsByTagName("item"); 

            int addedCount = 0; 
           
            for (int i = 0; i < items.getLength(); i++) {
                if (addedCount >= count) break; 

                org.w3c.dom.Element item = (org.w3c.dom.Element) items.item(i);
                NewsFeed news = new NewsFeed(); 
                
                news.setTitle(getText(item, "title"));
                news.setLink(getText(item, "link"));
                String htmlDescription = getText(item, "content:encoded");
                if (htmlDescription == null || htmlDescription.isEmpty()) {
                    htmlDescription = getText(item, "description");
                }
                news.setDescription(Jsoup.parse(htmlDescription).text()); 
                
                news.setGuid(getText(item, "guid"));
                news.setPubDate(getText(item, "pubDate"));

               
                String imageUrl = getAttribute(item, "media:thumbnail", "url");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    String htmlContent = getText(item, "content:encoded");
                    imageUrl = extractLargestImageFromContent(htmlContent);
                }
                news.setImageUrl(imageUrl);

               
                if (imageUrl != null && !imageUrl.isEmpty() && !downloadedGuids.contains(news.getGuid())) {
                    final String finalImageUrl = imageUrl;
                    String fileName = news.getGuid().replaceAll("[^a-zA-Z0-9]", "_") + "_full.jpg";
                    executor.submit(() -> { 
                        try {
                            downloadImage(finalImageUrl, fileName);
                        } catch (Exception ex) {
                            failedDownloads.put(news.getGuid(), "Failed: " + ex.getMessage());
                        }
                    });
                    news.setLocalImagePath("assets/" + fileName);
                    downloadedGuids.add(news.getGuid());
                }

               
                String combined = news.getTitle() + " " + news.getDescription();
                news.setCategory(detectCategory(combined));

                if (!repository.existsByGuid(news.getGuid())) {
                    rssFeed.add(news);
                    repository.insertNews(news); 
                    addedCount++;
                } else {
                    System.out.println("Skipped duplicate: " + news.getGuid()); 
                }
            }

            
            Log.logFailedDownloads(failedDownloads);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rssFeed; 
    }


   
    private void downloadImage(String imageUrl, String filename) {
        try {
            URL url = new URL(imageUrl);
            Path folder = Paths.get("assets");

            if (!Files.exists(folder)) {
                Files.createDirectories(folder); 
            }
            Path targetPath = folder.resolve(filename);

            try (InputStream in = url.openStream()) {
                
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } 
            System.out.println("Downloaded: " + filename);
        } catch (IOException e) { 
            System.err.println("Failed to download " + imageUrl + ": " + e.getMessage()); 
          
            e.printStackTrace();
        } catch (Exception e) { 
            System.err.println("An unexpected error occurred while downloading " + imageUrl + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private String getAttribute(org.w3c.dom.Element parent, String tagName, String attributeName) {
        NodeList list = parent.getElementsByTagName(tagName); 
        if (list.getLength() > 0) {
            org.w3c.dom.Element elem = (org.w3c.dom.Element) list.item(0); 
            return elem.getAttribute(attributeName); 
        }
        return null; 
    }

   
    private String getText(org.w3c.dom.Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName); 
        if (list.getLength() > 0 && list.item(0).getTextContent() != null) {
            return list.item(0).getTextContent().trim(); 
        }
        return ""; 
    }

    
    private String extractLargestImageFromContent(String html) {
        if (html == null) return null;
        org.jsoup.nodes.Document doc = Jsoup.parse(html); 
        org.jsoup.select.Elements images = doc.select("img"); 

        String largest = null;
        int maxWidth = 0;
        for (org.jsoup.nodes.Element img : images) {
            String src = img.attr("src"); 
            String widthAttr = img.attr("width"); 
            int width = 0;
            try {
                width = Integer.parseInt(widthAttr); 
            } catch (NumberFormatException ignored) {} 

            if (width > maxWidth) {
                maxWidth = width;
                largest = src;
            }
        }
        return largest; 
    }

  
    private static final Map<NewsCategory, List<String>> CATEGORY_KEYWORDS = Map.of(
            NewsCategory.MOON, List.of("moon", "lunar"),
            NewsCategory.MARS, List.of("mars"),
            NewsCategory.ARTEMIS, List.of("artemis"),
            NewsCategory.SPACE, List.of("galaxy", "telescope", "nebula", "hubble", "webb", "cluster")
    );

   
    public static NewsCategory detectCategory(String text) {
        if (text == null || text.isBlank()) return NewsCategory.UNKNOWN; 

        String lowerText = text.toLowerCase(); 
        Map<NewsCategory, Integer> scoreMap = new HashMap<>(); 

        System.out.println("detectCategory text " + text); 

        // Iterates through each category and its associated keywords.
        for (Map.Entry<NewsCategory, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            System.out.println("detectCategory entry " + entry); 

            int score = 0;
            // Checks each keyword for the current category against the news article's text.
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) { 
                    System.out.println("detectCategory keyword " + keyword);
                    score++;
                }
            }
            if (score > 0) { 
                scoreMap.put(entry.getKey(), score);
            }
        }

        // Finds the category with the highest score from the scoreMap.
        NewsCategory returnCategory = scoreMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue()) 
                .map(Map.Entry::getKey) 
                .orElse(NewsCategory.UNKNOWN); 

        System.out.println("detectCategory returnCategory " + returnCategory); 

        return returnCategory; 
    }
}
