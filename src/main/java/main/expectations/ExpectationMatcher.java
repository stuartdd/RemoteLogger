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
import main.Main;

/**
 *
 * @author 802996013
 */
public class ExpectationMatcher {

    private static Expectations expectations;

    public static void setExpectations(Expectations expectations) {
        ExpectationMatcher.expectations = expectations;
    }

    private static boolean doesNotMatchString(String ref, String subject) {
        if ((ref == null) || (ref.trim().length()==0)) {
            return false;
        }
        return (!ref.equalsIgnoreCase(subject));
    }

    private static Expectation matchExpectation(HttpExchange he) {
        if (expectations == null) {
            Main.log("No Expectation have been set!");
            return null;
        }
        Expectation found = null;
        for (Expectation exp : expectations.getExpectations()) {
            found = exp;
            if (doesNotMatchString(exp.getMethod(), he.getRequestMethod())) {
                found = null;
            }
            if (doesNotMatchString(exp.getUrl(), he.getRequestURI().getPath())) {
                found = null;
            }
        }
        return found;
    }

    public static void getResponse(HttpExchange he) {
        String response = "Not Found";
        int statusCode = 200;
        Expectation found = matchExpectation(he);
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
            try {
                he.sendResponseHeaders(statusCode, response.length());
                try (OutputStream os = he.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (IOException io) {
                io.printStackTrace();
                Main.log(new IOException("Output Stream write failed for " + found, io));
            }
        } else {
            Main.log("Expectation not met");
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
