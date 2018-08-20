/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import expectations.Expectation;
import expectations.Expectations;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 802996013
 */
public class ExpectationWrapperManager {

    private List<ExpectationWrapper> wrappedExpectations;
    private ExpectationWrapper selectedExpectationWrapper;
    private Expectations expectations;

    public ExpectationWrapperManager(Expectations expectations) {
        reload(expectations);
    }

    public void replaceSelectedExpectation(Expectation newExpectation) {
        if ((selectedExpectationWrapper != null) && (newExpectation != null)) {
            int index = selectedExpectationWrapper.getIndex();
            expectations.set(index, newExpectation);
            selectedExpectationWrapper.setExpectation(newExpectation);
        }
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
        if (index != null) {
            this.selectedExpectationWrapper = wrappedExpectations.get(index);
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

    public final void reload(Expectations expectations) {
        this.expectations = expectations;
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

}
