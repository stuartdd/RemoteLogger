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

import config.Config;
import java.io.File;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author stuar
 */
public class ConfigData extends Config {

    private Integer port;
    private String logDateFormat;
    private Boolean autoConnect;
    private String timeFormat;
    private String expectationsFile;
    private boolean verbose = false;
    private boolean includeHeaders = true;
    private boolean includeBody = true;
    private boolean includeEmpty = false;
    private boolean showTime = true;
    private double x;
    private double y;
    private double width;
    private double height;

    private DateTimeFormatter ts;

    public static ConfigData loadConfig(File fil) {
        return (ConfigData) Config.configFromJsonFile(ConfigData.class, fil);
    }

    public String timeStamp(long time) {
        DateTime dt = new DateTime(time);
        if (ts == null) {
            ts = DateTimeFormat.forPattern(getTimeFormat());
        }
        return dt.toString(ts);
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLogDateFormat() {
        return logDateFormat;
    }

    public void setLogDateFormat(String logDateFormat) {
        this.logDateFormat = logDateFormat;
    }

    public Boolean getAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(Boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public boolean isIncludeBody() {
        return includeBody;
    }

    public void setIncludeBody(boolean includeBody) {
        this.includeBody = includeBody;
    }

    public boolean isIncludeEmpty() {
        return includeEmpty;
    }

    public void setIncludeEmpty(boolean includeEmpty) {
        this.includeEmpty = includeEmpty;
    }

    public String getTimeFormat() {
        if (timeFormat == null) {
            return "HH:mm:ss.SSS";
        }
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public String getExpectationsFile() {
        return expectationsFile;
    }

    public void setExpectationsFile(String expectationsFile) {
        this.expectationsFile = expectationsFile;
    }
    
    

}
