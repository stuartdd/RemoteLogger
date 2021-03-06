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
import expectations.ExpectationManager;

/**
 *
 * @author stuart
 */
public class Server {

    private final int port;
    private final Notifier serverNotifier;
    private final ServerConfig serverConfig;
    private final ResponseHandler responseHandler;
    private final ExpectationManager expectationManager;
    private final ServerStatistics serverStatistics;

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
        this.serverStatistics = new ServerStatistics();
        if (serverConfig.expectations() == null) {
            expectationManager = new ExpectationManager(port, serverConfig.getExpectationsFile(), serverNotifier, serverStatistics, serverConfig.isLogProperties(), serverConfig.isLogProperties());
        } else {
            expectationManager = new ExpectationManager(port, serverConfig.expectations(), serverNotifier, serverStatistics, serverConfig.isLogProperties(), serverConfig.isLogProperties());
        }
        if ((serverNotifier != null) && expectationManager.hasNoExpectations()) {
            serverNotifier.log(System.currentTimeMillis(), port, "Server on " + port + " does not have any expectations defined. 404 will be returned");

        }
    }

    public int getPort() {
        return port;
    }

    public int getTimeToClose() {
        return serverConfig.getTimeToClose();
    }

    public Notifier getServerNotifier() {
        return serverNotifier;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public ExpectationManager getExpectationManager() {
        return expectationManager;
    }

    public ServerStatistics getServerStatistics() {
        return serverStatistics;
    }

    public void start() {
        serverThread = new ServerThread(this);
        serverThread.start();
    }

    public void stop() {
        if (serverThread != null) {
            serverThread.stopServer();
        }
    }

    public boolean isRunning() {
        if (serverThread != null) {
            return serverThread.isRunning();
        }
        return false;
    }

    public ServerState getServerState() {
        if (serverThread != null) {
            return serverThread.getServerState();
        }
        return ServerState.SERVER_STOPPED;
    }

    public void setAutoStart(boolean selected) {
        serverConfig.setAutoStart(selected);
    }

    public boolean isAutoStart() {
        return serverConfig.isAutoStart();
    }

    public void setShowPort(boolean selected) {
        serverConfig.setShowPort(selected);
    }

    public void setLogProperties(boolean selected) {
        serverConfig.setLogProperties(selected);
    }

    public boolean isShowPort() {
        return serverConfig.isShowPort();
    }

    public boolean isLogProperties() {
        return serverConfig.isLogProperties();
    }

    public void setCallBackClass(ResponseHandler responseHandler) {
        if (serverThread != null) {
            serverThread.setCallBackClass(responseHandler);
        }
    }

    @Override
    public String toString() {
        return "Server{" + "port=" + port + '}';
    }

}
