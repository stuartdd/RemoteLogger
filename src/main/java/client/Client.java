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

import common.Notifier;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author 802996013
 */
public class Client {

    public enum Method {
        GET, PUT, POST
    }
    private static final String NL = System.getProperty("line.separator");

    private ClientConfig config;
    private Notifier clientNotifier;

    public Client(ClientConfig config, Notifier clientNotifier) {
        this.clientNotifier = clientNotifier;
        this.config = config;
    }

    public ClientResponse send(String path, String body, Method method) {
        String fullHost;
        if (path != null) {
            if (path.startsWith("/")) {
                fullHost = config.getHost()
                        + (config.getPort() == null ? "" : ":" + config.getPort())
                        + path;
            } else {
                fullHost = config.getHost()
                        + (config.getPort() == null ? "" : ":" + config.getPort())
                        + "/" + path;
            }
        } else {
            fullHost = config.getHost();
        }

        if (clientNotifier != null) {
            clientNotifier.log(System.currentTimeMillis(), -1, "URL:" + fullHost);
        }

        URL obj;
        DataOutputStream wr = null;
        BufferedReader in = null;
        HttpURLConnection con;
        try {
            obj = new URL(fullHost);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(method.toString());
            con.setRequestProperty("User-Agent", config.getUserAgent());
            con.setRequestProperty("Accept-Language", config.getAcceptLang());
            if ((body == null) || (body.trim().length() == 0)) {
                con.setDoOutput(false);
            } else {
                con.setDoOutput(true);
                wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(body);
                wr.flush();
                wr.close();
                wr = null;
            }
            int responseCode = con.getResponseCode();
            InputStream is = null;
            try {
                is = con.getInputStream();
            } catch (IOException fnfe) {
                is = con.getErrorStream();
                if (is == null) {
                    if (clientNotifier != null) {
                        clientNotifier.log(System.currentTimeMillis(), -1, "RESPONSE [" + responseCode + "]: No Response");
                    }
                    return new ClientResponse(responseCode, "");
                }
            }
            in = new BufferedReader(new InputStreamReader(is));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                response.append(NL);
            }
            in.close();
            in = null;
            Map<String, String> headers = new HashMap<>();
            for (Map.Entry<String, List<String>> s : con.getHeaderFields().entrySet()) {
                if (s.getKey() == null) {
                    headers.put(listToString(s.getValue(), ';'), "");
                } else {
                    headers.put(s.getKey(), listToString(s.getValue(), ';'));
                }
            }
            if (clientNotifier != null) {
                clientNotifier.log(System.currentTimeMillis(), -1, "RESPONSE [" + responseCode + "]:" + response.toString().trim());
            }
            return new ClientResponse(responseCode, response.toString().trim(), headers);
        } catch (ClientException | IOException e) {
            if (clientNotifier != null) {
                clientNotifier.log(System.currentTimeMillis(), -1, "Failed to send to:" + fullHost, e);
            }
            throw new ClientException("Failed to send to:" + fullHost, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (wr != null) {
                    wr.close();
                }
            } catch (Throwable e) {
            }
        }
    }

    private String listToString(List<String> list, char delim) {
        StringBuilder sb = new StringBuilder();
        int mark = 0;
        for (String s : list) {
            sb.append(s);
            mark = sb.length();
            sb.append(delim);
        }
        sb.setLength(mark);
        return sb.toString();
    }
}
