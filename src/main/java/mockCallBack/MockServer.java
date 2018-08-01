/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mockCallBack;

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
        server = new Server(port, new ServerConfig(expectationFile, verbose), responseHandler, new MockServerNotifier());
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
        return this;
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

}
