/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    public Client(ClientConfig config) {
        this.config = config;
    }

    public ClientResponse send(String path, String body, Method method) {
        String fullHost = config.getHost()
                + (config.getPort() == null ? "" : ":" + config.getPort())
                + (path == null ? "" : "/" + path);
        System.out.println("URL:" + fullHost);
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
            } catch (FileNotFoundException fnfe) {
                return new ClientResponse(responseCode, "Not Found");
            }
            in = new BufferedReader(new InputStreamReader(is));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            in = null;
            return new ClientResponse(responseCode, response.toString());
        } catch (ClientException | IOException e) {
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
}
