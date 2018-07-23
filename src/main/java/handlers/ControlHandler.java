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
package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import expectations.ExpectationMatcher;
import java.io.IOException;
import java.io.OutputStream;
import common.Action;
import server.ServerManager;
import common.Notifier;

public class ControlHandler implements HttpHandler {
    private final int port;
    private final ExpectationMatcher expectationMatcher;
    private final Notifier serverNotifier;

    public ControlHandler(int port, ExpectationMatcher expectationMatcher, Notifier serverNotifier) {
        this.port = port;
        this.expectationMatcher = expectationMatcher;
        this.serverNotifier = serverNotifier;
    }


    @Override
    public void handle(HttpExchange he) throws IOException {
        if (he.getRequestURI().toString().contains("/stop")) {
            if (serverNotifier!= null) {
                serverNotifier.notifyAction(System.currentTimeMillis(), Action.SERVER_STOP, "Server on port "+port+" is shutting down");
            }
            ServerManager.stopServer(port);
            String response = "Server on port "+port+" will stop";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            String response = "control/?";
            he.sendResponseHeaders(404, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
