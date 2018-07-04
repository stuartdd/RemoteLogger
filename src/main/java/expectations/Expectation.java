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
package expectations;

/**
 *
 * @author 802996013
 */
public class Expectation {
    private String name;
    private String method;
    private String path;
    private String query;
    private String bodyType;
    private RequestContent body;
    private ResponseContent response;
 
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public RequestContent getBody() {
        return body;
    }

    public void setBody(RequestContent body) {
        this.body = body;
    }

    public ResponseContent getResponse() {
        return response;
    }

    public void setResponse(ResponseContent response) {
        this.response = response;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    @Override
    public String toString() {
        return "Expectation{name='" + name + "', method=" + method + ", path=" + path + ", response=" + (response==null?"Undefined":response)  + '}';
    }
    
}
