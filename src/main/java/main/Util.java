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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import main.expectations.BodyType;

/**
 *
 * @author stuar
 */
public class Util {

    public static final String NL = System.getProperty("line.separator");

    public static String trimmedNull(final String s) {
        if (s == null) {
            return null;
        }
        String st = s.trim();
        if (st.length() == 0) {
            return null;
        }
        return st;
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

    public static String readStream(final InputStream iStream) {
        long time = System.currentTimeMillis();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(iStream, "utf8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(Main.getTimeStamp(time)).append(line).append(NL);
            }
            return trimmedNull(sb.toString());
        } catch (IOException ex) {
            Main.log(time, "readStream:", ex);
            return null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Main.log(time, "readStream:", ex);
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

}
