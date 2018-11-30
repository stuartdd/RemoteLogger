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
import java.util.Map;
import model.MultiModelManager;

public class ExpectationWrapperManager {

    private final Map<Integer, MultiModelManager> wrapperList = new HashMap<>();
    private int selectedPort;

    public void add(int port, MultiModelManager list) {
        wrapperList.put(port, list);
    }

    public void setSelectedPort(int port) {
        this.selectedPort = port;
    }

    public MultiModelManager getSelectedModelManager() {
        return wrapperList.get(selectedPort);
    }

    public void selectFirst() {
        MultiModelManager m = getSelectedModelManager();
        if (!m.isEmpty()) {
            m.setSelectedModel(m.list()[0]);
        }
    }

    public boolean loadedFromFile() {
        return getSelectedModelManager().loadedFromFile();
    }

    public Expectation getSelectedExpectation() {
        return (Expectation) getSelectedModelManager().getSelectedModel();
    }
    int size() {
        return getSelectedModelManager().size();
    }

    public boolean isUpdated() {
        return getSelectedModelManager().isUpdated();
    }

    public void replaceSelectedExpectation(Expectation expectation) {
        getSelectedModelManager().replace(expectation);
    }

    public void save() {
        getSelectedModelManager().save();
    }

    public void reloadExpectations() {
        getSelectedModelManager().reload();
    }

    public void setLogProperties(boolean logProperties) {
        getSelectedModelManager().setLogProperties(logProperties);
    }

    public void delete() {
        getSelectedModelManager().deleteSelectedExpectation();
    }

    String getJson() {
        return getSelectedModelManager().getJson();
    }

    String getName() {
        return getSelectedModelManager().getName();
    }

    String checkNameIsUnique(String name) {
        if ((name == null) || (name.trim().length()==0)) {
            return "Name cannot be empty";
        }
        if (getSelectedModelManager().checkNameIsUnique(name)) {
            return null;
        }
        return "Duplicate Expectation names are not allowed";
    }

    void rename(String name) {
        getSelectedModelManager().renameSelectedExpectation(name);
    }

    void addExpectationWithName(String name) {
        getSelectedModelManager().addExpectationWithName(name);
    }

    public boolean canNotDelete() {
        return getSelectedModelManager().canNotDelete();
    }

}
