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

import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import json.JsonUtils;
import main.Main;
import xml.MappedXml;
import xml.ParseXmlException;

/**
 *
 * @author 802996013
 */
public class ExpectationMatcher {

    private static final String NL = System.getProperty("line.separator");
    private static final String LS = " -----------------------------------------------------------" + NL;
    private static Expectations expectations;
    private static long expectationsLoadTime;
    private static File expectationsFile;

    public static void setExpectations(File file) {
        ExpectationMatcher.expectationsFile = file;
        ExpectationMatcher.expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, file);
        ExpectationMatcher.expectationsLoadTime = file.lastModified();
    }

    private static Expectation matchExpectation(long time, HttpExchange he, String body) {
        if (expectations == null) {
            Main.log("No Expectation have been set!");
            return null;
        }
        
        if (expectationsFile.lastModified() != expectationsLoadTime) {
            ExpectationMatcher.expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, expectationsFile);
            ExpectationMatcher.expectationsLoadTime = expectationsFile.lastModified();
            Main.log("* NOTE Expectatations file Reloaded:"+expectationsFile.getAbsolutePath());
        }
        
        String bodyTrimmed = trimmedNull(body);
        Map<String, String> map = new HashMap<>();
        Expectation found = null;
        for (Expectation exp : expectations.getExpectations()) {
            found = exp;
            if (doesNotMatchStringOrNullExp(exp.getMethod(), he.getRequestMethod())) {
                found = null;
            }
            if (doesNotMatchStringOrNullExp(exp.getUrl(), he.getRequestURI().getPath())) {
                found = null;
            }
            if (exp.getXml() != null) {
                if (bodyTrimmed == null) {
                    found = null;
                } else {
                    try {
                        MappedXml mappedXml = new MappedXml(bodyTrimmed, null);
                        map.putAll(mappedXml.getMap());
                        if (expectations.isListMap()) {
                            logMap(time, map, "XML ");
                        }
                        if (doesNotMapAssertions(map, exp.getXml().getAsserts())) {
                            found = null;
                        }
                    } catch (ParseXmlException pe) {
                        Main.log("Failed to parse XML body content", pe.getCause());
                        found = null;
                    }
                }
            }
        }
        return found;
    }

    private static boolean doesNotMapAssertions(Map<String, String> map, Map<String, String> asserts) {
        for (Map.Entry<String, String> ass : asserts.entrySet()) {
            String actual = map.get(ass.getKey());
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

    private static boolean doesNotMatchStringOrNullExp(String exp, String subject) {
        if ((exp == null) || (exp.trim().length() == 0)) {
            return false;
        }
        return doesNotMatchString(exp, subject);
    }

    private static boolean doesNotMatchString(String exp, String subject) {
        return (!exp.equalsIgnoreCase(subject));
    }

    private static void logMap(long time, Map<String, String> map, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("* ").append(id).append(LS);
        for (Map.Entry<String, String> e : map.entrySet()) {
            sb.append("* ").append(id).append(' ').append(e.getKey()).append('=').append(e.getValue()).append(NL);
        }
        sb.append("* ").append(id).append(LS);
        Main.log(sb.toString().trim());
    }

    private static void logResponse(long time, String resp, int statusCode, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("* ").append(id).append(LS);
        sb.append("* ").append(id).append("STATUS:").append(statusCode).append(NL);
        sb.append("* ").append(id).append(LS);
        Scanner sc = new Scanner(resp);
        while (sc.hasNextLine()) {
            String l = sc.nextLine();
            sb.append("* ").append(id).append(' ').append(l).append(NL);
        }
        sb.append("* ").append(id).append(LS);
        Main.log(sb.toString().trim());
    }

    private static String trimmedNull(String s) {
        if (s == null) {
            return null;
        }
        String st = s.trim();
        if (st.length() == 0) {
            return null;
        }
        return st;
    }

    public static void getResponse(long time, HttpExchange he, String body) {
        String response = "Not Found";
        int statusCode = 404;
        Expectation found = matchExpectation(time, he, body);
        if (found != null) {
            Main.log("Expectation was met: " + found);
            try {
                Path path = locateResponseFile(found.getFile());
                response = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
                statusCode = found.getStatusCode();
                logResponse(time, response, statusCode, "RESP");
            } catch (IOException io) {
                Main.log(new IOException("Read file failed for " + found + ". " + io.getMessage(), io));
            }
        } else {
            Main.log("Expectation not met");
        }
        try {
            he.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (IOException io) {
            io.printStackTrace();
            Main.log(new IOException("Output Stream write failed for " + found, io));
        }

    }

    private static Path locateResponseFile(String file) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        for (String path : expectations.getPaths()) {
            sb.append(path).append(',');
            Path p = Paths.get(path, file);
            if (Files.exists(p)) {
                return p;
            }
        }
        throw new FileNotFoundException("File [" + file + "] Not Found in paths [" + sb + "]");
    }

}
