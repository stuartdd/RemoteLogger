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

    private final int port;
    private final Notifier serverNotifier;
    private final ServerConfig serverConfig;
    private final ResponseHandler responseHandler;
    private ServerThread serverThread;

    public Server(int port, ServerConfig serverConfig, ResponseHandler responseHandler, Notifier serverNotifier) {
        if (serverConfig == null) {
            throw new ServerConfigException("Server serverConfig is null");
        }
        this.port = port;
        this.serverNotifier = serverNotifier;
        this.serverConfig = serverConfig;
        this.responseHandler = responseHandler;
        this.serverThread = null;
    }

    public void start() {
        serverThread = new ServerThread(port, serverConfig, responseHandler, serverNotifier);
        serverThread.start();
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
        return ServerState.SERVER_STOPPED;
    }

    public void setAutoStart(boolean selected) {
        serverConfig.setAutoStart(selected);
    }

    public boolean isAutoStart() {
        return serverConfig.isAutoStart();
    }

    void setShowPort(boolean selected) {
        serverConfig.setShowPort(selected);
    }

    boolean isShowPort() {
        return serverConfig.isShowPort();
    }

}
