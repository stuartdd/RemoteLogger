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
package server;

import common.Action;
import com.sun.net.httpserver.HttpServer;
import expectations.ExpectationMatcher;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import handlers.ControlHandler;
import handlers.ExpectationHandler;
import common.Notifier;
import common.Util;

public class ServerThread extends Thread {

    private ServerState serverState = ServerState.SERVER_STOPPED;
    private final int port;
    private boolean running;
    private boolean canRun;
    private final ExpectationMatcher expectationMatcher;
    private final ExpectationHandler expectationHandler;
    private final ControlHandler controlHandler;
    private final int timeToClose;
    private final Notifier serverNotifier;

    public ServerThread(int port, ServerConfig config, ResponseHandler responseHandler, Notifier serverNotifier) {
        this.port = port;
        this.serverNotifier = serverNotifier;
        this.timeToClose = config.getTimeToClose();
        this.running = false;
        this.canRun = true;
        newState(ServerState.SERVER_PENDING, "");
        if (config.expectations() == null) {
            expectationMatcher = new ExpectationMatcher(config.getExpectationsFile(), serverNotifier);
        } else {
            expectationMatcher = new ExpectationMatcher(config.expectations(), serverNotifier);
        }
        if (expectationMatcher.hasNoExpectations()) {
            if (serverNotifier != null) {
                serverNotifier.log(System.currentTimeMillis(), port, "Server on " + port + " does not have any expectations defined. 404 will be returned");
            }
        }
        expectationHandler = new ExpectationHandler(port, expectationMatcher, responseHandler, serverNotifier);
        controlHandler = new ControlHandler(port, expectationMatcher, serverNotifier);
    }

    @Override
    public void run() {
        running = true;
        canRun = true;
        HttpServer server;
        try {
            newState(ServerState.SERVER_STARTING, "");
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/control", controlHandler);
            server.createContext("/", expectationHandler);
            server.setExecutor(null); // creates a default executor
            server.start();
            newState(ServerState.SERVER_RUNNING, null);
        } catch (IOException ex) {
            newState(ServerState.SERVER_FAIL, ex.getMessage());
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        while (canRun) {
            Util.sleep(2);;
        }
        newState(ServerState.SERVER_STOPPING, "Time to close:"+timeToClose);
        server.stop(timeToClose);
        newState(ServerState.SERVER_STOPPED, null);
        running = false;
    }

    private synchronized void newState(ServerState state, String additional) {
        serverState = state;
        if (serverNotifier != null) {
            serverNotifier.notifyAction(System.currentTimeMillis(), port, Action.SERVER_STATE, null, "");
            serverNotifier.log(System.currentTimeMillis(), port, serverState + (additional == null ? "" : ". " + additional));
        }
    }

    public ServerState state() {
        return serverState;
    }

    public boolean isRunning() {
        return running;
    }

    public void stopServer() {
        canRun = false;
        if (serverNotifier != null) {
            serverNotifier.log(System.currentTimeMillis(), port, "STOP-SERVER");
        }
    }

    private void sleeep(long ms) {
        try {
            sleep(ms);
        } catch (InterruptedException ex) {

        }
    }

    void setCallBackClass(ResponseHandler responseHandler) {
        if (expectationHandler!=null) {
            expectationHandler.setCallBackClass(responseHandler);
        }
    }

}
