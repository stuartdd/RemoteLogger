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

public class PackagedRequestWrapperManager {

    private static final String NL = System.getProperty("line.separator");
    private static final String LS = "-----------------------------------------" + NL;
    private static String readFileName;
    private static PackagedRequests packagedRequests;
    private static Notifier requestNotifier;
    private static boolean loadedFromFile;

    public static final String EXAMPLE_REQUEST = "{\n"
            + "    \"name\" : \"Get Request\",\n"
            + "    \"host\" : \"http://localhost\",\n"
            + "    \"port\" : 5002,\n"
            + "    \"path\" : \"the/path\",\n"
            + "    \"method\" : \"GET\",\n"
            + "    \"body\" : null,\n"
            + "    \"bodyTemplate\" : null,\n"
            + "    \"headers\" : { "
            + "         \"Accept\": \"application/json\",\n"
            + "         \"Header1\" : \"Value1\"\n"
            + "    }\n"
            + "  }";

    public static final String EXAMPLE_FILE = "{\n"
            + "  \"packagedRequests\" : [ " + EXAMPLE_REQUEST + " ],\n"
            + "  \"paths\" : [ \".\", \"/appl\", \"/bea\" ],\n"
            + "  \"verbose\" : false\n"
            + "}";

    public static boolean canNotDelete() {
        if (packagedRequests == null) {
            return true;
        }
        return packagedRequests.canNotDelete();
    }

    public static boolean isLoadedFromFile() {
        return loadedFromFile;
    }

    public static String getReadFileName() {
        return readFileName;
    }

    public static PackagedRequests getExampleFile() {
        return (PackagedRequests) Config.configFromJson(PackagedRequests.class, EXAMPLE_FILE);
    }

    public static PackagedRequest getExampleRequest() {
        return (PackagedRequest) Config.configFromJson(PackagedRequest.class, EXAMPLE_REQUEST);
    }

    public static PackagedRequestWrapperList getPackagedRequestWrapperList(String currentPackagedRequestName) {
        if (packagedRequests.size() == 0) {
            return null;
        }
        return new PackagedRequestWrapperList(packagedRequests, currentPackagedRequestName);
    }

    public static PackagedRequests getPackagedRequests() {
        return packagedRequests;
    }

    public static boolean isVerbose() {
        if (packagedRequests == null) {
            return false;
        }
        return packagedRequests.isVerbose();
    }

    public static Notifier getRequestNotifier() {
        return requestNotifier;
    }

    public static void setRequestNotifier(Notifier requestNotifier) {
        PackagedRequestWrapperManager.requestNotifier = requestNotifier;
    }

    public static void sendPackagedRequest(PackagedRequest packagedRequest) {
        String body = "";
        if (Util.isEmpty(packagedRequest.getBodyTemplate())) {
            if (Util.isEmpty(packagedRequest.getBody())) {
                body = "";
            } else {
                body = packagedRequest.getBody();
            }
        } else {
            String templateName = packagedRequest.getBodyTemplate();
            body = Util.locateResponseFile(templateName, "PackedRequest", packagedRequests.getPaths(), requestNotifier);
        }
        Map<String, String> headers = new HashMap<>();
        if (packagedRequest.getHeaders() != null) {
            for (Map.Entry<String, String> s : packagedRequest.getHeaders().entrySet()) {
                headers.put(s.getKey(), s.getValue());
            }
        }
        ClientConfig clientConfig = new ClientConfig(packagedRequest.getHost(), packagedRequest.getPort(), headers);
        if (requestNotifier != null) {
            requestNotifier.log(System.currentTimeMillis(), -1, "PACKAGED REQUEST: SEND:" + packagedRequest.toString());
        }
        Client client = new Client(clientConfig, requestNotifier);
        try {
            ClientResponse resp = client.send(packagedRequest.getPath(), body, packagedRequest.getMethod());
            logResponse(new MockResponse(resp.getBody(), resp.getStatus(), resp.getHeaders()), packagedRequest.getName());
        } catch (Exception e) {
            if (requestNotifier != null) {
                requestNotifier.log(System.currentTimeMillis(), -1, e);
            }
            logResponse(new MockResponse("PACKAGED REQUEST: SEND: failed:" + e.getMessage(), 500, null), packagedRequest.getName());
        }
    }

    private static void logResponse(MockResponse mockResponse, String name) {
        String id = "PACKAGED REQUEST: RESP:";
        StringBuilder sb = new StringBuilder();
        sb.append("-    ").append(id).append(' ').append(LS);
        sb.append("-    ").append(id).append(" With STATUS:").append(mockResponse.getStatus()).append(' ').append(NL);
        sb.append(mockResponse.getResponseBody()).append(NL);
        sb.append("-    ").append(id).append(' ').append(LS);
        if (requestNotifier != null) {
            requestNotifier.log(System.currentTimeMillis(), -1, NL + sb.toString().trim());
        }
    }

    public static PackagedRequestWrapperList reload(String currentPackagedRequestName) {
        packagedRequests = loadImpl(readFileName);
        return getPackagedRequestWrapperList(currentPackagedRequestName);
    }

    public static void load(String fileName) {
        readFileName = fileName;
        packagedRequests = loadImpl(fileName);
    }

    public static PackagedRequests loadImpl(String fileName) {
        PackagedRequests packagedRequests;
        loadedFromFile = false;
        File fil = new File(fileName);
        if (fil.exists()) {
            packagedRequests = (PackagedRequests) Config.configFromJsonFile(PackagedRequests.class, fil);
            loadedFromFile = true;
        } else {
            InputStream is = ConfigData.class.getResourceAsStream(fileName);
            if (is == null) {
                is = ConfigData.class.getResourceAsStream("/" + fileName);
                if (is == null) {
                    throw new ConfigDataException("Configuration data [" + fileName + "] could not be found (file or classpath)");
                }
            }
            packagedRequests = (PackagedRequests) Config.configFromJsonStream(PackagedRequests.class, is);
        }
        return packagedRequests;
    }

}
