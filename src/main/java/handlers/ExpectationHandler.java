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
import common.BodyType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import common.Action;
import common.Util;
import expectations.ExpectationMatcher;
import common.Notifier;
import mockCallBack.MockResponse;

/**
 *
 * @author stuart
 */
public class ExpectationHandler implements HttpHandler {

    private final int port;
    private final ExpectationMatcher expectationMatcher;
    private final Notifier serverNotifier;

    public ExpectationHandler(int port, ExpectationMatcher expectationMatcher, Notifier serverNotifier) {
        this.port = port;
        this.expectationMatcher = expectationMatcher;
        this.serverNotifier = serverNotifier;
    }


    @Override
    public void handle(HttpExchange he) throws IOException {
        long time = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<>();
        String body = Util.readStream(he.getRequestBody());
        if (body != null) {
            String bodyTrim = body.trim();
            if (serverNotifier != null) {
                serverNotifier.notifyAction(time, port, Action.LOG_BODY, bodyTrim);
            }
            map.put("BODY", bodyTrim);
            map.put("BODY-TYPE", Util.detirmineBodyType(bodyTrim));
        } else {
            map.put("BODY-TYPE", BodyType.EMPTY);
        }
        if (serverNotifier != null) {
            serverNotifier.notifyAction(time, port, Action.LOG_HEADER, "METHOD:" + he.getRequestMethod());
        }
        map.put("METHOD", he.getRequestMethod());
        String path = Util.trimmedNull(he.getRequestURI().getPath());
        if (path != null) {
            if (serverNotifier != null) {
                serverNotifier.notifyAction(time, port, Action.LOG_HEADER, "PATH:" + he.getRequestURI().getPath());
            }
            map.put("PATH", he.getRequestURI().getPath());
            splitIntoMap(map, "PATH", '/');
        }
        String query = Util.trimmedNull(he.getRequestURI().getQuery());
        if (query != null) {
            if (serverNotifier != null) {
                serverNotifier.notifyAction(time, port, Action.LOG_HEADER, "QUERY:" + he.getRequestURI().getQuery());
            }
            map.put("QUERY", he.getRequestURI().getQuery());
            splitIntoMap(map, "QUERY", '&');
        }
        for (Iterator<String> it = he.getRequestHeaders().keySet().iterator(); it.hasNext();) {
            String head = it.next();
            String value = Util.asString(he.getRequestHeaders().get(head));
            map.put("HEAD." + head, value);
            if (serverNotifier != null) {
                serverNotifier.notifyAction(time, port, Action.LOG_HEADER, "HEADER: " + head + "=" + value);
            }
        }
        map.put("INFO.BodyMapped", "false");
        if (expectationMatcher.hasNoExpectations()) {
            MockResponse.respond(he, 404, "No Expectation defined", null, null);
        } else {
            expectationMatcher.getResponse(time, port, he, map);
        }
        
    }

    private void splitIntoMap(Map<String, Object> map, String name, char delim) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        Object s = map.get(name);
        if (s == null) {
            return;
        }
        for (char c : s.toString().trim().toCharArray()) {
            if (c == delim) {
                count = addValueToMap(map, sb, name, count);
            } else {
                sb.append(c);
            }
        }
        addValueToMap(map, sb, name, count++);
    }

    private int addValueToMap(Map<String, Object> map, StringBuilder sb, String name, int count) {
        if (sb.length() == 0) {
            return count;
        }
        String part = sb.toString().trim();
        int pos = part.indexOf('=');
        if (pos > 0) {
            map.put(name + "." + part.substring(0, pos), part.substring(pos + 1));
        } else {
            map.put(name + "[" + count++ + "]", part);
        }
        sb.setLength(0);
        return count;
    }
}
