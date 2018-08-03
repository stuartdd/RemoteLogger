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

import java.util.ArrayList;
import java.util.List;
import json.JsonUtils;

/**
 *
 * @author 802996013
 */
public class Expectations {

    private List<Expectation> expectations = new ArrayList<>();
    private String[] paths;
    private boolean listMap;

    public List<Expectation> getExpectations() {
        return expectations;
    }

    public void setExpectations(List<Expectation> expectations) {
        this.expectations = expectations;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public boolean isListMap() {
        return listMap;
    }

    public void setListMap(boolean listMap) {
        this.listMap = listMap;
    }

    public Expectations withListMap(boolean listMap) {
        this.setListMap(listMap);
        return this;
    }
    
    public Expectations withPaths(String[] paths) {
        this.setPaths(paths);
        return this;
    }

    public Expectations addExpectation(String json) {
        Expectation ex = (Expectation) JsonUtils.beanFromJson(Expectation.class, json);
        expectations.add(ex);
        ExpectationMatcher.testExpectations(this);
        return this;
    }
    
    public static Expectations newExpectation(String json) {
        Expectations expectations = new Expectations();
        expectations.addExpectation(json);
        ExpectationMatcher.testExpectations(expectations);
        return expectations;
    }
    
    public static Expectations fromString(String json) {
        Expectations ex = (Expectations) JsonUtils.beanFromJson(Expectations.class, json);
        ExpectationMatcher.testExpectations(ex);
        if (ex.getExpectations().isEmpty()) {
            throw new ExpectationException("Expectations are empty.", 500);
        }
        return ex;
    }
}
