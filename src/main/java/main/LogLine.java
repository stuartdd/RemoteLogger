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

    private final long time;
    private final LogCatagory catagory;
    private final String text;
    private LogLine next;

    public LogLine(long time, String text, LogCatagory catagory) {
        if ((text == null) || (text.trim().length() == 0)) {
            this.text = "";
            this.catagory = LogCatagory.EMPTY;
        } else {
            this.text = text;
            this.catagory = catagory;
        }
        this.time = time;
        this.next = null;
    }

    public long getTime() {
        return time;
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

    @Override
    public String toString() {
        return "Catagory=" + catagory + ", Text=" + text;
    }

}
