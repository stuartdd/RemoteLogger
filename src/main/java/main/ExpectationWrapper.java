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

import expectations.Expectation;
import json.JsonUtils;

public class ExpectationWrapper {

    private Expectation expectation;
    private int index;
    private String reference;
    private boolean updated;

    public ExpectationWrapper(Expectation expectation, int index) {
        this.expectation = expectation;
        this.index = index;
        this.reference = toJson();
        this.updated = false;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String toJson() {
        return JsonUtils.toJsonFormatted(expectation);
    }

    public void setName(String name) {
        expectation.setName(name);
    }
    
    public String getName() {
        return expectation.getName();
    }

    public Expectation getExpectation() {
        return expectation;
    }

    public String getJson() {
        return JsonUtils.toJsonFormatted(expectation);
    }

    public int getIndex() {
        return index;
    }
    
    void setIndex(int index) {
        this.index = index;
    }

    void setExpectation(Expectation newExpectation) {
        this.expectation = newExpectation;
        updated = !JsonUtils.toJsonFormatted(newExpectation).equals(reference);
    }

    public boolean isUpdated() {
        return updated;
    }

    void setUpdated(boolean b) {
        this.updated = false;
    }


}
