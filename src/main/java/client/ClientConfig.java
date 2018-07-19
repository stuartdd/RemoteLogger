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
