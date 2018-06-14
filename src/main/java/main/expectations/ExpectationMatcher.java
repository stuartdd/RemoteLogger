/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.expectations;

import com.sun.net.httpserver.HttpExchange;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
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

    public static void setExpectations(Expectations expectations) {
        ExpectationMatcher.expectations = expectations;
    }

    private static Expectation matchExpectation(long time, HttpExchange he, String body) {
        if (expectations == null) {
            Main.log("No Expectation have been set!");
            return null;
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
                            listMap(time, map, "XML");
                        }
                        if (doesNotMapAssertions(map, exp.getXml().getAsserts())) {
                            found = null;
                        }
                    } catch (ParseXmlException pe) {
                        Main.log("Failed to parse XML body content",pe.getCause());
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

    private static void listMap(long time, Map<String, String> map, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("* ").append(id).append(LS);
        for (Map.Entry<String, String> e : map.entrySet()) {
            sb.append("* ").append(id).append(" MAP:").append(e.getKey()).append('=').append(e.getValue()).append(NL);
        }
        sb.append("* ").append(id).append(LS);
        Main.log(sb.toString());
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
            Main.log("Expectation met:" + found);
            try {
                Path path = locateResponseFile(found.getFile());
                response = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
                statusCode = found.getStatusCode();
                Main.log("Expectation response:\n-------------------\n" + response + "\n-------------------\nExpectation status:" + statusCode);
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
