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
import expectations.Expectations;
import java.util.ArrayList;
import java.util.List;
import json.JsonUtils;

/**
 *
 * @author stuar
 */
public class ExpectationWrapper {

    private final Expectation expectation;
    private final int index;

    public ExpectationWrapper(Expectation expectation, int index) {
        this.expectation = expectation;
        this.index = index;
    }

    @Override
    public String toString() {
        return getIndex()+":"+getName();
    }

    public Expectation getExpectation() {
        return expectation;
    }

    public String getName() {
        return expectation.getName();
    }

    public String getJson() {
        return JsonUtils.toJsonFormatted(expectation);
    }

    public int getIndex() {
        return index;
    }

    public static List<ExpectationWrapper> wrap(Expectations expectations) {
        List<ExpectationWrapper> list = new ArrayList<>();
        int index = 0;
        for (Expectation exp : expectations.getExpectations()) {
            list.add(new ExpectationWrapper(exp, index));
            index++;
        }
        return list;
    }

}
