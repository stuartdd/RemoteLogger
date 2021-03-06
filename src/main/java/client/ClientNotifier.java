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
package client;

import common.Action;
import common.Notifier;
import org.joda.time.DateTime;

/**
 *
 * @author stuart
 */
public class ClientNotifier implements Notifier {

    private final boolean verbose;

    public ClientNotifier(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void notifyAction(long time, int port, Action action, Object actionOn, String message) {
        if (verbose && (message != null) && (action != null)) {
            System.out.println(getTimeStamp(time) + (port <= 0?"":" ["+port+"] ") + action.name() + " " + message);
        }
    }

    public void log(long time, int port, String message) {
        if (verbose && (message != null)) {
            System.out.println(getTimeStamp(time) + (port <= 0?"":" ["+port+"] ") + "CLIENT: " + message);
        }
    }

    public void log(long time, int port, Throwable throwable) {
        if (throwable != null) {
            System.out.println(getTimeStamp(time) + (port <= 0?"":" ["+port+"] ") + "CLIENT: ERROR:" + throwable.getMessage());
        }
    }

    public void log(long time, int port, String message, Throwable throwable) {
        if (throwable != null) {
            System.out.println(getTimeStamp(time) + (port <= 0?"":" ["+port+"] ") + "CLIENT: ERROR:" + message + ": " + throwable.getMessage());
        }
    }

    public String getTimeStamp(long time) {
        return (new DateTime(time)).toString("HH:mm:ss.SSS: ");
    }
}
