/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Action;
import main.Main;

/**
 *
 * @author stuart
 */
public class LogHandler implements HttpHandler {

    private static final String NL = System.getProperty("line.separator");

    @Override
    public void handle(HttpExchange he) throws IOException {
        Main.notifyAction(Action.LOG_TEXT, readStream(he.getRequestBody()));
        Main.log(he.getRequestURI().toASCIIString());
        String response = "log/?";
        he.sendResponseHeaders(201, response.length());
        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private String readStream(InputStream iStream) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(iStream, "utf8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    sb.append(line).append(NL);
                }
            }
            return sb.toString();
        } catch (IOException ex) {
            return "";
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(LogHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
