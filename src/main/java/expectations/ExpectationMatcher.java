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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import json.JsonUtils;

import common.Util;
import common.ServerException;
import template.Template;
import xml.MappedXml;
import common.Notifier;

/**
 *
 * @author 802996013
 */
public class ExpectationMatcher {

    private static final String NL = System.getProperty("line.separator");
    private static final String LS = "-----------------------------------------------------------" + NL;
    private Expectations expectations;
    private final Notifier serverNotifier;
    private final File expectationsFile;
    private long expectationsLoadTime;

    public ExpectationMatcher(String fileName, Notifier serverNotifier) {
        this.serverNotifier = serverNotifier;
        if ((fileName == null) || (fileName.trim().length() == 0)) {
            expectations = null;
            expectationsFile = null;
            return;
        }
        File file = new File(fileName);
        if (file.exists()) {
            expectationsFile = file;
            expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, file);
            expectationsLoadTime = file.lastModified();
        } else {
            InputStream is = ExpectationMatcher.class.getResourceAsStream(fileName);
            if (is == null) {
                is = ExpectationMatcher.class.getResourceAsStream("/" + fileName);
                if (is == null) {
                    throw new ExpectationException("Expectations file: " + fileName + " not found (File or classpath)", 500);
                }
            }
            expectationsFile = null;
            expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, is);
            expectationsLoadTime = 0;
        }
        Map<String, String> map = new HashMap<>();
        for (Expectation e : expectations.getExpectations()) {
            if (map.containsKey(e.getName())) {
                throw new ExpectationException("Duplicate Expectation name found: " + e.getName(), 500);
            }
            map.put(e.getName(), e.getName());
        }
    }

    public static void respond(HttpExchange he, int status, String response) {
        OutputStream os = he.getResponseBody();
        try {
            he.sendResponseHeaders(status, response.length());
            os.write(response.getBytes());
            os.flush();
        } catch (IOException ex) {
            throw new ServerException("Failed to write '" + response + "' to output stream ", status, ex);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    throw new ServerException("Failed to close output stream ", status, ex);
                }
            }
        }
    }

    public void getResponse(long time, int port, HttpExchange he, Map<String, Object> map) {
        String response = "Not Found";
        int statusCode = 404;
        Map<String, String> headers = new HashMap<>();
        Expectation found = findMatchingExpectation(time, port, map);
        if (found != null) {
            try {
                if (serverNotifier != null) {
                    serverNotifier.log(time, port, "Matched " + found);
                }
                if (found.getMethod().equalsIgnoreCase("get")) {
                    statusCode = 200;
                }
                if (found.getResponse() != null) {
                    if (Util.isEmpty(found.getResponse().getTemplate())) {
                        if (Util.isEmpty(found.getResponse().getBody())) {
                            response = "Body is undefined";
                        } else {
                            response = found.getResponse().getBody();
                        }
                    } else {
                        response = locateResponseFile(port, found.getResponse().getTemplate(), map);
                    }
                    statusCode = found.getResponse().getStatus();
                    headers = found.getResponse().getHeaders();
                } else {
                    if (found.getMethod().equalsIgnoreCase("GET")) {
                        statusCode = 200;
                    } else {
                        statusCode = 201;
                    }
                    response = "Response is undefined";
                }
                map.put("STATUS", "" + statusCode);
                response = Template.parse(response, map, true);
                logResponse(time, port, response, statusCode, "RESP");
            } catch (ExpectationException ee) {
                statusCode = ee.getStatus();
                if (serverNotifier != null) {
                    serverNotifier.log(time, port, new IOException("Read file failed for expectation: " + found.getName() + ". " + ee.getMessage(), ee));
                }
            }
        } else {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "Expectation not met");
            }
        }
        for (Map.Entry<String, String> s : headers.entrySet()) {
            he.getResponseHeaders().add(s.getKey(), Template.parse(s.getValue(), map, true));
        }
        respond(he, statusCode, response);
    }

    public Expectations getExpectations() {
        return expectations;
    }

    private Expectation findMatchingExpectation(long time, int port, Map<String, Object> map) {
        if (expectations == null) {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "No Expectation have been set!");
            }
            return null;
        }
        if (expectationsFile != null) {
            if (expectationsFile.lastModified() != expectationsLoadTime) {
                expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class,
                        expectationsFile);
                expectationsLoadTime = expectationsFile.lastModified();
                if (serverNotifier != null) {
                    serverNotifier.log(time, port, "* NOTE Expectatations file Reloaded:" + expectationsFile.getAbsolutePath());
                }
            }
        }
        Expectation found = null;
        for (Expectation exp : expectations.getExpectations()) {
            found = testExpectationMatches(time, port, exp, map);
            if (found != null) {
                if (serverNotifier != null) {
                    serverNotifier.log(time, port, "----MATCH:'" + exp.getName() + "' This expectation was met!");
                }
                break;
            }
        }
        if (expectations.isListMap()) {
            logMap(time, port, map);
        }
        return found;
    }

    private Expectation testExpectationMatches(long time, int port, Expectation exp, Map<String, Object> map1) {
        if (doesNotMatchStringOrNullExp(exp.getMethod(), map1.get("METHOD"))) {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "MIS-MATCH:'" + exp.getName() + "' METHOD:'" + exp.getMethod() + "' != '" + map1.get("METHOD") + "'");
            }
            return null;
        }
        if (doesNotMatchStringOrNullExp(exp.getPath(), map1.get("PATH"))) {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "MIS-MATCH:'" + exp.getName() + "' PATH:'" + exp.getPath() + "' != '" + map1.get("PATH") + "'");
            }
            return null;
        }
        if (doesNotMatchStringOrNullExp(exp.getQuery(), map1.get("QUERY"))) {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "MIS-MATCH:'" + exp.getName() + "' QUERY:'" + exp.getQuery() + "' != '" + map1.get("QUERY") + "'");
            }
            return null;
        }
        if (doesNotMatchStringOrNullExp(exp.getBodyType(), map1.get("BODY-TYPE"))) {
            if (serverNotifier != null) {
                serverNotifier.log(time, port, "MIS-MATCH:'" + exp.getName() + "' BODY-TYPE:'" + exp.getBodyType().toString() + "' != '" + map1.get("BODY-TYPE") + "'");
            }
            return null;
        }
        if (exp.getBody() != null) {
            String bodyTrimmed = Util.trimmedNull(map1.get("BODY"));
            if (bodyTrimmed == null) {
                return null;
            } else {
                try {
                    String bodyType = map1.get("BODY-TYPE").toString();
                    Map<String, Object> tempMap = mapBodyContent(time, bodyTrimmed, (BodyType) map1.get("BODY-TYPE"));
                    for (Map.Entry<String, Object> ent : tempMap.entrySet()) {
                        map1.put(bodyType + "." + ent.getKey(), ent.getValue());
                    }
                    if (doesNotMatchRequestAssertions(time, port, exp, map1)) {
                        return null;
                    }
                } catch (ExpectationException pe) {
                    if (serverNotifier != null) {
                        serverNotifier.log(time, port, "Expectation [" + exp.getName() + "] not met! Failed to map body content", pe.getCause());
                    }
                    return null;
                }
            }
        }
        return exp;
    }

    private boolean doesNotMatchRequestAssertions(long time, int port, Expectation exp, Map<String, Object> map) {
        if ((exp.getBody().getAsserts() == null) || exp.getBody().getAsserts().isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String> ass : exp.getBody().getAsserts().entrySet()) {
            Object actual = map.get(ass.getKey());
            if (actual == null) {
                if (serverNotifier != null) {
                    serverNotifier.log(time, port, "MIS-MATCH:" + exp.getName() + ": ASSERT:'" + ass.getKey() + "' Not Found");
                }
                return true;
            }
            if (!ass.getValue().equalsIgnoreCase("*")) {
                if (doesNotMatchString(ass.getValue(), actual)) {
                    if (serverNotifier != null) {
                        serverNotifier.log(time, port, "MIS-MATCH:" + exp.getName() + ": ASSERT:'" + ass.getKey() + "' != " + "'" + ass.getValue() + "'");
                    }
                    return true;
                }
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

    private void logMap(long time, int port, Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("REQUEST PROPERTIES: ").append(LS);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey().equals("BODY")) {
                sb.append(e.getKey()).append('=').append("<-- Excluded. See elsewhere in the logs -->").append(NL);
            } else {
                sb.append(e.getKey()).append('=').append(e.getValue()).append(NL);
            }
        }
        sb.append("REQUEST PROPERTIES: ").append(LS);
        if (serverNotifier != null) {
            serverNotifier.log(time, port, NL + sb.toString().trim());
        }
    }

    private void logResponse(long time, int port, String resp, int statusCode, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("* ").append(id).append(' ').append(LS);
        sb.append("* ").append(id).append(' ').append("STATUS:").append(statusCode).append(NL);
        sb.append("* ").append(id).append(' ').append(LS);
        sb.append(resp).append(NL);
        sb.append("* ").append(id).append(' ').append(LS);
        if (serverNotifier != null) {
            serverNotifier.log(time, port, NL + sb.toString().trim());
        }
    }

    private String locateResponseFile(int port, String fileName, Map map) {
        if (fileName == null) {
            throw new ExpectationException("File for Expectation is not defined", 500);
        }
        String file = Template.parse(fileName, map, true);
        StringBuilder sb = new StringBuilder();
        for (String path : expectations.getPaths()) {
            sb.append('"').append(path).append('"').append(',');
            Path p = Paths.get(path, file);
            if (Files.exists(p)) {
                try {
                    if (serverNotifier != null) {
                        serverNotifier.log(System.currentTimeMillis(), port, "Template file found:    " + p.toString());
                    }
                    return new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
                } catch (IOException ex) {
                    throw new ExpectationException("File [" + file + "] Not readable from file", 500, ex);
                }
            } else {
                if (serverNotifier != null) {
                    serverNotifier.log(System.currentTimeMillis(), port, "Template file NOT found:    " + p.toString());
                }
            }
        }
        try {
            return readResource(port, file, sb.toString());
        } catch (IOException ex) {
            throw new ExpectationException("File [" + file + "] Not readable from class path", 500, ex);
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

    public boolean hasNoExpectations() {
        return ((expectations == null) || (expectations.getExpectations().isEmpty()));
    }
}
