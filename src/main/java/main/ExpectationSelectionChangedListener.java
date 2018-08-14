/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import common.Action;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author 802996013
 */
public class ExpectationSelectionChangedListener implements ChangeListener {

    private boolean supressActions = true;

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        if (supressActions) {
            return;
        }
        if (oldValue != newValue) {
            Main.notifyAction(System.currentTimeMillis(), -1, Action.EXPECTATION_SELECTED, newValue, "Expectation Selected [" + newValue + "]");
        }
    }

    public void setSupressActions(boolean supressActions) {
        this.supressActions = supressActions;
    }

}