/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;
import config.Config;
import java.io.File;

/**
 *
 * @author stuar
 */
public class ConfigData extends Config {
    private Integer port;
    private String logDateFormat;
    private Boolean autoConnect;
    private boolean verbose = false;
    private boolean includeHeaders = false;
    private double x;
    private double y;
    private double width;
    private double height;
    
    public static ConfigData loadConfig(File fil){ 
      return (ConfigData) Config.configFromJsonFile(ConfigData.class, fil);        
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

    
}
