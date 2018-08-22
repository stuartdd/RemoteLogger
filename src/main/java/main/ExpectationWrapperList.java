/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import expectations.Expectation;
import expectations.ExpectationManager;
import expectations.Expectations;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 802996013
 */
public class ExpectationWrapperList {

    private final ExpectationManager expectationManager;
    private List<ExpectationWrapper> wrappedExpectations;
    private int selectedIndex;
    private boolean updated;


    public ExpectationWrapperList(ExpectationManager expectationManager) {
        this.expectationManager = expectationManager;
        this.updated = false;
        refresh();
    }

    void setLogProperties(boolean logProperties) {
        expectationManager.setLogProperties(logProperties);
    }

    public void replaceSelectedExpectation(Expectation newExpectation) {
        if (isSelected() && (newExpectation != null)) {
            expectationManager.set(selectedIndex, newExpectation);
            refresh();
            updated = true;
        }
    }

    ExpectationWrapperList deleteSelectedExpectation() {
        if (isSelected()) {
            expectationManager.remove(getSelectedExpectation());
            refresh();
            selectFirst();
            updated = true;
        }
        return this;
    }

    public List<ExpectationWrapper> getWrappedExpectations() {
        return wrappedExpectations;
    }

    public Expectation getSelectedExpectation() {
        if (isSelected()) {
            return wrappedExpectations.get(selectedIndex).getExpectation();
        }
        return null;
    }

    public ExpectationWrapper getSelectedExpectationWrapper() {
        if (isSelected()) {
            return wrappedExpectations.get(selectedIndex);
        }
        return null;
    }

    public void setSelectedExpectationWrapper(ExpectationWrapper selectedExpectationWrapper) {
        this.selectedIndex = indexOfWrappedExpectation(selectedExpectationWrapper);
    }

    public void setSelectedExpectationWrapper(Integer index) {
        this.selectedIndex = index;
    }

    void selectFirst() {
        this.selectedIndex = 0;
    }

    public boolean isSelected() {
        return ((selectedIndex >= 0) && (selectedIndex < wrappedExpectations.size()));
    }

    public boolean loadedFromFile() {
        return expectationManager.isLoadedFromAFile();
    }

    public boolean isUpdated() {
        if (updated) {
            return true;
        }
        for (ExpectationWrapper w : wrappedExpectations) {
            if (w.isUpdated()) {
                return true;
            }
        }
        return false;
    }

    private final synchronized void refresh() {
        Expectations expectations = expectationManager.getExpectations();
        wrappedExpectations.clear();
        for (int index = 0; index < expectations.size(); index++) {
            wrappedExpectations.add(new ExpectationWrapper(expectations.get(index), index));
        }
        if (!isSelected()) {
            selectFirst();
        }
    }

    public ExpectationWrapperList save() {
        expectationManager.save();
        refresh();
        this.updated = false;
        return this;
    }

    ExpectationWrapperList reloadExpectations() {
        expectationManager.reloadExpectations(true);
        refresh();
        selectFirst();
        updated = false;
        return this;
    }

    private int indexOfWrappedExpectation(ExpectationWrapper expectationWrapper) {
        return indexOfExpectation(expectationWrapper.getExpectation());
    }

    private int indexOfExpectation(Expectation expectation) {
        for (int i = 0; i < wrappedExpectations.size(); i++) {
            if (expectation.getName().equals(wrappedExpectations.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

}
