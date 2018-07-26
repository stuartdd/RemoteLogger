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

public class ServerThread extends Thread {

    private ServerState serverState = ServerState.SERVER_STOPPED;
    private final int port;
    private boolean running;
    private boolean canRun;
    private ExpectationMatcher expectationMatcher;
    private final Notifier serverNotifier;

    public ServerThread(int port, ServerConfig config, Notifier serverNotifier) {
        this.port = port;
        this.serverNotifier = serverNotifier;
        this.running = false;
        this.canRun = true;
        newState(ServerState.SERVER_PENDING, "");
        expectationMatcher = new ExpectationMatcher(config.getExpectationsFile(), serverNotifier);
        if (expectationMatcher.hasNoExpectations()) {
            if (serverNotifier != null) {
                serverNotifier.log(System.currentTimeMillis(), port, "Server on " + port + " does not have any expectations defined. 404 will be returned");
            }
        }
    }

    @Override
    public void run() {
        running = true;
        HttpServer server;
        try {
            newState(ServerState.SERVER_STARTING, "");
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/control", new ControlHandler(port, expectationMatcher, serverNotifier));
            server.createContext("/", new ExpectationHandler(port, expectationMatcher, serverNotifier));
            server.setExecutor(null); // creates a default executor
            server.start();
            newState(ServerState.SERVER_RUNNING, null);
        } catch (IOException ex) {
            newState(ServerState.SERVER_FAIL, ex.getMessage());
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        while (canRun) {
            sleeep(500);
        }
        newState(ServerState.SERVER_STOPPING, null);
        server.stop(1);
        newState(ServerState.SERVER_STOPPED, null);
        running = false;
    }

    private synchronized void newState(ServerState state, String additional) {
        serverState = state;
        if (serverNotifier != null) {
            serverNotifier.notifyAction(System.currentTimeMillis(), port, Action.SERVER_STATE, "");
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
    }

    private void sleeep(long ms) {
        try {
            sleep(500);
        } catch (InterruptedException ex) {

        }
    }

}
