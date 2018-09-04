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

import common.Action;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PackagedRequestSelectionChangedListener implements ChangeListener {

    private int supressActionCounts = 0;

    @Override
    public synchronized void changed(ObservableValue observable, Object oldValue, Object newValue) {
        if (supressActionCounts > 0) {
            return;
        }
        if (oldValue != newValue) {
            Main.notifyAction(System.currentTimeMillis(), -1, Action.PACKAGE_REQUEST_SELECTED, newValue, "Packaged Request Selected [" + newValue + "]");
        }
    }

    public synchronized void supressActions(boolean supressActions) {
        if (supressActions) {
            supressActionCounts++;
        } else {
            supressActionCounts--;
        }
    }

}
