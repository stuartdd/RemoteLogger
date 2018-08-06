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
package mockServer;

import com.sun.net.httpserver.HttpExchange;
import expectations.ExpectationManager;
import java.util.Map;

/**
 *
 * @author stuar
 */
public class MockRequest {

    private final Map<String, Object> map;
    private final Map<String, String> headers;
    private final Map<String, String> queries;
    private final int port;
    private final String body;
    private final String path;
    private final String method;
    private final ExpectationManager expectationMatcher;

    public MockRequest(int port, Map<String, Object> map, Map<String, String> headers, Map<String, String> queries, ExpectationManager expectationMatcher) {
        this.port = port;
        this.map = map;
        this.headers = headers;
        this.queries = queries;
        this.method = getMapObject("METHOD");
        this.body = getMapObject("BODY");
        this.path = getMapObject("PATH");
        this.expectationMatcher = expectationMatcher;
    }

    public MockResponse getResponseData(Map<String, Object> map) {
        return expectationMatcher.getResponseData(port, map);
    }
    public MockResponse getResponseData() {
        return expectationMatcher.getResponseData(port, map);
    }

    private String getMapObject(String key) {
        Object o = map.get(key);
        if (o == null) {
            return "";
        }
        return o.toString();
    }

    public String getBody() {
        return body;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueries() {
        return queries;
    }

    public String getMethod() {
        return method;
    }

    public int getPort() {
        return port;
    }

}
