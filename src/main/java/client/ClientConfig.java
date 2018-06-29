/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author 802996013
 */
public class ClientConfig {
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String HOST = "http://localhost";
    private static final String ACCEPT_LANGUAGE = "en-US,en;q=0.5";

    private String host = HOST;
    private String userAgent = USER_AGENT;
    private String acceptLang = ACCEPT_LANGUAGE;
    private Integer port = null;

    public ClientConfig(int port) {
        this.port = port;
    }
    
    public ClientConfig(String host) {
        this.host = host;
    }
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAcceptLang() {
        return acceptLang;
    }

    public void setAcceptLang(String acceptLang) {
        this.acceptLang = acceptLang;
    }
    
    
}
