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

import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import json.JsonUtils;
import main.ConfigData;
import main.Main;
import main.Util;
import template.Template;
import xml.MappedXml;
import xml.ParseXmlException;

/**
 *
 * @author 802996013
 */
public class ExpectationMatcher {

    private static final String NL = System.getProperty("line.separator");
    private static final String LS = "-----------------------------------------------------------" + NL;
    private static Expectations expectations;
    private static long expectationsLoadTime;
    private static File expectationsFile;

    public static void getResponse(long time, HttpExchange he, Map<String, Object> map) {
        String response = "Not Found";
        int statusCode = 404;
        Map<String, String> headers = new HashMap<>();
        Expectation found = findMatchingExpectation(time, map);
        if (found != null) {
            try {
                Main.log(time, "Met " + found);
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
                        response = locateResponseFile(found.getResponse().getTemplate(), map);
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
                logResponse(time, response, statusCode, "RESP");
            } catch (IOException io) {
                Main.log(time, new IOException("Read file failed for " + found + ". " + io.getMessage(), io));
            }
        } else {
            Main.log(time, "Expectation not met");
        }

        try {
            for (Map.Entry<String, String> s : headers.entrySet()) {
                he.getResponseHeaders().add(s.getKey(), Template.parse(s.getValue(), map, true));
            }
            he.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (IOException io) {
            Main.log(time, new IOException("Output Stream write failed for " + found, io));
        }
    }

    public static void setExpectations(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            ExpectationMatcher.expectationsFile = file;
            ExpectationMatcher.expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, file);
            ExpectationMatcher.expectationsLoadTime = file.lastModified();
        } else {
            InputStream is = ConfigData.class.getResourceAsStream(fileName);
            if (is == null) {
                is = ConfigData.class.getResourceAsStream("/" + fileName);
                if (is == null) {
                    throw new ExpectationException("Expectations file: " + fileName + " not found (File or classpath)");
                }
            }
            ExpectationMatcher.expectationsFile = null;
            ExpectationMatcher.expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, is);
            ExpectationMatcher.expectationsLoadTime = 0;
        }
        Map<String, String> map = new HashMap<>();
        for (Expectation e : ExpectationMatcher.expectations.getExpectations()) {
            if (map.containsKey(e.getName())) {
                throw new ExpectationException("Duplicate Expectation name found: " + e.getName());
            }
            map.put(e.getName(), e.getName());
        }
    }

    private static Expectation findMatchingExpectation(long time, Map<String, Object> map) {
        if (expectations == null) {
            Main.log(time, "No Expectation have been set!");
            return null;
        }
        if (ExpectationMatcher.expectationsFile != null) {
            if (expectationsFile.lastModified() != expectationsLoadTime) {
                ExpectationMatcher.expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class,
                        expectationsFile);
                ExpectationMatcher.expectationsLoadTime = expectationsFile.lastModified();
                Main.log(time, "* NOTE Expectatations file Reloaded:" + expectationsFile.getAbsolutePath());
            }
        }
        String bodyTrimmed = Util.trimmedNull(map.get("BODY"));
        Expectation found = null;
        String mapType = "---";
        Map<String, String> tempMap = new HashMap<>();
        for (Expectation exp : expectations.getExpectations()) {
            found = exp;
            if (doesNotMatchStringOrNullExp(exp.getMethod(), map.get("METHOD"))) {
                found = null;
            }
            if (doesNotMatchStringOrNullExp(exp.getUrl(), map.get("PATH"))) {
                found = null;
            }
            if (doesNotMatchStringOrNullExp(exp.getQuery(), map.get("QUERY"))) {
                found = null;
            }
            if (exp.getRequest() != null) {
                if (bodyTrimmed == null) {
                    found = null;
                } else {
                    try {
                        BodyType bodyType = Util.detirmineBodyType(bodyTrimmed);
                        switch (bodyType) {
                            case XML:
                                MappedXml mappedXml = new MappedXml(bodyTrimmed, null);
                                tempMap.putAll(mappedXml.getMap());
                                mapType = "XML";
                                break;
                            case JSON:
                                tempMap.putAll(JsonUtils.flatMap(bodyTrimmed));
                                mapType = "JSON";
                        }
                        if (doesNotMatchRequestAssertions(map, exp.getRequest().getAsserts())) {
                            found = null;
                        }
                    } catch (ParseXmlException pe) {
                        Main.log(time, "Failed to parse body content", pe.getCause());
                        found = null;
                    }
                }
            }
            if (found != null) {
                break;
            }
        }
        map.putAll(tempMap);
        if (expectations.isListMap()) {
            logMap(time, map, mapType);
        }
        return found;
    }

    private static boolean doesNotMatchRequestAssertions(Map<String, Object> map, Map<String, String> asserts) {
        if ((asserts == null) || asserts.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String> ass : asserts.entrySet()) {
            Object actual = map.get(ass.getKey());
            if (actual == null) {
                return true;
            }
            if (!ass.getValue().equalsIgnoreCase("*")) {
                if (doesNotMatchString(ass.getValue(), actual)) {
                    return true;
                }
            }
        }
        return false;
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

    private static void logMap(long time, Map<String, Object> map, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("* ").append(id).append(' ').append(LS);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            sb.append("* ").append(id).append(' ').append(e.getKey()).append('=').append(e.getValue()).append(NL);
        }
        sb.append("* ").append(id).append(' ').append(LS);
        Main.log(time, sb.toString().trim());
    }

    private static void logResponse(long time, String resp, int statusCode, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("* ").append(id).append(' ').append(LS);
        sb.append("* ").append(id).append(' ').append("STATUS:").append(statusCode).append(NL);
        sb.append("* ").append(id).append(' ').append(LS);
        Scanner sc = new Scanner(resp);
        while (sc.hasNextLine()) {
            String l = sc.nextLine();
            sb.append("* ").append(id).append(' ').append(l).append(NL);
        }
        sb.append("* ").append(id).append(' ').append(LS);
        Main.log(time, sb.toString().trim());
    }

    private static String locateResponseFile(String fileName, Map map) throws FileNotFoundException {
        if (fileName == null) {
            throw new FileNotFoundException("File for Expectation is not defined");
        }
        String file = Template.parse(fileName, map, true);
        StringBuilder sb = new StringBuilder();
        for (String path : expectations.getPaths()) {
            sb.append(path).append(',');
            Path p = Paths.get(path, file);
            if (Files.exists(p)) {
                try {
                    return new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
                } catch (IOException ex) {
                    throw new ExpectationException("File [" + file + "] Not readable from file", ex);
                }
            }
        }
        try {
            return readResource(fileName, sb.toString());
        } catch (IOException ex) {
            throw new ExpectationException("File [" + file + "] Not readable from class path", ex);
        }
    }

    private static String readResource(String fileName, String list) throws IOException {
        InputStream is = ExpectationMatcher.class.getResourceAsStream(fileName);
        if (is == null) {
            is = ExpectationMatcher.class.getResourceAsStream("/" + fileName);
        }
        if (is == null) {
            throw new ExpectationException("File [" + fileName + "] Not Found in paths [" + list + "]");
        }
        StringBuilder sb = new StringBuilder();
        int content;
        while ((content = is.read()) != -1) {
            sb.append((char) content);
        }
        return sb.toString();
    }
}
