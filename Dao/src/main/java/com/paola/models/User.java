/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paola.Models;

/**
 *
 * @author paola
 */
public class User {
    private int id;
    private String userName;
    private boolean isAdmin;
	
	public User(int id, String userName, boolean isAdmin) {
        this.id = id;
        this.userName = userName;
        this.isAdmin = isAdmin;
    }

 
    public int getId() {
        return id;
    }

    public String getUsername() {
        return userName;
    }
        
    public boolean isAdmin() {
        return isAdmin;
    }
}
