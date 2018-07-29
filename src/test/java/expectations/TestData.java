/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 