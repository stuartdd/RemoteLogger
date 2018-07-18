/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
