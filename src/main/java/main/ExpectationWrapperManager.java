/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import expectations.Expectation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author 802996013
 */
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

}
