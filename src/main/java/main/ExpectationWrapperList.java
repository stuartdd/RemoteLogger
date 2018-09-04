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

public class ExpectationWrapperList {

    private final ExpectationManager expectationManager;
    private final List<ExpectationWrapper> wrappedExpectations;
    private int selectedIndex;
    private boolean updated;

    public ExpectationWrapperList(ExpectationManager expectationManager) {
        this.expectationManager = expectationManager;
        this.updated = false;
        wrappedExpectations = new ArrayList<>();
        Expectations expectations = expectationManager.getExpectations();
        for (int index = 0; index < expectations.size(); index++) {
            wrappedExpectations.add(new ExpectationWrapper(expectations.get(index), index));
        }
        this.selectedIndex = 0;
    }

    void setLogProperties(boolean logProperties) {
        expectationManager.setLogProperties(logProperties);
    }

    public void replaceSelectedExpectation(Expectation newExpectation) {
        if (isSelected() && (newExpectation != null)) {
            Expectation expectation = expectationManager.replace(getSelectedExpectation(), newExpectation);
            wrappedExpectations.get(selectedIndex).setExpectation(expectation);
            setUpdated(true);
        }
    }

    void addExpectationWithName(String name) {
        Expectation ex = expectationManager.getBasicExpectationWithName(name);
        expectationManager.add(ex);
        ExpectationWrapper wrapper = new ExpectationWrapper(ex, -1);
        wrappedExpectations.add(wrapper);
        wrapper.setIndex(indexOfExpectationWrapper(wrapper));
        setUpdated(true);
    }

    public void deleteSelectedExpectation() {
        if (isSelected()) {
            expectationManager.remove(getSelectedExpectation());
            wrappedExpectations.remove(selectedIndex);
            this.selectedIndex = 0;
            setUpdated(true);
        }
    }

    void renameSelectedExpectation(String name) {
        if (isSelected()) {
            getSelectedExpectationWrapper().setName(name);
            setUpdated(true);
        }
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
        this.selectedIndex = indexOfExpectationWrapper(selectedExpectationWrapper);
    }

    public void setSelectedExpectationWrapper(Integer index) {
        this.selectedIndex = index;
    }

    int size() {
        return wrappedExpectations.size();
    }

    public boolean isSelected() {
        return ((selectedIndex >= 0) && (selectedIndex < wrappedExpectations.size()));
    }

    public boolean loadedFromFile() {
        return expectationManager.isLoadedFromAFile();
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
        if (!updated) {
            for (ExpectationWrapper w : wrappedExpectations) {
                w.setUpdated(false);
            }
        }
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

    public void save() {
        expectationManager.save();
        setUpdated(false);
    }

    public void reloadExpectations() {
        expectationManager.reloadExpectations(true);
        wrappedExpectations.clear();
        Expectations expectations = expectationManager.getExpectations();
        for (int index = 0; index < expectations.size(); index++) {
            wrappedExpectations.add(new ExpectationWrapper(expectations.get(index), index));
        }
        this.selectedIndex = 0;
        setUpdated(false);
    }

    private int indexOfExpectationWrapper(ExpectationWrapper expectationWrapper) {
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

    void selectFirst() {
        selectedIndex = 0;
    }

    String getJson() {
        return getSelectedExpectationWrapper().getJson();
    }

    String getName() {
        return getSelectedExpectationWrapper().getName();
    }

    boolean checkNewExpectationName(String name) {
        for (ExpectationWrapper w : wrappedExpectations) {
            if (w.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    boolean canNotDelete() {
        return expectationManager.canNotDelete();
    }


}
