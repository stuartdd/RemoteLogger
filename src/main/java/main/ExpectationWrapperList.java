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
import expectations.ExpectationManager;
import expectations.Expectations;
import java.util.ArrayList;
import java.util.List;
import json.JsonUtils;

public class ExpectationWrapperList {

    private final ExpectationManager expectationManager;
    private int selectedIndex;
    private boolean updated;

    public ExpectationWrapperList(ExpectationManager expectationManager) {
        this.expectationManager = expectationManager;
        this.updated = false;
        this.selectedIndex = 0;
    }

    void setLogProperties(boolean logProperties) {
        expectationManager.setLogProperties(logProperties);
    }

    public void replaceSelectedExpectation(Expectation newExpectation) {
        if (isSelected() && (newExpectation != null)) {
            expectationManager.replace(getSelectedExpectation(), newExpectation);
            setUpdated(true);
        }
    }

    void addExpectationWithName(String name) {
        Expectation ex = expectationManager.getBasicExpectationWithName(name);
        expectationManager.add(ex);
        this.selectedIndex = 0;
        setUpdated(true);
    }

    public void deleteSelectedExpectation() {
        if (isSelected()) {
            expectationManager.remove(getSelectedExpectation());
            this.selectedIndex = 0;
            setUpdated(true);
        }
    }

    void renameSelectedExpectation(String name) {
        if (isSelected()) {
            getSelectedExpectation().setName(name);
            setUpdated(true);
        }
    }

    public List<ExpectationWrapper> getWrappedExpectations() {
        List<ExpectationWrapper> list = new ArrayList<>();
        for (int i = 0; i < expectationManager.size(); i++) {
            list.add(new ExpectationWrapper(expectationManager.get(i), i));
        }
        return list;
    }

    public Expectation getSelectedExpectation() {
        if (isSelected()) {
            return expectationManager.get(selectedIndex);
        }
        return null;
    }

    public void setSelectedExpectationWrapper(ExpectationWrapper selectedExpectationWrapper) {
        this.selectedIndex = indexOfExpectationWrapper(selectedExpectationWrapper);
    }

    public void setSelectedExpectationWrapper(Integer index) {
        this.selectedIndex = index;
    }

    int size() {
        return expectationManager.size();
    }

    public boolean isSelected() {
        return ((selectedIndex >= 0) && (selectedIndex < expectationManager.size()));
    }

    public boolean loadedFromFile() {
        return expectationManager.isLoadedFromAFile();
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void save() {
        expectationManager.save();
        setUpdated(false);
    }

    public void reloadExpectations() {
        expectationManager.reloadExpectations(true);
        this.selectedIndex = 0;
        setUpdated(false);
    }

    public void selectFirst() {
        selectedIndex = 0;
    }

    public String getJson() {
        Expectation exp = getSelectedExpectation();
        if (exp!=null) {
            JsonUtils.toJsonFormatted(exp);
        }
        return null;
    }

    public String getName() {
        Expectation exp = getSelectedExpectation();
        if (exp!=null) {
            exp.getName();
        }
        return null;
    }

    public boolean checkNewExpectationName(String name) {
        return (expectationManager.findIndexByName(name) <0);
    }

    public boolean canNotDelete() {
        return expectationManager.canNotDelete();
    }

    public ExpectationWrapper getSelectedExpectationWrapper() {
        return new ExpectationWrapper(getSelectedExpectation(), selectedIndex);
    }
    
    private int indexOfExpectationWrapper(ExpectationWrapper expectationWrapper) {
        return indexOfExpectation(expectationWrapper.getExpectation());
    }

    private int indexOfExpectation(Expectation expectation) {
         return expectationManager.findIndexByName(expectation.getName());
    }


}
