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
import common.Notifier;
import java.io.IOException;
import java.net.InetSocketAddress;
import common.Util;

public class ServerThread extends Thread {

    private ServerState serverState = ServerState.SERVER_STOPPED;
    private final Server server;
    private boolean running;
    private boolean serverCanRun;
    private final ExpectationHandler expectationHandler;
    private final ControlHandler controlHandler;

    public ServerThread(Server server) {
        this.server = server;
        this.running = false;
        this.serverCanRun = true;
        this.expectationHandler = new ExpectationHandler(server);
        this.controlHandler = new ControlHandler(server);
        newState(ServerState.SERVER_PENDING, "");
    }

    @Override
    public void run() {
        running = true;
        serverCanRun = true;
        HttpServer httpServer;
        try {
            newState(ServerState.SERVER_STARTING, "");
            httpServer = bind(server.getPort(), server.getServerNotifier());
            httpServer.createContext("/control", controlHandler);
            httpServer.createContext("/", expectationHandler);
            httpServer.setExecutor(null); // creates a default executor
            httpServer.start();
        } catch (IOException ex) {
            newState(ServerState.SERVER_FAIL, ex.getMessage());
            return;
        }
        newState(ServerState.SERVER_RUNNING, null);
        do {
            Util.sleep(50);
        } while (serverCanRun);
        newState(ServerState.SERVER_STOPPING, "["+serverCanRun+"] Time to close:" + server.getTimeToClose());
        httpServer.stop(server.getTimeToClose());
        newState(ServerState.SERVER_STOPPED, null);
        running = false;
    }

    private HttpServer bind(int port, Notifier notifier) throws IOException {
        int tries = 0;
        HttpServer server = null;
        IOException exception = null;
        while ((tries < 20) && (server == null)) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (IOException ex) {
                exception = ex;
            }
            tries++;
            if (notifier != null) {
                notifier.log(System.currentTimeMillis(), port, "Server Port in use. Try " + tries);
            }
            Util.sleep(100);
        }
        if ((server == null) && (exception != null)) {
            throw exception;
        }
        return server;
    }

    private synchronized void newState(ServerState state, String additional) {
        serverState = state;
        if (server.getServerNotifier() != null) {
            server.getServerNotifier().notifyAction(System.currentTimeMillis(), server.getPort(), Action.SERVER_STATE, server, "");
            server.getServerNotifier().log(System.currentTimeMillis(), server.getPort(), serverState + (additional == null ? "" : ". " + additional));
        }
    }

    public ServerState getServerState() {
        return serverState;
    }

    public boolean isRunning() {
        return running;
    }

    public void stopServer() {
        if (server.getServerNotifier() != null) {
            server.getServerNotifier().log(System.currentTimeMillis(), server.getPort(), "STOP-SERVER Via stopServer()");
        }
        serverCanRun = false;
    }

    void setCallBackClass(ResponseHandler responseHandler) {
        if (expectationHandler != null) {
            expectationHandler.setCallBackClass(responseHandler);
        }
    }

}
