/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    @Override
    public void notifyAction(long time, Action action, String message) {
        System.out.println(getTimeStamp(time) + action.name() + " " + message);
    }

    public void log(long time, String message) {
        if (message != null) {
            System.out.println(getTimeStamp(time) + "CLIENT: " + message);
        }
    }

    public void log(long time, Throwable throwable) {
        if (throwable != null) {
            System.out.println(getTimeStamp(time) + "CLIENT: " + "ERROR:" + throwable.getMessage());
        }
    }

    public void log(long time, String message, Throwable throwable) {
        if (throwable != null) {
            System.out.println(getTimeStamp(time) + "CLIENT: " + "ERROR:" + message + ": " + throwable.getMessage());
        }
    }

    public String getTimeStamp(long time) {
        return (new DateTime(time)).toString("HH:mm:ss.SSS: ");
    }
}
