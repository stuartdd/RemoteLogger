package server;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Action;
import main.Main;
import main.handlers.ControlHandler;
import main.handlers.LogHandler;

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
            server.createContext("/log", new LogHandler());
            server.setExecutor(null); // creates a default executor
            Main.log("Starting server on port " + port);
            server.start();
            Main.notifyAction(Action.SERVER_START,"Server started on port "+port);
        } catch (IOException ex) {
            Main.notifyAction(Action.SERVER_FAIL,ex.getClass().getSimpleName()+": port:"+port+" Error:"+ex.getMessage());
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        while (canRun) {
            sleeep(500);
        }
        Main.notifyAction(Action.SERVER_STOPPING,"Server is stopping");
        sleeep(500);
        if (server != null) {
            server.stop(1);
            server = null;
        }
        Main.notifyAction(Action.SERVER_STOP,"Ready to start the server");
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
