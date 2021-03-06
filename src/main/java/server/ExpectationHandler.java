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
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import common.BodyType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import common.Action;
import common.Util;
import expectations.ExpectationManager;
import common.Notifier;
import expectations.Expectation;
import java.util.TreeMap;
import mockServer.MockRequest;
import mockServer.MockResponse;

/**
 *
 * @author stuart
 */
public class ExpectationHandler implements HttpHandler {

    private final int port;
    private final Server server;
    private final ExpectationManager expectationManager;
    private ResponseHandler responseHandler;
    private final Notifier serverNotifier;
    private final boolean verbose;

    public ExpectationHandler(Server server) {
        this.server = server;
        this.verbose = server.getServerConfig().isVerbose();
        this.port = server.getPort();
        this.expectationManager = server.getExpectationManager();
        this.responseHandler = server.getResponseHandler();
        this.serverNotifier = server.getServerNotifier();
    }

    public void setCallBackClass(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        long time = System.currentTimeMillis();
        this.server.getServerStatistics().inc(ServerStatistics.STAT.REQUEST, true);
        Map<String, Object> map = new TreeMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> queries = new HashMap<>();
        String body = Util.readStream(he.getRequestBody());
        if (body != null) {
            String bodyTrim = body.trim();
            if (serverNotifier != null) {
                serverNotifier.notifyAction(time, port, Action.LOG_BODY, null, bodyTrim);
            }
            map.put("BODY", bodyTrim);
            map.put("BODY-TYPE", Util.detirmineBodyType(bodyTrim));
            Util.loadPropertiesFromBody(map, bodyTrim);
        } else {
            map.put("BODY-TYPE", BodyType.EMPTY);
        }
        if ((serverNotifier != null) && (verbose)) {
            serverNotifier.notifyAction(time, port, Action.LOG_HEADER, null, "METHOD:" + he.getRequestMethod());
        }
        map.put("METHOD", he.getRequestMethod());
        String path = Util.trimmedNull(he.getRequestURI().getPath());
        if (path != null) {
            if ((serverNotifier != null) && (verbose)) {
                serverNotifier.notifyAction(time, port, Action.LOG_HEADER, null, "PATH:" + he.getRequestURI().getPath());
            }
            map.put("PATH", he.getRequestURI().getPath());
            splitIntoMap(map, null, "PATH", '/');
        }
        if ((serverNotifier != null) && (verbose)) {
            serverNotifier.log(time, port, "RECEIVED ---> On PORT=" + port + " BODY-TYPE=" + map.get("BODY-TYPE") + " METHOD=" + map.get("METHOD") + " PATH=" + map.get("PATH"));
        }
        String query = Util.trimmedNull(he.getRequestURI().getQuery());
        if (query != null) {
            if ((serverNotifier != null) && (verbose)) {
                serverNotifier.notifyAction(time, port, Action.LOG_HEADER, null, "QUERY:" + he.getRequestURI().getQuery());
            }
            map.put("QUERY", he.getRequestURI().getQuery());
            splitIntoMap(map, queries, "QUERY", '&');
        }

        for (Iterator<String> it = he.getRequestHeaders().keySet().iterator(); it.hasNext();) {
            String head = it.next();
            String value = Util.asString(he.getRequestHeaders().get(head));
            map.put("HEAD." + head, value);
            headers.put(head, value);
            if ((serverNotifier != null) && (verbose)) {
                serverNotifier.notifyAction(time, port, Action.LOG_HEADER, null, "HEADER: " + head + "=" + value);
            }
        }

        Expectation foundExpectation = expectationManager.findMatchingExpectation(System.currentTimeMillis(), map);

        if (responseHandler != null) {
            MockRequest mockRequest = null;
            if (expectationManager.hasNoExpectations()) {
                mockRequest = new MockRequest(port, map, headers, queries, expectationManager, null);
            } else {
                if (foundExpectation != null) {
                    mockRequest = new MockRequest(port, map, headers, queries, expectationManager, foundExpectation);
                }
            }
            if (mockRequest != null) {
                MockResponse mockResponse = responseHandler.handle(mockRequest, map);
                if (mockResponse != null) {
                    mockResponse.respond(he, map);
                    this.server.getServerStatistics().inc(ServerStatistics.STAT.RESPONSE, true);
                    return;
                }
            }
        }
        if (expectationManager.hasNoExpectations()) {
            MockResponse.respond(he, 404, "No Expectation defined", null, null);
        } else {
            expectationManager.getResponse(time, he, map, headers, queries, foundExpectation);
        }
        this.server.getServerStatistics().inc(ServerStatistics.STAT.RESPONSE, true);
    }

    private void splitIntoMap(Map<String, Object> map, Map<String, String> queries, String name, char delim) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        Object s = map.get(name);
        if (s == null) {
            return;
        }
        for (char c : s.toString().trim().toCharArray()) {
            if (c == delim) {
                count = addValueToMap(map, queries, sb, name, count);
            } else {
                sb.append(c);
            }
        }
        addValueToMap(map, queries, sb, name, count++);
    }

    private int addValueToMap(Map<String, Object> map, Map<String, String> queries, StringBuilder sb, String name, int count) {
        if (sb.length() == 0) {
            return count;
        }
        String part = sb.toString().trim();
        int pos = part.indexOf('=');
        if (pos > 0) {
            map.put(name + "." + part.substring(0, pos), part.substring(pos + 1));
            if (queries != null) {
                queries.put(part.substring(0, pos), part.substring(pos + 1));
            }
        } else {
            map.put(name + "[" + count++ + "]", part);
        }
        sb.setLength(0);
        return count;
    }

}
