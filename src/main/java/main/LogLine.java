/*
 * Copyright (C) 2018 stuartdd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package main;

/**
 *
 * @author 802996013
 */
public class LogLine {

    private static final String NL = System.getProperty("line.separator");

    private final long time;
    private final int port;
    private final LogCatagory catagory;
    private final String text;
    private LogLine next;

    public LogLine(long time, int port, String text, LogCatagory catagory) {
        if ((text == null) || (text.trim().length() == 0)) {
            this.text = "";
            this.catagory = LogCatagory.EMPTY;
        } else {
            this.text = text;
            this.catagory = catagory;
        }
        this.time = time;
        this.port = port;
        this.next = null;
    }

    public long getTime() {
        return time;
    }

    public int getPort() {
        return port;
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

    public void render(StringBuilder sb, ConfigData config) {
        if (Main.getConfig().isShowTime()) {
            sb.append(Main.getConfig().timeStamp(time)).append(":");
        }
        if ((port > 0) && (config.isShowPort())) {
            sb.append("[").append(port).append("] ");
        }
        sb.append(text).append(NL);
    }

    @Override
    public String toString() {
        return "Catagory=" + catagory + ", Text=" + text;
    }

}
