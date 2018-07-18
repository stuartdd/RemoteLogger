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
public class ServerThread extends Thread {

    private final ServerConfig config;
    private boolean running;
    private boolean canRun;
    private HttpServer server;
    private ExpectationMatcher expectationMatcher;
    private final Notifier serverNotifier;

    public ServerThread(ServerConfig config, Notifier serverNotifier) {
        this.serverNotifier = serverNotifier;
        this.config = config;
        this.running = false;
        this.canRun = true;
        if (config.getExpectationsFile() != null) {
            expectationMatcher = new ExpectationMatcher(config.getExpectationsFile(), serverNotifier);
        }
    }

    @Override
    public void run() {
        running = true;
        try {
            server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
            server.createContext("/control", new ControlHandler(config.getPort(), expectationMatcher, serverNotifier));
            server.createContext("/", new ExpectationHandler(config.getPort(), expectationMatcher, serverNotifier));
            server.setExecutor(null); // creates a default executor
            if (serverNotifier != null) {
                serverNotifier.notifyAction(System.currentTimeMillis(), Action.SERVER_START, "Server starting on port " + config.getPort());
            }
            server.start();
            if (serverNotifier != null) {
                serverNotifier.notifyAction(System.currentTimeMillis(), Action.SERVER_START, "Server started on port " + config.getPort());
            }
        } catch (IOException ex) {
            if (serverNotifier != null) {
                serverNotifier.notifyAction(System.currentTimeMillis(), Action.SERVER_FAIL, ex.getClass().getSimpleName() + ": port:" + config.getPort() + " Error:" + ex.getMessage());
            }
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        while (canRun) {
            sleeep(500);
        }
        if (serverNotifier != null) {
            serverNotifier.notifyAction(System.currentTimeMillis(), Action.SERVER_STOPPING, "Server on port " + config.getPort() + " is stopping");
        }
        sleeep(500);
        if (server != null) {
            server.stop(1);
            server = null;
        }
        if (serverNotifier != null) {
            serverNotifier.notifyAction(System.currentTimeMillis(), Action.SERVER_STOP, "Ready to start the server");
        }
        running = false;
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
