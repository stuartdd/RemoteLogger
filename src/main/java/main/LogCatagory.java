/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author stuart
 */
public enum LogCatagory {
    EMPTY(" :"), BODY("B:"), HEADER("H:");
    
    private String name;
    
    LogCatagory(String name) {
        this.name = name.substring(0,1);
    }

    public String getName() {
        return name;
    }
    
 }
