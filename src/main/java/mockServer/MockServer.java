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
package mockServer;

import common.Util;
import expectations.Expectations;
import server.ResponseHandler;
import server.Server;
import server.ServerConfig;

/**
 *
 * @author stuart
 */
public class MockServer {

    private Server server;

    public MockServer(int port, ResponseHandler responseHandler, String expectationFile, boolean verbose) {
        server = new Server(port, new ServerConfig(expectationFile, 0, verbose, true), responseHandler, new MockServerNotifier());
    }
    
    public MockServer(int port, ResponseHandler responseHandler, Expectations expectations, boolean verbose) {
        server = new Server(port, new ServerConfig(expectations, 0, verbose, true), responseHandler, new MockServerNotifier());
    }
    
    public MockServer(int port, ResponseHandler responseHandler, boolean verbose) {
        server = new Server(port, new ServerConfig(new Expectations(), 0, verbose, true), responseHandler, new MockServerNotifier());
    }

    public void setCallBackClass(ResponseHandler responseHandler) {
        if (server != null) {
            server.setCallBackClass(responseHandler);
        }        
    }
    public boolean isRunning() {
        if (server != null) {
            return server.isRunning();
        }
        return false;
    }

    public MockServer start() {
        if (server != null) {
            server.start();
        }
        int count = 0;
        while (!server.isRunning() && (count < 10000)) {
            Util.sleep(2);
            count++;
        }
        return this;
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
        int count = 0;
        while (server.isRunning() && (count < 10000)) {
            Util.sleep(2);
            count++;
        }
    }

}
