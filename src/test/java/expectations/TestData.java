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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import json.JsonUtils;

/**
 *
 * @author stuart
 */
public class TestData {

    Map<String, List<String>> map = new HashMap<>();

    public static TestData load(String fileName) {
        InputStream is = TestData.class.getResourceAsStream(fileName);
        if (is == null) {
            throw new TestDataException("Data File:" + fileName + " Not found");
        }
        TestData td = (TestData) JsonUtils.beanFromJson(TestData.class, is);
        return td;
    }

    public Map<String, List<String>> getMap() {
        return map;
    }

    public void setMap(Map<String, List<String>> map) {
        this.map = map;
    }

    public List<String>  keys() {
        List<String>  l = new ArrayList<>();
        for (String s:map.keySet()) {
            l.add(s);
        }
        return l;
    }
    
    public List map(String key) {
        List<String> val = map.get(key);
        if (val == null) {
            throw new TestDataException("KEY:" + key + " Not found");
        }
        return val;
    }
}
 