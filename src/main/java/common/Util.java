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
package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.jcp.xml.dsig.internal.dom.Utils;

/**
 *
 * @author stuar
 */
public class Util {

    public static final String NL = System.getProperty("line.separator");

    public static String trimmedNull(final Object s) {
        if (s == null) {
            return null;
        }
        String st = s.toString().trim();
        if (st.length() == 0) {
            return null;
        }
        return st;
    }

    public static boolean isEmpty(final Object s) {
        if (s == null) {
            return true;
        }
        String st = s.toString().trim();
        if (st.length() == 0) {
            return true;
        }
        return false;
    }

    public static String firstN(String s, int n) {
        if (Util.isEmpty(s)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= ' ') {
                sb.append(s.charAt(i));
            } else {
                sb.append(' ');
            }
            if (sb.length() >= n) {
                return sb.toString();
            }
        }
        return sb.toString();
    }

    public static String asString(final List<String> list) {
        StringBuilder sb = new StringBuilder();
        int len = 0;
        for (String s : list) {
            sb.append(s);
            len = sb.length();
            sb.append(';');
        }
        sb.setLength(len);
        return sb.toString();
    }

    public static String readResource(final String resourceName) {
        InputStream is = Util.class.getResourceAsStream(resourceName);
        if (is == null) {
            is = Util.class.getResourceAsStream("/" + resourceName);
            if (is == null) {
                throw new ServerException("Resource data [" + resourceName + "] could not be found.", 500);
            }
        }
        return readStream(is);
    }

    public static String readStream(final InputStream iStream) {
        long time = System.currentTimeMillis();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(iStream, "utf8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(NL);
            }
            return trimmedNull(sb.toString());
        } catch (IOException ex) {
            return null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
            }
        }

    }

    public static BodyType detirmineBodyType(String bodyTrimmed) {
        char cN = bodyTrimmed.charAt(bodyTrimmed.length() - 1);
        if ((bodyTrimmed.charAt(0) == '<') && (cN == '>')) {
            return BodyType.XML;
        }
        if ((cN == '}') && (bodyTrimmed.indexOf(" {") < bodyTrimmed.length() - 1)) {
            return BodyType.JSON;
        }
        return BodyType.TXT;
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    public static String cleanString(String in) {
        return cleanString(in, Integer.MAX_VALUE);
    }

    public static String cleanString(String in, int max) {
        if (in == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : in.trim().toCharArray()) {
            if ((c >= ' ') && (c < 127)) {
                sb.append(c);
            }
        }
        if (sb.length() > max) {
            sb.setLength(max);
        }
        return sb.toString();
    }

    public static List<String> split(String s, char delim) {
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();
        for (char c : s.toCharArray()) {
            if (c == delim) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        return list;
    }

    public static String locateResponseFile(String fileName, String type, String[] paths, Notifier notifier) {
        if (fileName == null) {
            throw new FileException("File for " + type + " is not defined");
        }
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            sb.append('"').append(path).append('"').append(',');
            Path p = Paths.get(path, fileName);
            if (Files.exists(p)) {
                try {
                    if (notifier != null) {
                        notifier.log(System.currentTimeMillis(), -1, "Template file found:    " + p.toString());
                    }
                    return new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
                } catch (IOException ex) {
                    throw new FileException("File [" + fileName + "] Not readable from file");
                }
            } else {
                if (notifier != null) {
                    notifier.log(System.currentTimeMillis(), -1, "Template file NOT found:    " + p.toString());
                }
            }
        }
        try {
            return readResource(fileName, type, sb.toString(), notifier);
        } catch (IOException ex) {
            throw new FileException("File [" + fileName + "] Not readable from class path", ex);
        }
    }

    private static String readResource(String file, String type, String list, Notifier notifier) throws IOException {
        InputStream is = Util.class.getResourceAsStream(file);
        if (is == null) {
            is = FileException.class.getResourceAsStream("/" + file);
        }
        if (is == null) {
            if (notifier != null) {
                notifier.log(System.currentTimeMillis(), -1, type + " resource NOT found:" + file);
            }
            throw new FileException(type + " resource [" + file + "] Not Found in paths [" + list + "] or on the class path");
        }
        if (notifier != null) {
            notifier.log(System.currentTimeMillis(), -1, type + " resource found:    " + file);
        }
        StringBuilder sb = new StringBuilder();
        int content;
        while ((content = is.read()) != -1) {
            sb.append((char) content);
        }
        return sb.toString();
    }

}
