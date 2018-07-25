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

import common.Notifier;

/**
 *
 * @author stuart
 */
public class Server {

    int port;
    Notifier serverNotifier;
    ServerConfig serverConfig;
    ServerThread serverThread;

    public Server(int port, ServerConfig serverConfig, Notifier serverNotifier) {
        if (serverConfig == null) {
            throw new ServerConfigException("Server serverConfig is null");
        }
        this.port = port;
        this.serverNotifier = serverNotifier;
        this.serverConfig = serverConfig;
        this.serverThread = null;
    }

    public void start() {
        serverThread = new ServerThread(port, serverConfig, serverNotifier);
        serverThread.start();
        serverNotifier.log(System.currentTimeMillis(), "Started server on port:" + port);
    }

    public void stop() {
        if (serverThread != null) {
            serverThread.stopServer();
            serverThread = null;
        }
    }

    public boolean isRunning() {
        if (serverThread != null) {
            return serverThread.isRunning();
        }
        return false;
    }

    public ServerState state() {
        if (serverThread != null) {
            return serverThread.state();
        }
        return ServerState.SERVER_PENDING;        
    }

}
