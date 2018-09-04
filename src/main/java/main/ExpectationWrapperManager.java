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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpectationWrapperManager {

    private final Map<Integer, ExpectationWrapperList> wrapperList = new HashMap<>();
    private int selectedPort;

    public void add(int port, ExpectationWrapperList list) {
        wrapperList.put(port, list);
    }

    public void setSelectedPort(int port) {
        this.selectedPort = port;
    }

    public ExpectationWrapperList getSelectedExpectationWrapperList() {
        return wrapperList.get(selectedPort);
    }

    public void selectFirst() {
        getSelectedExpectationWrapperList().selectFirst();
    }

    public List<ExpectationWrapper> getWrappedExpectations() {
        return getSelectedExpectationWrapperList().getWrappedExpectations();
    }

    public boolean loadedFromFile() {
        return getSelectedExpectationWrapperList().loadedFromFile();
    }

    public Expectation getSelectedExpectation() {
        return getSelectedExpectationWrapperList().getSelectedExpectation();
    }

    public ExpectationWrapper getSelectedExpectationWrapper() {
        return getSelectedExpectationWrapperList().getSelectedExpectationWrapper();
    }
    int size() {
        return getSelectedExpectationWrapperList().size();
    }

    public boolean isSelected() {
        return getSelectedExpectationWrapperList().isSelected();
    }

    public boolean isUpdated() {
        return getSelectedExpectationWrapperList().isUpdated();
    }

    public void replaceSelectedExpectation(Expectation expectation) {
        getSelectedExpectationWrapperList().replaceSelectedExpectation(expectation);
    }

    public void save() {
        getSelectedExpectationWrapperList().save();
    }

    public void reloadExpectations() {
        getSelectedExpectationWrapperList().reloadExpectations();
    }

    public void setSelectedExpectationWrapper(Integer integer) {
        getSelectedExpectationWrapperList().setSelectedExpectationWrapper(integer);
    }

    public void setLogProperties(boolean logProperties) {
        getSelectedExpectationWrapperList().setLogProperties(logProperties);
    }

    public void delete() {
        getSelectedExpectationWrapperList().deleteSelectedExpectation();
    }

    String getJson() {
        return getSelectedExpectationWrapperList().getJson();
    }

    String getName() {
        return getSelectedExpectationWrapperList().getName();
    }

    String checkNewExpectationName(String name) {
        if ((name == null) || (name.trim().length()==0)) {
            return "Name cannot be empty";
        }
        if (getSelectedExpectationWrapperList().checkNewExpectationName(name)) {
            return null;
        }
        return "Duplicate Expectation names are not allowed";
    }

    void rename(String name) {
        getSelectedExpectationWrapperList().renameSelectedExpectation(name);
    }

    void addExpectationWithName(String name) {
        getSelectedExpectationWrapperList().addExpectationWithName(name);
    }

    public boolean canNotDelete() {
        return getSelectedExpectationWrapperList().canNotDelete();
    }

}
