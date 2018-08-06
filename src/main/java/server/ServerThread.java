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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import common.Util;

public class ServerThread extends Thread {

    private ServerState serverState = ServerState.SERVER_STOPPED;
    private final Server server;
    private boolean running;
    private boolean canRun;
    private final ExpectationHandler expectationHandler;

    public ServerThread(Server server) {
        this.server = server;
        this.running = false;
        this.canRun = true;
        this.expectationHandler = new ExpectationHandler(server);
        newState(ServerState.SERVER_PENDING, "");
    }

    @Override
    public void run() {
        running = true;
        canRun = true;
        HttpServer httpServer;
        try {
            newState(ServerState.SERVER_STARTING, "");
            httpServer = HttpServer.create(new InetSocketAddress(server.getPort()), 0);
            httpServer.createContext("/control", new ControlHandler(server));
            httpServer.createContext("/", expectationHandler);
            httpServer.setExecutor(null); // creates a default executor
            httpServer.start();
            newState(ServerState.SERVER_RUNNING, null);
        } catch (IOException ex) {
            newState(ServerState.SERVER_FAIL, ex.getMessage());
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        while (canRun) {
            Util.sleep(10);
        }
        newState(ServerState.SERVER_STOPPING, "Time to close:" + server.getTimeToClose());
        httpServer.stop(server.getTimeToClose());
        newState(ServerState.SERVER_STOPPED, null);
        running = false;
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
        canRun = false;
        if (server.getServerNotifier() != null) {
            server.getServerNotifier().log(System.currentTimeMillis(), server.getPort(), "STOP-SERVER");
        }
    }

    private void sleeep(long ms) {
        try {
            sleep(ms);
        } catch (InterruptedException ex) {

        }
    }

    void setCallBackClass(ResponseHandler responseHandler) {
        if (expectationHandler != null) {
            expectationHandler.setCallBackClass(responseHandler);
        }
    }

}
