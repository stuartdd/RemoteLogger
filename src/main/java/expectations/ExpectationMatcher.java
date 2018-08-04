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

import common.BodyType;
import com.sun.net.httpserver.HttpExchange;
import common.Action;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import json.JsonUtils;

import common.Util;
import template.Template;
import xml.MappedXml;
import common.Notifier;
import mockServer.MockResponse;

/**
 *
 * @author 802996013
 */
public class ExpectationMatcher {

    private static final String NL = System.getProperty("line.separator");
    private static final String LS = "-----------------------------------------------------------" + NL;
    private Expectations expectations;
    private final Notifier serverNotifier;
    private File expectationsFile;
    private long expectationsLoadTime;

    public ExpectationMatcher(Expectations expectations, Notifier serverNotifier) {
        this.serverNotifier = serverNotifier;
        this.expectations = expectations;
        testExpectations(expectations);
    }

    public ExpectationMatcher(String fileName, Notifier serverNotifier) {
        this.serverNotifier = serverNotifier;
        if ((fileName == null) || (fileName.trim().length() == 0)) {
            expectations = null;
            expectationsFile = null;
            return;
        }
        loadExpectations(fileName);
    }

    public void getResponse(long time, int port, HttpExchange he, Map<String, Object> map, Map<String, String> headers, Map<String, String> queries) {
        getResponseData(port, map).respond(he, map);
    }

    public MockResponse getResponseData(int port, Map<String, Object> map) {
        String response = "Not Found";
        int statusCode = 404;
        Map<String, String> responseHeaders = new HashMap<>();
        loadPropertiesFromBody(map, (String) map.get("BODY"));
        if (expectations.isListMap() && (serverNotifier != null)) {
            logMap(System.currentTimeMillis(), port, map, "REQUEST PROPERTIES");
        }

        Expectation foundExpectation = findMatchingExpectation(System.currentTimeMillis(), port, map);
        if (foundExpectation != null) {
            try {
                if (serverNotifier != null) {
                    serverNotifier.log(System.currentTimeMillis(), port, "MATCHED " + foundExpectation);
                }
                if (foundExpectation.getResponse() != null) {
                    ResponseContent responseContent = foundExpectation.getResponse();
                    if (Util.isEmpty(responseContent.getTemplate())) {
                        if (Util.isEmpty(responseContent.getBody())) {
                            response = "Body is undefined";
                        } else {
                            response = Template.parse(responseContent.getBody(), map, true);
                        }
                    } else {
                        String templateName = Template.parse(responseContent.getTemplate(), map, true);
                        response = locateResponseFile(port, templateName);
                    }
                    statusCode = responseContent.getStatus();
                    responseHeaders = responseContent.getHeaders();
                } else {
                    if (foundExpectation.getMethod().equalsIgnoreCase("GET")) {
                        statusCode = 200;
                    } else {
                        statusCode = 201;
                    }
                    response = "Response is undefined";
                }
                map.put("STATUS", "" + statusCode);
                logResponse(System.currentTimeMillis(), port, response, statusCode, "RESP");
            } catch (ExpectationException ee) {
                statusCode = ee.getStatus();
                if (serverNotifier != null) {
                    serverNotifier.log(System.currentTimeMillis(), port, new IOException("Read file failed for expectation: " + foundExpectation.getName() + ". " + ee.getMessage(), ee));
                }
            }
        } else {
            if (serverNotifier != null) {
                serverNotifier.log(System.currentTimeMillis(), port, "Expectation not met");
            }
        }
        return new MockResponse(response, statusCode, responseHeaders);
    }

    public boolean hasNoExpectations() {
        return ((expectations == null) || (expectations.getExpectations().isEmpty()));
    }

    private Expectation findMatchingExpectation(long time, int port, Map<String, Object> map) {
        if (expectations == null) {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "No Expectation have been set!");
            }
            return null;
        }
        reloadExpectations(time, port);
        Expectation found;
        for (Expectation exp : expectations.getExpectations()) {
            found = testExpectationMatches(time, port, exp, map);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Expectation testExpectationMatches(long time, int port, Expectation exp, Map<String, Object> map1) {
        if (doesNotMatchStringOrNullExp(exp.getMethod(), map1.get("METHOD"))) {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "MIS-MATCH:'" + exp.getName() + "' METHOD:'" + exp.getMethod() + "' != '" + map1.get("METHOD") + "'");
            }
            return null;
        }
        if (doesNotMatchStringOrNullExp(exp.getBodyType(), map1.get("BODY-TYPE"))) {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "MIS-MATCH:'" + exp.getName() + "' BODY-TYPE:'" + exp.getBodyType() + "' != '" + map1.get("BODY-TYPE") + "'");
            }
            return null;
        }
        if (!exp.multiPathMatch(map1.get("PATH"))) {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "MIS-MATCH:'" + exp.getName() + "' PATH:'" + exp.getPath() + "' != '" + map1.get("PATH") + "'");
            }
            return null;
        }
        if ((exp.getAsserts() != null) && (!exp.getAsserts().isEmpty())) {
            if (doesNotMatchAllAssertions(time, port, exp, map1)) {
                return null;
            }
        }
        return exp;
    }

    private void loadPropertiesFromBody(Map map, String bodyTrimmed) {
        if ((bodyTrimmed == null) || (bodyTrimmed.isEmpty())) {
            return;
        }
        BodyType bodyType = (BodyType) map.get("BODY-TYPE");
        String bodyTypeName = bodyType.name();
        Map<String, Object> tempMap = mapBodyContent(System.currentTimeMillis(), bodyTrimmed, bodyType);
        for (Map.Entry<String, Object> ent : tempMap.entrySet()) {
            map.put(bodyTypeName + "." + ent.getKey(), ent.getValue());
        }
    }

    private boolean doesNotMatchAllAssertions(long time, int port, Expectation exp, Map<String, Object> map) {
        for (Map.Entry<String, String> ass : exp.getAsserts().entrySet()) {
            Object actual = map.get(ass.getKey());
            if (actual == null) {
                if (serverNotifier != null) {
                    serverNotifier.log(time, port, "MIS-MATCH:'" + exp.getName() + "': ASSERT:'" + ass.getKey() + "' Not Found");
                }
                return true;
            }
            if (!exp.assertMatch(ass.getKey(), actual.toString())) {
                if (serverNotifier != null) {
                    serverNotifier.log(time, port, "MIS-MATCH:'" + exp.getName() + "': ASSERT:' " + ass.getKey() + "'='" + ass.getValue() + "'. Does not match '" + actual + "'");
                }
                return true;
            }
        }
        return false;
    }

    private static Map<String, Object> mapBodyContent(long time, String body, BodyType bodyType) {
        Map<String, Object> tempMap = new HashMap<>();
        try {
            switch (bodyType) {
                case XML:
                    MappedXml mappedXml = new MappedXml(body, null);
                    tempMap.putAll(mappedXml.getMap());
                    break;
                case JSON:
                    tempMap.putAll(JsonUtils.flatMap(body));
            }
        } catch (Exception pe) {
            throw new ExpectationException("Failed to parse body text: Type:" + bodyType, 500, pe);
        }
        return tempMap;
    }

    private static boolean doesNotMatchStringOrNullExp(String exp, Object subject) {
        if ((exp == null) || (exp.trim().length() == 0)) {
            return false;
        }
        return doesNotMatchString(exp, subject.toString());
    }

    private static boolean doesNotMatchString(String exp, Object subject) {
        return (!exp.equalsIgnoreCase(subject.toString()));
    }

    private void logMap(long time, int port, Map<String, Object> map, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(": ").append(LS);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey().equals("BODY")) {
                sb.append(e.getKey()).append('=').append("<-- Excluded. See elsewhere in the logs -->").append(NL);
            } else {
                sb.append(e.getKey()).append('=').append(e.getValue()).append(NL);
            }
        }
        sb.append(id).append(": ").append(LS);
        if (serverNotifier != null) {
            serverNotifier.log(time, port, NL + sb.toString().trim());
        }
    }

    private void logResponse(long time, int port, String resp, int statusCode, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(id).append(' ').append(LS);
        sb.append("* ").append(id).append(' ').append("STATUS:").append(statusCode).append(NL);
        sb.append("* ").append(id).append(' ').append(LS);
        sb.append(resp).append(NL);
        sb.append("* ").append(id).append(' ').append(LS);
        if (serverNotifier != null) {
            serverNotifier.log(time, port, NL + sb.toString().trim());
        }
    }

    private String locateResponseFile(int port, String fileName) {
        if (fileName == null) {
            throw new ExpectationException("File for Expectation is not defined", 500);
        }
        StringBuilder sb = new StringBuilder();
        for (String path : expectations.getPaths()) {
            sb.append('"').append(path).append('"').append(',');
            Path p = Paths.get(path, fileName);
            if (Files.exists(p)) {
                try {
                    if (serverNotifier != null) {
                        serverNotifier.log(System.currentTimeMillis(), port, "Template file found:    " + p.toString());
                    }
                    return new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
                } catch (IOException ex) {
                    throw new ExpectationException("File [" + fileName + "] Not readable from file", 500, ex);
                }
            } else {
                if (serverNotifier != null) {
                    serverNotifier.log(System.currentTimeMillis(), port, "Template file NOT found:    " + p.toString());
                }
            }
        }
        try {
            return readResource(port, fileName, sb.toString());
        } catch (IOException ex) {
            throw new ExpectationException("File [" + fileName + "] Not readable from class path", 500, ex);
        }
    }

    private String readResource(int port, String file, String list) throws IOException {
        InputStream is = ExpectationMatcher.class.getResourceAsStream(file);
        if (is == null) {
            is = ExpectationMatcher.class.getResourceAsStream("/" + file);
        }
        if (is == null) {
            if (serverNotifier != null) {
                serverNotifier.log(System.currentTimeMillis(), port, "Template resource NOT found:" + file);
            }
            throw new ExpectationException("File [" + file + "] Not Found in paths [" + list + "] or on the class path", 404);
        }
        if (serverNotifier != null) {
            serverNotifier.log(System.currentTimeMillis(), port, "Template resource found:    " + file);
        }
        StringBuilder sb = new StringBuilder();
        int content;
        while ((content = is.read()) != -1) {
            sb.append((char) content);
        }
        return sb.toString();
    }

    private void loadExpectations(String expectationsFileName) {
        File file = new File(expectationsFileName);
        if (file.exists()) {
            expectationsFile = file;
            expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, file);
            expectationsLoadTime = file.lastModified();
        } else {
            InputStream is = ExpectationMatcher.class.getResourceAsStream(expectationsFileName);
            if (is == null) {
                is = ExpectationMatcher.class.getResourceAsStream("/" + expectationsFileName);
                if (is == null) {
                    throw new ExpectationException("Expectations file: " + expectationsFileName + " not found (File or classpath)", 500);
                }
            }
            expectationsFile = null;
            expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, is);
            expectationsLoadTime = 0;
        }
        testExpectations(expectations);
        if (serverNotifier != null) {
            serverNotifier.notifyAction(System.currentTimeMillis(), -1, Action.LOAD_EXPECTATIONS, expectations, "Expectations loaded OK");
        }
    }

    private void reloadExpectations(long time, int port) {
        if (expectationsFile != null) {
            if (expectationsFile.lastModified() != expectationsLoadTime) {
                Expectations temp = (Expectations) JsonUtils.beanFromJson(Expectations.class, expectationsFile);
                try {
                    testExpectations(temp);
                    expectations = temp;
                    expectationsLoadTime = expectationsFile.lastModified();
                    if (serverNotifier != null) {
                        serverNotifier.notifyAction(System.currentTimeMillis(), -1, Action.LOAD_EXPECTATIONS, expectations, "Expectations loaded OK");
                        serverNotifier.log(time, port, "* NOTE Expectatations file Reloaded:" + expectationsFile.getAbsolutePath());
                    }
                } catch (ExpectationException ex) {
                    if (serverNotifier != null) {
                        serverNotifier.log(System.currentTimeMillis(), port, "Reload of expectation failed " + expectationsFile.getAbsolutePath(), ex);
                    } else {
                        ex.printStackTrace();
                    }
                    expectationsFile = null;
                }
            }
        }
    }

    public static void testExpectations(Expectations expectations) {
        if (expectations == null) {
            throw new ExpectationException("Expectations are null.", 500);
        }
        if (expectations.getExpectations() == null) {
            throw new ExpectationException("Expectations are empty.", 500);
        }
        Map<String, String> map = new HashMap<>();
        for (Expectation e : expectations.getExpectations()) {
            if (map.containsKey(e.getName())) {
                throw new ExpectationException("Duplicate Expectation name found: " + e.getName(), 500);
            }
            map.put(e.getName(), e.getName());
        }
    }

}
