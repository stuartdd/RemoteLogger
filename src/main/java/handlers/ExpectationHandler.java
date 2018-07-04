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
package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import expectations.BodyType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import main.Action;
import main.Main;
import main.Util;
import expectations.ExpectationMatcher;

/**
 *
 * @author stuart
 */
public class ExpectationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        long time = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<>();
        String body = Util.readStream(he.getRequestBody());
        if (body != null) {
            String bodyTrim = body.trim();
            Main.notifyAction(time, Action.LOG_BODY, bodyTrim);
            map.put("BODY", bodyTrim);
            map.put("BODY-TYPE", Util.detirmineBodyType(bodyTrim));
        } else {
            map.put("BODY-TYPE", BodyType.EMPTY);            
        }
        Main.notifyAction(time, Action.LOG_HEADER, "METHOD:" + he.getRequestMethod());
        map.put("METHOD", he.getRequestMethod());
        String path = Util.trimmedNull(he.getRequestURI().getPath());
        if (path != null) {
            Main.notifyAction(time, Action.LOG_HEADER, "PATH:" + he.getRequestURI().getPath());
            map.put("PATH", he.getRequestURI().getPath());
        }
        String query = Util.trimmedNull(he.getRequestURI().getQuery());
        if (query != null) {
            Main.notifyAction(time, Action.LOG_HEADER, "QUERY:" + he.getRequestURI().getQuery());
            map.put("QUERY", he.getRequestURI().getQuery());
        }
        for (Iterator<String> it = he.getRequestHeaders().keySet().iterator(); it.hasNext();) {
            String head = it.next();
            String value = Util.asString(he.getRequestHeaders().get(head));
            map.put(head, value);
            Main.notifyAction(time, Action.LOG_HEADER, "HEADER: " + head + "=" + value);
        }
        ExpectationMatcher.getResponse(time, he, map);
    }
}
