/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expectations;

import java.util.Map;

/**
 *
 * @author stuart
 */
public class ForwardContent {
    private String host;
    private Integer port;
    private String path;
    private String method;
    private String body;
    private String bodyTemplate;
    private boolean forwardHeaders = false;
    private Map<String, String>headers;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public boolean isForwardHeaders() {
        return forwardHeaders;
    }

    public void setForwardHeaders(boolean forwardHeaders) {
        this.forwardHeaders = forwardHeaders;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "ForwardContent{" + "host=" + host + ", port=" + port + ", path=" + path + ", method=" + method + ", forwardHeaders=" + forwardHeaders + '}';
    }
    
}
