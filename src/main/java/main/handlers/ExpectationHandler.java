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
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import main.Action;
import main.Main;
import main.Util;
import main.expectations.ExpectationMatcher;

/**
 *
 * @author stuart
 */
public class ExpectationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        long time = System.currentTimeMillis();
        Map<String, String> map = new HashMap<>();
        String body = Util.readStream(he.getRequestBody());
        map.put("METHOD", he.getRequestMethod());
        if ((body != null) && (body.trim().length() > 0)) {
            map.put("BODY", body);
            Main.notifyAction(time, Action.LOG_BODY, body.trim());
        }
        map.put("PATH", he.getRequestURI().getPath());
        map.put("QUERY", he.getRequestURI().getQuery());
        Main.notifyAction(time, Action.LOG_HEADER, "URI:" + he.getRequestURI());
        Main.notifyAction(time, Action.LOG_HEADER, "PATH:" + he.getRequestURI().getPath());
        Main.notifyAction(time, Action.LOG_HEADER, "QUERY:" + he.getRequestURI().getQuery());
        for (Iterator<String> it = he.getRequestHeaders().keySet().iterator(); it.hasNext();) {
            String head = it.next();
            String value = Util.asString(he.getRequestHeaders().get(head));
            map.put(head, value);
            Main.notifyAction(time, Action.LOG_HEADER, "HEADER: " + head + "=" + value);
        }
        ExpectationMatcher.getResponse(time, he, map);
    }
}
