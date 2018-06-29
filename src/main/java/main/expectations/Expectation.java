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
package main.expectations;

/**
 *
 * @author 802996013
 */
public class Expectation {
    private String method;
    private String url;
    private String query;
    private RequestContent request;
    private ResponseContent response;
    
    private String file;
    private int statusCode = 200;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public RequestContent getRequest() {
        return request;
    }

    public void setRequest(RequestContent request) {
        this.request = request;
    }

    public ResponseContent getResponse() {
        return response;
    }

    public void setResponse(ResponseContent response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "Expectation{" + "method=" + method + ", url=" + url + ", file=" + file + ", statusCode=" + statusCode + '}';
    }
    
}
