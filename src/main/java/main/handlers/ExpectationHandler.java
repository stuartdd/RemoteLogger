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
package main.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Action;
import main.Main;
import main.expectations.ExpectationMatcher;

/**
 *
 * @author stuart
 */
public class ExpectationHandler implements HttpHandler {

    private static final String NL = System.getProperty("line.separator");

    @Override
    public void handle(HttpExchange he) throws IOException {
        String body = readStream(he.getRequestBody());
        long time = System.currentTimeMillis();

        if ((body != null) && (body.trim().length() > 0)) {
            Main.notifyAction(time, Action.LOG_HEADER, he.getRequestMethod() + ":" + he.getRequestURI());
            Main.notifyAction(time, Action.LOG_BODY, body.trim());
        } else {
            Main.notifyAction(time, Action.LOG_HEADER, he.getRequestMethod() + ":" + he.getRequestURI());
        }
        for (Iterator<String> it = he.getRequestHeaders().keySet().iterator(); it.hasNext();) {
            String head = it.next();
            Main.notifyAction(time, Action.LOG_HEADER, asString("HEADER:" + head, he.getRequestHeaders().get(head)));
        }
        ExpectationMatcher.getResponse(time, he, body);
    }

    private String asString(String key, List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(key).append(':');
        for (String s : list) {
            sb.append('"').append(s).append('"');
        }
        return sb.toString();
    }

    private String readStream(InputStream iStream) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(iStream, "utf8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(Main.getTimeStamp()).append(line).append(NL);
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
                Logger.getLogger(ExpectationHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
