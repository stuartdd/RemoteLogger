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
package mockCallBack;

import java.util.Map;

/**
 *
 * @author stuar
 */
public class MockRequest {
    private final String body;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queries;
    private final String method;

    public MockRequest(Object body, Object path, Map<String, String> headers, Map<String, String> queries, Object method) {
        this.body = (body==null?"":body.toString());
        this.path = (path==null?"":path.toString());
        this.headers = headers;
        this.queries = queries;
        this.method = (method==null?"":method.toString());;
    }
    
    
}
