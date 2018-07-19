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

import java.util.HashMap;
import java.util.Map;
import common.Util;

/**
 *
 * @author 802996013
 */
public class ResponseContent {
    private String body;
    private int status;
    private String template;
    private Map<String, String> headers = new HashMap<>();

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "{" + "status=" + status + ", body[20]='" + (Util.isEmpty(body)?"":Util.firstN(body, 20)) + "', template=" + (Util.isEmpty(template)?"Undefined":template) + ", headers=" + headers.size() + '}';
    }
    
    
    
}
