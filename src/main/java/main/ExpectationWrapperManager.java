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

    boolean loadedFromFile() {
        return getSelectedExpectationWrapperList().loadedFromFile();
    }

    Expectation getSelectedExpectation() {
        return getSelectedExpectationWrapperList().getSelectedExpectation();
    }

    ExpectationWrapper getSelectedExpectationWrapper() {
        return getSelectedExpectationWrapperList().getSelectedExpectationWrapper();
    }

    boolean isSelected() {
        return getSelectedExpectationWrapperList().isSelected();
    }

    boolean updated() {
        return getSelectedExpectationWrapperList().updated();
    }

    void replaceSelectedExpectation(Expectation validClonedExpectation) {
        getSelectedExpectationWrapperList().replaceSelectedExpectation(validClonedExpectation);
    }

    void save() {
        getSelectedExpectationWrapperList().save();
    }

    void refresh() {
        getSelectedExpectationWrapperList().refresh();
    }

    void reloadExpectations() {
        getSelectedExpectationWrapperList().reloadExpectations();
    }

    void setSelectedExpectationWrapper(Integer integer) {
        getSelectedExpectationWrapperList().setSelectedExpectationWrapper(integer);
    }

}
