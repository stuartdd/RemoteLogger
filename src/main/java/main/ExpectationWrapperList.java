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
    private ExpectationWrapper selectedExpectationWrapper;
    private boolean updated;

    public ExpectationWrapperList(ExpectationManager expectationManager) {
        this.expectationManager = expectationManager;
        this.updated = false;
        refresh();
    }

    public void replaceSelectedExpectation(Expectation newExpectation) {
        if ((selectedExpectationWrapper != null) && (newExpectation != null)) {
            int index = selectedExpectationWrapper.getIndex();
            expectationManager.set(index, newExpectation);
            selectedExpectationWrapper.setExpectation(newExpectation);
        }
    }

    public ExpectationWrapperList save() {
        expectationManager.save();
        refresh();
        this.updated = false;
        return this;
    }

    public List<ExpectationWrapper> getWrappedExpectations() {
        return wrappedExpectations;
    }

    public ExpectationWrapper getSelectedExpectationWrapper() {
        return selectedExpectationWrapper;
    }

    public void setSelectedExpectationWrapper(ExpectationWrapper selectedExpectationWrapper) {
        this.selectedExpectationWrapper = selectedExpectationWrapper;
    }

    public void setSelectedExpectationWrapper(Integer index) {
        if ((index != null) && (index >= 0)) {
            this.selectedExpectationWrapper = wrappedExpectations.get(index);
        } else {
            this.selectedExpectationWrapper = wrappedExpectations.get(0);
        }
    }

    void selectFirst() {
        this.selectedExpectationWrapper = wrappedExpectations.get(0);
    }

    public Expectation getSelectedExpectation() {
        if (selectedExpectationWrapper != null) {
            return selectedExpectationWrapper.getExpectation();
        }
        return null;
    }

    public boolean isSelected() {
        return (selectedExpectationWrapper != null);
    }

    public ExpectationWrapper findByName(String name) {
        for (ExpectationWrapper w : wrappedExpectations) {
            if (w.getName().equals(name)) {
                return w;
            }
        }
        return null;
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

    private final void refresh() {
        Expectations expectations = expectationManager.getExpectations();
        wrappedExpectations = new ArrayList<>();
        for (int index = 0; index < expectations.size(); index++) {
            wrappedExpectations.add(new ExpectationWrapper(expectations.get(index), index));
        }
        if (selectedExpectationWrapper == null) {
            selectFirst();
        } else {
            selectedExpectationWrapper = findByName(selectedExpectationWrapper.getName());
        }
    }

    ExpectationWrapperList reloadExpectations() {
        expectationManager.reloadExpectations(true);
        refresh();
        selectFirst();
        updated = false;
        return this;
    }

    void setLogProperties(boolean logProperties) {
        expectationManager.setLogProperties(logProperties);
    }

    ExpectationWrapperList deleteSelectedExpectation() {
        expectationManager.remove(selectedExpectationWrapper.getExpectation());
        refresh();
        selectFirst();
        updated = true;
        return this;
    }

}
