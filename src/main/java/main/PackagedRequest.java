/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.HashMap;
import java.util.Map;
import json.JsonUtils;

/**
 *
 * @author 802996013
 */
class PackagedRequest {

    private String name;
    private String host;
    private Integer port;
    private String path;
    private String method;
    private String body;
    private String bodyTemplate;
    private Map<String, String> headers;

    public PackagedRequest clone() {
        PackagedRequest clone = new PackagedRequest();
        clone.setHost(getHost());
        clone.setPort(getPort());
        clone.setPath(getPath());
        clone.setMethod(getMethod());
        clone.setBody(getBody());
        clone.setBodyTemplate(getBodyTemplate());
        clone.setHeaders(clone(getHeaders()));
        return clone;
    }

    private Map<String, String> clone(Map<String, String> headers) {
        if (headers == null) {
            return null;
        }
        HashMap<String, String> clone = new HashMap<>();
        for (Map.Entry<String, String> h : headers.entrySet()) {
            clone.put(h.getKey(), h.getValue());
        }
        return clone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String tojSON() {
        return JsonUtils.toJsonFormatted(this);
    }

    @Override
    public String toString() {
        return "PackagedRequest{" + "name=" + name + ", host=" + host + ", port=" + port + ", path=" + path + ", method=" + method + '}';
    }

}
