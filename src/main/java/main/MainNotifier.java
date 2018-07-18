/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import common.Action;
import common.Notifier;

/**
 *
 * @author stuart
 */
public class MainNotifier implements Notifier {

    @Override
    public void notifyAction(long time, Action action, String message) {
        Main.notifyAction(time, action, message);
    }

    @Override
    public void log(long time, String message) {
        Main.log(time, message);
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
