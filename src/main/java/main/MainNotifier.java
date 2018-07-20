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
import common.Notifier;

/**
 *
 * @author stuart
 */
public class MainNotifier implements Notifier {
    private final boolean verbose ;

    public MainNotifier(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void notifyAction(long time, Action action, String message) {
        Main.notifyAction(time, action, message);
    }

    @Override
    public void log(long time, String message) {
        if (verbose) {
            Main.log(time, message);
        }
    }

    @Override
    public void log(long time, Throwable throwable) {
        Main.log(time, throwable);
    }

    @Override
    public void log(long time, String message, Throwable throwable) {
        Main.log(time, message, throwable);
    }
    
}
