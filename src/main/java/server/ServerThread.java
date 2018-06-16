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

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Action;
import main.Main;
import main.handlers.ControlHandler;
import main.handlers.ExpectationHandler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author stuart
 */
public class ServerThread extends Thread {

    private final int port;
    private boolean running;
    private boolean canRun;
    private HttpServer server;

    public ServerThread(int port) {
        this.port = port;
        this.running = false;
        this.canRun = true;
    }

    @Override
    public void run() {
        running = true;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/control", new ControlHandler());
            server.createContext("/", new ExpectationHandler());
            server.setExecutor(null); // creates a default executor
            Main.log("Starting server on port " + port);
            server.start();
            Main.notifyAction(System.currentTimeMillis(), Action.SERVER_START,"Server started on port "+port);
        } catch (IOException ex) {
            Main.notifyAction(System.currentTimeMillis(), Action.SERVER_FAIL,ex.getClass().getSimpleName()+": port:"+port+" Error:"+ex.getMessage());
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        while (canRun) {
            sleeep(500);
        }
        Main.notifyAction(System.currentTimeMillis(), Action.SERVER_STOPPING,"Server is stopping");
        sleeep(500);
        if (server != null) {
            server.stop(1);
            server = null;
        }
        Main.notifyAction(System.currentTimeMillis(), Action.SERVER_STOP,"Ready to start the server");
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void stopServer(int i) {
        canRun = false;
    }

    private void sleeep(long ms) {
        try {
            sleep(500);
        } catch (InterruptedException ex) {

        }
    }

}
