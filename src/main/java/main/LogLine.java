/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author 802996013
 */
public class LogLine {

    String text;
    LogCatagory catagory;
    LogLine next;

    public LogLine(String text, LogCatagory catagory) {
        if ((text == null) || (text.trim().length() == 0)) {
            this.text = "";
            this.catagory = LogCatagory.EMPTY;
        } else {
            this.text = text;
            this.catagory = catagory;
        }
        this.next = null;
    }

    public String getText() {
        return text;
    }

    public LogCatagory getCatagory() {
        return catagory;
    }

    public LogLine getNext() {
        return next;
    }

    public void setNext(LogLine next) {
        this.next = next;
    }
}
