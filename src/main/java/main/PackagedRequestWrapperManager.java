/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import client.Client;
import client.ClientConfig;
import client.ClientResponse;
import common.Notifier;
import common.Util;
import config.Config;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import mockServer.MockResponse;

/**
 *
 * @author 802996013
 */
public class PackagedRequestWrapperManager {

    private static final String NL = System.getProperty("line.separator");
    private static final String LS = "-----------------------------------------" + NL;
    private static String readFileName;

    public static void sendPackagedRequest(PackagedRequest packagedRequest, Notifier notifier) {
        String body = "";
        if (Util.isEmpty(packagedRequest.getBodyTemplate())) {
            if (Util.isEmpty(packagedRequest.getBody())) {
                body = "";
            } else {
                body = packagedRequest.getBody();
            }
        } else {
            String templateName = packagedRequest.getBodyTemplate();
            body = Util.locateResponseFile(templateName, "PackedRequest", null, notifier);
        }
        Map<String, String> headers = new HashMap<>();
        if (packagedRequest.getHeaders() != null) {
            for (Map.Entry<String, String> s : packagedRequest.getHeaders().entrySet()) {
                headers.put(s.getKey(), s.getValue());
            }
        }
        ClientConfig clientConfig = new ClientConfig(packagedRequest.getHost(), packagedRequest.getPort(), headers);
        if (notifier != null) {
            notifier.log(System.currentTimeMillis(), -1, "SENDING PACKAGED REQUEST: " + packagedRequest.toString());
        }
        Client client = new Client(clientConfig, notifier);
        try {
            ClientResponse resp = client.send(packagedRequest.getPath(), body, packagedRequest.getMethod());
            logResponse(new MockResponse(resp.getBody(), resp.getStatus(), resp.getHeaders()), packagedRequest.getName(), notifier);
        } catch (Exception e) {
            if (notifier != null) {
                notifier.log(System.currentTimeMillis(), -1, e);
            }
            logResponse(new MockResponse("SENDING PACKAGED REQUEST: failed:" + e.getMessage(), 500, null), packagedRequest.getName(), notifier);
        }
    }

    private static void logResponse(MockResponse mockResponse, String name, Notifier notifier) {
        String id = "PACKAGED REQUEST";
        StringBuilder sb = new StringBuilder();
        sb.append("-    ").append(id).append(' ').append(LS);
        sb.append("-    ").append(id).append(" With STATUS:").append(mockResponse.getStatus()).append(' ').append(NL);
        sb.append(mockResponse.getResponseBody()).append(NL);
        sb.append("-    ").append(id).append(' ').append(LS);
        if (notifier != null) {
            notifier.log(System.currentTimeMillis(), -1, NL + sb.toString().trim());
        }
    }

    public static PackagedRequestWrapperList load(String fileName) {
        File fil = new File(fileName);
        if (fil.exists()) {
            readFileName = fileName;
            return (ConfigData) Config.configFromJsonFile(ConfigData.class, fil);
        } else {
            InputStream is = ConfigData.class.getResourceAsStream(fileName);
            if (is == null) {
                is = ConfigData.class.getResourceAsStream("/" + fileName);
                if (is == null) {
                    throw new ConfigDataException("Configuration data [" + fileName + "] could not be found (file or classpath)");
                }
            }
            writeFileName = null;
            readFileName = fileName;
            return (ConfigData) Config.configFromJsonStream(ConfigData.class, is);
        }

    }

}
