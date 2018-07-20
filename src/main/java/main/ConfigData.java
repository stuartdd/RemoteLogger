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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import server.ServerConfig;

/**
 *
 * @author stuar
 */
public class ConfigData extends Config {

    private Map<String, ServerConfig> servers = new HashMap<>();
    private String logDateFormat;
    private String timeFormat;
    private Boolean autoConnect;
    private boolean includeHeaders = true;
    private boolean includeBody = true;
    private boolean includeEmpty = false;
    private boolean showTime = true;
    private double x;
    private double y;
    private double width;
    private double height;

    private static String writeFileName;
    private static String readFileName;

    private DateTimeFormatter ts;

    public static ConfigData loadConfig(String fileName) {
        File fil = new File(fileName);
        if (fil.exists()) {
            writeFileName = fil.getAbsolutePath();
            readFileName = writeFileName;
            return (ConfigData) Config.configFromJsonFile(ConfigData.class, fil);
        } else {
            InputStream is = ConfigData.class.getResourceAsStream(fileName);
            if (is == null) {
                is = ConfigData.class.getResourceAsStream("/" + fileName);
                if (is == null) {
                    throw new ConfigDataException("Configuration data [" + fileName + "] could not be found (file or classpath)");
                }
            }
            writeFileName = null;
            readFileName = fileName;
            return (ConfigData) Config.configFromJsonStream(ConfigData.class, is);
        }
    }

    public Map<String, ServerConfig> getServers() {
        return servers;
    }

    public void setServers(Map<String, ServerConfig> servers) {
        this.servers = servers;
    }

 
    public String timeStamp(long time) {
        DateTime dt = new DateTime(time);
        if (ts == null) {
            ts = DateTimeFormat.forPattern(getTimeFormat());
        }
        return dt.toString(ts);
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

    public static String writeFileName() {
        return writeFileName;
    }

    public static String readFileName() {
        return readFileName;
    }

}
